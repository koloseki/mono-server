package com.pans.mono.mono_server.config;

import com.pans.mono.mono_server.dto.ChatMessage;
import com.pans.mono.mono_server.service.RoomTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomTracker roomTracker;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) accessor.getSessionAttributes().get("username");
        String roomId = (String) accessor.getSessionAttributes().get("roomId");

        if (username != null && roomId != null) {
            roomTracker.userLeft(roomId, username);

            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setSender(username);
            leaveMessage.setType(ChatMessage.MessageType.LEAVE);
            leaveMessage.setRoomId(roomId);
            leaveMessage.setTimestamp(System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/room." + roomId, leaveMessage);
        }
    }
}
