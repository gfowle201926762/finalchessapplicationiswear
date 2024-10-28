package com.chess.application.websocket.handler;

import com.chess.application.controller.model.*;
import com.chess.application.model.*;
import com.chess.application.services.GameStoreService;
import com.chess.application.services.MoveGeneratorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Service
public class WebSocketHandler extends TextWebSocketHandler {

    private Map<WebSocketSession, String> sessions = new HashMap<>();
    private Map<String, WebSocketSession> connections = new HashMap<>();
    private final Queue<String> waitingPlayersWhite = new ConcurrentLinkedDeque<>();
    private final Queue<String> waitingPlayersBlack = new ConcurrentLinkedDeque<>();
    private final Queue<String> waitingPlayersRandom = new ConcurrentLinkedDeque<>();
    private final MoveGeneratorService moveGeneratorService;
    private final ObjectMapper objectMapper;
    private final GameStoreService gameStoreService;

    public WebSocketHandler(MoveGeneratorService moveGeneratorService, GameStoreService gameStoreService) {
        this.moveGeneratorService = moveGeneratorService;
        this.objectMapper = new ObjectMapper();
        this.gameStoreService = gameStoreService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = sessions.remove(session);
        connections.remove(id);
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

    private void sendInitialisedPayload(WebSocketSession session, Game initialisedGame) throws JsonProcessingException {
        InitialisePayload initialisePayload = InitialisePayload.builder()
            .id(initialisedGame.getUuid())
            .fenString(initialisedGame.getLastFenString())
            .build();
        String returnMessage = objectMapper.writeValueAsString(initialisePayload);
        sendMessage(session, new TextMessage(returnMessage));
    }

    private void initialiseGame(JsonNode jsonNode, WebSocketSession session) {

        String clientId = UUID.randomUUID().toString(); // jsonNode.get("playerId").asText();
        Colour clientColour = Colour.valueOf(jsonNode.get("startPlayer").asText().equals("white") ? "WHITE" : "BLACK");
//        Colour opponentColour = (Colour.WHITE.equals(clientColour) ? Colour.BLACK : Colour.WHITE);
        connections.put(clientId, session);
        sessions.put(session, clientId);

        try {
            if (OpponentType.COMPUTER.equals(OpponentType.valueOf(jsonNode.get("opponentType").asText()))) {
                Settings settings = Settings.builder()
                    .breadth(jsonNode.get("breadth").asLong())
                    .startPlayer(Objects.equals(jsonNode.get("startPlayer").asText(), "white") ? 0L : 1L)
                    .timeLimit(jsonNode.get("timeLimit").asLong())
                    .build();
                Game game = Game.builder()
                    .opponentType(OpponentType.valueOf(jsonNode.get("opponentType").asText()))
                    .whiteId(Colour.WHITE.equals(clientColour) ? clientId : "Engine")
                    .blackId(Colour.BLACK.equals(clientColour) ? clientId : "Engine")
                    .settings(settings)
                    .build();
                Game initialisedGame = moveGeneratorService.initialiseGame(game);
                sendInitialisedPayload(session, initialisedGame);
                ReturnPayload returnPayload = moveGeneratorService.respondToMove(MoveWrapper.builder()
                    .uuid(initialisedGame.getUuid())
                    .move(Move.builder().build())
                    .javaRequestType(initialisedGame.getSettings().getStartPlayer() == 1L ? JavaRequestType.LEGAL_MOVES : JavaRequestType.ENGINE)
                    .build());
                String returnMessagePayload = objectMapper.writeValueAsString(returnPayload);
                sendMessage(session, new TextMessage(returnMessagePayload));
            }
            if (OpponentType.HUMAN.equals(OpponentType.valueOf(jsonNode.get("opponentType").asText()))) {
                if (Colour.WHITE.equals(clientColour)) {
                    // opponent should be white, add this connection to the black queue
                    waitingPlayersBlack.offer(clientId);
                }
                if (Colour.BLACK.equals(clientColour)) {
                    // opponent should be black, add this connection to the white queue
                    waitingPlayersWhite.offer(clientId);
                }
                if (Colour.RANDOM.equals(clientColour)) {
                    // might not need this.
                    waitingPlayersRandom.offer(clientId);
                }
                connectPlayer();
            }
        } catch (Exception e) {
            log.error("Could not send initialise payload back to client.", e);
        }
    }

    private void createHumanGame(String whitePlayer, String blackPlayer) throws JsonProcessingException {
        Game initialisedGame = moveGeneratorService.initialiseGame(Game.builder()
            .opponentType(OpponentType.HUMAN)
            .whiteId(whitePlayer)
            .blackId(blackPlayer)
            .settings(Settings.builder().startPlayer(1L).build())
            .build());
        System.out.println("whitePlayer: " + whitePlayer);
        System.out.println("blackPlayer: " + blackPlayer);
        sendInitialisedPayload(connections.get(whitePlayer), initialisedGame);
        sendInitialisedPayload(connections.get(blackPlayer), initialisedGame);

        WebSocketSession whiteSession = connections.get(whitePlayer);

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(Move.builder().build())
            .javaRequestType(JavaRequestType.LEGAL_MOVES)
            .build());
        String returnMessagePayload = objectMapper.writeValueAsString(returnPayload);
        System.out.println("SEND MESSAGE from createHumanGame");
        sendMessage(whiteSession, new TextMessage(returnMessagePayload));
    }

