package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.dto.ChatMessage;
import com.pans.mono.mono_server.model.Message;
import com.pans.mono.mono_server.repository.MessageRepository;
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
    private final MessageRepository messageRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, RoomTracker roomTracker, MessageRepository messageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.roomTracker = roomTracker;
        this.messageRepository = messageRepository;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        if (message.getType() == ChatMessage.MessageType.JOIN && message.getRoomId() != null) {
            headerAccessor.getSessionAttributes().put("username", message.getSender());
            headerAccessor.getSessionAttributes().put("roomId", message.getRoomId());
        }

        if (message.getType() == ChatMessage.MessageType.CHAT && message.getRoomId() != null) {
            Message entity = new Message();
            entity.setSender(message.getSender());
            entity.setContent(message.getContent());
            entity.setRoomId(message.getRoomId());
            entity.setTimestamp(message.getTimestamp());
            messageRepository.save(entity);
        }

        String destination = message.getRoomId() != null
                ? "/topic/room." + message.getRoomId()
                : "/topic/public";
        messagingTemplate.convertAndSend(destination, message);
    }
}
