package com.example.doanck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.Seat;
import com.example.doanck.model.Room;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Lấy tất cả ghế trong 1 phòng
    List<Seat> findByRoom(Room room);
}