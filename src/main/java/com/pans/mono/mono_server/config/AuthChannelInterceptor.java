package com.pans.mono.mono_server.config;

import com.pans.mono.mono_server.model.User;
import com.pans.mono.mono_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;
import java.util.Optional;

/**
 * Authenticates WebSocket connections at the STOMP protocol level.
 *
 * <p>REST endpoints use the {@code Authorization: Bearer} header, but WebSocket clients
 * send headers only once — during the initial STOMP CONNECT frame. This interceptor
 * runs before every inbound message and, on CONNECT, reads the {@code X-Auth-Token}
 * header, validates the session token against the database, and injects a
 * {@link Principal} into the STOMP session so downstream handlers know who the user is.
 *
 * <p>If the token is missing or invalid the connection is rejected immediately.
 */
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final UserRepository userRepository;

    /**
     * Intercepts inbound STOMP frames. Only CONNECT frames are inspected —
     * all other frame types pass through unchanged.
     *
     * @param message the inbound STOMP message
     * @param channel the channel the message is being sent to
     * @return the original message (with Principal attached on CONNECT)
     * @throws IllegalArgumentException if the token is missing or does not match any user
     */
    @Override
    public Message<?> preSend(Message<?> message, org.springframework.messaging.MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("X-Auth-Token");

            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Missing X-Auth-Token header");
            }
            Optional<User> userOptional = userRepository.findBySessionToken(token);

            if (userOptional.isEmpty()) {
                throw new IllegalArgumentException("Invalid X-Auth-Token");
            }

            User user = userOptional.get();
            Principal principal = () -> user.getUsername();

            accessor.setUser(principal);
        }
        return message;
    }
}