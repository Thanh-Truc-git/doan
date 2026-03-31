package com.example.doanck.service;

import com.example.doanck.model.Booking;
import com.example.doanck.model.Movie;
import com.example.doanck.model.PendingTicketOrder;
import com.example.doanck.model.Payment;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.repository.PendingTicketOrderRepository;
import com.example.doanck.repository.PaymentRepository;
import com.example.doanck.repository.ShowtimeRepository;
import com.example.doanck.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PendingTicketOrderService {

    private final PendingTicketOrderRepository pendingTicketOrderRepository;
    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final MovieService movieService;
    private final UserService userService;
    private final BookingService bookingService;
    private final VoucherService voucherService;
    private final PaymentRepository paymentRepository;

    public PendingTicketOrderService(
            PendingTicketOrderRepository pendingTicketOrderRepository,
            TicketRepository ticketRepository,
            ShowtimeRepository showtimeRepository,
            MovieService movieService,
            UserService userService,
            BookingService bookingService,
            VoucherService voucherService,
            PaymentRepository paymentRepository) {
        this.pendingTicketOrderRepository = pendingTicketOrderRepository;
        this.ticketRepository = ticketRepository;
        this.showtimeRepository = showtimeRepository;
        this.movieService = movieService;
        this.userService = userService;
        this.bookingService = bookingService;
        this.voucherService = voucherService;
        this.paymentRepository = paymentRepository;
    }

    public void fulfillPendingOrdersForUser(String username) {
        List<PendingTicketOrder> pendingOrders =
                pendingTicketOrderRepository.findByUsernameAndProcessedTrueAndFulfilledFalse(username);

        for (PendingTicketOrder pendingOrder : pendingOrders) {
            fulfillPendingOrder(pendingOrder);
        }
    }

    public boolean fulfillPendingOrder(PendingTicketOrder pendingOrder) {
        if (pendingOrder == null || pendingOrder.isFulfilled()) {
            return true;
        }

        if (!pendingOrder.isProcessed()) {
            return false;
        }

        User user = userService.findByUsername(pendingOrder.getUsername());
        if (user == null || pendingOrder.getSeats() == null || pendingOrder.getSeats().isBlank()) {
            return false;
        }

        Showtime showtime = resolveShowtime(pendingOrder.getShowtimeId(), pendingOrder.getMovieId());
        String[] seatList = pendingOrder.getSeats().split(",");
        int resolvedSeats = 0;
        Booking booking = null;
        double ticketPrice = pendingOrder.getSeatCount() > 0 && pendingOrder.getTotalAmount() > 0
                ? pendingOrder.getTotalAmount() / pendingOrder.getSeatCount()
                : TicketService.DEFAULT_TICKET_PRICE;

        for (String seatValue : seatList) {
            String seatNumber = seatValue.trim();
            if (seatNumber.isEmpty()) {
                continue;
            }

            boolean exists = showtime != null
                    ? ticketRepository.existsByUserUsernameAndSeatNumberAndShowtimeIdAndStatusNot(
                            user.getUsername(), seatNumber, showtime.getId(), "CANCELLED")
                    : ticketRepository.existsByUserUsernameAndSeatNumberAndShowtimeIsNullAndStatusNot(
                            user.getUsername(), seatNumber, "CANCELLED");

            if (exists) {
                resolvedSeats++;
                continue;
            }

            if (bookingService.isSeatBooked(seatNumber, showtime)) {
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
            ticket.setPrice(ticketPrice);

            Ticket savedTicket = bookingService.bookSeat(ticket);
            if (savedTicket != null) {
                resolvedSeats++;
            }
        }

        if (resolvedSeats == seatList.length) {
            if (booking != null) {
                redeemVoucherIfNeeded(pendingOrder, user);
                createPaymentIfMissing(pendingOrder, booking);
            }
            pendingOrder.setFulfilled(true);
            pendingTicketOrderRepository.save(pendingOrder);
            return true;
        }

        return false;
    }

    private Showtime resolveShowtime(Long showtimeId, Long movieId) {
        if (showtimeId != null) {
            Showtime showtime = showtimeRepository.findById(showtimeId).orElse(null);
            if (showtime != null) {
                return showtime;
            }
        }

        if (movieId == null) {
            return null;
        }

        Movie movie = movieService.getMovieById(movieId);
        if (movie == null) {
            return null;
        }

        List<Showtime> showtimes = showtimeRepository.findByMovie(movie);
        return showtimes.isEmpty() ? null : showtimes.get(0);
    }

    private void redeemVoucherIfNeeded(PendingTicketOrder pendingOrder, User user) {
        if (pendingOrder.getAppliedVoucherCode() == null
                || pendingOrder.getAppliedVoucherCode().isBlank()
                || pendingOrder.getVoucherDiscountAmount() <= 0) {
            return;
        }

        voucherService.redeemVoucher(
                pendingOrder.getAppliedVoucherCode(),
                user,
                pendingOrder.getVoucherDiscountAmount());
    }

    private void createPaymentIfMissing(PendingTicketOrder pendingOrder, Booking booking) {
        if (paymentRepository.findByBooking(booking).isPresent()) {
            return;
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(Math.max(0, pendingOrder.getFinalAmount()));
        payment.setStatus("PAID");
        payment.setMethod(resolvePaymentMethod(pendingOrder));
        payment.setPaymentTime(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private String resolvePaymentMethod(PendingTicketOrder pendingOrder) {
        if (pendingOrder.getFinalAmount() <= 0 && pendingOrder.getVoucherDiscountAmount() > 0) {
            return "VOUCHER";
        }

        if (pendingOrder.getVoucherDiscountAmount() > 0) {
            return "VNPAY+VOUCHER";
        }

        return "VNPAY";
    }
}
