# 프론트엔드–백엔드 연동 (인증)

## 목표

- **Spring Security OAuth2 Client** 패턴: 프론트는 `window.location` 으로 백엔드 **`/oauth2/authorization/google`** 에 진입만 한다.
- **프론트에 Google `client_id` 불필요** — 클라이언트 시크릿·OAuth 설정은 백엔드만 가진다.
- **서비스 인증 상태**는 백엔드가 발급한 **Access(·Refresh) JWT** 로 관리한다.
- 보호 API는 `Authorization: Bearer {accessToken}` (기존과 동일).

## 환경 변수 (Vite)

| 변수 | 설명 |
|------|------|
| `VITE_API_BASE_URL` | API·OAuth가 같은 호스트면 베이스 URL (끝 `/` 없음). OAuth 진입 URL은 `{BASE}/oauth2/authorization/google` 로 조합 |
| `VITE_OAUTH_GOOGLE_URL` | (선택) **전체** OAuth 진입 URL. ngrok 등 공개 URL만 쓸 때 설정 |

둘 다 비우면 프론트는 상대 경로 **`/oauth2/authorization/google`** 로 이동한다. 로컬 개발 시 `vite.config.ts` 의 `/oauth2`, `/login/oauth2` 프록시가 백엔드로 넘기는지 확인한다.

`frontend/.env.example` 참고.

## Google 로그인 흐름 (리다이렉트)

1. 사용자가 로그인 페이지에서 **Google 계정으로 로그인** 클릭.
2. 브라우저가 백엔드 `…/oauth2/authorization/google` 로 이동.
3. 백엔드가 Google 동의 화면으로 리다이렉트 후, 콜백에서 사용자 처리.
4. 백엔드가 **프론트로 리다이렉트**하면서 JWT를 쿼리·쿠키·fragment 등 팀이 정한 방식으로 넘긴다.
5. 프론트는 그 시점에 토큰을 읽어 `localStorage` 등에 저장(별도 구현). 저장 후 `/dashboard` 등으로 이동.

> **참고**: 위 4~5단계는 백엔드 `successHandler`·프론트 랜딩 페이지 스크립트에서 맞춰야 한다. 현재 저장소의 `authStorage` 키는 그대로 쓸 수 있다.

## (선택) ID 토큰 API 로그인

백엔드가 `POST /api/auth/google/login` + `{ "idToken" }` 를 지원하는 경우, 프론트에서 `loginWithGoogleIdTokenAndStore` (`shared/api/auth.ts`) 를 호출하는 방식도 가능하다. 기본 UI는 OAuth 리다이렉트만 사용한다.

## 클라이언트 저장소

| 키 | 용도 |
|----|------|
| `ylh.accessToken.v1` | `Authorization` 헤더 |
| `ylh.refreshToken.v1` | 추후 `refresh` API 연동 |
| `ylh.authUser.v1` | JSON — UI 캐시(백엔드가 줄 때만) |

## 백엔드 경로 정렬

Spring 기본 OAuth2 진입점은 보통 `/oauth2/authorization/google` 이다. `context-path` 나 리버스 프록시를 쓰면 프론트의 `VITE_OAUTH_GOOGLE_URL` 또는 `VITE_API_BASE_URL` 을 그에 맞게 설정한다.

## 관련 프론트 파일

| 파일 | 역할 |
|------|------|
| `frontend/src/features/auth/GoogleSignInButton.tsx` | OAuth 진입 URL로 `location.assign` |
| `frontend/src/shared/constants/googleOAuth.ts` | `getGoogleOAuthAuthorizeUrl()` |
| `frontend/src/pages/auth/LoginPage.tsx` | 로그인 랜딩 |
| `frontend/vite.config.ts` | 개발용 `/oauth2` 프록시 |
