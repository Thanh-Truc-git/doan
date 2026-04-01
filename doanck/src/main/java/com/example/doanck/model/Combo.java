package com.example.doanck.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "combo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double originalPrice;
    private double discountedPrice;
    private double discountPercent;
    private String image;
    private int quantity;
    private boolean active = true;

    @Transient
    private MultipartFile imageFile;

    public Combo(String name, double originalPrice, double discountedPrice) {
        this.name = name;
        this.originalPrice = originalPrice;
        this.discountedPrice = discountedPrice;
        this.discountPercent = ((originalPrice - discountedPrice) / originalPrice) * 100;
    }
}
