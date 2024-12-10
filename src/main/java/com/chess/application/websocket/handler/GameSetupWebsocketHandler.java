package com.chess.application.websocket.handler;

import com.chess.application.model.*;
import com.chess.application.services.MoveGeneratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
public class GameSetupWebsocketHandler extends AbstractGameWebSocketHandler {

    private final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> connections = new ConcurrentHashMap<>();
    private final Queue<String> waitingPlayersWhite = new ConcurrentLinkedDeque<>();
    private final Queue<String> waitingPlayersBlack = new ConcurrentLinkedDeque<>();
    private final MoveGeneratorService moveGeneratorService;
    private final ObjectMapper objectMapper;

    @Autowired
    public GameSetupWebsocketHandler(MoveGeneratorService moveGeneratorService, ObjectMapper objectMapper) {
        super(objectMapper);
        this.moveGeneratorService = moveGeneratorService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = sessions.remove(session);
        if (id == null) {
            return;
        }
        waitingPlayersBlack.remove(id);
        waitingPlayersWhite.remove(id);
        connections.remove(id);
    }

    private void savePlayerSessions(WebSocketSession session, JsonNode jsonNode) {
        String clientId = jsonNode.get("playerId").asText();
        sessions.put(session, clientId);
        connections.put(clientId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());
        OpponentType opponentType = OpponentType.valueOf(jsonNode.get("opponentType").asText());
        String clientId = jsonNode.get("playerId").asText();
        Colour clientColour = jsonNode.get("colour").asText().equals("white") ? Colour.WHITE : Colour.BLACK;
        if (OpponentType.COMPUTER.equals(opponentType)) {
            handleComputerInitialisation(jsonNode, clientColour, session);
        } else {
            savePlayerSessions(session, jsonNode);
            handleHumanInitialisation(jsonNode, clientColour, clientId);
        }
    }

    private void connectPlayers(JsonNode jsonNode) throws JsonProcessingException {
        if (waitingPlayersWhite.peek() != null && waitingPlayersBlack.peek() != null) {
            String clientIdWhite = waitingPlayersWhite.poll();
            String clientIdBlack = waitingPlayersBlack.poll();
            Game game = moveGeneratorService.initialiseGame(constructGame(jsonNode, clientIdWhite, clientIdBlack));
            String payload = objectMapper.writeValueAsString(game.getUuid());
            TextMessage textMessage = new TextMessage(payload);
            sendMessage(connections.get(clientIdWhite), textMessage);
            sendMessage(connections.get(clientIdBlack), textMessage);
        }
    }

    private void handleHumanInitialisation(JsonNode jsonNode, Colour clientColour, String clientId) {
        if (Colour.WHITE.equals(clientColour)) {
            waitingPlayersWhite.offer(clientId);
        } else {
            waitingPlayersBlack.offer(clientId);
        }
        try {
            connectPlayers(jsonNode);
        } catch (JsonProcessingException e) {
            log.warn("Failed to connect players.", e);
        }
    }

    private void handleComputerInitialisation(JsonNode jsonNode, Colour clientColour, WebSocketSession session) throws JsonProcessingException {
        String clientId = jsonNode.get("playerId").asText();
        System.out.println();
        Game game = constructGame(jsonNode,
            Colour.WHITE.equals(clientColour) ? clientId : "Engine",
            Colour.BLACK.equals(clientColour) ? clientId : "Engine");
        System.out.println("game whiteId: " + game.getWhiteId());
        System.out.println("game blackId: " + game.getBlackId());
        Game initialisedGame = moveGeneratorService.initialiseGame(game);
        System.out.println("initialisedGame whiteId: " + initialisedGame.getWhiteId());
        System.out.println("initialisedGame blackId: " + initialisedGame.getBlackId());
        String returnMessagePayload = objectMapper.writeValueAsString(initialisedGame.getUuid());
        sendMessage(session, new TextMessage(returnMessagePayload));
    }

    Game constructGame(JsonNode jsonNode, String whiteId, String blackId) {
        Settings settings = Settings.builder()
            .breadth(jsonNode.get("breadth") == null ? 0L : jsonNode.get("breadth").asLong())
            .engineColour((Objects.equals(jsonNode.get("colour").asText(), "white")) ? 1L : 0L)
            .timeLimit(jsonNode.get("timeLimit") == null ? 0L : jsonNode.get("timeLimit").asLong())
            .build();
        return Game.builder()
            .opponentType(OpponentType.valueOf(jsonNode.get("opponentType").asText()))
            .whiteId(whiteId)
            .blackId(blackId)
            .settings(settings)
            .build();
    }
}
