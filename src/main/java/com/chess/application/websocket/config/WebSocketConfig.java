package com.chess.application.websocket.config;

import com.chess.application.websocket.handler.GamePlayGameWebSocketHandler;
import com.chess.application.websocket.handler.GameSetupWebsocketHandlerGame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GamePlayGameWebSocketHandler gamePlayWebSocketHandler;
    private final GameSetupWebsocketHandlerGame gameSetupWebsocketHandler;

    @Autowired
    public WebSocketConfig(GamePlayGameWebSocketHandler gamePlayWebSocketHandler, GameSetupWebsocketHandlerGame gameSetupWebsocketHandler) {
        this.gamePlayWebSocketHandler = gamePlayWebSocketHandler;
        this.gameSetupWebsocketHandler = gameSetupWebsocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gamePlayWebSocketHandler, "/websockets/game")
            .setAllowedOrigins("http://127.0.0.1:8080");

        registry.addHandler(gameSetupWebsocketHandler, "/websockets/game-setup")
            .setAllowedOrigins("http://127.0.0.1:8080");
    }
}
