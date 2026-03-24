package com.example.doanck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.Booking;

public interface BookingRepository extends JpaRepository<Booking,Long>{

}