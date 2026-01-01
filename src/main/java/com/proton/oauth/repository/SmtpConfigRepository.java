package com.proton.oauth.repository;

import com.proton.oauth.entity.SmtpConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmtpConfigRepository extends JpaRepository<SmtpConfigEntity, Long> {
}

