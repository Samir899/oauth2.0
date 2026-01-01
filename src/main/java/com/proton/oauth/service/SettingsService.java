package com.proton.oauth.service;

import com.proton.oauth.entity.EmailTemplateEntity;
import com.proton.oauth.entity.SmtpConfigEntity;
import java.util.List;
import java.util.Optional;

public interface SettingsService {
    SmtpConfigEntity getSmtpConfig();
    void updateSmtpConfig(SmtpConfigEntity config);
    
    List<EmailTemplateEntity> findAllTemplates();
    Optional<EmailTemplateEntity> findTemplateById(Long id);
    EmailTemplateEntity saveTemplate(EmailTemplateEntity template);
    void deleteTemplate(Long id);
    
    void sendTestEmail(Long templateId, String recipient, String testUserIdentifier);
}

