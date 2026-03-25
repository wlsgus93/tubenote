package com.myapp.learningtube.global.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * HS256 서명용 비밀(바이트 길이는 충분히 길게 — 최소 32바이트 권장).
     */
    private String secret;

    /** Access JWT 만료(ms). */
    private long accessTokenValidityMs = 900_000L;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenValidityMs() {
        return accessTokenValidityMs;
    }

    public void setAccessTokenValidityMs(long accessTokenValidityMs) {
        this.accessTokenValidityMs = accessTokenValidityMs;
    }
}
