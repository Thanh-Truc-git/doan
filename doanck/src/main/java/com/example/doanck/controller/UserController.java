package com.example.doanck.controller;

import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.service.TicketService;
import com.example.doanck.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/user")
    public String userProfile(Model model){
        User user = userService.getUserById(1L);
        model.addAttribute("user", user);

        List<Ticket> tickets = user != null ? ticketService.getTicketsByUser(user) : List.of();
        model.addAttribute("tickets", tickets);
        model.addAttribute("username", user != null ? user.getUsername() : "User");

        return "my-tickets";
    }
}
