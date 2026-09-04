package com.vst.backend.config;

import com.vst.backend.websocket.PriceBoardWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Raw WebSocket, no STOMP broker — deliberately kept minimal, per the project's Phase 3 decision
 * to skip Kafka/Redis and push updates directly over WebSocket.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PriceBoardWebSocketHandler priceBoardWebSocketHandler;

    public WebSocketConfig(PriceBoardWebSocketHandler priceBoardWebSocketHandler) {
        this.priceBoardWebSocketHandler = priceBoardWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(priceBoardWebSocketHandler, "/ws/price-board")
                .setAllowedOrigins("*");
    }
}
