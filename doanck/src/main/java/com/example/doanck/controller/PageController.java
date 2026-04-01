package com.example.doanck.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;
import java.util.List;
import com.example.doanck.model.Ticket;
import com.example.doanck.model.User;
import com.example.doanck.service.TicketService;
import com.example.doanck.service.UserService;

@Controller
public class PageController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/snacks")
    public String snacks() {
        return "redirect:/orders/snacks";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "notifications";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping({"/favourites", "/favorites"})
    public String favourites() {
        return "favourites";
    }

    @GetMapping("/change-password")
    public String changePassword() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(@RequestParam("currentPassword") String currentPassword,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu chưa khớp.");
            return "redirect:/change-password";
        }

        boolean changed = userService.changePassword(principal.getName(), currentPassword, newPassword);
        if (!changed) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không đúng.");
            return "redirect:/change-password";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công.");
        return "redirect:/change-password";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User user = null;
        if (principal != null) {
            user = userService.findByUsername(principal.getName());
        }

        model.addAttribute("userProfile", user);
        model.addAttribute("username", user != null ? user.getUsername() : (principal != null ? principal.getName() : "User"));
        model.addAttribute("email", user != null ? user.getEmail() : "N/A");
        model.addAttribute("role", user != null ? user.getRole() : "USER");
        return "profile";
    }

    @GetMapping("/booking-history")
    public String bookingHistory(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            List<Ticket> tickets = user != null ? ticketService.getTicketsByUser(user) : List.of();
            model.addAttribute("tickets", tickets);
        } else {
            model.addAttribute("tickets", List.of());
        }
        model.addAttribute("username", principal != null ? principal.getName() : "User");
        return "booking-history";
    }
}
