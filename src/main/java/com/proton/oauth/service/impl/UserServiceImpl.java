package com.proton.oauth.service.impl;

import com.proton.oauth.dto.UserRegistrationDto;
import com.proton.oauth.entity.RoleEntity;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.exception.ResourceNotFoundException;
import com.proton.oauth.exception.UserAlreadyExistsException;
import com.proton.oauth.repository.RoleRepository;
import com.proton.oauth.repository.UserRepository;
import com.proton.oauth.service.EmailService;
import com.proton.oauth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, 
                           RoleRepository roleRepository, 
                           PasswordEncoder passwordEncoder, 
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public UserEntity registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + registrationDto.getEmail());
        }

        String username = generateUniqueUsername(registrationDto.getFirstName(), registrationDto.getLastName());
        String otp = UUID.randomUUID().toString().substring(0, 8);

        UserEntity user = new UserEntity();
        user.setFirstName(registrationDto.getFirstName().trim());
        user.setLastName(registrationDto.getLastName().trim());
        user.setEmail(registrationDto.getEmail().trim().toLowerCase());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(otp));
        user.setPasswordResetRequired(true);
        user.setEnabled(true);

        RoleEntity userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_USER not found"));
        user.setRoles(Collections.singleton(userRole));

        UserEntity savedUser = userRepository.save(user);
        log.info("New user registered: {}", username);

        sendWelcomeEmail(savedUser, otp);
        return savedUser;
    }

    @Override
    public UserEntity createAdminUser(String firstName, String lastName, String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }

        UserEntity admin = new UserEntity();
        admin.setFirstName(firstName.trim());
        admin.setLastName(lastName.trim());
        admin.setEmail(email.trim().toLowerCase());
        admin.setUsername(generateUniqueUsername(firstName, lastName));
        
        String otp = UUID.randomUUID().toString().substring(0, 8);
        admin.setPassword(passwordEncoder.encode(otp));
        admin.setPasswordResetRequired(true);
        admin.setEnabled(true);

        Set<RoleEntity> roles = new HashSet<>();
        roleRepository.findByName("ROLE_USER").ifPresent(roles::add);
        roleRepository.findByName("ROLE_ADMIN").ifPresent(roles::add);
        admin.setRoles(roles);

        UserEntity savedAdmin = userRepository.save(admin);
        log.info("New admin created: {}", savedAdmin.getUsername());

        sendWelcomeEmail(savedAdmin, otp);
        return savedAdmin;
    }

    @Override
    public void updateUserByAdmin(Long id, String firstName, String lastName, boolean enabled, boolean forceReset, List<Long> roleIds, Authentication currentUser) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!canEditUser(currentUser, user)) {
            log.warn("Unauthorized attempt to edit user {} by {}", id, currentUser.getName());
            throw new AccessDeniedException("You do not have permission to edit this user");
        }

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEnabled(enabled);
        user.setPasswordResetRequired(forceReset);

        if (isSuperAdmin(currentUser) && roleIds != null) {
            Set<RoleEntity> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
        }

        userRepository.save(user);
        log.info("User {} updated by admin {}", user.getUsername(), currentUser.getName());
    }

    @Override
    public boolean canEditUser(Authentication currentUser, UserEntity targetUser) {
        if (isSuperAdmin(currentUser)) {
            return true;
        }
        if (isAdmin(currentUser)) {
            Set<String> roleNames = targetUser.getRoles().stream()
                    .map(RoleEntity::getName)
                    .collect(Collectors.toSet());
            return roleNames.size() == 1 && roleNames.contains("ROLE_USER");
        }
        return false;
    }

    @Override
    public String generateUniqueUsername(String firstName, String lastName) {
        String base = (firstName.trim() + "." + lastName.trim()).toLowerCase().replaceAll("\\s+", "");
        String username = base;
        int count = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + count;
            count++;
        }
        return username;
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) { return userRepository.findByUsername(username); }

    @Override
    public Optional<UserEntity> findByEmail(String email) { return userRepository.findByEmail(email); }

    @Override
    public Optional<UserEntity> findById(Long id) { return userRepository.findById(id); }

    @Override
    public List<UserEntity> findAllUsers() { return userRepository.findAll(); }

    @Override
    public List<UserEntity> searchUsers(String query) {
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().contains(query) || u.getEmail().contains(query))
                .toList();
    }

    @Override
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            Map<String, String> variables = new HashMap<>();
            variables.put("resetLink", "http://localhost:9000/reset-password?token=" + token);
            emailService.sendEmailWithUser(email, "PASSWORD_RESET", user, variables);
            log.info("Password reset initiated for {}", email);
        }, () -> log.warn("Password reset attempted for non-existent email: {}", email));
    }

    @Override
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        UserEntity user = userRepository.findByPasswordResetToken(token)
                .filter(u -> u.getPasswordResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> {
                    log.warn("Invalid or expired password reset token attempted: {}", token);
                    return new IllegalArgumentException("Invalid or expired reset token");
                });

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
        log.info("Password successfully reset for user: {}", user.getUsername());
    }

    @Override
    public void forceResetPassword(Authentication authentication, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetRequired(false);
        userRepository.save(user);
        log.info("Force password reset completed for user: {}", user.getUsername());
    }

    private void sendWelcomeEmail(UserEntity user, String otp) {
        Map<String, String> variables = new HashMap<>();
        variables.put("temporaryPassword", otp);
        emailService.sendEmailWithUser(user.getEmail(), "WELCOME_OTP", user, variables);
    }

    private boolean isSuperAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
