package com.example.doanck.repository;

import com.example.doanck.model.Booking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByOrderByBookingTimeDesc();
}
