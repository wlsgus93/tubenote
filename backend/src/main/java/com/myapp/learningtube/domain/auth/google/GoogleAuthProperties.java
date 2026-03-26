package com.myapp.learningtube.domain.auth.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.auth")
public class GoogleAuthProperties {

    /**
     * Google OAuth Client ID (audience 검증·액세스 토큰 갱신에 사용).
     */
    private String clientId;

    /** OAuth 클라이언트 시크릿 — refresh_token 으로 access_token 갱신 시 필요. */
    private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}

