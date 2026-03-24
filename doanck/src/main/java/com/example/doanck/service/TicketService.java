package com.example.doanck.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.doanck.model.Showtime;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.repository.TicketRepository;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    public List<Ticket> findByShowtime(Showtime showtime){
        return ticketRepository.findByShowtime(showtime);
    }

    public List<Ticket> findByUser(User user){
        return ticketRepository.findByUser(user);
    }

    public Ticket save(Ticket ticket){
        return ticketRepository.save(ticket);
    }
    public Ticket findByQrCode(String code){
        return ticketRepository.findByQrCode(code);
    }
}