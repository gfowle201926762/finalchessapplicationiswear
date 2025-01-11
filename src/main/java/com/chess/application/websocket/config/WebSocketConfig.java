package com.chess.application.websocket.config;

import com.chess.application.websocket.handler.GamePlayWebSocketHandler;
import com.chess.application.websocket.handler.GameSetupWebsocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GamePlayWebSocketHandler gamePlayWebSocketHandler;
    private final GameSetupWebsocketHandler gameSetupWebsocketHandler;

    @Autowired
    public WebSocketConfig(GamePlayWebSocketHandler gamePlayWebSocketHandler, GameSetupWebsocketHandler gameSetupWebsocketHandler) {
        this.gamePlayWebSocketHandler = gamePlayWebSocketHandler;
        this.gameSetupWebsocketHandler = gameSetupWebsocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gamePlayWebSocketHandler, "/websockets/game")
//            .setAllowedOrigins("http://127.0.0.1:8080");
            .setAllowedOrigins("*");
        registry.addHandler(gameSetupWebsocketHandler, "/websockets/game-setup")
//            .setAllowedOrigins("http://127.0.0.1:8080");
            .setAllowedOrigins("*");
    }
}
