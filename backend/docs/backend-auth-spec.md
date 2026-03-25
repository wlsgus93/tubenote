# 백엔드 인증/인가 명세 (JWT)

> STEP 5 산출물. 공통 응답 형식은 `backend-api-spec.md`와 **반드시 일치**한다.  
> Refresh 저장소는 `backend-db-spec.md`의 `refresh_tokens` 테이블을 따른다.

---

## 1. 인증 방식

| 항목 | 내용 |
|------|------|
| 프로토콜 | HTTP `Authorization: Bearer {accessToken}` |
| 토큰 종류 | **Access JWT**(무상태) + **Refresh**(DB에 해시 저장, 클라이언트는 원문 보관) |
| 프레임워크 | Spring Security 6.x + 커스텀 `JwtAuthenticationFilter` |
| 비밀번호 | BCrypt(또는 동등) — `users.password_hash` |

---

## 2. JWT 구조

### 2.1 형식

- **JWS** Compact Serialization, 헤더 `alg`, `typ: JWT`.

### 2.2 알고리즘·키

| 환경 | 알고리즘 | 비고 |
|------|----------|------|
| 초기(MVP) | **HS256** | `JWT_SECRET` 환경 변수, 길이·엔트로피 충분히 확보 |
| 확장 | RS256 | 키 회전·마이크로서비스 검증 분리 시 검토 |

### 2.3 Access Token 클레임

| 클레임 | 필수 | 설명 |
|--------|------|------|
| `sub` | Y | 사용자 ID (`users.id`를 문자열로, 예: `"42"`) |
| `typ` | Y | `"access"` — Refresh와 구분 |
| `iat` | Y | 발급 시각(초) |
| `exp` | Y | 만료 시각(초) |
| `role` | Y | 단일 역할 문자열, 예: `MEMBER` / `ADMIN`(검증 시 `ROLE_` 접두 부여) |

**금지**: 비밀번호, Refresh 원문, PII 과다(이메일 등은 넣지 않음).

### 2.4 Refresh Token (JWT로 발급하는 경우)

| 클레임 | 필수 | 설명 |
|--------|------|------|
| `sub` | Y | 동일 |
| `typ` | Y | `"refresh"` |
| `iat`, `exp` | Y | Refresh 만료(예: 7일) |
| `jti` | 권장 | DB 저장 해시는 `hash(jti + secretSalt)` 또는 **전체 토큰 문자열 해시** 중 하나로 통일 |

> Refresh를 **불투명 랜덤 문자열**로만 발급하고 JWT로 만들지 않아도 된다. 이 경우에도 DB에는 **동일하게 해시** 저장.

---

## 3. Access / Refresh 정책

### 3.1 수명(권장 기본값, 환경 변수로 덮어쓰기)

| 토큰 | 기본 TTL | 설명 |
|------|----------|------|
| Access | **15분** | 짧게 유지, 탈취 피해 축소 |
| Refresh | **7일** | 모바일/웹 “로그인 유지” 정책에 맞게 조정 |

### 3.2 저장 위치

| 대상 | 클라이언트 | 서버 |
|------|------------|------|
| Access | 메모리·짧은 저장소 권장 | 저장하지 않음 |
| Refresh | 모바일 Secure Storage / 웹은 **HttpOnly Secure Cookie** 또는 **본문**(SPA는 XSS 리스크 인지) | `refresh_tokens.token_hash` |

### 3.3 로테이션(권장)

- `POST /api/v1/auth/refresh` 성공 시: **기존 Refresh 행 `revoked_at` 설정**, **새 Refresh 행** insert, 새 Access+Refresh 반환.
- **이미 폐기된 Refresh로 재시도** → **401**, `AUTH_REFRESH_REUSED` 또는 `AUTH_UNAUTHORIZED`(팀 선택, 문서·코드 일치).

### 3.4 로그아웃

- `POST /api/v1/auth/logout`: 해당 기기/세션의 Refresh **폐기**(`revoked_at`). Access는 TTL까지 유효(짧으면 허용).

