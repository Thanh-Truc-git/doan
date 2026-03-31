package com.example.doanck.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
public class PendingTicketOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String txnRef;
    private String username;
    private String seats;
    private Long showtimeId;
    private Long movieId;
    private boolean processed;
    private boolean fulfilled;
    private LocalDateTime createdAt;
    private int seatCount;
    private double totalAmount;
    private double finalAmount;
    private double voucherDiscountAmount;
    private String appliedVoucherCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTxnRef() {
        return txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        this.fulfilled = fulfilled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(int seatCount) {
        this.seatCount = seatCount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(double finalAmount) {
        this.finalAmount = finalAmount;
    }

    public double getVoucherDiscountAmount() {
        return voucherDiscountAmount;
    }

    public void setVoucherDiscountAmount(double voucherDiscountAmount) {
        this.voucherDiscountAmount = voucherDiscountAmount;
    }

    public String getAppliedVoucherCode() {
        return appliedVoucherCode;
    }

    public void setAppliedVoucherCode(String appliedVoucherCode) {
        this.appliedVoucherCode = appliedVoucherCode;
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
