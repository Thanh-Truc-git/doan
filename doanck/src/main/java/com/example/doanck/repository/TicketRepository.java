package com.example.doanck.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;

public interface TicketRepository extends JpaRepository<Ticket, Long>{

    List<Ticket> findByShowtime(Showtime showtime);

    List<Ticket> findByUser(User user);
    Ticket findByQrCode(String qrCode);
    boolean existsBySeatNumberAndShowtime(String seatNumber, Showtime showtime);
    List<Ticket> findByBookingCode(String bookingCode);
}