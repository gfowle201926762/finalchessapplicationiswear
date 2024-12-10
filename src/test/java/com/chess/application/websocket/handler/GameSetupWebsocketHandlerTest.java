package com.chess.application.websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GameSetupWebsocketHandlerTest {

    @Autowired
    GameSetupWebsocketHandler gameSetupWebsocketHandler;

    GameSetupWebsocketHandler spyGameSetupWebsocketHandler;

    @Captor
    ArgumentCaptor<TextMessage> textMessageCaptor;

    @Captor
    ArgumentCaptor<TextMessage> opponentTextMessageCaptor;

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
        spyGameSetupWebsocketHandler = spy(gameSetupWebsocketHandler);
        when(session.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
        when(opponentSession.isOpen()).thenReturn(true);
    }

    private void printTextMessaged(List<TextMessage> capturedTextMessages) {
        capturedTextMessages.forEach(message -> {
            try {
                System.out.println("captured message: " + objectMapper.readTree(message.getPayload()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Test
    void computerInitialisation() throws Exception {

        String playerId = "testPlayer";
        String gameId = "testGame";
        String opponentType = "COMPUTER";
        String colour = "white";

        TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "INITIALISE",
            "playerId", playerId,
            "opponentId", "placementOpponentId", // not needed
            "opponentType", opponentType,
            "breadth", 100,
            "colour", colour,
            "timeLimit", 1
        )));

        spyGameSetupWebsocketHandler.handleTextMessage(session, textMessage);

        verify(spyGameSetupWebsocketHandler, times(1)).sendMessage(eq(session), textMessageCaptor.capture());
        List<TextMessage> capturedTextMessages = textMessageCaptor.getAllValues();
        assertComputerGameInitialisation(capturedTextMessages);
    }

    @Test
    void humanInitialisation() throws Exception {

        String whitePlayerId = "whitePlayer";
        String whitePlayerId2 = "whitePlayer2";
        String blackPlayerId = "blackPlayer";

        String gameId = "testGame";
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

        TextMessage whiteTextMessage2 = new TextMessage(objectMapper.writeValueAsString(Map.of(
            "messageType", "INITIALISE",
            "playerId", whitePlayerId2,
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
        verify(spyGameSetupWebsocketHandler, times(0)).sendMessage(eq(session), any());

        spyGameSetupWebsocketHandler.handleTextMessage(session2, whiteTextMessage2);
        verify(spyGameSetupWebsocketHandler, times(0)).sendMessage(eq(session2), any());

        spyGameSetupWebsocketHandler.handleTextMessage(opponentSession, blackTextMessage);

        verify(spyGameSetupWebsocketHandler, times(1)).sendMessage(eq(session), textMessageCaptor.capture());
        List<TextMessage> capturedTextMessages = textMessageCaptor.getAllValues();

        verify(spyGameSetupWebsocketHandler, times(1)).sendMessage(eq(opponentSession), opponentTextMessageCaptor.capture());
        List<TextMessage> capturedOpponentTextMessages = opponentTextMessageCaptor.getAllValues();
        assertHumanGameInitialisation(capturedTextMessages, capturedOpponentTextMessages);

        verify(spyGameSetupWebsocketHandler, times(0)).sendMessage(eq(session2), any());
    }

    private void assertHumanGameInitialisation(List<TextMessage> capturedTextMessages, List<TextMessage> capturedOpponentTextMessages) {
        assertComputerGameInitialisation(capturedTextMessages);
        assertComputerGameInitialisation(capturedOpponentTextMessages);
        assertEquals(capturedTextMessages.get(0).getPayload(), capturedOpponentTextMessages.get(0).getPayload());
    }

    private void assertComputerGameInitialisation(List<TextMessage> capturedTextMessages) {
        assertEquals(1, capturedTextMessages.size());
        assertDoesNotThrow(() -> UUID.fromString(objectMapper.readTree(capturedTextMessages.get(0).getPayload()).asText()));
    }
}
