package com.vst.backend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vst.backend.event.PriceBoardUpdateEvent;
import com.vst.backend.model.PriceQuote;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Broadcasts price board updates to every connected client — no per-client subscription, since
 * the watchlist itself is global (see {@link com.vst.backend.repository.WatchlistRepository}).
 */
@Component
public class PriceBoardWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(PriceBoardWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper;

    public PriceBoardWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @EventListener
    public void onPriceBoardUpdate(PriceBoardUpdateEvent event) {
        broadcast(event.quotes());
    }

    private void broadcast(List<PriceQuote> quotes) {
        if (sessions.isEmpty()) {
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(quotes);
        } catch (IOException e) {
            log.error("Failed to serialize price board update", e);
            return;
        }

        TextMessage message = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                log.warn("Failed to send price board update to session {}", session.getId(), e);
            }
        }
    }
}
