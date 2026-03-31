package com.example.doanck.service;

import com.example.doanck.model.Movie;
import com.example.doanck.model.Showtime;
import com.example.doanck.repository.MovieRepository;
import com.example.doanck.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> getMoviesWithUpcomingShowtimes() {
        LocalDateTime now = LocalDateTime.now();
        return movieRepository.findAll().stream()
                .map(movie -> attachUpcomingShowtimes(movie, now))
                .filter(movie -> movie.getShowtimes() != null && !movie.getShowtimes().isEmpty())
                .sorted(Comparator.comparing(movie -> movie.getShowtimes().get(0).getStartTime()))
                .collect(Collectors.toList());
    }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    public Movie getMovieByIdWithUpcomingShowtimes(Long id) {
        Movie movie = getMovieById(id);
        if (movie == null) {
            return null;
        }
        return attachUpcomingShowtimes(movie, LocalDateTime.now());
    }

    public Showtime getNextAvailableShowtime(Movie movie) {
        if (movie == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        return showtimeRepository.findByMovie(movie).stream()
                .filter(showtime -> isShowtimeActiveAt(showtime, now))
                .min(Comparator.comparing(Showtime::getStartTime))
                .orElse(null);
    }

    public boolean isShowtimeActive(Showtime showtime) {
        LocalDateTime endTime = getShowtimeEndTime(showtime);
        return endTime != null && endTime.isAfter(LocalDateTime.now());
    }

    public LocalDateTime getShowtimeEndTime(Showtime showtime) {
        if (showtime == null || showtime.getStartTime() == null) {
            return null;
        }

        int durationMinutes = 0;
        if (showtime.getMovie() != null && showtime.getMovie().getDuration() != null) {
            durationMinutes = Math.max(showtime.getMovie().getDuration(), 0);
        }

        return showtime.getStartTime().plusMinutes(durationMinutes);
    }

    public Movie saveMovie(Movie movie) {
        if (movie.getTrailerUrl() != null && !movie.getTrailerUrl().trim().isEmpty()) {
            String url = movie.getTrailerUrl().trim();
            if (url.contains("watch?v=")) {
                url = url.replace("watch?v=", "embed/");
            }
            movie.setTrailerUrl(url);
        }

        if (movie.getId() != null) {
            Optional<Movie> oldMovieOpt = movieRepository.findById(movie.getId());
            if (oldMovieOpt.isPresent()) {
                Movie oldMovie = oldMovieOpt.get();

                if (movie.getPoster() == null || movie.getPoster().trim().isEmpty()) {
                    movie.setPoster(oldMovie.getPoster());
                }
                if (movie.getTrailerUrl() == null || movie.getTrailerUrl().trim().isEmpty()) {
                    movie.setTrailerUrl(oldMovie.getTrailerUrl());
                }
                if (movie.getDescription() == null || movie.getDescription().trim().isEmpty()) {
                    movie.setDescription(oldMovie.getDescription());
                }
                if (movie.getGenre() == null || movie.getGenre().trim().isEmpty()) {
                    movie.setGenre(oldMovie.getGenre());
                }
                if (movie.getDuration() == null) {
                    movie.setDuration(oldMovie.getDuration());
                }
            }
        }

        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
        } else {
            System.out.println("Movie does not exist with id = " + id);
        }
    }

    private Movie attachUpcomingShowtimes(Movie movie, LocalDateTime referenceTime) {
        List<Showtime> upcomingShowtimes = showtimeRepository.findByMovie(movie).stream()
                .filter(showtime -> isShowtimeActiveAt(showtime, referenceTime))
                .sorted(Comparator.comparing(Showtime::getStartTime))
                .collect(Collectors.toList());

        movie.setShowtimes(upcomingShowtimes);
        return movie;
    }

    private boolean isShowtimeActiveAt(Showtime showtime, LocalDateTime referenceTime) {
        LocalDateTime endTime = getShowtimeEndTime(showtime);
        return endTime != null && endTime.isAfter(referenceTime);
    }
}
