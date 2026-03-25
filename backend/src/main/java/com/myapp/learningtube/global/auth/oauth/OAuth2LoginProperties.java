package com.myapp.learningtube.global.auth.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "learningtube.auth")
public class OAuth2LoginProperties {

    /**
     * OAuth 로그인 성공/실패 후 돌아갈 프론트엔드 베이스 URL.
     * 예: http://localhost:5173 또는 https://my-frontend.ngrok-free.app
     */
    private String frontendBaseUrl = "http://localhost:5173";

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }
}

