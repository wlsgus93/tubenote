package com.myapp.learningtube.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI learningTubeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LearningTube API")
                        .description(
                                "유튜브 학습 영상 관리 플랫폼 백엔드 API.\n\n"
                                        + "**Swagger JWT 테스트:** 1) `Auth` → `POST /auth/test-login` "
                                        + "(`username`: `test@learningtube.local`, `password`: `test`) 실행 후 응답의 `data.accessToken` 복사 "
                                        + "2) 상단 **Authorize** → Value에 `Bearer ` 없이 토큰만 붙여넣기 (또는 `Bearer <token>` 전체 입력 — UI에 따라 다름). "
                                        + "3) `GET /auth/me` 호출.\n\n"
                                        + "**CORS:** 로컬 프론트(Vite 5173, CRA 3000 등)는 `learningtube.cors.allowed-origins` 로 허용. `Authorization` 헤더·`credentials` 사용 가능.\n\n"
                                        + "**성공 envelope (`ApiSuccessResponse`):** `success`, `requestId`, `data`, 선택 `meta`(페이징), 선택 `message`.\n"
                                        + "**실패 envelope (`ApiErrorResponse`):** `success:false`, `requestId`, `error`: `{ code, message, details? }`.\n"
                                        + "**인증:** 보호 API에 토큰 없음/무효 → **401** `AUTH_UNAUTHORIZED`; 인증은 되었으나 권한 부족(Security) → **403** `ACCESS_FORBIDDEN`; 비즈니스상 금지(예: 타인 리소스)는 주로 **404** 로 응답해 코드와 맞출 수 있음.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(
                                                "Access JWT. `POST /api/v1/auth/test-login` 으로 발급. "
                                                        + "Authorize 입력 시 보통 **토큰 문자열만** 입력하면 됩니다.")));
    }
}
