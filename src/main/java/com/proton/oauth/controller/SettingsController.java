package com.proton.oauth.controller;

import com.proton.oauth.entity.EmailTemplateEntity;
import com.proton.oauth.entity.SmtpConfigEntity;
import com.proton.oauth.service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public String index() {
        return "redirect:/admin/settings/templates";
    }

    @GetMapping("/smtp")
    public String smtpSettings(Model model) {
        model.addAttribute("smtp", settingsService.getSmtpConfig());
        return "admin/settings/smtp";
    }

    @PostMapping("/smtp")
    public String updateSmtp(@ModelAttribute SmtpConfigEntity config) {
        settingsService.updateSmtpConfig(config);
        return "redirect:/admin/settings/smtp?success=true";
    }

    @GetMapping("/templates")
    public String templateSettings(Model model) {
        model.addAttribute("templates", settingsService.findAllTemplates());
        return "admin/settings/templates";
    }

    @GetMapping("/templates/new")
    public String newTemplateForm(Model model) {
        model.addAttribute("template", new EmailTemplateEntity());
        return "admin/settings/create-template";
    }

    @PostMapping("/templates/new")
    public String createTemplate(@ModelAttribute EmailTemplateEntity template, 
                                 @RequestParam(required = false) String action,
                                 @RequestParam(required = false) String testRecipient,
                                 @RequestParam(required = false) String testUserIdentifier) {
        
        EmailTemplateEntity saved = settingsService.saveTemplate(template);
        
        if ("test".equals(action)) {
            try {
                settingsService.sendTestEmail(saved.getId(), testRecipient, testUserIdentifier);
                return "redirect:/admin/settings/templates/edit/" + saved.getId() + "?success=test_sent";
            } catch (Exception e) {
                return "redirect:/admin/settings/templates/edit/" + saved.getId() + "?error=test_failed";
            }
        }
        
        return "redirect:/admin/settings/templates?success=created";
    }

    @GetMapping("/templates/edit/{id}")
    public String editTemplate(@PathVariable Long id, Model model) {
        EmailTemplateEntity template = settingsService.findTemplateById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid template Id:" + id));
        model.addAttribute("template", template);
        return "admin/settings/edit-template";
    }

    @PostMapping("/templates/edit/{id}")
    public String updateTemplate(@PathVariable Long id, 
                                 @ModelAttribute EmailTemplateEntity template,
                                 @RequestParam(required = false) String action,
                                 @RequestParam(required = false) String testRecipient,
                                 @RequestParam(required = false) String testUserIdentifier) {
        template.setId(id);
        settingsService.saveTemplate(template);
        
        if ("test".equals(action)) {
            try {
                settingsService.sendTestEmail(id, testRecipient, testUserIdentifier);
                return "redirect:/admin/settings/templates/edit/" + id + "?success=test_sent";
            } catch (Exception e) {
                return "redirect:/admin/settings/templates/edit/" + id + "?error=test_failed";
            }
        }
        
        return "redirect:/admin/settings/templates?success=updated";
    }

    @PostMapping("/templates/delete/{id}")
    public String deleteTemplate(@PathVariable Long id) {
        settingsService.deleteTemplate(id);
        return "redirect:/admin/settings/templates?success=deleted";
    }
}
