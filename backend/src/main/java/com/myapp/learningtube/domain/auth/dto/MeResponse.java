package com.myapp.learningtube.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 인증된 테스트 사용자 정보")
public class MeResponse {

    @Schema(description = "임시 사용자 ID", example = "1")
    private long userId;

    @Schema(description = "역할", example = "MEMBER")
    private String role;

    public MeResponse() {}

    public MeResponse(long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

