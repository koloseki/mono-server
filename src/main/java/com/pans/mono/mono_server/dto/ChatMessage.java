package com.pans.mono.mono_server.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String sender;
    private String content;
    private MessageType type;
    private String roomId;
    private long timestamp;

    private String receiver;
    private String fileUrl;
    private String fileName;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        SYSTEM
    }
}
