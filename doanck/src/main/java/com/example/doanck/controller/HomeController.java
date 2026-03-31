package com.example.doanck.controller;

import com.example.doanck.model.Movie;
import com.example.doanck.model.Room;
import com.example.doanck.model.Seat;
import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.repository.SeatRepository;
import com.example.doanck.service.ComboService;
import com.example.doanck.service.MovieService;
import com.example.doanck.service.TicketService;
import com.example.doanck.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private ComboService comboService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private VoucherService voucherService;

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        List<Movie> movies = movieService.getMoviesWithUpcomingShowtimes();
        model.addAttribute("movies", movies);
        model.addAttribute("combos", comboService.getActiveCombos());
        model.addAttribute("featuredMovie", movies.isEmpty() ? null : movies.get(0));
        model.addAttribute("username", principal != null ? principal.getName() : "User");
        return "movies";
    }

    @GetMapping("/moviescate")
    public String moviesCategory(Model model) {
        model.addAttribute("movies", movieService.getMoviesWithUpcomingShowtimes());
        return "moviescate";
    }

    @GetMapping("/seat-map")
    public String seatMap(
            @RequestParam(value = "movieId", required = false) Long movieId,
            @RequestParam(value = "voucherError", required = false) String voucherError,
            Model model,
            Principal principal) {
        if (movieId == null) {
            return "redirect:/movies";
        }

        Movie movie = movieService.getMovieById(movieId);
        Showtime selectedShowtime = movieService.getNextAvailableShowtime(movie);
        if (!movieService.isShowtimeActive(selectedShowtime)) {
            return "redirect:/movies?expired=1";
        }

        populateSeatMapModel(movie, selectedShowtime, model, principal, voucherError);
        return "seat-map";
    }

    @GetMapping("/seat-map/{movieId}")
    public String seatMap(
            @PathVariable("movieId") String movieId,
            @RequestParam(value = "voucherError", required = false) String voucherError,
            Model model,
            Principal principal) {
        Long parsedMovieId;
        try {
            parsedMovieId = Long.parseLong(movieId);
        } catch (NumberFormatException ex) {
            return "redirect:/movies";
        }

        Movie movie = movieService.getMovieById(parsedMovieId);
        Showtime selectedShowtime = movieService.getNextAvailableShowtime(movie);
        if (!movieService.isShowtimeActive(selectedShowtime)) {
            return "redirect:/movies?expired=1";
        }

        populateSeatMapModel(movie, selectedShowtime, model, principal, voucherError);
        return "seat-map";
    }

    @GetMapping("/cinemas")
    public String cinemas() {
        return "cinemas";
    }

    private void populateSeatMapModel(
            Movie movie,
            Showtime selectedShowtime,
            Model model,
            Principal principal,
            String voucherError) {
        model.addAttribute("movie", movie);
        model.addAttribute("movieId", movie.getId());
        model.addAttribute("showtimeId", selectedShowtime.getId());
        model.addAttribute("showtime", selectedShowtime);
        model.addAttribute(
                "availableVouchers",
                principal != null ? voucherService.getAvailableVouchers(principal.getName()) : List.of());
        model.addAttribute("voucherError", voucherError);

        Room room = selectedShowtime.getRoom();
        if (room != null) {
            List<Seat> seats = seatRepository.findByRoom(room);
            model.addAttribute("seats", seats);
        }

        List<Ticket> tickets = ticketService.getTicketsByShowtime(selectedShowtime);
        List<String> bookedSeats = tickets.stream()
                .map(Ticket::getSeatNumber)
                .filter(seatNumber -> seatNumber != null && !seatNumber.isBlank())
                .collect(Collectors.toList());

        model.addAttribute("bookedSeats", bookedSeats);
    }
}
