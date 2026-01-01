package com.proton.oauth.repository;

import com.proton.oauth.entity.EmailTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplateEntity, Long> {
    Optional<EmailTemplateEntity> findByName(String name);
}

