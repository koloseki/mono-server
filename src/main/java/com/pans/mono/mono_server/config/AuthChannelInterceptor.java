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

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor{

    private final UserRepository userRepository;

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