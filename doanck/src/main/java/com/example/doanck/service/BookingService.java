package com.example.doanck.service;

import com.example.doanck.model.Booking;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.repository.BookingRepository;
import com.example.doanck.repository.TicketRepository;
import com.example.doanck.util.QRCodeService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private QRCodeService qrCodeService;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByBookingTimeDesc();
    }

    public Booking createBooking(User user, Showtime showtime) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setBookingTime(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public boolean isSeatBooked(String seatNumber, Showtime showtime) {
        if (seatNumber == null || seatNumber.isBlank()) {
            return true;
        }

        String normalizedSeat = seatNumber.trim();
        return showtime != null
                ? ticketRepository.existsBySeatNumberAndShowtimeAndStatusNot(normalizedSeat, showtime, "CANCELLED")
                : ticketRepository.existsBySeatNumberAndShowtimeIsNullAndStatusNot(normalizedSeat, "CANCELLED");
    }

    @Transactional
    public Ticket bookSeat(Ticket ticket) {

        try {
            if (ticket.getShowtime() != null) {
                boolean exists = ticketRepository
                        .existsBySeatNumberAndShowtimeAndStatusNot(
                                ticket.getSeatNumber(),
                                ticket.getShowtime(),
                                "CANCELLED");

                if (exists) {
                    return null;
                }
            }

            String qrText =
                    "Movie Ticket | Code:"
                            + (ticket.getTicketCode() != null ? ticket.getTicketCode() : "N/A")
                            + " | Seat:"
                            + ticket.getSeatNumber()
                            + " | Showtime:"
                            + (ticket.getShowtime() != null
                            ? ticket.getShowtime().getId()
                            : "N/A");

            try {
                String qrCode = qrCodeService.generateQRCode(qrText);
                ticket.setQrCode(qrCode);
            } catch (Exception e) {
                ticket.setQrCode(null);
            }

            try {
                return ticketRepository.save(ticket);
            } catch (Exception saveException) {
                if (ticket.getQrCode() != null) {
                    ticket.setQrCode(null);
                    return ticketRepository.save(ticket);
                }
                throw saveException;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
