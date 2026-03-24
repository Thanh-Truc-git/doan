package com.example.doanck.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.doanck.model.Movie;
import com.example.doanck.model.Room;
import com.example.doanck.model.Showtime;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovie(Movie movie);

    List<Showtime> findByRoom(Room room);

    List<Showtime> findByMovieId(Long movieId);
}