package com.proton.oauth.config;

import com.proton.oauth.entity.EmailTemplateEntity;
import com.proton.oauth.entity.RoleEntity;
import com.proton.oauth.entity.SmtpConfigEntity;
import com.proton.oauth.repository.EmailTemplateRepository;
import com.proton.oauth.repository.RoleRepository;
import com.proton.oauth.repository.SmtpConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, 
                                     EmailTemplateRepository emailTemplateRepository,
                                     SmtpConfigRepository smtpConfigRepository) {
        return args -> {
            log.info("Starting data initialization...");
            
            ensureRoleExists(roleRepository, "ROLE_USER");
            ensureRoleExists(roleRepository, "ROLE_ADMIN");
            ensureRoleExists(roleRepository, "ROLE_SUPER_ADMIN");

            ensureEmailTemplateExists(emailTemplateRepository, "PASSWORD_RESET", 
                "Reset Your Password - Proton Tech", 
                "<!DOCTYPE html><html><head><style>body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #1a1f36; margin: 0; padding: 0; background-color: #f7f9fc; }.wrapper { padding: 40px 20px; }.container { max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e5e9f2;}.header { padding: 40px 40px 20px; text-align: center; }.logo-text{font-size: 26px;font-weight: 800;color: #1877f2;letter-spacing: -1px;text-transform: uppercase;}.content { padding: 0 40px 40px; }.hero-text { font-size: 24px; font-weight: 700; color: #1c1e21; margin-bottom: 20px;text-align: center;}.greeting { font-size: 16px; margin-bottom: 15px; font-weight: 600; color: #1c1e21; }.body-text { font-size: 15px; color: #4e5d78; margin-bottom: 25px; }.cta-container { text-align: center; margin: 35px 0; }.button { background-color: #1877f2; color: #ffffff !important; padding: 16px 40px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; display: inline-block;box-shadow: 0 4px 12px rgba(24, 119, 242, 0.2);}.security-badge{background-color: #f0f7ff;color: #1877f2;padding: 15px;border-radius: 8px;font-size: 13px;border: 1px solid #d1e7ff;margin-bottom: 25px;}.fallback-link{font-size: 12px;color: #8d949e;word-break: break-all;text-align: center;margin-top: 25px;}.divider { height: 1px; background-color: #e5e9f2; margin: 30px 0; }.footer { text-align: center; font-size: 12px; color: #8d949e; padding-bottom: 40px; }</style></head><body><div class=\"wrapper\"><div class=\"container\"><div class=\"header\"><div class=\"logo-text\">PROTON TECH</div></div><div class=\"content\"><div class=\"hero-text\">Reset Password</div><p class=\"greeting\">Hi [[firstName]],</p><p class=\"body-text\">We received a request to reset the password for your account (<strong>[[username]]</strong>). To maintain your account security, please click the button below to set a new password.</p><div class=\"cta-container\"><a href=\"[[resetLink]]\" class=\"button\">Create New Password</a></div><div class=\"security-badge\"><strong>Notice:</strong> This link will expire in 60 minutes. If you did not make this request, you can safely ignore this email; no changes have been made to your account.</div><div class=\"divider\"></div><p class=\"body-text\" style=\"font-size: 12px; text-align: center;\">Sent by the <strong>Proton Tech Identity Team</strong>.</p><div class=\"fallback-link\">Trouble with the button? Copy this link:<br>[[resetLink]]</div></div></div><div class=\"footer\"><p>&copy; 2025 Proton Tech. All rights reserved.</p><p>Your Security is our priority.</p></div></div></body></html>",
                "Required variables: [[firstName]], [[username]], [[resetLink]]");

            ensureEmailTemplateExists(emailTemplateRepository, "WELCOME_OTP", 
                "Welcome to Proton Tech - Your Temporary Password", 
                "<!DOCTYPE html><html><head><style>body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #1a1f36; margin: 0; padding: 0; background-color: #f7f9fc; }.wrapper { padding: 40px 20px; }.container { max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e5e9f2;}.header { padding: 40px 40px 20px; text-align: center; }.logo-text{font-size: 26px;font-weight: 800;color: #1877f2;letter-spacing: -1px;text-transform: uppercase;}.content { padding: 0 40px 40px; }.hero-text { font-size: 24px; font-weight: 700; color: #1c1e21; margin-bottom: 20px;text-align: center;}.greeting { font-size: 16px; margin-bottom: 15px; font-weight: 600; color: #1c1e21; }.body-text { font-size: 15px; color: #4e5d78; margin-bottom: 25px; }.otp-container { background-color: #f0f7ff; border: 2px dashed #1877f2; border-radius: 8px; padding: 25px; text-align: center; margin: 30px 0; }.otp-label { font-size: 13px; text-transform: uppercase; letter-spacing: 1px; color: #1877f2; font-weight: 700; margin-bottom: 8px; }.otp-code { font-size: 32px; font-weight: 800; color: #1c1e21; letter-spacing: 4px; font-family: 'Courier New', monospace; }.cta-container { text-align: center; margin: 35px 0; }.button { background-color: #1877f2; color: #ffffff !important; padding: 16px 40px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; display: inline-block;box-shadow: 0 4px 12px rgba(24, 119, 242, 0.2);}.security-badge{background-color: #fdf6f6;color: #1a1f36;padding: 15px;border-radius: 8px;font-size: 13px;border-left: 4px solid #f27474;margin-bottom: 25px;}.divider { height: 1px; background-color: #e5e9f2; margin: 30px 0; }.footer { text-align: center; font-size: 12px; color: #8d949e; padding-bottom: 40px; }</style></head><body><div class=\"wrapper\"><div class=\"container\"><div class=\"header\"><div class=\"logo-text\">PROTON TECH</div></div><div class=\"content\"><div class=\"hero-text\">Welcome to Proton Tech!</div><p class=\"greeting\">Hi [[firstName]],</p><p class=\"body-text\">Thank you for joining <strong>Proton Tech</strong>. Your account has been successfully created. Please use the temporary password below for your first login.</p><div class=\"otp-container\"><div class=\"otp-label\">Temporary Password</div><div class=\"otp-code\">[[temporaryPassword]]</div></div><p class=\"body-text\">For security reasons, you will be prompted to create a new password immediately after your first login. You can use your username (<strong>[[username]]</strong>) or email (<strong>[[email]]</strong>).</p><div class=\"cta-container\"><a href=\"http://localhost:9000/login\" class=\"button\">Log In to Your Account</a></div><div class=\"security-badge\"><strong>Important:</strong> This temporary password is valid for 24 hours. Please change it immediately to secure your account.</div><div class=\"divider\"></div><p class=\"body-text\" style=\"font-size: 12px; text-align: center;\">Sent by the <strong>Proton Tech Identity Team</strong>.</p></div></div><div class=\"footer\"><p>&copy; 2025 Proton Tech. All rights reserved.</p><p>Your Security is our priority.</p></div></div></body></html>",
                "Required variables: [[firstName]], [[username]], [[email]], [[temporaryPassword]]");

            ensureSmtpConfigExists(smtpConfigRepository);
            
            log.info("Data initialization completed.");
        };
    }

    private void ensureRoleExists(RoleRepository roleRepository, String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(new RoleEntity(roleName));
            log.info("Created role: {}", roleName);
        }
    }

    private void ensureEmailTemplateExists(EmailTemplateRepository repository, String name, String subject, String body, String description) {
        repository.findByName(name).ifPresentOrElse(
            template -> {
                if (!template.getBody().contains("<!DOCTYPE html>")) {
                    template.setSubject(subject);
                    template.setBody(body);
                    template.setDescription(description);
                    repository.save(template);
                    log.info("Updated template {} to HTML version", name);
                }
            },
            () -> {
                repository.save(new EmailTemplateEntity(name, subject, body, description));
                log.info("Created template: {}", name);
            }
        );
    }

    private void ensureSmtpConfigExists(SmtpConfigRepository repository) {
        if (repository.count() == 0) {
            SmtpConfigEntity config = new SmtpConfigEntity();
            config.setHost("smtp.gmail.com");
            config.setPort(587);
            config.setUsername("your-email@gmail.com");
            config.setPassword("your-app-password");
            config.setFromEmail("no-reply@proton.com");
            repository.save(config);
            log.info("Created default SMTP configuration.");
        }
    }
}
