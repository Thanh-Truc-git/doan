package com.example.doanck.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.stereotype.Controller;

@Controller
public class SeatWebSocketController {

    @MessageMapping("/seat/book")
    @SendTo("/topic/seats")
    public String seatBooked(String seat){

        return seat;

    }

}