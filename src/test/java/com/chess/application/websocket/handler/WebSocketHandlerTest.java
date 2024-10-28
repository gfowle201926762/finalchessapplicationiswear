package com.chess.application.websocket.handler;

import com.chess.application.services.GameStoreService;
import com.chess.application.services.MoveGeneratorService;
import com.chess.application.services.NativeEngineService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.when;

public class WebSocketHandlerTest {

    WebSocketHandler webSocketHandler;

    @Mock
    WebSocketSession webSocketSession;

    public void setup() {
        GameStoreService gameStoreService = new GameStoreService();
        NativeEngineService nativeEngineService = new NativeEngineService();
        MoveGeneratorService moveGeneratorService = new MoveGeneratorService(gameStoreService, nativeEngineService);
        webSocketHandler = new WebSocketHandler(moveGeneratorService, gameStoreService);
    }

//    @Test
//    public void testBug() {
//        when(webSocketSession.isOpen()).thenReturn(false);
//        TextMessage message = new TextMessage()
//        webSocketHandler.handleTextMessage(webSocketSession, message);
//    }
}
