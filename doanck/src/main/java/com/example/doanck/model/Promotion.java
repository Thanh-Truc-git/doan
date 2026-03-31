package com.example.doanck.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;
    private String description;
    private double discountPercent;
    private double discountAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int usageLimit;
    private int usageCount = 0;
    private boolean active = true;

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active 
            && now.isAfter(startDate) 
            && now.isBefore(endDate)
            && usageCount < usageLimit;
    }
}