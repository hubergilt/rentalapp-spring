package com.rentalapp.repository;

import com.rentalapp.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    long countByStatus(Room.Status status);
}
