package com.example.doanck.controller;

import com.example.doanck.repository.MovieRepository;
import com.example.doanck.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @GetMapping("/movies")
    public String movies(Model model){

        model.addAttribute("movies",
                movieRepository.findAll());

        return "movies";
    }
}