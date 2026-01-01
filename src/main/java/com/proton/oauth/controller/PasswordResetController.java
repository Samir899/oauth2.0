package com.proton.oauth.controller;

import com.proton.oauth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private final UserService userService;

    public PasswordResetController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.initiatePasswordReset(email);
        log.info("Password reset request received for email: {}", email);
        return ResponseEntity.ok("If an account exists with that email, a reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        try {
            userService.resetPassword(
                request.get("token"), 
                request.get("password"), 
                request.get("confirmPassword")
            );
            log.info("Password successfully reset using token.");
            return ResponseEntity.ok("Password has been reset successfully.");
        } catch (IllegalArgumentException e) {
            log.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during password reset: ", e);
            return ResponseEntity.internalServerError().body("An error occurred during password reset.");
        }
    }

    @PostMapping("/force-reset-password")
    public ResponseEntity<String> forceResetPassword(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            userService.forceResetPassword(
                authentication, 
                request.get("password"), 
                request.get("confirmPassword")
            );
            log.info("Forced password reset completed for user: {}", authentication.getName());
            return ResponseEntity.ok("Password has been updated successfully.");
        } catch (IllegalArgumentException e) {
            log.warn("Forced password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during forced password reset: ", e);
            return ResponseEntity.internalServerError().body("An error occurred during password update.");
        }
    }
}
