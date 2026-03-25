# Swagger / OpenAPI 문서화 명세

> STEP 6 산출물. 공통 응답·에러 envelope는 `backend-api-spec.md`, JWT·보안 스키마는 `backend-auth-spec.md` §10과 **정합**한다.  
> 구현 시 **springdoc-openapi**(Spring Boot 3) + OpenAPI **3.0** 을 가정한다.

---

## 1. 적용 범위

| 항목 | 규칙 |
|------|------|
| 대상 API | `/api/v1/**` 비즈니스 API 전부; 헬스 등 envelope 예외 API는 **문서에 명시** |
| 문서 UI | `/swagger-ui.html` 또는 `/swagger-ui/index.html`(버전에 따름) |
| OpenAPI JSON | `/v3/api-docs` |
| 비노출 | Actuator·내부 전용 경로는 프로덕션에서 비활성 또는 문서 제외 |

---

## 2. 태그(Tags) 분류 기준

태그는 **URL 접두보다 도메인 경계**를 우선한다. `name`은 **한글 사용자 친화 + 영문 병기** 또는 **영문 단일** 중 팀에서 하나로 통일한다. 아래는 **영문 `name` 권장** 예시.

| 태그 name | 설명 | 포함 API(예시) |
|-----------|------|----------------|
| `Health` | 가용성 | `GET /api/v1/health` |
| `Auth` | 인증·세션 | signup, login, refresh, logout |
| `User` | 계정 프로필 | `GET/PATCH /api/v1/users/me` |
| `My Channels` | 내 등록 채널 | `/api/v1/me/channels` |
| `Collections` | 학습 컬렉션·UserVideo 담기·순서 | `/api/v1/collections`, `.../{id}/videos`, `.../order` (STEP 11) |
| `Collection Videos` | (흡수됨) 중첩 경로는 `Collections` 태그로 문서화 | |
| `Videos` | 공용 Video + 사용자 UserVideo | `POST /api/v1/videos/import-url`, 목록/상세, 학습상태·진행·핀·아카이브 PATCH (STEP 9) |
| `Notes` | 노트 | `/api/v1/videos/{userVideoId}/notes`, `/api/v1/notes/{noteId}` (STEP 10) |
| `Highlights` | 하이라이트 | `/api/v1/videos/{userVideoId}/highlights`, `/api/v1/highlights/{highlightId}` (STEP 10) |
| `Watch Queue` | 나중에 보기 | `/api/v1/me/watch-queue` |
| `Progress` | 시청 진행 | `/api/v1/me/progress/**` |
| `Sync` | 동기화 작업 | `/api/v1/sync/jobs` |
| `Admin` | 운영(향후) | `ROLE_ADMIN` 전용 — 미도입 시 태그 생략 |

### 2.1 규칙

- 한 Controller 클래스는 **1개 태그**를 기본으로 한다. 복합인 경우 **주 태그 1개** + `@Operation(tags = ...)` 는 예외적으로만 사용.
- `@Tag(name = "...", description = "...")` 에 **도메인 책임**을 1~2문장으로 적는다.
- 태그 **알파벳 순** 또는 **사용자 여정 순**(Auth → User → …)으로 `OpenAPI` `tags` 정렬을 `OpenApiCustomizer`로 맞출 수 있다.

---

## 3. Controller 문서화 규칙

### 3.1 클래스 레벨

| 어노테이션 | 필수 | 내용 |
|------------|------|------|
| `@Tag` | Y | `name`, `description` |
| `@SecurityRequirement(name = "bearerAuth")` | 보호 API 전용 클래스에 Y | 공개 전용 `AuthController`는 **생략** |

### 3.2 메서드 레벨

| 어노테이션 | 필수 | 내용 |
|------------|------|------|
| `@Operation` | Y | `summary`(짧게), `description`(전제·부작용·인증 필요 여부) |
| `@Parameter` | 경로/쿼리 있을 때 | `name`, `description`, `example`, `required` |
| `@ApiResponse` | Y | **200**(또는 정의한 2xx) + 유스케이스별 4xx(§7) |
| `@SecurityRequirement` | 클래스에 없을 때만 메서드에 | 공개 메서드는 명시적으로 생략 가능 |

### 3.3 HTTP 메서드·요약

