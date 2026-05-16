package com.pans.mono.mono_server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomDto {
    private String id;
    private String name;
    private int userCount;
}
