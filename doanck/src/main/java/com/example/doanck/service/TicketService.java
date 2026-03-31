package com.example.doanck.service;

import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.model.Voucher;
import com.example.doanck.repository.TicketRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    public static final double DEFAULT_TICKET_PRICE = 100000;

    private static final Pattern CODE_PATTERN = Pattern.compile("Code:([^|]+)");
    private static final Pattern SEAT_PATTERN = Pattern.compile("Seat:([^|]+)");
    private static final Pattern SHOWTIME_PATTERN = Pattern.compile("Showtime:([^|]+)");

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private VoucherService voucherService;

    public List<Ticket> getAllTickets(){
        return ticketRepository.findAll();
    }

    public List<Ticket> getAdminTicketHistory() {
        return ticketRepository.findAll().stream()
                .sorted(
                        Comparator.comparing(
                                        this::resolveHistoryTime,
                                        Comparator.nullsLast(Comparator.naturalOrder()))
                                .reversed()
                                .thenComparing(
                                        Ticket::getId,
                                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Ticket getTicketById(Long id){
        return ticketRepository.findById(id).orElse(null);
    }

    public Ticket saveTicket(Ticket ticket){
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getTicketsByUser(User user){
        return ticketRepository.findByUser(user);
    }

    public List<Ticket> getTicketsByUsername(String username){
        return ticketRepository.findByUserUsernameOrderByIdDesc(username);
    }

    public List<Ticket> getTicketsByShowtime(Showtime showtime){
        return ticketRepository.findByShowtimeAndStatusIn(showtime, List.of("BOOKED", "CHECKED_IN"));
    }

    public long countSoldTickets() {
        return ticketRepository.countByStatusIn(List.of("BOOKED", "CHECKED_IN"));
    }

    public long countCheckedInTickets() {
        return ticketRepository.countByStatusIn(List.of("CHECKED_IN"));
    }

    public long countBookedTickets() {
        return ticketRepository.countByStatusIn(List.of("BOOKED"));
    }

    public double getSoldTicketRevenue() {
        return countSoldTickets() * DEFAULT_TICKET_PRICE;
    }

    public Ticket getTicketByCode(String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            return null;
        }

        return ticketRepository.findByTicketCode(ticketCode.trim()).orElse(null);
    }

    public Ticket resolveTicketFromScanValue(String scanValue) {
        if (scanValue == null || scanValue.isBlank()) {
            return null;
        }

        String normalizedValue = scanValue.trim();

        Ticket directTicket = getTicketByCode(normalizedValue);
        if (directTicket != null) {
            return directTicket;
        }

        String extractedCode = extractValue(normalizedValue, CODE_PATTERN);
        if (extractedCode != null) {
            Ticket ticketByCode = getTicketByCode(extractedCode);
            if (ticketByCode != null) {
                return ticketByCode;
            }
        }

        String seatNumber = extractValue(normalizedValue, SEAT_PATTERN);
        String showtimeValue = extractValue(normalizedValue, SHOWTIME_PATTERN);

        if (seatNumber == null) {
            return null;
        }

        if (showtimeValue == null || showtimeValue.equalsIgnoreCase("N/A")) {
            return ticketRepository
                    .findTopBySeatNumberAndShowtimeIsNullOrderByIdDesc(seatNumber.trim())
                    .orElse(null);
        }

        try {
            Long showtimeId = Long.parseLong(showtimeValue.trim());
            return ticketRepository
                    .findTopBySeatNumberAndShowtimeIdOrderByIdDesc(seatNumber.trim(), showtimeId)
                    .orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public CheckInResult checkInByScanValue(String scanValue) {
        Ticket ticket = resolveTicketFromScanValue(scanValue);

        if (ticket == null) {
            return new CheckInResult(false, "Không tìm thấy vé từ mã QR hoặc mã vé.", null);
        }

        if ("CHECKED_IN".equalsIgnoreCase(ticket.getStatus())) {
            return new CheckInResult(false, "Vé này đã được check-in trước đó.", ticket);
        }

        if ("CANCELLED".equalsIgnoreCase(ticket.getStatus())) {
            return new CheckInResult(false, "Ve nay da bi huy va khong con hieu luc.", ticket);
        }

        ticket.setStatus("CHECKED_IN");
        Ticket savedTicket = ticketRepository.save(ticket);
        return new CheckInResult(true, "Check-in vé thành công.", savedTicket);
    }

    public boolean canCancelTicket(Ticket ticket) {
        if (ticket == null || ticket.getShowtime() == null || ticket.getShowtime().getStartTime() == null) {
            return false;
        }

        if (!"BOOKED".equalsIgnoreCase(ticket.getStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime showtimeStart = ticket.getShowtime().getStartTime();

        if (!showtimeStart.isAfter(now)) {
            return false;
        }

        return !showtimeStart.isBefore(now.plusHours(12));
    }

    public CancelTicketResult cancelTicketWithVoucher(Long ticketId, String username) {
        if (ticketId == null || username == null || username.isBlank()) {
            return new CancelTicketResult(false, "Khong tim thay ve de huy.", null, null);
        }

        Ticket ticket = ticketRepository.findByIdAndUserUsername(ticketId, username).orElse(null);
        if (ticket == null) {
            return new CancelTicketResult(false, "Khong tim thay ve thuoc tai khoan hien tai.", null, null);
        }

        if (!canCancelTicket(ticket)) {
            return new CancelTicketResult(
                    false,
                    "Chi duoc huy ve khi suat chieu con cach hien tai it nhat 12 tieng.",
                    ticket,
                    null);
        }

        User user = ticket.getUser();
        if (user == null) {
            return new CancelTicketResult(false, "Khong tim thay tai khoan so huu ve.", ticket, null);
        }

        double refundAmount = resolveTicketPrice(ticket);
        Voucher voucher = voucherService.createRefundVoucher(user, ticket, refundAmount);

        ticket.setStatus("CANCELLED");
        ticket.setCancelledAt(LocalDateTime.now());
        ticket.setRefundVoucherCode(voucher.getCode());
        ticketRepository.save(ticket);

        return new CancelTicketResult(true, "Huy ve thanh cong va da hoan tien ve voucher.", ticket, voucher);
    }

    public double resolveTicketPrice(Ticket ticket) {
        if (ticket == null || ticket.getPrice() <= 0) {
            return DEFAULT_TICKET_PRICE;
        }
        return ticket.getPrice();
    }

    private String extractValue(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find()) {
            return null;
        }

        String value = matcher.group(1);
        return value != null ? value.trim() : null;
    }

    public record CheckInResult(boolean success, String message, Ticket ticket) {
    }

    public record CancelTicketResult(boolean success, String message, Ticket ticket, Voucher voucher) {
    }

    private LocalDateTime resolveHistoryTime(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        if (ticket.getCancelledAt() != null) {
            return ticket.getCancelledAt();
        }

        if (ticket.getBooking() != null && ticket.getBooking().getBookingTime() != null) {
            return ticket.getBooking().getBookingTime();
        }

        if (ticket.getShowtime() != null) {
            return ticket.getShowtime().getStartTime();
        }

        return null;
    }

}
