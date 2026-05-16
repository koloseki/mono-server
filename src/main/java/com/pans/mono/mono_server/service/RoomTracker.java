package com.pans.mono.mono_server.service;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomTracker {

    private final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();

    public void userJoined(String roomId, String username) {
        roomUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(username);
    }

    public void userLeft(String roomId, String username) {
        Set<String> users = roomUsers.get(roomId);
        if (users != null) {
            users.remove(username);
        }
    }

    public int getUserCount(String roomId) {
        Set<String> users = roomUsers.get(roomId);
        return users == null ? 0 : users.size();
    }

    public String getRoomIdForUser(String username) {
        for (Map.Entry<String, Set<String>> entry : roomUsers.entrySet()) {
            if (entry.getValue().contains(username)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
