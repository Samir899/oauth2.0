package com.proton.oauth.controller;

import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminController {

    private final UserService userService;

    public SuperAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/create-admin")
    public String createAdminForm() {
        return "super-admin/create-admin";
    }

    @PostMapping("/create-admin")
    public String createAdmin(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              Model model) {
        try {
            UserEntity admin = userService.createAdminUser(firstName, lastName, email);
            model.addAttribute("success", "Admin user created successfully! A temporary password has been sent to their email. Username: " + admin.getUsername());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "super-admin/create-admin";
    }
}
