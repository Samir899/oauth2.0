package com.proton.oauth.controller;

import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.repository.RoleRepository;
import com.proton.oauth.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserManagementController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model, @RequestParam(required = false) String search, Authentication authentication) {
        List<UserEntity> users = (search != null && !search.isEmpty()) 
                ? userService.searchUsers(search) 
                : userService.findAllUsers();
        
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("currentUser", authentication);
        return "admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model, Authentication authentication) {
        UserEntity user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        if (!userService.canEditUser(authentication, user)) {
            return "redirect:/admin/users?error=access_denied";
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
                             @RequestParam(defaultValue = "false") boolean passwordResetRequired,
                             @RequestParam(required = false) List<Long> roleIds,
                             Authentication authentication) {
        try {
            userService.updateUserByAdmin(id, firstName, lastName, enabled, passwordResetRequired, roleIds, authentication);
            return "redirect:/admin/users?success=updated";
        } catch (Exception e) {
            return "redirect:/admin/users?error=" + e.getMessage();
        }
    }
}
