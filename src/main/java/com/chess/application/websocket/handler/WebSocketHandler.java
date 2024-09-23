package com.chess.application.websocket.handler;

import com.chess.application.controller.model.*;
import com.chess.application.model.Game;
import com.chess.application.model.ReturnPayload;
import com.chess.application.model.Status;
import com.chess.application.services.MoveGeneratorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class WebSocketHandler extends TextWebSocketHandler {

    private static Set<WebSocketSession> sessions = new HashSet<>();
    private final MoveGeneratorService moveGeneratorService;
    private final ObjectMapper objectMapper;

    public WebSocketHandler(MoveGeneratorService moveGeneratorService) {
        this.moveGeneratorService = moveGeneratorService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        System.out.println("MESSAGE RECEIVED: " + message.getPayload());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        if (jsonNode.get("messageType").asText().equals("INITIALISE")) {
            initialiseGame(jsonNode, session);
        }
        if (jsonNode.get("messageType").asText().equals("SEND_MOVE")) {
            sendMove(jsonNode, session);
        }
        if (jsonNode.get("messageType").asText().equals("GAME_STATUS_UPDATE")) {
            handleGameStatusUpdate(jsonNode, session);
        }
        if (jsonNode.get("messageType").asText().equals("REQUEST_REMATCH")) {
            sendMove(jsonNode, session);
        }
    }

    private void requestRematch(JsonNode jsonNode, WebSocketSession session) {
        // send to opponent first
    }

    private void rematchAccepted() {
//        initialiseGame();
    }

    private void handleGameStatusUpdate(JsonNode jsonNode, WebSocketSession session) {
        moveGeneratorService.updateGameSettings(jsonNode.get("uuid").asText(), Status.valueOf(jsonNode.get("status").asText()));
    }

    private void initialiseGame(JsonNode jsonNode, WebSocketSession session) {
        Settings settings = Settings.builder()
            .breadth(jsonNode.get("breadth").asLong())
            .startPlayer(Objects.equals(jsonNode.get("startPlayer").asText(), "white") ? 0L : 1L)
            .timeLimit(jsonNode.get("timeLimit").asLong())
            .build();
        Game game = Game.builder()
            .opponentType(OpponentType.valueOf(jsonNode.get("opponentType").asText()))
            .playerId(jsonNode.get("playerId").asText())
            .opponentId(jsonNode.get("opponentId").asText())
            .settings(settings)
            .build();
        Game initialisedGame = moveGeneratorService.initialiseGame(game);
        InitialisePayload initialisePayload = InitialisePayload.builder()
            .id(initialisedGame.getUuid())
            .fenString(initialisedGame.getLastFenString())
            .build();
        try {
            String returnMessage = objectMapper.writeValueAsString(initialisePayload);
            sendMessage(session, new TextMessage(returnMessage));
            if (OpponentType.COMPUTER.equals(initialisedGame.getOpponentType())) {
                ReturnPayload returnPayload = moveGeneratorService.respondToMove(MoveWrapper.builder()
                    .uuid(initialisedGame.getUuid())
                    .move(Move.builder().build())
                    .build());
                String returnMessagePayload = objectMapper.writeValueAsString(returnPayload);
                sendMessage(session, new TextMessage(returnMessagePayload));
            }
        } catch (Exception e) {
            log.error("Could not send initialise payload back to client. Exception: {}", e, e);
        }
    }

    private void sendMove(JsonNode jsonNode, WebSocketSession session) {
        ReturnPayload returnPayload = moveGeneratorService.respondToMove(MoveWrapper.builder()
            .uuid(jsonNode.get("uuid").asText())
            .move(Move.builder()
                .origin(jsonNode.get("origin").asLong())
                .destination(jsonNode.get("destination").asLong())
                .promotion(jsonNode.get("promotion").asLong())
                .castle(jsonNode.get("castle").asBoolean())
                .castleType(jsonNode.get("castleType").asLong())
                .build())
            .build());

        try {
            String returnMessage = objectMapper.writeValueAsString(returnPayload);
            sendMessage(session, new TextMessage(returnMessage));
        } catch (Exception e) {
            log.error("Could not send return payload back to client.");
        }
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        System.out.println("SEND MESSAGE: " + message.getPayload());
        if (session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.warn("Failed to send message");
            }
        } else {
            log.warn("Session not open");
        }
    }
}
