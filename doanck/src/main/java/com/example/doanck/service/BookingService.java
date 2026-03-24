package com.example.doanck.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Ticket;
import com.example.doanck.repository.TicketRepository;
import com.example.doanck.util.QRCodeService;

import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Transactional
    public void bookSeat(List<String> seats, Ticket baseTicket, String role) {

        boolean isAdmin =
                role.equalsIgnoreCase("ADMIN") ||
                        role.equalsIgnoreCase("STAFF") ||
                        role.equalsIgnoreCase("ROLE_ADMIN") ||
                        role.equalsIgnoreCase("ROLE_STAFF");

        // =========================
        // USER → 1 BOOKING
        // =========================
        if (!isAdmin) {

            String bookingCode = baseTicket.getBookingCode();

            String qrText = "BOOKING:" + bookingCode;
            String qrCode = qrCodeService.generateQRCode(qrText);

            for (String seat : seats) {

                if (seat == null || seat.trim().isEmpty()) continue;

                boolean exists = ticketRepository
                        .existsBySeatNumberAndShowtime(
                                seat,
                                baseTicket.getShowtime());

                if (exists) {
                    throw new RuntimeException("Seat " + seat + " already booked");
                }

                Ticket ticket = new Ticket();

                ticket.setSeatNumber(seat);
                ticket.setShowtime(baseTicket.getShowtime());
                ticket.setUser(baseTicket.getUser());

                ticket.setBookingCode(bookingCode);
                ticket.setQrCode(qrCode);

                ticketRepository.save(ticket);
            }

        }
        // =========================
        // ADMIN → mỗi ghế 1 BOOKING
        // =========================
        else {

            for (String seat : seats) {

                if (seat == null || seat.trim().isEmpty()) continue;

                boolean exists = ticketRepository
                        .existsBySeatNumberAndShowtime(
                                seat,
                                baseTicket.getShowtime());

                if (exists) {
                    throw new RuntimeException("Seat " + seat + " already booked");
                }


                String bookingCode = UUID.randomUUID().toString();

                String qrText = "TICKET:" + bookingCode;
                String qrCode = qrCodeService.generateQRCode(qrText);

                Ticket ticket = new Ticket();

                ticket.setSeatNumber(seat);
                ticket.setShowtime(baseTicket.getShowtime());
                ticket.setUser(baseTicket.getUser());

                ticket.setBookingCode(bookingCode);
                ticket.setQrCode(qrCode);

                ticketRepository.save(ticket);
            }
        }
    }
}