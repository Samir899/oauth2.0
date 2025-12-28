package com.proton.oauth.controller;

import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            // In a real app, you would send an email here.
            // For development, we print it to the console.
            System.out.println("==========================================");
            System.out.println("PASSWORD RESET REQUEST FOR: " + email);
            System.out.println("RESET LINK: http://localhost:9000/reset-password?token=" + token);
            System.out.println("==========================================");
        }

        // We return the same message even if user doesn't exist for security (prevent email enumeration)
        return ResponseEntity.ok("If an account exists with that email, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("password");

        Optional<UserEntity> userOptional = userRepository.findByPasswordResetToken(token);

        if (userOptional.isEmpty() || userOptional.get().getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token.");
        }

        UserEntity user = userOptional.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password has been reset successfully.");
    }
}