### 3.5 시계

- `exp` 검증 시 클라이언트-서버 시차는 **짧은 Access TTL**로 흡수; 필요 시 `leeway` 소수 초 허용(구현 옵션).

---

## 4. 인증 필터 구조

### 4.1 필터 순서(개념)

1. **Security 필터 체인** 기본 순서 유지.
2. **`JwtAuthenticationFilter`** (`OncePerRequestFilter`):  
   - `Authorization` 헤더가 `Bearer ` 로 시작하면 토큰 추출.  
   - **공개 경로**는 필터 내부에서 **조기 통과**(또는 Security에서 `permitAll`로 인증 생략 시 필터가 스킵되도록 경로 매칭).  
   - 토큰 **검증 성공** 시 `UsernamePasswordAuthenticationToken`(또는 커스텀 `JwtAuthenticationToken`)을 `SecurityContext`에 설정.  
   - **검증 실패** 시 `SecurityContext` 비우고 **다음 필터 진행** — 인증이 필요한 엔드포인트에서는 이후 `AuthenticationEntryPoint`에서 401 처리.

> 구현 패턴: `permitAll` 경로에서는 필터가 **검증 실패를 예외로 던지지 않고** 그냥 통과; 보호 경로에서만 인증 없으면 401.

### 4.2 책임 분리

| 구성 요소 | 책임 |
|-----------|------|
| `JwtTokenProvider` | 서명·검증·파싱·만료 확인, `typ` 검증 |
| `JwtAuthenticationFilter` | 헤더 추출, Provider 호출, `SecurityContext` 설정 |
| `JwtAuthenticationEntryPoint` | 인증 실패 → JSON envelope + 401 |
| `JwtAccessDeniedHandler` | 인가 실패 → JSON envelope + 403 |

### 4.3 금지

- 컨트롤러에서 `Bearer` 문자열 직접 파싱.
- `sub` 대신 요청 본문의 `userId`를 신뢰.

---

## 5. Security Config 초안

### 5.1 원칙

- `@Configuration` + `@EnableWebSecurity`.
- **세션**: `SessionCreationPolicy.STATELESS`.
- **CSRF**: REST JWT 사용 시 **비활성화**(또는 cookie 기반 Refresh 도입 시 예외 검토).
- **CORS**: 별도 `CorsConfigurationSource` 빈; 운영은 허용 오리진 명시.

### 5.2 `authorizeHttpRequests` (경로 예시)

