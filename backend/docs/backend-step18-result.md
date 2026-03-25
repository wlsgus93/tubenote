# STEP 18 결과 — 프론트 연동용 계약 안정화

## 구현·수정 요약

| 구분 | 내용 |
|------|------|
| CORS | `CorsProperties` + `CorsConfig` + `SecurityConfig.cors` + `OPTIONS /**` permit |
| 설정 | `application.yml` `learningtube.cors.*` |
| 성공 응답 | `ApiSuccessResponse.message` + `ok(..., meta, message)` 오버로드 |
| Videos DTO | `UserVideoSummaryResponse` `@JsonInclude(NON_NULL)`, `getProgressSeconds()` |
| Video 상세 DTO | `UserVideoDetailResponse` `@JsonInclude(NON_NULL)` |
| Dashboard | `DashboardVideoCardDto.durationSeconds` + `DashboardMapper` |
| Health | `requestId` null 시 UUID 폴백 |
| 문서 | `docs/frontend-backend-contract.md`, `backend-api-spec.md` §1.2·§2.1, OpenAPI 설명 |
| 부트스트랩 | `LearningTubeApplication` 에 `CorsProperties` 등록 |

## 인증·에러 (확인만, 코드 변경 최소)

- 401: `JwtAuthenticationEntryPoint` → `AUTH_UNAUTHORIZED`
- 403: `JwtAccessDeniedHandler` → `ACCESS_FORBIDDEN`
- 테스트 로그인: `POST /api/v1/auth/test-login` (`test@learningtube.local` / `test`)

## 다음 연동 우선순위 (제안)

1. 토큰 발급·저장·Bearer 인터셉터  
2. `GET /api/v1/dashboard`  
3. `GET /api/v1/videos` + `GET /api/v1/videos/{userVideoId}`  
4. 401 공통 처리(재로그인)
