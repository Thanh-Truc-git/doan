package com.example.doanck.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.doanck.model.Seat;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.Room;
import com.example.doanck.repository.SeatRepository;
import com.example.doanck.repository.ShowtimeRepository;
import com.example.doanck.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/seats")
public class SeatController {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/{showtimeId}")
    public String seatMap(@PathVariable Long showtimeId, Model model) {
        Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
        if (showtime == null) return "redirect:/movies";

        Room room = showtime.getRoom();
        List<Seat> seats = seatRepository.findByRoom(room);
        List<Ticket> tickets = ticketService.getTicketsByShowtime(showtime);
        List<String> bookedSeats = tickets.stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toList());

        model.addAttribute("seats", seats);
        model.addAttribute("bookedSeats", bookedSeats);
        model.addAttribute("showtimeId", showtimeId);
        model.addAttribute("movie", showtime.getMovie());

        return "seat-map";
    }
}
