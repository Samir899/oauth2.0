package com.proton.oauth.service;

import com.proton.oauth.dto.UserRegistrationDto;
import com.proton.oauth.entity.UserEntity;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserEntity registerUser(UserRegistrationDto registrationDto);
    UserEntity createAdminUser(String firstName, String lastName, String email);
    void updateUserByAdmin(Long id, String firstName, String lastName, boolean enabled, boolean forceReset, List<Long> roleIds, Authentication currentUser);
    boolean canEditUser(Authentication currentUser, UserEntity targetUser);
    String generateUniqueUsername(String firstName, String lastName);
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findById(Long id);
    List<UserEntity> findAllUsers();
    List<UserEntity> searchUsers(String query);
    
    // Password Reset methods
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword, String confirmPassword);
    void forceResetPassword(Authentication authentication, String newPassword, String confirmPassword);
}

