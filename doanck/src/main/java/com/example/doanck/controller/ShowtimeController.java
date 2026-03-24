package com.example.doanck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.doanck.model.Showtime;
import com.example.doanck.model.Movie;
import com.example.doanck.repository.ShowtimeRepository;
import com.example.doanck.repository.MovieRepository;

@Controller
@RequestMapping("/showtimes")
public class ShowtimeController {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @GetMapping("/movie/{movieId}")
    public String showtimesByMovie(
            @PathVariable Long movieId,
            Model model){

        Movie movie =
                movieRepository.findById(movieId).orElse(null);

        List<Showtime> showtimes =
                showtimeRepository.findByMovieId(movieId);

        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", showtimes);

        return "movie-detail";
    }
}