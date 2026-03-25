# STEP 21 결과 — Google ID Token 로그인(API) + 내부 JWT(access/refresh) 발급

## 1. 구현 완료 항목

- `POST /api/auth/google/login` 구현 (호환 alias: `POST /api/v1/auth/google/login`)
- Google ID token 서버 검증 구현
  - audience(`google.auth.client-id`) 검증
  - issuer 검증(`accounts.google.com` 계열)
  - 서명/만료 검증(라이브러리 검증 결과)
- `User` / `UserOAuthAccount` 연동
  - `provider=GOOGLE`, `providerSubject=sub` 기반 조회
  - 없으면 email 기반 기존 사용자 연동 또는 자동 가입
- 내부 토큰 발급
  - Access JWT: `JwtTokenProvider` 재사용
  - Refresh token: opaque 문자열 발급 + DB에 SHA-256 해시 저장(`refresh_tokens`)
- Swagger 문서화 반영(Controller operation/response)
- 공통 예외 처리(`BusinessException` + `ErrorCode`)와 연결
- 로깅 반영(신규가입/연동생성/기존로그인 이벤트, 토큰 원문 로깅 금지)

## 2. 생성/수정한 파일 목록

### 생성

- `docs/backend-step21-plan.md`
- `docs/backend-step21-result.md`
- `src/main/java/com/myapp/learningtube/domain/auth/dto/GoogleLoginRequest.java`
- `src/main/java/com/myapp/learningtube/domain/auth/dto/GoogleLoginResponse.java`
- `src/main/java/com/myapp/learningtube/domain/auth/dto/AuthUserResponse.java`
- `src/main/java/com/myapp/learningtube/domain/auth/google/GoogleAuthController.java`
- `src/main/java/com/myapp/learningtube/domain/auth/google/GoogleLoginService.java`
- `src/main/java/com/myapp/learningtube/domain/auth/google/GoogleIdTokenVerifierService.java`
- `src/main/java/com/myapp/learningtube/domain/auth/google/GoogleAuthProperties.java`
- `src/main/java/com/myapp/learningtube/domain/auth/refresh/RefreshToken.java`
- `src/main/java/com/myapp/learningtube/domain/auth/refresh/RefreshTokenRepository.java`
- `src/main/java/com/myapp/learningtube/domain/auth/refresh/RefreshTokenService.java`

### 수정

- `build.gradle` (Google API Client 의존성 추가)
- `src/main/java/com/myapp/learningtube/LearningTubeApplication.java` (`GoogleAuthProperties` 등록)
- `src/main/java/com/myapp/learningtube/global/error/ErrorCode.java` (Google 로그인 에러코드 추가)
- `src/main/java/com/myapp/learningtube/domain/user/User.java` (`profileImageUrl` 추가)
- `src/main/java/com/myapp/learningtube/global/security/JwtAuthenticationFilter.java` (Auth 패키지 이동 반영)
- 여러 `domain/*/*Controller.java` (principal import 경로 변경)
- 문서: `docs/backend-api-spec.md`, `docs/backend-auth-spec.md`

### 삭제

- `src/main/java/com/myapp/learningtube/global/auth/**` (Auth를 `domain.auth`로 이동하면서 중복 제거)

## 3. 핵심 클래스/구조 설명

- `domain.auth.google.GoogleAuthController`
  - Google 로그인 엔드포인트를 제공한다.
  - 요청 본문은 `{ idToken }`만 받는다.
  - 성공 시 내부 `accessToken`, `refreshToken`, `user`를 반환한다.

- `domain.auth.google.GoogleIdTokenVerifierService`
  - `GoogleIdTokenVerifier`로 ID token의 유효성을 검증한다.
  - 설정 `google.auth.client-id` 기반으로 audience를 제한한다.

- `domain.auth.google.GoogleLoginService`
  - 검증된 payload로 `UserOAuthAccount(provider=GOOGLE, providerSubject=sub)`를 우선 조회한다.
  - 없으면 email로 `User`를 찾고, 없으면 자동 가입한다.
  - Access JWT + Refresh를 발급한다.

- `domain.auth.refresh.RefreshTokenService`
  - Refresh token을 **opaque 문자열**로 발급하고, 서버는 **해시만 저장**한다.
  - 만료는 기본 7일(TTL 정책은 후속 단계에서 설정화 가능).

## 4. 반영된 설계 원칙

- Google 로그인은 “외부 토큰 검증 → 내부 JWT 발급”의 **진입점**으로만 사용
- 서비스 내부 인증은 Access JWT로 통일(추가 API는 `@SecurityRequirement` 기준)
- `User`와 `UserOAuthAccount`의 책임 분리(연동은 OAuthAccount가 담당)
- DTO 분리(Entity 직접 노출 금지)
- 민감 토큰 원문 로그 금지

## 5. Swagger 반영 내용

- `Auth` 태그에 Google 로그인 Operation 추가
- 200/400/401 응답 스키마를 `ApiSuccessResponse` / `ApiErrorResponse`로 문서화

## 6. 로깅 반영 내용

- 신규가입/연동생성/기존로그인 이벤트를 INFO로 기록
- `idToken/accessToken/refreshToken` 원문은 로그에 남기지 않음

## 7. 아쉬운 점 / 개선 포인트

- Refresh token은 UUID 기반 시드+base64url(MVP)이며, 후속에서 `SecureRandom 32B`로 강화 권장
- `UserOAuthAccount.providerEmail` 스냅샷 갱신을 엔티티 메서드로 정교화할 수 있음
- Refresh rotation(`/auth/refresh`) 및 로그아웃(`/auth/logout`)은 다음 단계 구현 필요

## 8. 다음 단계 TODO

- `POST /api/v1/auth/refresh` 구현(로테이션 + 재사용 감지 정책)
- `POST /api/v1/auth/logout` 구현(refresh 폐기)
- Refresh 저장 위치(body vs cookie) 보안정책 확정(HttpOnly Secure Cookie 권장)
- Google OAuth(YouTube scope) 동의/토큰 교환은 별도 플로우로 확장

