package com.proton.oauth.service.impl;

import com.proton.oauth.entity.EmailTemplateEntity;
import com.proton.oauth.entity.SmtpConfigEntity;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.exception.ResourceNotFoundException;
import com.proton.oauth.repository.EmailTemplateRepository;
import com.proton.oauth.repository.SmtpConfigRepository;
import com.proton.oauth.repository.UserRepository;
import com.proton.oauth.service.EmailService;
import com.proton.oauth.service.SettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class SettingsServiceImpl implements SettingsService {

    private final SmtpConfigRepository smtpConfigRepository;
    private final EmailTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public SettingsServiceImpl(SmtpConfigRepository smtpConfigRepository,
                               EmailTemplateRepository templateRepository,
                               UserRepository userRepository,
                               EmailService emailService) {
        this.smtpConfigRepository = smtpConfigRepository;
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public SmtpConfigEntity getSmtpConfig() {
        return smtpConfigRepository.findById(1L)
                .orElseThrow(() -> new ResourceNotFoundException("SMTP Configuration not found"));
    }

    @Override
    public void updateSmtpConfig(SmtpConfigEntity config) {
        config.setId(1L);
        smtpConfigRepository.save(config);
    }

    @Override
    public List<EmailTemplateEntity> findAllTemplates() {
        return templateRepository.findAll();
    }

    @Override
    public Optional<EmailTemplateEntity> findTemplateById(Long id) {
        return templateRepository.findById(id);
    }

    @Override
    public EmailTemplateEntity saveTemplate(EmailTemplateEntity template) {
        return templateRepository.save(template);
    }

    @Override
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    @Override
    public void sendTestEmail(Long templateId, String recipient, String testUserIdentifier) {
        EmailTemplateEntity template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));

        try {
            UserEntity testUser = null;
            if (testUserIdentifier != null && !testUserIdentifier.isEmpty()) {
                testUser = userRepository.findByUsername(testUserIdentifier)
                        .orElseGet(() -> userRepository.findByEmail(testUserIdentifier).orElse(null));
            }

            var config = smtpConfigRepository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("SMTP Configuration not found"));

            String testResetLink = String.format("%s://%s:%d/reset-password?token=TEST_TOKEN",
                    config.getFrontendProtocol(),
                    config.getFrontendHost(),
                    config.getFrontendPort());

            String testLoginLink = String.format("%s://%s:%d/login",
                    config.getFrontendProtocol(),
                    config.getFrontendHost(),
                    config.getFrontendPort());

            Map<String, String> extraVars = new HashMap<>();
            extraVars.put("resetLink", testResetLink);
            extraVars.put("loginLink", testLoginLink);
            extraVars.put("temporaryPassword", "TEST-OTP-1234");
            extraVars.put("otp", "TEST-OTP-1234");

            if (testUser != null) {
                emailService.sendEmailWithUser(recipient, template.getName(), testUser, extraVars);
            } else {
                emailService.sendEmail(recipient, template.getName(), extraVars);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send test email: " + e.getMessage());
        }
    }
}

