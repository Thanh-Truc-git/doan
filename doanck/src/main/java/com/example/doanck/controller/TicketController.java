package com.example.doanck.controller;

import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.model.Voucher;
import com.example.doanck.service.PendingTicketOrderService;
import com.example.doanck.service.TicketService;
import com.example.doanck.service.UserService;
import com.example.doanck.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    @Autowired
    private PendingTicketOrderService pendingTicketOrderService;

    @Autowired
    private VoucherService voucherService;

    @GetMapping({"/tickets", "/my-tickets"})
    public String myTickets(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName());
        pendingTicketOrderService.fulfillPendingOrdersForUser(principal.getName());
        List<Ticket> tickets = user != null
                ? ticketService.getTicketsByUser(user)
                : ticketService.getTicketsByUsername(principal.getName());
        List<Voucher> vouchers = voucherService.getUserVouchers(principal.getName());

        model.addAttribute("tickets", tickets);
        model.addAttribute("username", user != null ? user.getUsername() : principal.getName());
        model.addAttribute("vouchers", vouchers);
        model.addAttribute(
                "cancelableTicketIds",
                tickets.stream()
                        .filter(ticketService::canCancelTicket)
                        .map(Ticket::getId)
                        .toList());

        return "my-tickets";
    }

    @PostMapping("/tickets/{ticketId}/cancel")
    public String cancelTicket(
            @PathVariable Long ticketId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        TicketService.CancelTicketResult result =
                ticketService.cancelTicketWithVoucher(ticketId, principal.getName());

        if (result.success()) {
            String voucherCode = result.voucher() != null ? result.voucher().getCode() : "";
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    result.message() + (voucherCode.isBlank() ? "" : " Voucher: " + voucherCode));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.message());
        }

        return "redirect:/my-tickets";
    }
}
