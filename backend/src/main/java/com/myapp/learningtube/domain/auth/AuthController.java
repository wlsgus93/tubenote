package com.myapp.learningtube.domain.auth;

import com.myapp.learningtube.domain.auth.dto.MeResponse;
import com.myapp.learningtube.domain.auth.dto.TestLoginRequest;
import com.myapp.learningtube.domain.auth.dto.TokenResponse;
import com.myapp.learningtube.global.error.BusinessException;
import com.myapp.learningtube.global.error.ErrorCode;
import com.myapp.learningtube.global.filter.RequestIdFilter;
import com.myapp.learningtube.global.response.ApiErrorResponse;
import com.myapp.learningtube.global.response.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Auth",
        description =
                "인증 — 임시 테스트 로그인 (username=이메일 `test@learningtube.local`, password=`test`, JWT sub=userId=1)")
public class AuthController {

    /** DataInitializer 시드 사용자 이메일과 동일해야 함. */
    private static final String TEST_USERNAME = "test@learningtube.local";

    private static final String TEST_PASSWORD = "test";
    private static final long TEST_USER_ID = 1L;
    private static final String TEST_ROLE = "MEMBER";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthController(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/test-login")
    @Operation(
            summary = "테스트 로그인",
            description =
                    "고정 계정으로 Access JWT 발급. `username`은 **이메일** `test@learningtube.local`, `password`는 `test`. "
                            + "Swagger 예시값 그대로 실행 가능. 받은 accessToken을 Authorize에 입력.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "400",
                description = "검증 실패",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "자격 증명 불일치",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<TokenResponse> testLogin(
            @Valid @RequestBody TestLoginRequest request, HttpServletRequest httpRequest) {
        if (!TEST_USERNAME.equals(request.getUsername()) || !TEST_PASSWORD.equals(request.getPassword())) {
            throw new BusinessException(
                    ErrorCode.USER_INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtTokenProvider.createAccessToken(TEST_USER_ID, TEST_ROLE);
        long expiresInSec = Math.max(1L, jwtProperties.getAccessTokenValidityMs() / 1000);
        TokenResponse data = new TokenResponse(accessToken, "Bearer", expiresInSec);
        String requestId = resolveRequestId(httpRequest);
        return ApiSuccessResponse.ok(requestId, data);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "내 정보 (보호 API)", description = "Authorization: Bearer {accessToken} 필요")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(
                responseCode = "401",
                description = "미인증 또는 토큰 무효",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "권한 없음",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiSuccessResponse<MeResponse> me(
            @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletRequest httpRequest) {
        String requestId = resolveRequestId(httpRequest);
        MeResponse data = new MeResponse(principal.getId(), principal.getRole());
        return ApiSuccessResponse.ok(requestId, data);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        Object v = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return UUID.randomUUID().toString();
    }
}

