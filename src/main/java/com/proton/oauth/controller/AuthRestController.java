package com.proton.oauth.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.proton.oauth.repository.UserRepository;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.dto.CustomUserDetails;

import java.time.Instant;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthRestController {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthRestController(JwtEncoder jwtEncoder, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        UserEntity user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseGet(() -> userRepository.findByEmail(loginRequest.getUsername()).orElse(null));

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        if (!user.isEnabled()) {
            log.warn("Login failed (disabled) for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(401).body("Account is disabled");
        }

        log.info("Login successful for user: {}", user.getUsername());
        String token = generateToken(new CustomUserDetails(user));
        String fullName = user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "");
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.isPasswordResetRequired(), fullName));
    }

    private String generateToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();
        long expiry = 36000L; // 10 hours

        String scope = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        log.info("Generating token for subject: {}", userDetails.getUsername());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://192.168.1.6:9000") // Use real IP as issuer
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(userDetails.getUsername())
                .claim("scope", scope)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class AuthResponse {
        private String accessToken;
        private String username;
        private boolean passwordResetRequired;
        private String fullName;
    }
}

