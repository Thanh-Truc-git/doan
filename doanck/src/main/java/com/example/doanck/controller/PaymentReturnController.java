package com.example.doanck.controller;

import com.example.doanck.model.Booking;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.model.Movie;
import com.example.doanck.model.PendingTicketOrder;
import com.example.doanck.repository.PendingTicketOrderRepository;
import com.example.doanck.repository.ShowtimeRepository;
import com.example.doanck.service.BookingService;
import com.example.doanck.service.MovieService;
import com.example.doanck.service.PendingTicketOrderService;
import com.example.doanck.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.UUID;

@Controller
public class PaymentReturnController {

    private final PendingTicketOrderRepository pendingTicketOrderRepository;

    public PaymentReturnController(PendingTicketOrderRepository pendingTicketOrderRepository) {
        this.pendingTicketOrderRepository = pendingTicketOrderRepository;
    }

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private PendingTicketOrderService pendingTicketOrderService;

    @GetMapping("/payment-return")
    public String paymentReturn(
            HttpServletRequest request,
            HttpSession session,
            Principal principal) {

        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");
        PendingTicketOrder pendingTicketOrder = txnRef != null
                ? pendingTicketOrderRepository.findByTxnRef(txnRef).orElse(null)
                : null;

        if ("00".equals(responseCode)) {
            if (pendingTicketOrder != null) {
                if (!pendingTicketOrder.isProcessed()) {
                    pendingTicketOrder.setProcessed(true);
                    pendingTicketOrderRepository.save(pendingTicketOrder);
                }

                pendingTicketOrderService.fulfillPendingOrder(pendingTicketOrder);
                clearSession(session);
                return "redirect:/my-tickets";
            }

            String seats = firstNonBlank(
                    pendingTicketOrder != null ? pendingTicketOrder.getSeats() : null,
                    request.getParameter("seats"),
                    (String) session.getAttribute("seats"));
            Long showtimeId = firstNonNull(
                    pendingTicketOrder != null ? pendingTicketOrder.getShowtimeId() : null,
                    parseLong(request.getParameter("showtime")),
                    (Long) session.getAttribute("showtime"));
            Long movieId = firstNonNull(
                    pendingTicketOrder != null ? pendingTicketOrder.getMovieId() : null,
                    parseLong(request.getParameter("movieId")),
                    (Long) session.getAttribute("movieId"));

            String username = firstNonBlank(
                    principal != null ? principal.getName() : null,
                    pendingTicketOrder != null ? pendingTicketOrder.getUsername() : null,
                    firstNonBlank(
                            request.getParameter("username"),
                            (String) session.getAttribute("username")));
            User user = username != null ? userService.findByUsername(username) : null;

            if (user == null) {
                return "redirect:/login";
            }

            if (seats == null || seats.isBlank()) {
                clearSession(session);
                return "redirect:/my-tickets";
            }

            Showtime showtime = null;

            if (showtimeId != null) {
                showtime = showtimeRepository.findById(showtimeId).orElse(null);
            }

            if (showtime == null && movieId != null) {
                Movie movie = movieService.getMovieById(movieId);
                if (movie != null) {
                    List<Showtime> showtimes = showtimeRepository.findByMovie(movie);
                    if (!showtimes.isEmpty()) {
                        showtime = showtimes.get(0);
                    }
                }
            }

            String[] seatList = seats.split(",");
            Booking booking = null;

            for (String s : seatList) {
                try {
                    String seatNumber = s.trim();
                    if (seatNumber.isEmpty() || bookingService.isSeatBooked(seatNumber, showtime)) {
                        continue;
                    }

                    if (booking == null) {
                        booking = bookingService.createBooking(user, showtime);
                    }

                    Ticket ticket = new Ticket();

                    ticket.setSeatNumber(seatNumber);
                    ticket.setShowtime(showtime);
                    ticket.setUser(user);
                    ticket.setBooking(booking);
                    ticket.setStatus("BOOKED");
                    ticket.setTicketCode("TICKET-" + UUID.randomUUID());

                    bookingService.bookSeat(ticket);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            clearSession(session);

            return "redirect:/my-tickets";
        }

        return "payment-fail";
    }

    private void clearSession(HttpSession session) {
        session.removeAttribute("seats");
        session.removeAttribute("showtime");
        session.removeAttribute("movieId");
        session.removeAttribute("username");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
