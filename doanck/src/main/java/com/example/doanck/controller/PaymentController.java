package com.example.doanck.controller;

import com.example.doanck.config.VNPayConfig;
import com.example.doanck.model.Movie;
import com.example.doanck.model.PendingTicketOrder;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.User;
import com.example.doanck.repository.PendingTicketOrderRepository;
import com.example.doanck.repository.ShowtimeRepository;
import com.example.doanck.service.MovieService;
import com.example.doanck.service.TicketService;
import com.example.doanck.service.UserService;
import com.example.doanck.service.VoucherService;
import com.example.doanck.util.VNPayUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PendingTicketOrderRepository pendingTicketOrderRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieService movieService;
    private final VoucherService voucherService;
    private final UserService userService;

    public PaymentController(
            PendingTicketOrderRepository pendingTicketOrderRepository,
            ShowtimeRepository showtimeRepository,
            MovieService movieService,
            VoucherService voucherService,
            UserService userService) {
        this.pendingTicketOrderRepository = pendingTicketOrderRepository;
        this.showtimeRepository = showtimeRepository;
        this.movieService = movieService;
        this.voucherService = voucherService;
        this.userService = userService;
    }

    @GetMapping("/create")
    public String createPayment(
            @RequestParam(required = false) String seats,
            @RequestParam(required = false) Long showtime,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) String voucherCode,
            HttpSession session,
            Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        Showtime activeShowtime = null;
        if (showtime != null) {
            activeShowtime = showtimeRepository.findById(showtime).orElse(null);
            if (!movieService.isShowtimeActive(activeShowtime)) {
                return "redirect:/movies?expired=1";
            }
            if (activeShowtime.getMovie() != null) {
                movieId = activeShowtime.getMovie().getId();
            }
        } else if (movieId != null) {
            Movie movie = movieService.getMovieById(movieId);
            activeShowtime = movieService.getNextAvailableShowtime(movie);
            if (!movieService.isShowtimeActive(activeShowtime)) {
                return "redirect:/movies?expired=1";
            }
            showtime = activeShowtime.getId();
        } else {
            return "redirect:/movies";
        }

        if (seats != null) {
            session.setAttribute("seats", seats);
        }
        session.setAttribute("showtime", showtime);
        session.setAttribute("movieId", movieId);
        session.setAttribute("username", principal.getName());

        int seatCount = resolveSeatCount(seats);
        double totalAmount = seatCount * TicketService.DEFAULT_TICKET_PRICE;
        User user = userService.findByUsername(principal.getName());
        double voucherDiscount = voucherService.previewDiscount(voucherCode, user, totalAmount);

        if (voucherCode != null && !voucherCode.isBlank() && voucherDiscount <= 0) {
            return redirectSeatMapWithVoucherError(movieId, showtime, "invalid");
        }

        double finalAmount = Math.max(0, totalAmount - voucherDiscount);

        Map<String, String> params = new HashMap<>();
        String txnRef = String.valueOf(System.currentTimeMillis());

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf((long) (finalAmount * 100)));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan ve phim");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_IpAddr", "127.0.0.1");

        PendingTicketOrder pendingTicketOrder = new PendingTicketOrder();
        pendingTicketOrder.setTxnRef(txnRef);
        pendingTicketOrder.setUsername(principal.getName());
        pendingTicketOrder.setSeats(seats);
        pendingTicketOrder.setShowtimeId(showtime);
        pendingTicketOrder.setMovieId(movieId);
        pendingTicketOrder.setProcessed(false);
        pendingTicketOrder.setSeatCount(seatCount);
        pendingTicketOrder.setTotalAmount(totalAmount);
        pendingTicketOrder.setFinalAmount(finalAmount);
        pendingTicketOrder.setVoucherDiscountAmount(voucherDiscount);
        pendingTicketOrder.setAppliedVoucherCode(
                voucherCode != null && !voucherCode.isBlank() ? voucherCode.trim() : null);
        pendingTicketOrderRepository.save(pendingTicketOrder);

        if (finalAmount <= 0) {
            pendingTicketOrder.setProcessed(true);
            pendingTicketOrderRepository.save(pendingTicketOrder);
            return "redirect:/payment-return?vnp_ResponseCode=00&vnp_TxnRef=" + txnRef;
        }

        String returnUrl = UriComponentsBuilder
                .fromUriString(VNPayConfig.vnp_ReturnUrl)
                .queryParam("seats", seats)
                .queryParam("showtime", showtime)
                .queryParam("movieId", movieId)
                .queryParam("username", principal.getName())
                .build()
                .toUriString();

        params.put("vnp_ReturnUrl", returnUrl);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", formatter.format(new Date()));

        String query = VNPayUtil.createQueryString(params);
        String secureHash = VNPayUtil.hmacSHA512(VNPayConfig.vnp_HashSecret, query);
        query += "&vnp_SecureHash=" + secureHash;

        return "redirect:" + VNPayConfig.vnp_Url + "?" + query;
    }

    private int resolveSeatCount(String seats) {
        if (seats == null || seats.isBlank()) {
            return 0;
        }

        return (int) java.util.Arrays.stream(seats.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .count();
    }

    private String redirectSeatMapWithVoucherError(Long movieId, Long showtimeId, String errorCode) {
        if (movieId != null) {
            return "redirect:/seat-map/" + movieId + "?voucherError=" + errorCode;
        }
        return "redirect:/movies?voucherError=" + errorCode;
    }
}
