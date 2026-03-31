package com.example.doanck.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @PostMapping
    public String bookSeat(
            @RequestBody List<String> seats,
            HttpSession session){

        // lưu seats vào session
        session.setAttribute("seats", String.join(",", seats));

        return "ok";
    }

}