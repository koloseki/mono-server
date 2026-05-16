package com.pans.mono.mono_server.repository;

import com.pans.mono.mono_server.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, String> {
    boolean existsByName(String name);
}
