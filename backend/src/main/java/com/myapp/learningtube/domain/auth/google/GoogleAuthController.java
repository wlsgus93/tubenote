package com.myapp.learningtube.domain.auth.google;

import com.myapp.learningtube.domain.auth.dto.GoogleLoginRequest;
import com.myapp.learningtube.domain.auth.dto.GoogleLoginResponse;
import com.myapp.learningtube.global.logging.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import com.myapp.learningtube.global.response.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth/google", "/api/v1/auth/google"})
@Tag(name = "Auth", description = "인증 — Google ID token 로그인")
public class GoogleAuthController {

    private final GoogleLoginService googleLoginService;

    public GoogleAuthController(GoogleLoginService googleLoginService) {
        this.googleLoginService = googleLoginService;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Google 로그인",
            description =
                    "프론트가 전달한 Google ID token(credential)을 서버에서 검증한 뒤, 내부 Access JWT + Refresh token을 발급합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "요청 본문/필수값 오류",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Google ID token 검증 실패 또는 이메일 미검증",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<GoogleLoginResponse> login(
            @Valid @RequestBody GoogleLoginRequest body, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        GoogleLoginResponse data = googleLoginService.login(body.getIdToken(), userAgent, ipAddress);
        return ApiSuccessResponse.ok(resolveRequestId(request), data);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return UUID.randomUUID().toString();
    }
}

