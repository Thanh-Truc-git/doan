package com.example.doanck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment,Long>{
}