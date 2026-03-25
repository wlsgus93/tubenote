# STEP 22 계획 — Google OAuth2 Login(redirect) + YouTube readonly scope 준비

## 1. 단계 목표

- Spring Security `oauth2Login` 기반으로 Google 로그인(리다이렉트 플로우)을 제공한다.
- 로그인 성공 시 프론트엔드 대시보드로 redirect 한다.
- 사용자 정보를 `Member` 엔티티(email PK)에 Upsert 한다.
- 향후 YouTube Data API 호출을 위해 Google OAuth scope에 `youtube.readonly`를 포함한다.

## 2. 이번 단계에서 해결할 문제

- 기존 JWT 기반 API 보안 설정과 OAuth2 Login이 충돌하지 않도록 세션 정책을 조정한다.
- OAuth2 로그인 성공 시 서버가 “내부 JWT를 응답”하는 방식이 아니라, “프론트 redirect” 방식임을 명확히 한다.
- 토큰 원문을 로그/DB에 저장하지 않고, 필요한 사용자 프로필만 Upsert 한다.

## 3. 설계 대상

- 엔티티: `Member(email PK, name, picture, role)`
- OAuth2UserService: `CustomOAuth2UserService extends DefaultOAuth2UserService`
- SecurityConfig: `oauth2Login` + successHandler redirect + permitAll 경로 + H2 console frame 옵션
- 설정: `spring.security.oauth2.client.registration.google.scope`에 YouTube readonly 포함

## 4. 주요 결정 사항

- 로그인 성공 redirect: 기본값 `http://localhost:5173/dashboard` (환경변수로 덮어쓰기)
- scope: `profile`, `email`, `https://www.googleapis.com/auth/youtube.readonly`
- Member Upsert: email 기준으로 존재하면 profile 업데이트, 없으면 신규 생성

## 5. 생성/수정 예정 파일

- 생성: `Member.java`, `MemberRepository.java`, `CustomOAuth2UserService.java`
- 수정: `SecurityConfig.java`, `application.yml`, `build.gradle`
- 문서: `docs/backend-step22-plan.md`, `docs/backend-step22-result.md`

## 6. 구현 범위

- OAuth2 로그인 플로우 + Member Upsert + redirect까지 동작

## 7. 제외 범위

- YouTube API 실제 호출 구현(자막/영상 호출), access token 저장/갱신
- 내부 JWT 발급과의 통합(redirect 방식 유지)

## 8. 다음 단계 연결 포인트

- 로그인 후 YouTube API 호출 시 `OAuth2AuthorizedClientService`를 통해 액세스 토큰을 가져오는 구조로 확장 가능

