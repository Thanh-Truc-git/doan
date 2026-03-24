package com.example.doanck.controller;

import com.example.doanck.model.Ticket;
import com.example.doanck.service.TicketService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/checkin")
public class CheckinController {

    @Autowired
    private TicketService ticketService;

    // Trang mở camera quét QR
    @GetMapping
    public String scanner(){
        return "checkin";
    }

    // API xử lý QR
    @GetMapping("/{code}")
    @ResponseBody
    public String checkin(@PathVariable String code){

        Ticket ticket = ticketService.findByQrCode(code);

        if(ticket == null){
            return "❌ Vé không hợp lệ";
        }

        if(ticket.isUsed()){
            return "⚠ Vé đã được sử dụng";
        }

        ticket.setUsed(true);
        ticketService.save(ticket);

        return "✅ Check-in thành công";
    }

}