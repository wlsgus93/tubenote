# STEP 22 결과 — Google OAuth2 Login(redirect) + YouTube readonly scope 준비

## 1. 구현 완료 항목

- Google OAuth2 Login(`oauth2Login`) 활성화
- 로그인 성공 시 프론트 대시보드로 redirect (`app.frontend.login-success-redirect`)
- `CustomOAuth2UserService`로 Google 사용자 정보 Upsert (`Member`)
- Google OAuth scope에 `https://www.googleapis.com/auth/youtube.readonly` 포함 설정 예시 추가

## 2. 생성/수정한 파일 목록

### 생성

- `src/main/java/com/myapp/learningtube/domain/member/Member.java`
- `src/main/java/com/myapp/learningtube/domain/member/MemberRepository.java`
- `src/main/java/com/myapp/learningtube/domain/member/Role.java`
- `src/main/java/com/myapp/learningtube/domain/member/MemberRole.java`
- `src/main/java/com/myapp/learningtube/global/oauth/CustomOAuth2UserService.java`
- `docs/backend-step22-plan.md`
- `docs/backend-step22-result.md`

### 수정

- `build.gradle` (`spring-boot-starter-oauth2-client` 추가)
- `src/main/java/com/myapp/learningtube/global/security/SecurityConfig.java`
- `src/main/resources/application.yml` (Google registration + scope + redirect 설정)

## 3. 핵심 클래스/구조 설명

- `Member`
  - 요구사항대로 email을 PK로 사용하는 회원 엔티티
  - Google에서 받은 `name`, `picture`를 저장/갱신
  - `role`은 `MemberRole`로 관리하며 `Role` 인터페이스를 통해 권한 문자열을 제공

- `CustomOAuth2UserService`
  - `DefaultOAuth2UserService`를 상속
  - Google 로그인 성공 시 attributes에서 `email/name/picture`를 추출하고 DB에 Upsert
  - 토큰 원문은 저장/로그하지 않음

- `SecurityConfig`
  - `/oauth2/**`, `/login/**` permitAll
  - `oauth2Login.userInfoEndpoint().userService(customOAuth2UserService)` 연결
  - `successHandler`에서 프론트 대시보드로 redirect
  - H2 console 사용 시 frameOptions disable 처리

## 4. 설정(application.yml) 요약

- `spring.security.oauth2.client.registration.google.client-id/client-secret`
- `scope`에 `https://www.googleapis.com/auth/youtube.readonly` 포함
- redirect URL: `app.frontend.login-success-redirect`

## 5. 다음 단계 TODO

- YouTube Data API 호출 구현 시, 로그인으로 발급된 Google access token을 가져오는 방식 확정
  - 기본: `OAuth2AuthorizedClientService`로 authorized client 조회 후 access token 사용
  - 장기: `UserOAuthAccount`에 access token/refresh token 저장 + 갱신 정책 분리(보안/암호화 포함)

