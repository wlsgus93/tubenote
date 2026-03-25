package com.myapp.learningtube.domain.auth.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.auth")
public class GoogleAuthProperties {

    /**
     * Google OAuth Client ID (audience 검증에 사용).
     */
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

