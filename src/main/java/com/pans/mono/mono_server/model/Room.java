package com.pans.mono.mono_server.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "rooms")
public class Room {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String name;
}
