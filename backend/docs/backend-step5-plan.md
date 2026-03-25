# STEP 5 계획 — JWT 인증/인가·소유권 정책

## 1. 단계 목표

- **JWT 기반 인증(Access/Refresh)** 과 **Spring Security 6** 필터 체인 초안을 문서로 고정한다.
- **공개 API**와 **보호 API**를 경로·역할 기준으로 구분하고, **인증 실패(401)** 와 **인가 실패(403)** 응답을 `backend-api-spec.md` **공통 envelope** 와 맞춘다.
- **owner check**(리소스 소유자 검증)를 **서비스 계층**에서 일관되게 적용하는 규칙을 정의한다.
- **Swagger** `bearerAuth` 스키마 및 보호 API 문서화 규칙을 정리한다.

## 2. 이번 단계에서 해결할 문제

- 토큰 검증·예외 처리가 컨트롤러에 흩어지는 문제.
- 401과 403을 동일하게 처리하게 되어 **토큰 갱신**과 **권한 오류** UX가 섞이는 문제.
- `userId`를 클라이언트 입력으로 신뢰하는 실수(반드시 **Principal/subject** 기준).
- Refresh 토큰 **재사용·탈취** 시나리오에 대한 **로테이션** 여부 미결정.

## 3. 설계 대상

| 대상 | 설명 |
|------|------|
| JWT 클레임 | `sub`, `typ`, `iat`, `exp`, 선택 `jti`/`role` |
| 토큰 수명 | Access 짧음, Refresh 긺(환경 변수) |
| 저장소 | Refresh는 DB `refresh_tokens`(해시), Access는 무상태 |
| 필터 | `JwtAuthenticationFilter`, 예외 시 `AuthenticationEntryPoint`/`AccessDeniedHandler` |
| SecurityConfig | `permitAll` 목록, `anyRequest().authenticated()` |
| 소유권 | 리소스별 검증 위치·404 vs 403 정책 |
| Swagger | `components.securitySchemes.bearerAuth`, `@SecurityRequirement` |

## 4. 주요 결정 사항

- **알고리즘**: 초기 **HS256**(단일 `JWT_SECRET`); 추후 **RS256** 키 회전은 별도 이슈.
- **Access Token**: 만료 짧게(기본 **15분** 권장, 설정으로 변경).
- **Refresh Token**: 불투명 문자열 또는 JWT; DB에는 **해시만** 저장. **로테이션**(갱신 시 기존 폐기 + 신규 발급) **권장**.
- **인증 주체**: `sub` = `users.id` 문자열; `Authentication.getName()` 또는 커스텀 `UserPrincipal`로 통일.
- **인가**: 역할 `ROLE_MEMBER`, `ROLE_ADMIN`; **소유권**은 `@PreAuthorize` 최소화하고 **서비스에서 명시적 owner check**(디버깅·일관성).
- **타인 리소스**: 운영 정책으로 **404**(`*_NOT_FOUND`) 또는 **403**(`*_ACCESS_DENIED`) 선택 — 본 프로젝트는 **404로 통일**해 존재 여부 정보 누설 완화(관리자 API 제외).
- 실패 응답: `success: false`, `error.code` 는 `AUTH_*` / `ACCESS_*` 구분.

## 5. 생성/수정 예정 파일

| 파일 | 용도 |
|------|------|
| `docs/backend-step5-plan.md` | 본 문서 |
| `docs/backend-auth-spec.md` | 인증/인가·토큰·필터·Security·공개/보호·owner·Swagger |
| `docs/backend-step5-result.md` | STEP 5 산출 요약 |

## 6. 구현 범위

- 위 문서 3종 작성. **Java 코드 미작성**.

## 7. 제외 범위

- YouTube OAuth 연동·`UserYoutubeCredential` 상세.
- 멀티 테넌시·기기별 세션 UI.
- 구현 단위 테스트·통합 테스트 코드.

## 8. 다음 단계 연결 포인트

- **STEP 6** `backend-swagger-spec.md`: 전 태그에 bearerAuth 및 401/403 예시 통일.
- **STEP 7** 로깅: 인증 실패 시 토큰 원문 로그 금지.
- **구현**: `global.security`, `global.auth` 패키지에 `JwtAuthenticationFilter`, `JwtTokenProvider`, `SecurityConfig` 구현.
