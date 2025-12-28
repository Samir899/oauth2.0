package com.proton.oauth.controller;

import com.proton.oauth.entity.RoleEntity;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.repository.RoleRepository;
import com.proton.oauth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/super-admin")
public class SuperAdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/create-admin")
    public String createAdminForm() {
        return "super-admin/create-admin";
    }

    @PostMapping("/create-admin")
    public String createAdmin(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              @RequestParam String password,
                              Model model) {
        
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "super-admin/create-admin";
        }

        UserEntity admin = new UserEntity();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        
        String base = (firstName + "." + lastName).toLowerCase().replaceAll("\\s+", "");
        String username = base;
        int count = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + count;
            count++;
        }
        
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        
        Set<RoleEntity> roles = new HashSet<>();
        roleRepository.findByName("ROLE_USER").ifPresent(roles::add);
        roleRepository.findByName("ROLE_ADMIN").ifPresent(roles::add);
        admin.setRoles(roles);
        
        admin.setEnabled(true);

        userRepository.save(admin);
        
        model.addAttribute("success", "Admin user created successfully! Username: " + username);
        return "super-admin/create-admin";
    }
}
