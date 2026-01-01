package com.proton.oauth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "email_templates")
public class EmailTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g., "PASSWORD_RESET"

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    private String description; // Explains what variables are needed

    @Transient
    private String testData; // Temporary field for UI compatibility if needed, but not persisted

    public EmailTemplateEntity() {}

    public EmailTemplateEntity(String name, String subject, String body, String description) {
        this.name = name;
        this.subject = subject;
        this.body = body;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getTestData() { return testData; }
    public void setTestData(String testData) { this.testData = testData; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