- `summary`는 **동사 + 대상** (예: “컬렉션 목록 조회”, “로그인”).
- `description`에 **페이징**, **정렬**, **id 형식**(내부 numeric id vs YouTube id)을 구분해 적는다.

### 3.4 금지

- 빈 `summary` 또는 “API” 같은 무의미 문자열.
- Entity를 `@Schema(implementation = ...)` 로 노출.

---

## 4. Request / Response DTO 문서화 규칙

### 4.1 공통

- 모든 DTO 필드에 `@Schema` 를 붙인다: 최소 **`description`**, **`example`** (문자열/숫자), **`requiredMode`** (`REQUIRED` / `NOT_REQUIRED`).
- **패스워드·토큰·refresh** 필드: `example` 은 `"********"` 또는 생략(스키마만); description에 “민감정보, 로그 금지” 참고.
- **날짜·시간**: ISO-8601 예시 `2026-03-25T12:00:00Z`.
- **ID**: 내부 DB id는 `example = 1` 또는 `1001`; YouTube id는 문서에 **포맷 설명** + 짧은 예시 문자열.

### 4.2 Request DTO

- Bean Validation 제약(`@NotNull`, `@Size`, `@Email` 등)과 **동일한 필수 여부**가 OpenAPI에 드러나도록 `requiredMode` / `requiredProperties`를 맞춘다.
- **부분 수정 PATCH**: 변경 가능한 필드만 body에 포함한다는 설명을 `@Operation` 또는 DTO class-level `@Schema`에 적는다.

### 4.3 Response DTO

- **민감 필드 제외**: `passwordHash` 등은 DTO 자체에 두지 않음.
- 목록용 `*SummaryResponse` 와 상세 `*DetailResponse` 를 구분해 문서에 각각 스키마로 등록.

### 4.4 성공 envelope와 `data`

- 실제 JSON은 `success`, `requestId`, `data`, `meta` 래핑이므로 문서화 옵션은 다음 중 **하나로 통일**한다.

| 방식 | 설명 |
|------|------|
| A. 래퍼 스키마 | `ApiSuccessResponseXxx` 처럼 `data`에 구체 타입을 넣은 **전용 클래스**를 두고 `@Schema` |
| B. Operation별 content | `@Content(schema = @Schema(implementation = TokenResponse.class))` 만 적고, **전역 설명**에 “실제 응답은 envelope로 감싼다” 문구(비권장·혼란) |
| C. Customizer | `OpenApiCustomizer`로 `/api/v1/**` 응답에 공통 래퍼 **후처리**(구현 난이도 높음) |

**권장**: 구현 단계에서 **A** 또는 **도메인별 래퍼**로 클라이언트 혼동을 줄인다. 본 명세는 **문서에 envelope 필드를 명시한 스키마**를 `components.schemas`에 등록할 것을 요구한다.

---

## 5. enum 문서화 규칙

### 5.1 Java enum

- 각 상수에 **한글 설명**을 주석 또는 `@Schema(description = "...")` (springdoc이 지원하는 방식)으로 남긴다.
- OpenAPI `enum` 배열 값은 **코드값**(예: `MEMBER`, `ADMIN`)을 사용한다.

### 5.2 DTO 필드가 enum일 때

- `@Schema(description = "...", allowableValues = {"PRIVATE", "PUBLIC", "UNLISTED"})` 등으로 **가능한 값 나열**.
- **도메인 의미**를 `description`에 표 형태로 요약 가능.

### 5.3 에러 코드

- `error.code` 는 문자열 enum처럼 문서화: `ApiErrorResponse` 예시에 **대표 코드**를 여러 개 넣거나, 별도 문서 `backend-api-spec.md` §5.3과 **링크**한다.

---

## 6. 인증 API 문서화 규칙

### 6.1 `components.securitySchemes`

```yaml
bearerAuth:
  type: http
  scheme: bearer
  bearerFormat: JWT
  description: Access JWT. Header `Authorization: Bearer <accessToken>`
```

- Java: `@SecurityScheme` 또는 `OpenAPI` 빈에서 등록(`backend-auth-spec.md` §10.1과 동일).

### 6.2 공개 Auth API

- `signup`, `login`, `refresh`: **보안 요구사항 없음**; 응답 body에 **accessToken**, **refreshToken**(또는 cookie 안내) 필드 설명.
- **로그인 실패** 401: `USER_INVALID_CREDENTIALS` 예시.

