package com.example.doanck.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Room;
import com.example.doanck.model.Seat;
import com.example.doanck.repository.SeatRepository;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    public void createSeats(Room room) {

        for (char row = 'A'; row <= 'K'; row++) {

            for (int i = 1; i <= 15; i++) {

                Seat seat = new Seat();

                seat.setSeatNumber(row + "" + i);
                seat.setRoom(room);

                seatRepository.save(seat);
            }

        }

    }

}