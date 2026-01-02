package com.proton.oauth.controller;

import com.proton.oauth.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.initiatePasswordReset(email);
        log.info("Password reset request received for email: {}", email);
        return ResponseEntity.ok(Collections.singletonMap("message", "If an account exists with that email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        try {
            userService.resetPassword(
                request.get("token"), 
                request.get("password"), 
                request.get("confirmPassword")
            );
            log.info("Password successfully reset using token.");
            return ResponseEntity.ok(Collections.singletonMap("message", "Password has been reset successfully."));
        } catch (IllegalArgumentException e) {
            log.warn("Password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during password reset: ", e);
            return ResponseEntity.internalServerError().body(Collections.singletonMap("message", "An error occurred during password reset."));
        }
    }

    @PostMapping("/force-reset-password")
    public ResponseEntity<Map<String, String>> forceResetPassword(@RequestBody Map<String, String> request, 
                                                     Authentication authentication,
                                                     HttpServletRequest servletRequest) {
        try {
            userService.forceResetPassword(
                authentication, 
                request.get("password"), 
                request.get("confirmPassword")
            );
            
            // Invalidate session and clear security context to log the user out
            HttpSession session = servletRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            SecurityContextHolder.clearContext();

            log.info("Forced password reset completed and user logged out: {}", authentication.getName());
            return ResponseEntity.ok(Collections.singletonMap("message", "Password updated successfully."));
        } catch (IllegalArgumentException e) {
            log.warn("Forced password reset failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during forced password reset: ", e);
            return ResponseEntity.internalServerError().body(Collections.singletonMap("message", "An error occurred during password update."));
        }
    }
}
