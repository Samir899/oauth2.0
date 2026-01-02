package com.proton.oauth.controller;

import com.proton.oauth.dto.RegistrationResponseDto;
import com.proton.oauth.dto.UserRegistrationDto;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.exception.UserAlreadyExistsException;
import com.proton.oauth.service.UserService;
import jakarta.validation.Valid;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);
    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            UserEntity user = userService.registerUser(registrationDto);
            log.info("Successfully registered user: {}", user.getUsername());
            
            RegistrationResponseDto response = RegistrationResponseDto.builder()
                    .username(user.getUsername())
                    .message("Account created successfully. Please check your email for your temporary password.")
                    .build();
                    
            return ResponseEntity.ok(response);
        } catch (UserAlreadyExistsException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration: ", e);
            return ResponseEntity.internalServerError().body(Collections.singletonMap("message", "An unexpected error occurred during registration."));
        }
    }
}
