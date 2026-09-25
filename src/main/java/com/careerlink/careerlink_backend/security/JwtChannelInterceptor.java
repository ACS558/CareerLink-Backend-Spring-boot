package com.careerlink.careerlink_backend.security;

import com.careerlink.careerlink_backend.config.StompPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Missing Authorization header in WebSocket CONNECT frame");
            }

            String token = authHeader.substring(7);
            try {
                String email = jwtService.extractEmail(token);
                Long userId = jwtService.extractUserId(token);
                accessor.setUser(new StompPrincipal(email, userId));
            } catch (Exception e) {
                throw new MessagingException("Invalid or expired token in WebSocket handshake");
            }
        }

        return message;
    }
}
