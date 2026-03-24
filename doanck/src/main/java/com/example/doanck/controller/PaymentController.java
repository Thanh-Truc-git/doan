package com.example.doanck.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import com.example.doanck.service.BookingService;
import org.springframework.security.core.Authentication;
import com.example.doanck.model.User;
import com.example.doanck.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.doanck.config.VNPayConfig;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.repository.ShowtimeRepository;
import com.example.doanck.util.VNPayUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingService bookingService;

    // =========================
    // CREATE PAYMENT
    // =========================
    @GetMapping("/create")
    public String createPayment(
            @RequestParam String seats,
            @RequestParam Long showtime,
            HttpSession session){

        if(seats == null || seats.trim().isEmpty()){
            return "redirect:/movies";
        }

        Showtime show = showtimeRepository.findById(showtime).orElse(null);

        if(show == null){
            return "redirect:/movies";
        }

        // 👉 Lưu session
        session.setAttribute("seats", seats);
        session.setAttribute("showtime", showtime);

        String[] seatArr = seats.split(",");

        int quantity = 0;
        for(String seat : seatArr){
            if(!seat.trim().isEmpty()){
                quantity++;
            }
        }

        double total = show.getPrice() * quantity;
        long vnpAmount = (long)(total * 100);

        Map<String,String> params = new HashMap<>();

        params.put("vnp_Version","2.1.0");
        params.put("vnp_Command","pay");
        params.put("vnp_TmnCode",VNPayConfig.vnp_TmnCode);
        params.put("vnp_Amount",String.valueOf(vnpAmount));
        params.put("vnp_CurrCode","VND");

        params.put("vnp_TxnRef",String.valueOf(System.currentTimeMillis()));
        params.put("vnp_OrderInfo","Thanh toan ve phim");
        params.put("vnp_OrderType","other");
        params.put("vnp_Locale","vn");
        params.put("vnp_IpAddr","127.0.0.1");

        params.put("vnp_ReturnUrl",VNPayConfig.vnp_ReturnUrl);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate",formatter.format(new Date()));

        String query = VNPayUtil.createQueryString(params);

        String secureHash =
                VNPayUtil.hmacSHA512(
                        VNPayConfig.vnp_HashSecret,
                        query);

        query += "&vnp_SecureHash=" + secureHash;

        return "redirect:" + VNPayConfig.vnp_Url + "?" + query;
    }

    // =========================
    // PAYMENT RETURN
    // =========================
    @GetMapping("/return")
    public String paymentReturn(
            HttpServletRequest request,
            HttpSession session,
            Authentication authentication) {

        String responseCode = request.getParameter("vnp_ResponseCode");

        if (!"00".equals(responseCode)) {
            return "redirect:/movies";
        }

        String seats = (String) session.getAttribute("seats");
        Long showtimeId = (Long) session.getAttribute("showtime");

        if(seats == null || showtimeId == null){
            return "redirect:/movies";
        }

        Showtime showtime =
                showtimeRepository.findById(showtimeId).orElse(null);

        if(showtime == null){
            return "redirect:/movies";
        }

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        List<String> seatList = Arrays.asList(seats.split(","));

        // 🔥 FIX QUAN TRỌNG: tạo bookingCode tại đây
        String bookingCode = UUID.randomUUID().toString();

        Ticket baseTicket = new Ticket();
        baseTicket.setShowtime(showtime);
        baseTicket.setUser(user);
        baseTicket.setBookingCode(bookingCode); // 🔥 thêm dòng này

        bookingService.bookSeat(
                seatList,
                baseTicket,
                user.getRole()
        );

        session.removeAttribute("seats");
        session.removeAttribute("showtime");

        return "redirect:/tickets";
    }
}