package com.proton.oauth.service;

import com.proton.oauth.entity.EmailTemplateEntity;
import com.proton.oauth.entity.SmtpConfigEntity;
import com.proton.oauth.entity.UserEntity;
import com.proton.oauth.repository.EmailTemplateRepository;
import com.proton.oauth.repository.SmtpConfigRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final EmailTemplateRepository templateRepository;
    private final SmtpConfigRepository smtpConfigRepository;

    public EmailService(EmailTemplateRepository templateRepository, SmtpConfigRepository smtpConfigRepository) {
        this.templateRepository = templateRepository;
        this.smtpConfigRepository = smtpConfigRepository;
    }

    @Async
    public void sendEmail(String to, String templateName, Map<String, String> variables) {
        try {
            EmailTemplateEntity template = templateRepository.findByName(templateName)
                    .orElseThrow(() -> new RuntimeException("Email template not found: " + templateName));
            
            sendEmailInternal(to, template.getSubject(), template.getBody(), variables);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendEmailWithUser(String to, String templateName, UserEntity user, Map<String, String> extraVariables) {
        try {
            EmailTemplateEntity template = templateRepository.findByName(templateName)
                    .orElseThrow(() -> new RuntimeException("Email template not found: " + templateName));

            Map<String, String> variables = resolveUserVariables(user);
            if (extraVariables != null) {
                variables.putAll(extraVariables);
            }

            sendEmailInternal(to, template.getSubject(), template.getBody(), variables);
        } catch (Exception e) {
            log.error("Failed to send email with user to {}: {}", to, e.getMessage());
        }
    }

    private void sendEmailInternal(String to, String subject, String body, Map<String, String> variables) throws MessagingException {
        SmtpConfigEntity config = smtpConfigRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("SMTP configuration not found"));

        JavaMailSender mailSender = createMailSender(config);

        String processedBody = body;
        String processedSubject = subject;
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "[[" + entry.getKey() + "]]";
            String value = entry.getValue() != null ? entry.getValue() : "";
            processedBody = processedBody.replace(placeholder, value);
            processedSubject = processedSubject.replace(placeholder, value);
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(config.getFromEmail());
        helper.setTo(to);
        helper.setSubject(processedSubject);
        helper.setText(processedBody, true);

        mailSender.send(message);
        log.info("Email sent successfully to {}", to);
    }

    private Map<String, String> resolveUserVariables(UserEntity user) {
        Map<String, String> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("lastName", user.getLastName());
        variables.put("fullName", user.getFirstName() + " " + user.getLastName());
        variables.put("username", user.getUsername());
        variables.put("email", user.getEmail());
        return variables;
    }

    private JavaMailSender createMailSender(SmtpConfigEntity config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(config.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isStarttls()));
        props.put("mail.debug", "false");

        return mailSender;
    }
}
