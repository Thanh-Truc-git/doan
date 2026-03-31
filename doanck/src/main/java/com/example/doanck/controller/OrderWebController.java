package com.example.doanck.controller;

import com.example.doanck.model.Food;
import com.example.doanck.service.FoodService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderWebController {

    @Autowired
    private FoodService foodService;

    @GetMapping("/snacks")
    public String snacksPage(@RequestParam(value = "movieId", required = false) Long movieId, Model model) {
        model.addAttribute("foods", foodService.getActiveFoods());
        model.addAttribute("selectedMovieId", movieId != null ? movieId : 0L);
        return "food-order";
    }

    // ===== B?????C 1 =====
    @PostMapping("/create")
    public String handleOrderNow(@RequestParam("movieId") Long movieId, Model model) {
        model.addAttribute("foods", foodService.getActiveFoods());
        model.addAttribute("selectedMovieId", movieId);
        return "food-order";
    }

    // ===== B?????C 2 =====
    @PostMapping("/confirm")
    public String confirmOrder(HttpServletRequest request,
                               HttpSession session,
                               @RequestParam(value = "movieId", required = false) Long movieId,
                               @RequestParam(value = "selectedSeats", required = false) String selectedSeats) {

        Map<String, String[]> paramMap = request.getParameterMap();
        double totalAmount = 0;

        for (String paramName : paramMap.keySet()) {
            if (paramName.startsWith("food_")) {
                try {
                    int quantity = Integer.parseInt(request.getParameter(paramName));
                    if (quantity > 0) {
                        Long foodId = Long.parseLong(paramName.replace("food_", ""));
                        Food food = foodService.getFoodById(foodId);
                        if (food != null) {
                            totalAmount += food.getPrice() * quantity;
                        }
                    }
                } catch (Exception e) {
                    // b??? qua l???i
                }
            }
        }

        System.out.println("Movie ID: " + movieId + " | T???ng ti???n: " + totalAmount);

        String seatsInSession = (String) session.getAttribute("seats");
        String seats = (selectedSeats != null && !selectedSeats.isBlank()) ? selectedSeats.trim() : seatsInSession;
        Long showtimeId = (Long) session.getAttribute("showtime");

        if (seats != null && !seats.isBlank()) {
            session.setAttribute("seats", seats);
        }

        if (movieId != null && movieId > 0) {
            session.setAttribute("movieId", movieId);
        }
        Long effectiveMovieId = (Long) session.getAttribute("movieId");

        List<String> queries = new ArrayList<>();
        if (seats != null && !seats.isBlank()) {
            queries.add("seats=" + URLEncoder.encode(seats, StandardCharsets.UTF_8));
        }
        if (showtimeId != null) {
            queries.add("showtime=" + showtimeId);
        }
        if (effectiveMovieId != null && effectiveMovieId > 0) {
            queries.add("movieId=" + effectiveMovieId);
        }

        if (queries.isEmpty()) {
            return "redirect:/payment/create";
        }
        return "redirect:/payment/create?" + String.join("&", queries);
    }
}

