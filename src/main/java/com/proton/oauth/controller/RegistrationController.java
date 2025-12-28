package com.proton.oauth.controller;

import com.proton.oauth.dto.UserRegistrationDto;
import com.proton.oauth.entity.RoleEntity;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.repository.RoleRepository;
import com.proton.oauth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/users")
public class RegistrationController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        String firstName = registrationDto.getFirstName() != null ? registrationDto.getFirstName().trim() : "";
        String lastName = registrationDto.getLastName() != null ? registrationDto.getLastName().trim() : "";
        String email = registrationDto.getEmail() != null ? registrationDto.getEmail().trim() : "";

        if (firstName.isEmpty() || lastName.isEmpty()) {
            return ResponseEntity.badRequest().body("First name and last name are required");
        }

        if (!firstName.matches("^[a-zA-Z]+$") || !lastName.matches("^[a-zA-Z]+$")) {
            return ResponseEntity.badRequest().body("First name and last name should only contain letters (no spaces or special characters)");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        String username = generateUniqueUsername(firstName, lastName);

        UserEntity user = new UserEntity();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        
        RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles(Collections.singleton(userRole));
        
        user.setEnabled(true);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully. Your username is: " + username);
    }

    private String generateUniqueUsername(String firstName, String lastName) {
        String base = (firstName + "." + lastName).toLowerCase();
        String username = base;
        int count = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + count;
            count++;
        }
        return username;
    }
}