    private void connectPlayer() {
        // get the first two players from a queue.
        synchronized (waitingPlayersWhite) {
            synchronized (waitingPlayersBlack) {
                try {
                    if (waitingPlayersBlack.peek() != null && waitingPlayersWhite.peek() != null) {
                        String blackPlayer = waitingPlayersBlack.poll();
                        String whitePlayer = waitingPlayersWhite.poll();
                        createHumanGame(whitePlayer, blackPlayer);
                    }
                } catch (Exception e) {
                    log.error("Could not connect players", e);
                }
            }
        }
    }

    private void sendMove(JsonNode jsonNode, WebSocketSession session) {
        String gameId = jsonNode.get("uuid").asText();
        Game game = gameStoreService.getGame(gameId);
        OpponentType opponentType = game.getOpponentType();

        Move sentMove = Move.builder()
            .origin(jsonNode.get("origin").asLong())
            .destination(jsonNode.get("destination").asLong())
            .promotion(jsonNode.get("promotion").asLong())
            .castle(jsonNode.get("castle").asBoolean())
            .castleType(jsonNode.get("castleType").asLong())
            .build();

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(MoveWrapper.builder()
            .uuid(jsonNode.get("uuid").asText())
            .move(sentMove)
            .javaRequestType(OpponentType.COMPUTER.equals(opponentType) ? JavaRequestType.ENGINE : JavaRequestType.HUMAN)
            .build());

        try {
            if (OpponentType.COMPUTER.equals(opponentType)) {
                String returnMessage = objectMapper.writeValueAsString(returnPayload);
                sendMessage(session, new TextMessage(returnMessage));
            }
            if (OpponentType.HUMAN.equals(opponentType)) {
                returnPayload.setResponse(sentMove);
                String returnMessage = objectMapper.writeValueAsString(returnPayload);
                WebSocketSession whiteSession = connections.get(game.getWhiteId());
                WebSocketSession blackSession = connections.get(game.getBlackId());
                WebSocketSession receiverSession = (session == whiteSession ? blackSession : whiteSession);
                sendMessage(receiverSession, new TextMessage(returnMessage));
                if (!Status.ONGOING.equals(returnPayload.getStatus())) {
                    sendMessage(session, new TextMessage(objectMapper.writeValueAsString(new ReturnPayload(returnPayload.getStatus()))));
                }
            }
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
