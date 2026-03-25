# STEP 21 계획 — Google ID Token 로그인(API) + 내부 JWT(access/refresh) 발급

## 1. 단계 목표

- 프론트가 전달하는 **Google ID token(credential)** 을 백엔드에서 검증한다.
- 검증된 Google 사용자 정보를 기존 `User` / `UserOAuthAccount` 구조와 연결한다.
- 서비스 인증은 **내부 JWT(access) + refresh token(서버 DB 해시 저장)** 으로 통일한다.
- Swagger / 공통 예외 / 로깅 규칙과 일치하는 **실서비스 로그인 진입점**을 고정한다.

## 2. 이번 단계에서 해결할 문제

- Google ID token을 “그냥 디코드”하지 않고 **서명·issuer·audience·만료**를 검증해야 함
- Google `sub`(providerSubject) 기반 계정 연결이 없으면 **자동 가입** 또는 **기존 이메일 계정 연동**이 필요
- 현재 코드에는 Access JWT 발급은 있으나 **refresh token 발급/저장소(refresh_tokens)** 가 구현되어 있지 않음
- 로그인 이벤트(신규 가입/기존 로그인/연동 갱신)를 남기되 **민감 토큰 원문 로깅 금지**

## 3. 설계 대상

- API
  - `POST /api/auth/google/login` (추가로 호환 필요 시 `/api/v1/auth/google/login`도 동일 동작)
- Google 검증 컴포넌트
  - `GoogleIdTokenVerifier` 기반 검증 (audience/client id, issuer, exp/iat)
- 도메인 연동
  - `User`, `UserOAuthAccount(provider=GOOGLE, providerSubject=sub)`
- 토큰 발급
  - Access JWT: 기존 `JwtTokenProvider#createAccessToken(userId, role)` 재사용
  - Refresh: **opaque random token** 발급 → `refresh_tokens.token_hash`에 SHA-256 해시 저장

## 4. 주요 결정 사항

### 4.1 API 경로·버전

- **정식**: `POST /api/auth/google/login`
- 기존 `/api/v1/auth/*`와 혼재가 생길 수 있으므로,
  - `/api/v1/auth/google/login`을 **동일 핸들러로 alias** 제공할지 여부는 구현 시 함께 결정

### 4.2 Google ID token 검증 정책

- **audience**: 설정값 `google.auth.client-id` 와 일치해야 한다.
- **issuer**: `https://accounts.google.com` 또는 `accounts.google.com` 만 허용한다.
- **만료/서명**: 라이브러리 검증 결과가 유효해야 한다.
- **email**:
  - payload에 `email`이 없으면 실패
  - payload에 `email_verified=false`면 기본은 실패(운영 안전)

### 4.3 User / UserOAuthAccount 연동 정책

- 1순위: `UserOAuthAccount(provider=GOOGLE, providerSubject=sub)` 존재하면 해당 `user`로 로그인
- 2순위: 없으면 `users.email`(deletedAt null)로 사용자 조회
  - 존재하면 해당 `user`에 Google OAuthAccount를 생성하여 **연동**
  - 없으면 `User`를 생성하고 Google OAuthAccount를 생성하여 **자동 가입**
- `providerEmail`은 로그인 시점 payload의 email로 **스냅샷 갱신** 가능(검증 기준으로 사용하지 않음)
- 닉네임/프로필 반영(초기 정책)
  - **신규 가입**: nickname은 `name`(또는 `given_name`) 우선, 없으면 email local-part 사용
  - **기존 사용자**: nickname은 변경하지 않음(사용자 커스터마이징 보호)
  - **프로필 이미지**: `users.profile_image_url`이 비어 있으면 Google `picture`로 채움(이미 있으면 유지)

### 4.4 내부 JWT/Refresh 발급 정책

- Access JWT: 기존과 동일(typ=access, role 포함)
- Refresh:
  - 랜덤 문자열(예: 32 bytes) 생성 후 base64url로 인코딩하여 클라이언트에 전달
  - 서버는 원문을 저장하지 않고 `SHA-256(refreshToken)`의 hex를 `refresh_tokens.token_hash`에 저장
  - TTL 기본: 7일(추후 설정화 가능)
  - `user_agent`, `ip_address`는 요청에서 추출(선택 필드)

### 4.5 예외/에러코드

- `GOOGLE_ID_TOKEN_INVALID` (401): 검증 실패/issuer-aud mismatch/서명 실패/만료
- `GOOGLE_EMAIL_NOT_VERIFIED` (401): email_verified=false
- `GOOGLE_EMAIL_MISSING` (400): email 없음(정상 플로우에서는 거의 없음)

> 공통 응답 envelope는 기존 `ApiSuccessResponse` / `ApiErrorResponse`를 따른다.

### 4.6 로깅 정책

- 남길 로그(요약)
  - 신규 가입: `userId`, providerSubject 일부 마스킹, email 마스킹, 이벤트 타입
  - 기존 로그인: `userId`, providerSubject 일부 마스킹
  - 연동 생성: `userId`, providerSubject 일부 마스킹
- 금지
  - `idToken`, `accessToken`, `refreshToken` 원문 로깅 금지

## 5. 생성/수정 예정 파일

- Auth API/DTO/서비스
  - `src/main/java/com/myapp/learningtube/domain/auth/**` (Google login controller/service/dto)
- Google 검증 컴포넌트
  - `domain/auth/google/**` 또는 `domain/auth/**` 하위 최소 패키지
- Refresh token 저장소(신규)
  - `domain/auth/RefreshToken` 엔티티 + `RefreshTokenRepository`
- 기존 엔티티 보강(필요 시)
  - `domain/user/User`에 `profileImageUrl` 컬럼 추가(요구 응답 필드)
- 설정/의존성
  - `build.gradle`에 Google API Client 의존성 추가
  - `LearningTubeApplication`에 `GoogleAuthProperties` 등 `@EnableConfigurationProperties` 추가
- 문서
  - `docs/backend-api-spec.md`, `docs/backend-auth-spec.md`
  - `docs/backend-step21-plan.md`(본 문서), `docs/backend-step21-result.md`

## 6. 구현 범위

- Google ID token 검증 + 로그인 API + JWT/Refresh 발급까지 end-to-end 동작
- Swagger 문서화 및 공통 예외/응답 연결

## 7. 제외 범위

- YouTube scope 동의, Google OAuth access token 교환/갱신(refresh) 플로우
- 멀티 프로바이더(카카오 등) 추가 구현
- 계정 병합/이메일 변경 등 고급 계정 정책

## 8. 다음 단계 연결 포인트

- 프론트 연동 포인트
  - 프론트는 Google GIS에서 받은 `credential`(ID token)을 `idToken`으로 전달
  - 응답의 `accessToken`은 `Authorization: Bearer ...`로 사용
  - `refreshToken` 저장 방식(body vs cookie)은 다음 단계(보안정책)에서 확정 가능
- STEP 22(후속)
  - Refresh rotation, `/auth/refresh`, `/auth/logout` 구현
  - OAuth(YouTube) 권한 동의/토큰 교환은 별도 플로우로 확장

