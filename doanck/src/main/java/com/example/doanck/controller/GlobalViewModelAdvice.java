package com.example.doanck.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.doanck.model.User;
import com.example.doanck.service.UserService;

@ControllerAdvice
public class GlobalViewModelAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Principal principal) {
        if (principal == null) {
            return false;
        }

        User user = userService.findByUsername(principal.getName());
        return user != null && user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
