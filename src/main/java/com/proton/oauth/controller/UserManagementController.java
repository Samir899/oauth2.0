package com.proton.oauth.controller;

import com.proton.oauth.entity.RoleEntity;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.repository.RoleRepository;
import com.proton.oauth.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserManagementController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model, @RequestParam(required = false) String search, Authentication authentication) {
        List<UserEntity> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.findAll().stream()
                    .filter(u -> u.getUsername().contains(search) || u.getEmail().contains(search))
                    .toList();
        } else {
            users = userRepository.findAll();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("currentUser", authentication);
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model, Authentication authentication) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        if (!canEditUser(authentication, user)) {
            throw new AccessDeniedException("You do not have permission to edit this user");
        }
        
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "admin/edit-user";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, 
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam(defaultValue = "false") boolean enabled,
                             @RequestParam(required = false) List<Long> roleIds,
                             Authentication authentication) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        if (!canEditUser(authentication, user)) {
            throw new AccessDeniedException("You do not have permission to edit this user");
        }
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(enabled);
        
        // Only allow changing roles if current user is SUPER_ADMIN
        if (isSuperAdmin(authentication) && roleIds != null) {
            Set<RoleEntity> roles = new HashSet<>(roleRepository.findAllById(roleIds));
            user.setRoles(roles);
        }
        
        userRepository.save(user);
        return "redirect:/admin/users";
    }
    
    private boolean canEditUser(Authentication authentication, UserEntity targetUser) {
        if (isSuperAdmin(authentication)) {
            return true;
        }
        
        if (isAdmin(authentication)) {
            // Admin can only edit if target user has ONLY ROLE_USER
            Set<String> roleNames = targetUser.getRoles().stream()
                    .map(RoleEntity::getName)
                    .collect(Collectors.toSet());
            
            return roleNames.size() == 1 && roleNames.contains("ROLE_USER");
        }
        
        return false;
    }
    
    private boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
    
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