| 구분 | 패턴 | 설정 |
|------|------|------|
| 문서 | `/swagger-ui/**`, `/v3/api-docs/**` | `permitAll` |
| 헬스 | `GET /api/v1/health` | `permitAll` |
| 인증 | `POST /api/v1/auth/signup`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh` | `permitAll` |
| 그 외 API | `/api/v1/**` | `authenticated()` |

- **관리자 전용** API가 생기면 `hasRole("ADMIN")` 등으로 세분화.

### 5.3 예외 처리기 등록

```text
.exceptionHandling(e -> e
  .authenticationEntryPoint(jwtAuthenticationEntryPoint)
  .accessDeniedHandler(jwtAccessDeniedHandler)
)
```

### 5.4 필터 등록

- `JwtAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter` **앞** 또는 **대체** 위치에 `addFilterBefore` 로 삽입(프로젝트 템플릿에 맞춤).

---

## 6. 공개 API / 보호 API 구분

### 6.1 공개 (`permitAll`)

| API | Method | Path |
|-----|--------|------|
| 헬스 | GET | `/api/v1/health` |
| 회원가입 | POST | `/api/v1/auth/signup` |
| 로그인 | POST | `/api/v1/auth/login` |
| 토큰 갱신 | POST | `/api/v1/auth/refresh` |
| Swagger UI | GET | `/swagger-ui/**` |
| OpenAPI JSON | GET | `/v3/api-docs/**` |

> Actuator·기타 관리 엔드포인트는 **운영에서 비활성화 또는 IP 제한**; 공개 금지.

### 6.2 보호 (`authenticated`)

- 위 목록을 제외한 **전체 `/api/v1/**`** (예: `/api/v1/users/me`, `/api/v1/collections/**`, `/api/v1/me/**`).

### 6.3 역할 기반(추가)

- `ROLE_ADMIN` 전용 경로는 `hasRole("ADMIN")` + **별도 문서**에 경로 나열.

### 6.4 YouTube(Google) 액세스 토큰 — 구독 동기화 (STEP 12)

- `POST /api/v1/subscriptions/sync` 는 **JWT로 사용자만 식별**하고, YouTube Data API 호출에는 **`user_oauth_accounts` 중 `provider=GOOGLE` 의 `access_token`** 을 사용한다. `learningtube.youtube.stub=true`(기본)면 실제 API·토큰 없이 스텁 목록으로 동기화한다.
- 토큰 조회: 사용자·GOOGLE·`access_token` not null 중 **`updated_at` 최신 1건**(MVP). 만료 시 갱신(Refresh) 플로우는 추후 OAuth 모듈에서 확장.
- 실패 시 `YOUTUBE_ACCESS_TOKEN_MISSING`(400), `YOUTUBE_AUTH_FAILED`(401) 등은 `backend-api-spec.md` §8.4·`ErrorCode` 와 일치.
- **로그/Swagger**: 액세스·리프레시 토큰 원문 **비노출**.

---

## 7. 인증 실패 / 인가 실패 응답 규칙

### 7.1 공통

- **Content-Type**: `application/json`
- **본문**: `backend-api-spec.md` 실패 envelope (`success: false`, `requestId`, `error`).

### 7.2 인증 실패 — HTTP **401 Unauthorized**

| 상황 | `error.code` | `error.message`(예시) |
|------|--------------|------------------------|
| 헤더 없음 / Bearer 아님 | `AUTH_UNAUTHORIZED` | 인증이 필요합니다. |
| 서명·형식 무효 | `AUTH_UNAUTHORIZED` | 유효하지 않은 토큰입니다. |
| Access 만료 | `AUTH_TOKEN_EXPIRED` | 액세스 토큰이 만료되었습니다. |
| `typ` ≠ access | `AUTH_UNAUTHORIZED` | 유효하지 않은 토큰입니다. |
| 로그인 비밀번호 불일치 | `USER_INVALID_CREDENTIALS` | 이메일 또는 비밀번호가 올바르지 않습니다. |
| Refresh 무효·만료·재사용 의심 | `AUTH_UNAUTHORIZED` 또는 `AUTH_REFRESH_REUSED` | 다시 로그인해 주세요. |

### 7.3 인가 실패 — HTTP **403 Forbidden**

| 상황 | `error.code` | 비고 |
|------|--------------|------|
| 인증은 되었으나 `hasRole` 불충분 | `ACCESS_FORBIDDEN` | 관리자 API 등 |
| 메서드 수준 `@PreAuthorize` 실패 | `ACCESS_FORBIDDEN` | 사용 시 |

> **소유권 불일치**는 §8에 따라 **403 대신 404**로 응답할 수 있음 — 이 경우 `AuthenticationEntryPoint`가 아닌 **서비스 계층**에서 공통 예외로 처리.

### 7.4 헤더

- **401** 응답 시 필요하면 `WWW-Authenticate: Bearer` 추가 가능(선택).

---

## 8. 인가 정책 및 owner check

### 8.1 두 단계

1. **인증**: JWT 유효 + `SecurityContext`에 `userId` 존재.
2. **인가**:  
   - **역할**(ADMIN 등) 필요 시 → Spring Security 또는 서비스 검사.  
   - **소유권** → **항상 서비스 계층**에서 리소스 로드 후 검증.

### 8.2 owner check 원칙

- **기준 식별자**: `Long currentUserId = Long.parseLong(authentication.getName())` 또는 `UserPrincipal#getId()` — **`sub`와 동일**.
- **검증 위치**: 해당 도메인 `*Service`의 **쓰기/단건 조회** 진입점(목록은 `WHERE user_id = ?` 로 제한).
- **관리자**: `ROLE_ADMIN` 은 명시된 **관리용 API**에서만 타인 데이터 접근 허용; 일반 사용자 API에서는 **관리자라도 타인 소유 리소스 조작 금지**(정책 변경 시 문서 개정).

### 8.3 리소스별 owner 기준

| 리소스 | owner 판별 |
|--------|------------|
| User 프로필 | `users.id == currentUserId` (`/me`만 노출 시 경로로 이미 고정) |
| RefreshToken | 행의 `user_id == currentUserId` (로그아웃 시) |
| UserChannel | `user_channels.user_id == currentUserId` |
| Collection | `collections.user_id == currentUserId` |
| CollectionVideo | 상위 `collection.user_id == currentUserId` |
| Note / Highlight | `user_id == currentUserId` |
| WatchQueueItem / UserVideoProgress | `user_id == currentUserId` |
| SyncJob | `user_id` NULL이면 시스템 작업(일반 사용자 조회 제한); 비NULL이면 `user_id == currentUserId` |
| Channel / Video | **공용** — owner 없음; 변경은 **동기화·관리** 역할만(일반 사용자는 읽기/자기 목록에 포함 여부만) |

### 8.4 실패 시 HTTP·코드(권장)

- **소유권 위반 또는 타인 ID로 단건 조회 시도**: **HTTP 404**, `error.code` = `*_NOT_FOUND`(예: `COLLECTION_NOT_FOUND`).  
  - 이유: 리소스 존재 여부를 숨김.  
- **명백히 “금지”를 알려야 하는 UX**(예: 공유 컬렉션 도입 후)가 생기면 **403** + `COLLECTION_ACCESS_DENIED` 로 분기 가능 — 도메인 문서에 명시.

---

## 9. 토큰 재발급 정책

| 단계 | 동작 |
|------|------|
| 요청 | Body에 `refreshToken` (또는 cookie) |
| 검증 | 해시로 DB 조회, `revoked_at IS NULL`, `expires_at > now()` |
| 실패 | 401 + `AUTH_UNAUTHORIZED` |
| 성공 | 로테이션 시 기존 폐기 + 신규 Refresh 저장, 새 Access+Refresh 반환 |

**응답 DTO**(개념): `accessToken`, `accessTokenExpiresAt`, `refreshToken`, `refreshTokenExpiresAt`, `tokenType: "Bearer"`.

---

## 10. Swagger 보안 스키마 문서화 규칙

### 10.1 OpenAPI `components.securitySchemes`

```yaml
bearerAuth:
  type: http
  scheme: bearer
  bearerFormat: JWT
  description: Access JWT (Authorization Bearer)
```

### 10.2 Controller 규칙

- 보호 API 클래스 또는 메서드에 `@SecurityRequirement(name = "bearerAuth")`.
- 공개 API(`auth/signup` 등)는 **보안 요구사항 생략**.

### 10.3 응답 문서

- 보호 API마다 **401**(`AUTH_UNAUTHORIZED`, `AUTH_TOKEN_EXPIRED`), **403**(`ACCESS_FORBIDDEN`) 을 `ApiErrorResponse` 스키마로 `@ApiResponse` 등록.  
- 상세는 `backend-api-spec.md` §7 및 STEP 6 `backend-swagger-spec.md`에서 태그별 예시 보강.

### 10.4 로그인 응답

- `POST /auth/login` 성공 스키마에 **토큰 필드**를 명시; Refresh는 문서에 “노출 위치( body / cookie )” 주석.

---

## 11. 보안 체크리스트(구현 전)

- [ ] `JWT_SECRET` 운영 환경 분리·KMS/시크릿 매니저 저장.
- [ ] HTTPS 강제(운영).
- [ ] Refresh 해시만 DB 저장, 로그에 토큰 원문 금지.
- [ ] Rate limit: 로그인·refresh 엔드포인트(STEP 10 또는 API Gateway).
- [ ] 계정 잠금/로그인 실패 횟수(선택).

---

## 12. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 5 초안 |
