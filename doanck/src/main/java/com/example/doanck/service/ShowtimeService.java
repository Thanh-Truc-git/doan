package com.example.doanck.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Movie;
import com.example.doanck.model.Room;
import com.example.doanck.model.Showtime;
import com.example.doanck.repository.ShowtimeRepository;

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;


    // =========================
    // Lấy tất cả suất chiếu
    // =========================
    public List<Showtime> getAllShowtimes(){

        return showtimeRepository.findAll();

    }


    // =========================
    // Lấy suất chiếu theo ID
    // =========================
    public Showtime getShowtimeById(Long id){

        return showtimeRepository.findById(id).orElse(null);

    }


    // =========================
    // Lấy suất chiếu theo Movie
    // =========================
    public List<Showtime> getShowtimeByMovie(Movie movie){

        return showtimeRepository.findByMovie(movie);

    }


    // =========================
    // Lấy suất chiếu theo Room
    // =========================
    public List<Showtime> getShowtimeByRoom(Room room){

        return showtimeRepository.findByRoom(room);

    }


    // =========================
    // Tạo hoặc cập nhật suất chiếu
    // =========================
    public Showtime save(Showtime showtime){

        return showtimeRepository.save(showtime);

    }


    // =========================
    // Xóa suất chiếu
    // =========================
    public void delete(Long id){

        showtimeRepository.deleteById(id);

    }

}