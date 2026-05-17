package com.pans.mono.mono_server.config;

import com.pans.mono.mono_server.model.Room;
import com.pans.mono.mono_server.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            List<String> defaultRooms = List.of("General", "Rezydencja", "Programowanie");
            for (String name : defaultRooms) {
                Room room = new Room();
                room.setId(UUID.randomUUID().toString());
                room.setName(name);
                roomRepository.save(room);
            }
        }
    }
}
