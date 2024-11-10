package com.chess.application.websocket.handler;

import com.chess.application.controller.model.*;
import com.chess.application.model.*;
import com.chess.application.services.GameService;
import com.chess.application.services.MoveGeneratorService;
import com.chess.application.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GamePlayGameWebSocketHandler extends AbstractGameWebSocketHandler {

    private static int SECONDS_TO_TIMEOUT = 30;

    private Map<String, Game> activeHumanGames = new ConcurrentHashMap<>(); //Collections.newSetFromMap(new ConcurrentHashMap<Game, Boolean>());// HashSet<>();
    private Set<WebSocketSession> activeSessions = new HashSet<>();
    private final Map<WebSocketSession, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> connections = new ConcurrentHashMap<>();
    private final MoveGeneratorService moveGeneratorService;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final GameService gameService;
//    private final ThreadPoolExecutor executor;

    @Autowired
    public GamePlayGameWebSocketHandler(MoveGeneratorService moveGeneratorService, GameService gameService, UserService userService, ObjectMapper objectMapper) {
        super(objectMapper);
        this.moveGeneratorService = moveGeneratorService;
        this.objectMapper = objectMapper;
        this.gameService = gameService;
        this.userService = userService;
//        this.executor = executor;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        activeSessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = sessions.remove(session);
        Set<Game> games = activeHumanGames.values().stream()
            .filter(game -> game.getWhiteId().equals(id) || game.getBlackId().equals(id))
            .collect(Collectors.toSet());

        games
            .forEach(game -> {
                gameService.updateGame(game.getUuid(), game.getBlackId().equals(id) ? Status.WHITE_VICTORY : Status.BLACK_VICTORY, Status.Reason.ABANDONMENT);
                activeHumanGames.remove(game.getUuid());
                sendAbandonment(game, id);
        });

        connections.remove(id);
//        activeSessions.remove(session);
//        executor.execute(() -> removeConnections(session));
    }

    private void sendAbandonment(Game game, String abandoner) {
        Status status = game.getBlackId().equals(abandoner) ? Status.WHITE_VICTORY : Status.BLACK_VICTORY;
        ReturnPayload returnPayload = new ReturnPayload(status, Status.Reason.ABANDONMENT);
        TextMessage textMessage;
        try {
            textMessage = new TextMessage(objectMapper.writeValueAsString(returnPayload));
        }
        catch (JsonProcessingException e) {
            log.error("Could not process abandonment message", e);
            return;
        }
        gameService.updateGame(game.getUuid(), status, Status.Reason.ABANDONMENT);
        sendMessage(connections.get(game.getWhiteId()), textMessage);
        sendMessage(connections.get(game.getBlackId()), textMessage);
    }

    private void removeConnections(WebSocketSession session) {
        try {
            Thread.sleep(SECONDS_TO_TIMEOUT);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!activeSessions.contains(session)) {
            String id = sessions.remove(session);
            connections.remove(id);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        System.out.println("MESSAGE RECEIVED: " + message.getPayload());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        if (jsonNode.get("messageType").asText().equals("INITIALISE")) {
            startGame(jsonNode, session);
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

    private void handleGameStatusUpdate(JsonNode jsonNode, WebSocketSession session) {
        System.out.println("handleGameStatusUpdate");
        String clientId = sessions.get(session);
        String gameId = jsonNode.get("uuid").asText();
        Game game = gameService.getGame(gameId);
        Status.Reason reason = Status.Reason.valueOf(jsonNode.get("reason").asText());
        Status status = clientId.equals(game.getWhiteId()) ? Status.BLACK_VICTORY : Status.WHITE_VICTORY;
        moveGeneratorService.updateGameSettings(gameId, status, reason);

        TextMessage textMessage;
        try {
            textMessage = new TextMessage(objectMapper.writeValueAsString(new ReturnPayload(status, reason)));
        } catch (JsonProcessingException e) {
            log.error("Could not generate resignation message to send", e);
            return;
        }

        if (OpponentType.COMPUTER.equals(game.getOpponentType())) {
            sendMessage(session, textMessage);
        } else {
            activeHumanGames.remove(game.getUuid());
            sendMessage(connections.get(game.getWhiteId()), textMessage);
            sendMessage(connections.get(game.getBlackId()), textMessage);
        }
    }

    private void startComputerGame(WebSocketSession session, Game game) {
        try {
            sendInitialisedPayload(session, game, "Engine", true);
            ReturnPayload returnPayload = moveGeneratorService.respondToMove(MoveWrapper.builder()
                .uuid(game.getUuid())
                .move(Move.builder().build())
                .javaRequestType(Colour.BLACK.equals(Colour.get((int)game.getSettings().getEngineColour())) ? JavaRequestType.LEGAL_MOVES : JavaRequestType.ENGINE)
                .build());
            String returnMessagePayload = objectMapper.writeValueAsString(returnPayload);
            System.out.println("SENDING RETURN MESSAGE PAYLOAD");
            sendMessage(session, new TextMessage(returnMessagePayload));
        } catch (Exception e) {
            log.error("Could not send initialised payload for to player {}", sessions.get(session), e);
        }
    }

    private void startHumanGame(Game game) {
        synchronized (this) {
            try {
                String whiteId = game.getWhiteId();
                String blackId = game.getBlackId();

                if (!connections.containsKey(whiteId) || !connections.containsKey(blackId) || activeHumanGames.containsKey(game.getUuid())) {
                    System.out.println("returning from startHumanGame");
                    return;
                }
                activeHumanGames.put(game.getUuid(), game);

                System.out.println("SENDING INIT IN HUMAN GAME, id: " + game.getUuid() + ", white id:" + whiteId + ", black id: " + blackId);
                System.out.println(connections);
                sendInitialisedPayload(connections.get(whiteId), game, blackId, true);
                sendInitialisedPayload(connections.get(game.getBlackId()), game, whiteId, true);

                WebSocketSession whiteSession = connections.get(whiteId);

                ReturnPayload returnPayload = moveGeneratorService.respondToMove(MoveWrapper.builder()
                    .uuid(game.getUuid())
                    .move(Move.builder().build())
                    .javaRequestType(JavaRequestType.LEGAL_MOVES)
                    .build());
                String returnMessagePayload = objectMapper.writeValueAsString(returnPayload);
                System.out.println("SEND MESSAGE from createHumanGame");
                sendMessage(whiteSession, new TextMessage(returnMessagePayload));

            } catch (Exception e) {
                log.error("Could not send initialised payload for to players {} and {}", game.getWhiteId(), game.getBlackId(), e);
            }
        }
    }

    private void replayGame(WebSocketSession session, String gameId) {
        Game game = gameService.getGame(gameId);
        String opponentId = sessions.get(session).equals(game.getWhiteId()) ? game.getBlackId() : game.getWhiteId();
        try {
            sendInitialisedPayload(session, game, opponentId, false);
        } catch (JsonProcessingException e) {
            log.error("Could not send initialisation message for replay", e);
            return;
        }
        TextMessage textMessage;
        System.out.println("printing moves");
        game.getMoves().forEach(move -> {
            System.out.println(move);
        });
        try {
            textMessage = new TextMessage(objectMapper.writeValueAsString(game));
        } catch (JsonProcessingException e) {
            log.error("Could not prepare replay game message", e);
            return;
        }
        System.out.println(textMessage);
        sendMessage(session, textMessage);
    }


    private void startGame(JsonNode jsonNode, WebSocketSession session) {
        String clientId = jsonNode.get("playerId").asText();
        String gameId = jsonNode.get("gameId").asText();

        sessions.put(session, clientId);
        connections.put(clientId, session);

        Game game = gameService.getGame(gameId);
        if (!Status.ONGOING.equals(game.getStatus())) {
            replayGame(session, gameId);
        }
        else if (OpponentType.COMPUTER.equals(game.getOpponentType())) {
            startComputerGame(session, game);
        } else {
            startHumanGame(game);
        }
    }

    private void sendMove(JsonNode jsonNode, WebSocketSession session) {
        String gameId = jsonNode.get("uuid").asText();
        Game game = gameService.getGame(gameId);
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
                    sendMessage(session, new TextMessage(objectMapper.writeValueAsString(new ReturnPayload(returnPayload.getStatus(), returnPayload.getReason()))));
                    activeHumanGames.remove(game.getUuid());
                }
            }
        } catch (Exception e) {
            log.error("Could not send return payload back to client.");
        }
    }
}
