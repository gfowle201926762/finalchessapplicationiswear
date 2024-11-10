package com.chess.application.websocket.handler;

import com.chess.application.controller.model.InitialisePayload;
import com.chess.application.model.Colour;
import com.chess.application.model.Game;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Slf4j
public abstract class AbstractGameWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    public AbstractGameWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void sendMessage(WebSocketSession session, TextMessage message) {
        System.out.println("SEND MESSAGE: " + message.getPayload());
        if (session.isOpen()) {
            try {
                session.sendMessage(message);
            } catch (Exception e) {
                log.warn("Failed to send message", e);
            }
        } else {
            log.warn("Session not open");
        }
    }

    void sendInitialisedPayload(WebSocketSession session, Game initialisedGame, String opponentUsername, boolean mostRecentMove) throws JsonProcessingException {
        InitialisePayload initialisePayload = InitialisePayload.builder()
            .id(initialisedGame.getUuid())
            .fenString(mostRecentMove ? initialisedGame.getLastFenString() : initialisedGame.getFirstFenString())
            .opponentUsername(opponentUsername)
            .colour(opponentUsername.equals(initialisedGame.getWhiteId()) ? Colour.BLACK : Colour.WHITE)
            .build();
        String returnMessage = objectMapper.writeValueAsString(initialisePayload);
        System.out.println("SENDING INITIALISED PAYLOAD");
        System.out.println("WHITE ID: " + initialisedGame.getWhiteId());
        System.out.println("BLACK ID: " + initialisedGame.getBlackId());
        sendMessage(session, new TextMessage(returnMessage));
    }
}
