# STEP 5 결과 — JWT 인증/인가·소유권 명세 고정

## 1. 구현 완료 항목

- **Access/Refresh** 정책(TTL 권장값, HS256, 클레임 `sub`/`typ`/역할, Refresh **DB 해시**·**로테이션** 권장)을 `backend-auth-spec.md`에 정의함.
- **필터 체인** 역할(`JwtTokenProvider`, `JwtAuthenticationFilter`, `AuthenticationEntryPoint`, `AccessDeniedHandler`)과 **Security Config** 초안(`STATELESS`, `permitAll` 목록, 그 외 `authenticated`)을 문서화함.
- **공개/보호 API** 경로 표로 구분함.
- **401 vs 403** 및 `error.code`(`AUTH_*`, `ACCESS_*`, 로그인 `USER_INVALID_CREDENTIALS`)를 `backend-api-spec.md` envelope와 맞춤.
- **owner check** 를 서비스 계층으로 고정하고, 리소스별 기준 표·**타인 리소스 404 권장** 정책을 명시함.
- **Swagger** `bearerAuth`, `@SecurityRequirement`, 401/403 `ApiErrorResponse` 문서화 규칙을 정리함.
- **Java 코드 미작성**(문서만).

## 2. 생성/수정한 파일 목록

| 파일 | 비고 |
|------|------|
| `docs/backend-step5-plan.md` | 신규 |
| `docs/backend-auth-spec.md` | 신규 |
| `docs/backend-step5-result.md` | 신규 |
| `docs/backend-api-spec.md` | §4·§5.3에 `AUTH_REFRESH_REUSED` 추가, 개정 0.2 |

## 3. 핵심 클래스/구조 설명

- 구현 시 권장: `global.security.JwtAuthenticationFilter`, `global.auth.JwtTokenProvider`, `JwtAuthenticationEntryPoint`, `JwtAccessDeniedHandler`, `SecurityConfig`.
- 단일 기준: **`docs/backend-auth-spec.md`**.

## 4. 반영된 설계 원칙

- 토큰 파싱 **단일 진입점**; `userId`는 **JWT `sub`** 만 신뢰.
- 인증(401)·인가(403)·소유권(404) **분리**.
- Refresh **재사용 감지** 시 코드 정책은 구현 시 `AUTH_REFRESH_REUSED` 통일 여부 확정.

## 5. Swagger 반영 내용

- `bearerAuth` 스키마, 보호 API에 `@SecurityRequirement` 및 401/403 응답 스키마 의무 — §10.

## 6. 로깅 반영 내용

- 토큰 원문 로그 금지는 §11 체크리스트에 반영(상세는 STEP 7).

## 7. 아쉬운 점 / 개선 포인트

- RS256·키 회전·JWKS는 미포함.
- Refresh를 **HttpOnly Cookie**로만 받을 경우 Spring Security·CSRF 설정을 별도 명세로 보강 필요.

## 8. 다음 단계 TODO

| 순서 | 작업 |
|------|------|
| STEP 6 | `backend-swagger-spec.md` — 태그별 보안·401/403 예시 통일 |
| 구현 | Security 필터·Provider·EntryPoint·Handler 및 `AuthController` |

---

## 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 5 완료 기준 초안 |
