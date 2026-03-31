package com.example.doanck.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.Payment;
import com.example.doanck.model.Booking;

public interface PaymentRepository extends JpaRepository<Payment,Long>{

    Optional<Payment> findByBooking(Booking booking);
}
