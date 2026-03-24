package com.example.doanck.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.repository.ShowtimeRepository;
import com.example.doanck.service.TicketService;

@Controller
public class SeatController {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/seats/{showtimeId}")
    public String seatPage(@PathVariable Long showtimeId, Model model){

        Showtime showtime =
                showtimeRepository.findById(showtimeId).orElse(null);

        List<Ticket> bookedSeats =
                ticketService.findByShowtime(showtime);

        model.addAttribute("showtime", showtime);
        model.addAttribute("bookedSeats", bookedSeats);

        return "seat-map";
    }

}