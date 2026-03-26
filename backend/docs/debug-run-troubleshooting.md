# 디버깅 기록 — 로컬에서 Spring Boot 실행이 실패할 때

---

## 1. Logback `%wEx` (기동 초기 단계에서 실패)

### 증상

- `There is no conversion class registered for conversion word [wEx]`

### 원인·조치

- `logback-spring.xml` 패턴의 `%wEx` → `%ex` 로 변경.  
- 상세: 기존 `docs/debug-fix-logback-wex.md` 참고.

---

## 2. 포트 8080 이미 사용 중 (Tomcat 시작 직후 실패)

### 증상 (로그 핵심)

```
APPLICATION FAILED TO START

Description:
Web server failed to start. Port 8080 was already in use.

Action:
Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.
```

- 그 전 로그에는 `Tomcat initialized with port 8080`, `Starting ProtocolHandler ["http-nio-8080"]` 까지는 정상으로 보일 수 있음.
- **원인**: 이미 다른 프로세스가 **8080** 을 점유 중 (예: 이전에 띄운 `bootRun`, IntelliJ Run이 종료되지 않음, 다른 웹서버).

### 조치 (택 1)

**A. 8080 쓰는 프로세스 종료 (Windows)**

PowerShell 또는 CMD:

```powershell
netstat -ano | findstr :8080
```

`LISTENING` 행의 **마지막 숫자(PID)** 확인 후:

```powershell
taskkill /PID <PID> /F
```

**B. 애플리케이션 포트 변경**

`src/main/resources/application.yml`:

```yaml
server:
  port: 8081
```

또는 실행 시 한 번만:

```text
.\gradlew.bat bootRun --args='--server.port=8081'
```

### 확인

- 8080을 비우거나 다른 포트로 기동한 뒤 `Tomcat started on port ...` 로 끝나는지 확인.

| 항목 | 내용 |
|------|------|
| 관련 설정 | `server.port` (`application.yml`) |
| 기록 일자 | 2026-03-25 |

---

## 3. Google OAuth 콜백(`/login/oauth2/code/google`)에서 Whitelabel 500

### 증상

- 브라우저: `Internal Server Error (500)`, Whitelabel Error Page.
- 로그: `LazyInitializationException` (또는 `could not initialize proxy … no Session`).

### 원인

- `spring.jpa.open-in-view: false` 인 상태에서, OAuth 성공 직후 `OAuth2AuthenticationSuccessHandler`가 **트랜잭션·영속성 컨텍스트 밖**에서 `UserOAuthAccount.getUser()`(LAZY)에 접근함.

### 조치

- `UserOAuthAccountRepository.findByProviderAndProviderSubject`에 `@EntityGraph(attributePaths = "user")`를 두어, 조회 시 `User`를 함께 로드한다.

| 항목 | 내용 |
|------|------|
| 관련 코드 | `UserOAuthAccountRepository`, `OAuth2AuthenticationSuccessHandler` |
| 기록 일자 | 2026-03-26 |

---

## 4. Google 로그인 `400 redirect_uri_mismatch`

### 증상

- Google 화면: `액세스 차단됨` / `redirect_uri_mismatch`.

### 흔한 원인

1. **Google Cloud Console**의「승인된 리디렉션 URI」에, 앱이 실제로 보내는 `redirect_uri` 와 **완전히 같은** 문자열이 없음 (http vs https, `localhost` vs `127.0.0.1`, 포트, 경로 `/login/oauth2/code/google` 오타).
2. **리버스 프록시(HTTPS 터널 등)** 뒤인데 `redirect_uri` 가 내부 주소(`http://localhost:8080/...`)로 나감 → 콘솔에는 공개 HTTPS 주소만 등록된 경우 불일치.
3. `application.yml` 에 **`spring.forward-headers-strategy`** 만 있고 **`server.forward-headers-strategy`** 가 없음 → Spring Boot 는 `spring.*` 키를 쓰지 않아 포워드 헤더가 반영되지 않음.

### 조치

- `server.forward-headers-strategy: framework` 사용 (`backend/src/main/resources/application.yml` 의 `server:` 블록).
- 콘솔에 **지금 쓰는 흐름마다** URI 추가 (로컬 8080, 공개 HTTPS 호스트, Vite 등 프록시를 쓰면 그 호스트·포트 기준 URI).
- 그래도 맞지 않으면 환경 변수 **`GOOGLE_REDIRECT_URI`** 로 전체 리디렉션 URI 를 고정 (콘솔과 동일한 한 줄).

| 항목 | 내용 |
|------|------|
| 관련 설정 | `server.forward-headers-strategy`, `spring.security.oauth2.client.registration.google.redirect-uri`, `GOOGLE_REDIRECT_URI` |
| 기록 일자 | 2026-03-26 |

---

## 5. 실제 YouTube 구독 목록(`POST /api/v1/subscriptions/sync`)

### 전제

- 기본값: `learningtube.youtube.stub=${YOUTUBE_STUB:false}` → **실제** YouTube Data API `subscriptions.list` 사용. 스텁만 쓰려면 `YOUTUBE_STUB=true`.
- `google.auth.client-id` / `google.auth.client-secret` 이 채워져 있어야 **액세스 토큰 refresh** 가 동작한다(OAuth 클라이언트와 동일 값).
- Google 로그인 시 `refresh_token` 수령을 위해 `access_type=offline`·`prompt=consent` 가 인가 요청에 붙는다(`GoogleOAuth2AuthorizationRequestConfig`).

### 흐름

1. Google 로그인으로 `user_oauth_accounts` 에 access/refresh 저장.
2. `POST /api/v1/subscriptions/sync`(JWT) → 토큰 만료 임박 시 refresh 후 `subscriptions.list` 호출 → DB 반영.
3. `GET /api/v1/subscriptions` 로 목록 조회.

| 항목 | 내용 |
|------|------|
| 관련 코드 | `GoogleOAuthAccessTokenService`, `YoutubeSubscriptionsRestAdapter`, `SubscriptionService` |
| 기록 일자 | 2026-03-26 |
