package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.dto.RoomDto;
import com.pans.mono.mono_server.model.Room;
import com.pans.mono.mono_server.model.User;
import com.pans.mono.mono_server.repository.RoomRepository;
import com.pans.mono.mono_server.repository.UserRepository;
import com.pans.mono.mono_server.service.RoomTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomTracker roomTracker;

    @GetMapping
    public ResponseEntity<?> getRooms(@RequestHeader("Authorization") String authHeader) {
        if (resolveUser(authHeader) == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        List<RoomDto> rooms = roomRepository.findAll().stream()
                .map(r -> new RoomDto(r.getId(), r.getName(), roomTracker.getUserCount(r.getId())))
                .toList();

        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinRoom(@PathVariable String id,
                                      @RequestHeader("Authorization") String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        Optional<Room> roomOpt = roomRepository.findById(id);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Room room = roomOpt.get();
        roomTracker.userJoined(room.getId(), user.getUsername());

        return ResponseEntity.ok(new RoomDto(room.getId(), room.getName(), roomTracker.getUserCount(room.getId())));
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request,
                                        @RequestHeader("Authorization") String authHeader) {
        if (resolveUser(authHeader) == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (request.name == null || request.name.isBlank()) {
            return ResponseEntity.badRequest().body("Room name cannot be empty");
        }

        if (roomRepository.existsByName(request.name.trim())) {
            return ResponseEntity.badRequest().body("Room with this name already exists");
        }

        Room room = new Room();
        room.setId(UUID.randomUUID().toString());
        room.setName(request.name.trim());
        roomRepository.save(room);

        return ResponseEntity.ok(new RoomDto(room.getId(), room.getName(), 0));
    }

    private User resolveUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return userRepository.findBySessionToken(token).orElse(null);
    }

    public static class CreateRoomRequest {
        public String name;
    }
}
