package com.example.doanck.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "food_id", nullable = true)
    private Food food;

    @ManyToOne
    @JoinColumn(name = "combo_id", nullable = true)
    private Combo combo;

    private int quantity;
    private double unitPrice;
    private double subtotal;
    private String notes;

    public void calculateSubtotal() {
        subtotal = unitPrice * quantity;
    }

    public OrderDetail(Order order, Food food, int quantity) {
        this.order = order;
        this.food = food;
        this.quantity = quantity;
        this.unitPrice = food.getPrice();
        calculateSubtotal();
    }

    public OrderDetail(Order order, Combo combo, int quantity) {
        this.order = order;
        this.combo = combo;
        this.quantity = quantity;
        this.unitPrice = combo.getDiscountedPrice();
        calculateSubtotal();
    }
}