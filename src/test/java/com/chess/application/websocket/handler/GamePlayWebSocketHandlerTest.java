package com.chess.application.websocket.handler;


import com.chess.application.model.Colour;
import com.chess.application.model.Square;
import com.chess.application.model.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GamePlayWebSocketHandlerTest {

    @Autowired
    GameSetupWebsocketHandler gameSetupWebsocketHandler;

    GameSetupWebsocketHandler spyGameSetupWebsocketHandler;

    @Autowired
    GamePlayWebSocketHandler gamePlayWebSocketHandler;

    GamePlayWebSocketHandler spyGamePlayWebSocketHandler;

    @Captor
    ArgumentCaptor<TextMessage> textMessageCaptor;

    @Captor
    ArgumentCaptor<TextMessage> opponentTextMessageCaptor;

    @Captor
    ArgumentCaptor<TextMessage> setupTextMessageCaptor;

    @Captor
    ArgumentCaptor<TextMessage> setupOpponentTextMessageCaptor;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    WebSocketSession session;

    @Mock
    WebSocketSession session2;

    @Mock
    WebSocketSession opponentSession;

    @BeforeEach
    void setup() throws IOException {
        spyGamePlayWebSocketHandler = spy(gamePlayWebSocketHandler);
        spyGameSetupWebsocketHandler = spy(gameSetupWebsocketHandler);
        when(session.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
        when(opponentSession.isOpen()).thenReturn(true);
    }

    @Test
    void testHandlingNonExistentGame() throws Exception {
        String whitePlayerId = "whitePlayer";
        String gameId = "testGame";

        TextMessage whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "INITIALISE",
            "playerId", whitePlayerId,
            "gameId", gameId
        )));

        assertDoesNotThrow(() -> spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage));
        verify(spyGamePlayWebSocketHandler, times(0)).sendMessage(any(), any());
    }

    @Test
    void testWhiteVictory_ByCheckmate() throws Exception {
        String gameId = assertInitialisation();

        TextMessage whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.e2.ordinal(),
            "destination", Square.e4.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(2, 2, Status.ONGOING, Status.Reason.NONE);

        TextMessage blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b8.ordinal(),
            "destination", Square.a6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(3, 2, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.f1.ordinal(),
            "destination", Square.c4.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(3, 3, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a6.ordinal(),
            "destination", Square.b8.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(4, 3, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.d1.ordinal(),
            "destination", Square.h5.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(4, 4, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b8.ordinal(),
            "destination", Square.a6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(5, 4, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.h5.ordinal(),
            "destination", Square.f7.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(6, 5, Status.WHITE_VICTORY, Status.Reason.CHECKMATE);
    }

    @EnumSource(Colour.class)
    @ParameterizedTest
    void testVictory_ByResignation(Colour clientColour) throws Exception {
        if (Colour.RANDOM.equals(clientColour)) {
            return;
        }
        String gameId = assertInitialisation();
        TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "GAME_STATUS_UPDATE",
            "uuid", gameId,
            "reason", "RESIGNATION"
        )));
        if (clientColour.equals(Colour.WHITE)) {
            spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, textMessage);
        } else {
            spyGamePlayWebSocketHandler.handleTextMessage(session, textMessage);
        }

        assertGameStatus(3, 2, clientColour.equals(Colour.WHITE) ? Status.WHITE_VICTORY : Status.BLACK_VICTORY, Status.Reason.RESIGNATION);
    }

    @EnumSource(Colour.class)
    @ParameterizedTest
    void testVictory_ByAbandonment(Colour clientColour) throws Exception {
        if (Colour.RANDOM.equals(clientColour)) {
            return;
        }
        assertInitialisation();

        verify(spyGamePlayWebSocketHandler, times(2)).sendMessage(eq(session), textMessageCaptor.capture());
        verify(spyGamePlayWebSocketHandler, times(1)).sendMessage(eq(opponentSession), opponentTextMessageCaptor.capture());

        if (Colour.WHITE.equals(clientColour)) {
            spyGamePlayWebSocketHandler.afterConnectionClosed(opponentSession, CloseStatus.NORMAL);
        } else {
            spyGamePlayWebSocketHandler.afterConnectionClosed(session, CloseStatus.NORMAL);
        }

        assertGameStatus(3, 2, clientColour.equals(Colour.WHITE) ? Status.WHITE_VICTORY : Status.BLACK_VICTORY, Status.Reason.ABANDONMENT);
    }

    @Test
    void testBlackVictory_ByCheckmate() throws Exception {
        String gameId = assertInitialisation();

        TextMessage whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b1.ordinal(),
            "destination", Square.a3.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(2, 2, Status.ONGOING, Status.Reason.NONE);

        TextMessage blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.e7.ordinal(),
            "destination", Square.e5.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(3, 2, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a3.ordinal(),
            "destination", Square.b1.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(3, 3, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.f8.ordinal(),
            "destination", Square.c5.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(4, 3, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b1.ordinal(),
            "destination", Square.a3.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(4, 4, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.d8.ordinal(),
            "destination", Square.h4.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(5, 4, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a3.ordinal(),
            "destination", Square.b1.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(5, 5, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.h4.ordinal(),
            "destination", Square.f2.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(6, 6, Status.BLACK_VICTORY, Status.Reason.CHECKMATE);
    }

    @Test
    void testDraw_ByStalemate() throws Exception {
        String gameId = assertInitialisation();

        TextMessage whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.e2.ordinal(),
            "destination", Square.e3.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(2, 2, Status.ONGOING, Status.Reason.NONE);

        TextMessage blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a7.ordinal(),
            "destination", Square.a5.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(3, 2, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.d1.ordinal(),
            "destination", Square.h5.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(3, 3, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a8.ordinal(),
            "destination", Square.a6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(4, 3, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.h5.ordinal(),
            "destination", Square.a5.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(4, 4, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.h7.ordinal(),
            "destination", Square.h5.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(5, 4, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.h2.ordinal(),
            "destination", Square.h4.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(5, 5, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a6.ordinal(),
            "destination", Square.h6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(6, 5, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a5.ordinal(),
            "destination", Square.c7.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(6, 6, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.f7.ordinal(),
            "destination", Square.f6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(7, 6, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.c7.ordinal(),
            "destination", Square.d7.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(7, 7, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.e8.ordinal(),
            "destination", Square.f7.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(8, 7, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.d7.ordinal(),
            "destination", Square.b7.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(8, 8, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.d8.ordinal(),
            "destination", Square.d3.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(9, 8, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b7.ordinal(),
            "destination", Square.b8.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(9, 9, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.d3.ordinal(),
            "destination", Square.h7.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(10, 9, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b8.ordinal(),
            "destination", Square.c8.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(10, 10, Status.ONGOING, Status.Reason.NONE);

        blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.f7.ordinal(),
            "destination", Square.g6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        assertGameStatus(11, 10, Status.ONGOING, Status.Reason.NONE);

        whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.c8.ordinal(),
            "destination", Square.e6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        assertGameStatus(12, 11, Status.DRAW, Status.Reason.STALEMATE);
    }

    @Test
    void testDraw_ByRepetition() throws Exception {
        String gameId = assertInitialisation();

        TextMessage whiteTextMessage1 = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b1.ordinal(),
            "destination", Square.a3.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));

        TextMessage whiteTextMessage2 = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a3.ordinal(),
            "destination", Square.b1.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));

        TextMessage blackTextMessage1 = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.b8.ordinal(),
            "destination", Square.a6.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));

        TextMessage blackTextMessage2 = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "SEND_MOVE",
            "uuid", gameId,
            "origin", Square.a6.ordinal(),
            "destination", Square.b8.ordinal(),
            "promotion", 0,
            "castle", 0,
            "castleType", 0
        )));
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage1);
        assertGameStatus(2, 2, Status.ONGOING, Status.Reason.NONE);

        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage1);
        assertGameStatus(3, 2, Status.ONGOING, Status.Reason.NONE);

        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage2);
        assertGameStatus(3, 3, Status.ONGOING, Status.Reason.NONE);

        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage2);
        assertGameStatus(4, 3, Status.ONGOING, Status.Reason.NONE);

        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage1);
        assertGameStatus(4, 4, Status.ONGOING, Status.Reason.NONE);

        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage1);
        assertGameStatus(5, 4, Status.ONGOING, Status.Reason.NONE);

        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage2);
        assertGameStatus(5, 5, Status.ONGOING, Status.Reason.NONE);

        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage2);
        assertGameStatus(6, 5, Status.ONGOING, Status.Reason.NONE);

        // starting position doesn't count?
        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage1);
        assertGameStatus(7, 6, Status.DRAW, Status.Reason.REPETITION);
    }

    private String assertInitialisation() throws Exception {
        String whitePlayerId = "whitePlayer";
        String blackPlayerId = "blackPlayer";

        String gameId = setupHumanGame();

        TextMessage whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "INITIALISE",
            "playerId", whitePlayerId,
            "gameId", gameId
        )));

        TextMessage blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "INITIALISE",
            "playerId", blackPlayerId,
            "gameId", gameId
        )));

        spyGamePlayWebSocketHandler.handleTextMessage(session, whiteTextMessage);
        spyGamePlayWebSocketHandler.handleTextMessage(opponentSession, blackTextMessage);
        verify(spyGamePlayWebSocketHandler, times(2)).sendMessage(eq(session), textMessageCaptor.capture());
        verify(spyGamePlayWebSocketHandler, times(1)).sendMessage(eq(opponentSession), opponentTextMessageCaptor.capture());

        List<TextMessage> capturedTextMessages = textMessageCaptor.getAllValues();
        List<TextMessage> opponentCapturedTextMessages = opponentTextMessageCaptor.getAllValues();

        assertEquals(2, capturedTextMessages.size());
        assertEquals(1, opponentCapturedTextMessages.size());

        TextMessage initialisation = capturedTextMessages.get(0);
        JsonNode jsonNode = objectMapper.readTree(initialisation.getPayload());
        assertEquals(gameId, jsonNode.get("id").asText());
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", jsonNode.get("fenString").asText());
        assertEquals(blackPlayerId, jsonNode.get("opponentUsername").asText());
        assertEquals(Colour.WHITE.toString(), jsonNode.get("colour").asText());

        TextMessage legalMoves = capturedTextMessages.get(1);
        jsonNode = objectMapper.readTree(legalMoves.getPayload());
        assertEquals(20, countNonNullElements(jsonNode.get("clientLegalMoves")));

        initialisation = opponentCapturedTextMessages.get(0);
        jsonNode = objectMapper.readTree(initialisation.getPayload());
        assertEquals(gameId, jsonNode.get("id").asText());
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", jsonNode.get("fenString").asText());
        assertEquals(whitePlayerId, jsonNode.get("opponentUsername").asText());
        assertEquals(Colour.BLACK.toString(), jsonNode.get("colour").asText());

        return gameId;
    }

    private String setupHumanGame() throws Exception {
        String whitePlayerId = "whitePlayer";
        String blackPlayerId = "blackPlayer";
        String opponentType = "HUMAN";

        TextMessage whiteTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "INITIALISE",
            "playerId", whitePlayerId,
            "opponentId", "placementOpponentId", // not needed
            "opponentType", opponentType,
            "breadth", 100,
            "colour", "white",
            "timeLimit", 1
        )));

        TextMessage blackTextMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "INITIALISE",
            "playerId", blackPlayerId,
            "opponentId", "placementOpponentId", // not needed
            "opponentType", opponentType,
            "breadth", 100,
            "colour", "black",
            "timeLimit", 1
        )));

        spyGameSetupWebsocketHandler.handleTextMessage(session, whiteTextMessage);
        spyGameSetupWebsocketHandler.handleTextMessage(opponentSession, blackTextMessage);

        verify(spyGameSetupWebsocketHandler, times(1)).sendMessage(eq(session), setupTextMessageCaptor.capture());
        List<TextMessage> capturedTextMessages = setupTextMessageCaptor.getAllValues();

        verify(spyGameSetupWebsocketHandler, times(1)).sendMessage(eq(opponentSession), setupOpponentTextMessageCaptor.capture());
        List<TextMessage> capturedOpponentTextMessages = setupOpponentTextMessageCaptor.getAllValues();

        assertEquals(1, capturedTextMessages.size());
        assertEquals(1, capturedOpponentTextMessages.size());
        assertDoesNotThrow(() -> UUID.fromString(objectMapper.readTree(capturedTextMessages.get(0).getPayload()).asText()));
        assertDoesNotThrow(() -> UUID.fromString(objectMapper.readTree(capturedOpponentTextMessages.get(0).getPayload()).asText()));
        assertEquals(capturedTextMessages.get(0).getPayload(), capturedOpponentTextMessages.get(0).getPayload());

        return objectMapper.readTree(capturedTextMessages.get(0).getPayload()).asText();
    }

    private void assertGameStatus(int clientMessageCount, int opponentMessageCount, Status status, Status.Reason reason) throws JsonProcessingException {
        verify(spyGamePlayWebSocketHandler, times(clientMessageCount)).sendMessage(eq(session), textMessageCaptor.capture());
        verify(spyGamePlayWebSocketHandler, times(opponentMessageCount)).sendMessage(eq(opponentSession), opponentTextMessageCaptor.capture());
        assertEquals(status, Status.valueOf(objectMapper.readTree(textMessageCaptor.getValue().getPayload()).get("status").asText()));
        assertEquals(status, Status.valueOf(objectMapper.readTree(opponentTextMessageCaptor.getValue().getPayload()).get("status").asText()));
        assertEquals(reason, Status.Reason.valueOf(objectMapper.readTree(textMessageCaptor.getValue().getPayload()).get("reason").asText()));
        assertEquals(reason, Status.Reason.valueOf(objectMapper.readTree(opponentTextMessageCaptor.getValue().getPayload()).get("reason").asText()));
    }

    private int countNonNullElements(JsonNode jsonNode) {
        AtomicInteger i = new AtomicInteger(0);
        jsonNode.elements().forEachRemaining(node -> {
            if (node != null && node.asText() != null && !node.asText().equals("null")) {
                i.incrementAndGet();
            }
        });
        return i.get();
    }
}
