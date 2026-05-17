package com.pans.mono.mono_server.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "messages")
public class Message {
    public enum MessageType {
        TEXT,
        IMAGE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sender;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String roomId;

    @Column(nullable = false)
    private long timestamp;
}
