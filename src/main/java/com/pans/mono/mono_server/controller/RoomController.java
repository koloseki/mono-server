package com.pans.mono.mono_server.controller;

import com.pans.mono.mono_server.dto.RoomDto;
import com.pans.mono.mono_server.model.Message;
import com.pans.mono.mono_server.model.Room;
import com.pans.mono.mono_server.model.User;
import com.pans.mono.mono_server.repository.MessageRepository;
import com.pans.mono.mono_server.repository.RoomRepository;
import com.pans.mono.mono_server.repository.UserRepository;
import com.pans.mono.mono_server.service.RoomTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Room management and message history")
public class RoomController {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomTracker roomTracker;
    private final MessageRepository messageRepository;

    @Operation(summary = "List all rooms with online user count")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of rooms"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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

    @Operation(summary = "Join a room")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Joined successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
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

    @Operation(summary = "Create a new room")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Room created"),
        @ApiResponse(responseCode = "400", description = "Name empty or already taken"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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

    @Operation(summary = "Get message history for a room")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of messages ordered by timestamp"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable String id,
                                         @RequestHeader("Authorization") String authHeader) {
        if (resolveUser(authHeader) == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (!roomRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<Message> messages = messageRepository.findByRoomIdOrderByTimestampAsc(id);
        return ResponseEntity.ok(messages);
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
