package com.proton.oauth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "smtp_configs")
public class SmtpConfigEntity {

    @Id
    private Long id = 1L; // Only one config

    private String host;
    private int port;
    private String username;
    private String password;
    private String fromEmail;
    private boolean auth = true;
    private boolean starttls = true;

    public SmtpConfigEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }

    public boolean isAuth() { return auth; }
    public void setAuth(boolean auth) { this.auth = auth; }

    public boolean isStarttls() { return starttls; }
    public void setStarttls(boolean starttls) { this.starttls = starttls; }
}

