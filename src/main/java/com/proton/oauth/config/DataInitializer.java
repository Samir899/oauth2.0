package com.proton.oauth.config;

import com.proton.oauth.entity.RoleEntity;
import com.proton.oauth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            ensureRoleExists(roleRepository, "ROLE_USER");
            ensureRoleExists(roleRepository, "ROLE_ADMIN");
            ensureRoleExists(roleRepository, "ROLE_SUPER_ADMIN");
        };
    }

    private void ensureRoleExists(RoleRepository roleRepository, String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(new RoleEntity(roleName));
        }
    }
}

