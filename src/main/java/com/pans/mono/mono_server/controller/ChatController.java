package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.dto.ChatMessage;
import com.pans.mono.mono_server.service.RoomTracker;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomTracker roomTracker;

    public ChatController(SimpMessagingTemplate messagingTemplate, RoomTracker roomTracker) {
        this.messagingTemplate = messagingTemplate;
        this.roomTracker = roomTracker;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        if (message.getType() == ChatMessage.MessageType.JOIN && message.getRoomId() != null) {
            headerAccessor.getSessionAttributes().put("username", message.getSender());
            headerAccessor.getSessionAttributes().put("roomId", message.getRoomId());
        }
        String destination = message.getRoomId() != null
                ? "/topic/room." + message.getRoomId()
                : "/topic/public";
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        if (message.getRoomId() != null) {
            headerAccessor.getSessionAttributes().put("roomId", message.getRoomId());
            roomTracker.userJoined(message.getRoomId(), message.getSender());
        }
        String destination = message.getRoomId() != null
                ? "/topic/room." + message.getRoomId()
                : "/topic/public";
        messagingTemplate.convertAndSend(destination, message);
    }

    @MessageMapping("/chat.privateMessage")
    public void sendPrivateMessage(@Payload ChatMessage message) {
        messagingTemplate.convertAndSendToUser(
                message.getReceiver(),
                "/queue/messages/",
                message
        );
    }
}
