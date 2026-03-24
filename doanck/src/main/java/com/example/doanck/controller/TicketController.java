package com.example.doanck.controller;

import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.service.TicketService;
import com.example.doanck.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String myTickets(Authentication authentication, Model model){

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        List<Ticket> tickets = ticketService.findByUser(user);


        Map<String, List<Ticket>> groupedTickets = tickets.stream()
                .collect(Collectors.groupingBy(ticket -> {
                    // 🔥 nếu null → gán tạm để không crash
                    return ticket.getBookingCode() != null
                            ? ticket.getBookingCode()
                            : "UNKNOWN_" + ticket.getId();
                }));

        // convert sang List<List<Ticket>>
        List<List<Ticket>> ticketGroups = new ArrayList<>(groupedTickets.values());

        model.addAttribute("ticketsGrouped", ticketGroups);

        return "my-tickets";
    }
}