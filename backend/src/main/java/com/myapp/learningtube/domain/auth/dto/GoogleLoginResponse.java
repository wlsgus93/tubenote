package com.myapp.learningtube.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Google 로그인 성공 응답 — 내부 JWT(access/refresh) + 사용자 정보")
public class GoogleLoginResponse {

    @Schema(description = "내부 Access JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "내부 Refresh token (opaque)", example = "tQ8Yw5oQ6f7f... (저장용)")
    private String refreshToken;

    @Schema(description = "로그인한 사용자")
    private AuthUserResponse user;

    public GoogleLoginResponse() {}

    public GoogleLoginResponse(String accessToken, String refreshToken, AuthUserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public AuthUserResponse getUser() {
        return user;
    }

    public void setUser(AuthUserResponse user) {
        this.user = user;
    }
}

