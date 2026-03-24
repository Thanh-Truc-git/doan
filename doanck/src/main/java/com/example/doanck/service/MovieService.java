package com.example.doanck.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Movie;
import com.example.doanck.repository.MovieRepository;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    // Lấy danh sách phim
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Lấy phim theo id
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    // Lưu phim
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // Xóa phim
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

}