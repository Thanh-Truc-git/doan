package com.example.doanck.model;

import jakarta.persistence.*;

@Entity
public class BookedSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    @ManyToOne
    private Showtime showtime;

    public BookedSeat(){}

    public Long getId() {
        return id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

}