### 6.3 보호 Auth API

- `logout`: `@SecurityRequirement(name = "bearerAuth")` + **401** 문서화.

### 6.4 금지

- Swagger 예시에 **실제 JWT** 문자열 전체를 넣지 않는다. `eyJhbGciOi...` 형태의 **짧게 잘린 가짜** 또는 `"<accessToken>"` placeholder.

---

## 7. 에러 응답 문서화 규칙

### 7.1 공통 스키마 (`components.schemas`)

| 스키마 | 필드 |
|--------|------|
| `ApiErrorBody` | `code`(string), `message`(string), `details`(array/object/null) |
| `ApiErrorResponse` | `success`(false), `requestId`(string), `error`(`$ref: ApiErrorBody`) |

- 검증 실패 시 `details` 항목: `field`, `reason`, `rejectedValue`(선택).

### 7.2 Operation별 최소 세트

| API 유형 | 문서화할 HTTP 코드 |
|----------|-------------------|
| 보호 API 전반 | **401** (`AUTH_UNAUTHORIZED`, `AUTH_TOKEN_EXPIRED`), **403** (`ACCESS_FORBIDDEN`) |
| Body 있는 POST/PATCH/PUT | **400** (`COMMON_VALIDATION_FAILED` + details 예시) |
| 단건 id 조회·수정·삭제 | **404** (`*_NOT_FOUND`) |
| 생성·중복 가능 | **409** (`USER_EMAIL_DUPLICATE` 등) |
| **500** | 태그 `description` 또는 전역 `info.description`에 1회 명시 + `COMMON_INTERNAL_ERROR` 예시(선택) |

### 7.3 `@ApiResponse` 예시

- 각 에러 응답에 `content = @Content(examples = @ExampleObject(name = "tokenExpired", value = "..."))` 로 **JSON 문자열** 또는 `ref` 활용.
- **동일 스키마** `implementation = ApiErrorResponse.class` 로 통일.

### 7.4 인증 vs 인가 문서 메시지

- **401**: “인증되지 않았거나 토큰이 만료됨 → Refresh 또는 재로그인”.
- **403**: “인증은 되었으나 이 리소스에 대한 권한 없음(역할)” — **소유권 404 정책**인 경우, 403은 역할 전용임을 `description`에 명시.

---

## 8. 예시값(Examples) 작성 규칙

### 8.1 일반

- **실제 서비스 이메일/전화/주소** 사용 금지. `user@example.com`, `test@example.com` 사용.
- **한국어** `message` 예시는 괜찮으나, 클라이언트 i18n 전제 시 “예시 문구”임을 description에 적는다.
- **YouTube id** 형식에 맞는 **가짜** 문자열(길이·문자 패턴).

### 8.2 일관성

- 동일 DTO가 여러 API에 쓰이면 **example 값 통일**(같은 사용자 id, 같은 collection id).
- 숫자 id는 `1`, `42` 등 짧은 값.

### 8.3 민감정보

- 비밀번호 예시: `"password123!"` 은 **교육용으로만** 사용하거나 `"********"` 로 대체.
- 토큰: placeholder만.

### 8.4 배열·페이징

- 목록 example은 **1~2개 요소**로 충분; `meta`는 `totalElements` 등 합리적 숫자.

---

## 9. OpenAPI `info` 및 전역 설명

| 필드 | 내용 |
|------|------|
| `title` | LearningTube API |
| `version` | `1.0.0` (앱 버전과 별도 관리 가능) |
| `description` | Base path `/api/v1`, 공통 envelope, 인증 방식, 500 처리, 링크 to `backend-api-spec.md` |

---

## 10. 구현 체크리스트 (코드 작성 시)

- [ ] `OpenApiConfig` 에 `bearerAuth` SecurityScheme 등록.
- [ ] `ApiErrorResponse` / `ApiErrorBody` 스키마 클래스 + springdoc 인식.
- [ ] 모든 Controller `@Tag` + 메서드 `@Operation`.
- [ ] 보호 API **401·403** + 리소스별 **404·400·409** 반영.
- [ ] DTO 필드 `@Schema` 전부.
- [ ] 프로덕션에서 Swagger 비활성 프로파일 검토.

---

## 11. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 6 초안 |
