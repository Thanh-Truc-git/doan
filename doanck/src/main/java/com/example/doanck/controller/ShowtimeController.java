package com.example.doanck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import com.example.doanck.model.Showtime;
import com.example.doanck.repository.ShowtimeRepository;

@Controller
@RequestMapping("/showtimes")
public class ShowtimeController {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @GetMapping
    public String showtimes(Model model){

        List<Showtime> showtimes = showtimeRepository.findAll();

        model.addAttribute("showtimes", showtimes);

        return "admin/showtimes";
    }
}
