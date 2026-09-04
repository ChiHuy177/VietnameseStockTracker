package com.vst.backend.websocket;

import com.vst.backend.event.PriceBoardUpdateEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Relays price board updates to STOMP subscribers of {@code /topic/price-board}. */
@Component
public class PriceBoardBroadcaster {

    private static final String DESTINATION = "/topic/price-board";

    private final SimpMessagingTemplate messagingTemplate;

    public PriceBoardBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onPriceBoardUpdate(PriceBoardUpdateEvent event) {
        messagingTemplate.convertAndSend(DESTINATION, event.quotes());
    }
}
