package com.example.doanck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

}