package com.chess.application.websocket.handler;

import com.chess.application.services.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.when;

public class WebSocketHandlerTest {


    @Mock
    WebSocketSession webSocketSession;

//    public void setup() {
//        GameStoreService gameStoreService = new GameStoreService();
//        NativeEngineService nativeEngineService = new NativeEngineService();
//        MoveGeneratorService moveGeneratorService = new MoveGeneratorService(gameStoreService, nativeEngineService);
//        UserService userService = new UserServiceImpl();
//        webSocketHandler = new WebSocketHandler(moveGeneratorService, gameStoreService);
//    }

//    @Test
//    public void testBug() {
//        when(webSocketSession.isOpen()).thenReturn(false);
//        TextMessage message = new TextMessage()
//        webSocketHandler.handleTextMessage(webSocketSession, message);
//    }
}
