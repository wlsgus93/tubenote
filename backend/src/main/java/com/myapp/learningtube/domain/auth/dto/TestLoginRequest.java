package com.myapp.learningtube.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "임시 테스트 로그인 요청 — 아이디는 이메일 형식(시드 사용자와 동일)")
public class TestLoginRequest {

    @NotBlank
    @Email
    @Schema(
            description = "고정 테스트 계정 이메일 (DataInitializer 시드와 동일)",
            example = "test@learningtube.local",
            format = "email")
    private String username;

    @NotBlank
    @Schema(description = "고정값: test", example = "test")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

