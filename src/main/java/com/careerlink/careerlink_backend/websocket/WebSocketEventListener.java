package com.careerlink.careerlink_backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        log.info("WebSocket connected: {}", user);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String user = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        log.info("WebSocket disconnected: {}", user);
    }
}
