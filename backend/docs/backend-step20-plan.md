# STEP 20 계획 — Auth 패키지 단순화 및 `domain` 이동 (OAuth 확장 대비)

## 1. 단계 목표

- `Auth` 관련 코드를 `global`에서 분리하여 **`domain.auth`로 이동**한다.
- 패키지를 과도하게 쪼개지 않고, 예시처럼 **한 군데에 묶인 Auth 모듈** 형태로 정리한다.
- 이후 Google/Kakao 등 **OAuth2 로그인 + JWT 발급**을 추가할 수 있도록 패키지 경계를 단순·명확하게 만든다.

## 2. 이번 단계에서 해결할 문제

- 현재 `global.auth.*`가 `AuthController`, `CustomUserPrincipal`, `jwt` 등을 포함하고 있어 “도메인(Auth)”과 “전역(Security)” 책임이 섞여 보임
- 여러 도메인 컨트롤러들이 `global.auth.CustomUserPrincipal`에 의존하고 있어, Auth를 domain으로 옮길 때 일괄 수정이 필요
- `@EnableConfigurationProperties(JwtProperties.class)` 등이 `global.auth.jwt`를 직접 참조하여 패키지 이동 시 부팅 실패 가능

## 3. 설계 대상

- 패키지 이동
  - `com.myapp.learningtube.global.auth.*` → `com.myapp.learningtube.domain.auth.*`
  - 하위 DTO, JWT 구성요소도 `domain.auth` 하위로 이동 (과도한 분리 지양)
- 의존성/참조 수정
  - `global.security.JwtAuthenticationFilter`의 principal/provider import
  - 각 도메인 Controller의 `CustomUserPrincipal` import
  - `LearningTubeApplication`의 `@EnableConfigurationProperties` 대상 클래스 패키지
- 문서 반영
  - `docs/backend-architecture.md`(패키지 구조)
  - `docs/backend-auth-spec.md`(클래스 위치/책임)

## 4. 주요 결정 사항

- **Auth는 domain으로 이동**: 컨트롤러/서비스/DTO/JWT/Principal을 하나의 `domain.auth` 범주로 묶는다.
- **Security는 global에 유지**: 필터 체인/예외 핸들러/보안 정책은 `global.security`에서 유지한다.
- **패키지 단순화**: 현재 수준에서 불필요한 세분화를 늘리지 않으며, OAuth 도입 시에도 `domain.auth.oauth` 정도의 최소 단위만 추가한다.

## 5. 생성/수정 예정 파일

- 이동(신규 경로로 생성 후 기존 파일 제거)
  - `src/main/java/com/myapp/learningtube/global/auth/**` → `src/main/java/com/myapp/learningtube/domain/auth/**`
- 수정
  - `src/main/java/com/myapp/learningtube/global/security/JwtAuthenticationFilter.java`
  - `src/main/java/com/myapp/learningtube/domain/**/**Controller.java` (principal import)
  - `src/main/java/com/myapp/learningtube/LearningTubeApplication.java`
  - 문서: `docs/backend-architecture.md`, `docs/backend-auth-spec.md`
- 문서
  - `docs/backend-step20-plan.md`(본 문서), `docs/backend-step20-result.md`

## 6. 구현 범위

- Java package 선언 변경 및 파일 경로 이동
- import 일괄 수정
- 스프링 부트 부팅(설정 바인딩) 깨짐 요소 정리

## 7. 제외 범위

- OAuth2 로그인 기능 자체 구현(구글/카카오), 사용자 연동, refresh token 정책 추가
- 권한(Role) 모델링/인가 정책 변경

## 8. 다음 단계 연결 포인트

- STEP 21: `domain.auth.oauth` 추가 및 OAuth2 로그인 플로우(Authorization Code) + 성공 시 JWT 발급 핸들링 설계/구현

