package com.example.doanck.model;

import jakarta.persistence.*;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @ManyToOne
    private User user;

    @ManyToOne
    private Showtime showtime;

    @ManyToOne
    private Booking booking;
    private boolean used = false;
    private String bookingCode;
    private boolean checkedIn = false;
    public Ticket(){}

    public Long getId() {
        return id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getQrCode() {
        return qrCode;
    }

    public User getUser() {
        return user;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}