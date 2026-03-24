package com.example.doanck.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;

    private Double price;

    @ManyToOne
    private Movie movie;

    @ManyToOne
    private Room room;

    public Showtime(){}

    public Long getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Movie getMovie() {
        return movie;
    }

    public Room getRoom() {
        return room;
    }

    public Double getPrice() {
        return price;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}