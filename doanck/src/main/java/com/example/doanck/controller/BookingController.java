package com.example.doanck.controller;

import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.service.BookingService;
import com.example.doanck.service.ShowtimeService;
import com.example.doanck.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller // ❗ đổi từ RestController → Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ShowtimeService showtimeService;

    @Autowired
    private UserService userService;

    @PostMapping("/book")
    public String bookSeats(
            @RequestParam List<String> seats,
            @RequestParam Long showtimeId,
            Principal principal) {

        // 🔐 Lấy user hiện tại
        User user = userService.findByUsername(principal.getName());

        // 🎬 Lấy suất chiếu
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);

        // 🎟 Ticket mẫu
        Ticket baseTicket = new Ticket();
        baseTicket.setShowtime(showtime);
        baseTicket.setUser(user);

        // 🚀 Gọi service
        bookingService.bookSeat(
                seats,
                baseTicket,
                user.getRole()
        );

        // ✅ redirect đúng
        return "redirect:/tickets";
    }
}