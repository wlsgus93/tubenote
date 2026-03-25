package com.myapp.learningtube.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Google 로그인 요청 (프론트가 전달한 Google ID token)")
public class GoogleLoginRequest {

    @NotBlank
    @Schema(
            description = "Google ID token (GIS credential)",
            example = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ii4uLiIsInR5cCI6IkpXVCJ9...")
    private String idToken;

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}

