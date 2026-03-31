package com.example.doanck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.doanck.model.Movie;
import com.example.doanck.service.MovieService;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "*")
public class MovieApiController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/now-showing")
    public List<Movie> getNowShowing() {
        return movieService.getAllMovies();
    }

    @GetMapping("/coming-soon")
    public List<Movie> getComingSoon() {
        return movieService.getAllMovies();
    }
}