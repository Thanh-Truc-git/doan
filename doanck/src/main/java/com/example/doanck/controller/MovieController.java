package com.example.doanck.controller;

import com.example.doanck.model.Movie;
import com.example.doanck.service.ComboService;
import com.example.doanck.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ComboService comboService;

    @GetMapping
    public String movies(Model model, Principal principal) {
        try {
            List<Movie> movies = movieService.getMoviesWithUpcomingShowtimes();
            model.addAttribute("movies", movies);
            model.addAttribute("combos", comboService.getActiveCombos());
            model.addAttribute("featuredMovie", movies.isEmpty() ? null : movies.get(0));
            model.addAttribute("username", principal != null ? principal.getName() : "User");
            return "movies";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }
}
