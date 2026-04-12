package com.pans.mono.mono_server.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    private String receiver;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}
