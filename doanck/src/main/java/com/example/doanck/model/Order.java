package com.example.doanck.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`order`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime orderTime;
    private double totalAmount;
    private double discountAmount = 0;
    private double finalAmount;
    private String status; // PENDING, PAID, CANCELLED, COMPLETED
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = true)
    private Promotion promotion;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        orderTime = LocalDateTime.now();
        status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculateTotal() {
        totalAmount = orderDetails.stream()
                .mapToDouble(OrderDetail::getSubtotal)
                .sum();

        if (promotion != null && promotion.isValid()) {
            if (promotion.getDiscountPercent() > 0) {
                discountAmount = totalAmount * (promotion.getDiscountPercent() / 100);
            } else {
                discountAmount = promotion.getDiscountAmount();
            }
        }

        finalAmount = totalAmount - discountAmount;
    }
}