package com.example.doanck.model;

import jakarta.persistence.*;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String qrCode;

    // 🔥 THÊM
    private String status;

    private String ticketCode;

    private double price;

    private java.time.LocalDateTime cancelledAt;

    private String refundVoucherCode;

    // ======================
    // RELATION
    // ======================

    @ManyToOne
    @JoinColumn(name = "user_id") // 🔥 QUAN TRỌNG
    private User user;

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    public Ticket(){}

    // ======================
    // GETTER
    // ======================
    public Long getId() { return id; }

    public String getSeatNumber() { return seatNumber; }

    public String getQrCode() { return qrCode; }

    public String getStatus() { return status; }

    public String getTicketCode() { return ticketCode; }

    public double getPrice() { return price; }

    public java.time.LocalDateTime getCancelledAt() { return cancelledAt; }

    public String getRefundVoucherCode() { return refundVoucherCode; }

    public User getUser() { return user; }

    public Showtime getShowtime() { return showtime; }

    public Booking getBooking() { return booking; }

    // ======================
    // SETTER
    // ======================
    public void setId(Long id) { this.id = id; }

    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public void setStatus(String status) { this.status = status; }

    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public void setPrice(double price) { this.price = price; }

    public void setCancelledAt(java.time.LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public void setRefundVoucherCode(String refundVoucherCode) { this.refundVoucherCode = refundVoucherCode; }

    public void setUser(User user) { this.user = user; }

    public void setShowtime(Showtime showtime) { this.showtime = showtime; }

    public void setBooking(Booking booking) { this.booking = booking; }
}
