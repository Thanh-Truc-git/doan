package com.example.doanck.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.User;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsBySeatNumberAndShowtime(String seatNumber, Showtime showtime);

    boolean existsBySeatNumberAndShowtimeIsNull(String seatNumber);

    boolean existsBySeatNumberAndShowtimeAndStatusNot(String seatNumber, Showtime showtime, String status);

    boolean existsBySeatNumberAndShowtimeIsNullAndStatusNot(String seatNumber, String status);

    List<Ticket> findByUser(User user);

    List<Ticket> findByUserUsernameOrderByIdDesc(String username);

    Optional<Ticket> findByTicketCode(String ticketCode);

    boolean existsByUserUsernameAndSeatNumberAndShowtimeId(String username, String seatNumber, Long showtimeId);

    boolean existsByUserUsernameAndSeatNumberAndShowtimeIsNull(String username, String seatNumber);

    boolean existsByUserUsernameAndSeatNumberAndShowtimeIdAndStatusNot(
            String username,
            String seatNumber,
            Long showtimeId,
            String status);

    boolean existsByUserUsernameAndSeatNumberAndShowtimeIsNullAndStatusNot(
            String username,
            String seatNumber,
            String status);

    Optional<Ticket> findTopBySeatNumberAndShowtimeIdOrderByIdDesc(String seatNumber, Long showtimeId);

    Optional<Ticket> findTopBySeatNumberAndShowtimeIsNullOrderByIdDesc(String seatNumber);

    List<Ticket> findByShowtime(Showtime showtime);

    List<Ticket> findByShowtimeAndStatusIn(Showtime showtime, List<String> statuses);

    long countByStatusIn(List<String> statuses);

    Optional<Ticket> findByIdAndUserUsername(Long id, String username);
}
