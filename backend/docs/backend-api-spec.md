# 백엔드 API 명세 (초안)

> STEP 4 기준. 공통 응답·예외·에러 코드를 고정한다.  
> 리소스별 상세 DTO·모든 엔드포인트 목록은 이후 단계에서 확장한다.

---

## 1. 기본 사항

| 항목 | 값 |
|------|-----|
| Base path | `/api/v1` |
| 프로토콜 | HTTPS(운영), HTTP(로컬) |
| 형식 | `application/json; charset=UTF-8` |
| 시간 | ISO-8601 UTC 권장(예: `2026-03-25T12:00:00Z`) |

### 1.1 공통 헤더

| 헤더 | 필수 | 설명 |
|------|------|------|
| `Authorization` | 보호 API | `Bearer {accessToken}` |
| `Content-Type` | 요청 본문 있을 때 | `application/json` |
| `X-Request-Id` | 선택 | 클라이언트 생성 UUID; 없으면 서버 생성 후 응답에 반영 |

> 예외: **Google 로그인**은 실서비스 연동 편의를 위해 `POST /api/auth/google/login` 경로를 추가로 제공할 수 있다(호환 alias로 `/api/v1/auth/google/login` 도 동일 동작).

### 1.2 CORS (브라우저 프론트)

- 설정: `learningtube.cors.*` (`application.yml`). 기본 허용 origin: Vite `http://localhost:5173`, CRA `http://localhost:3000` 등.
- `Authorization` 헤더·`credentials` 요청 지원. 연동 요약: **`docs/frontend-backend-contract.md`**.

---

## 2. 공통 성공 응답

### 2.1 구조

```json
{
  "success": true,
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "data": { },
  "meta": null,
  "message": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| success | boolean | Y | 성공 시 항상 `true` |
| requestId | string | Y | 추적용 상관 ID |
| data | object \| array \| null | Y | 비즈니스 데이터; 목록만 반환 시 array |
| meta | object \| null | N | 페이징·부가 정보(§2.2) |
| message | string \| null | N | 선택적 성공 메시지; 미사용 시 생략 가능(`null` 미직렬화) |

- HTTP **2xx** 에서 위 형식을 사용한다.
- **본문 없는 성공**(예: 204 No Content) 사용 여부: **비권장**. 삭제·갱신도 `data: null` 과 **200** 으로 통일해 클라이언트 파싱을 단순화한다(필요 시 예외 API는 명세에 별도 표기).

### 2.2 meta (페이징 등)

```json
{
  "meta": {
    "page": 1,
    "size": 20,
    "totalElements": 125,
    "totalPages": 7,
    "sort": "updatedAt,desc"
  }
}
```

| 필드 | 설명 |
|------|------|
| page | 1 기반 페이지 번호 |
| size | 페이지 크기 |
| totalElements | 전체 요소 수 |
| totalPages | 전체 페이지 수 |
| sort | 정렬 표현(선택) |

---

## 3. 공통 실패 응답

### 3.1 구조

```json
{
  "success": false,
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "error": {
    "code": "COLLECTION_NOT_FOUND",
    "message": "컬렉션을 찾을 수 없습니다.",
    "details": null
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| success | boolean | Y | 실패 시 항상 `false` |
| requestId | string | Y | 성공 응답과 동일 |
| error | object | Y | 아래 하위 필드 |
| error.code | string | Y | 기계 판독 코드(§5) |
| error.message | string | Y | 사람이 읽을 수 있는 설명(클라이언트 표시용) |
| error.details | array \| object \| null | N | 검증 오류 목록·부가 키(§3.2) |

### 3.2 details (검증 실패 예시)

```json
{
  "error": {
    "code": "COMMON_VALIDATION_FAILED",
    "message": "입력값이 올바르지 않습니다.",
    "details": [
      { "field": "email", "reason": "올바른 이메일 형식이 아닙니다.", "rejectedValue": null },
      { "field": "name", "reason": "공백일 수 없습니다.", "rejectedValue": "" }
    ]
  }
}
```

| 항목 | 설명 |
|------|------|
| field | DTO 필드명(camelCase) |
| reason | 해당 필드 거절 사유 |
| rejectedValue | 선택; 민감값(비밀번호 등)은 **null** 또는 마스킹 |

---

## 4. HTTP 상태 코드와 매핑 원칙

| HTTP | 용도 | error.code 예시 |
|------|------|-----------------|
| 400 | 잘못된 요청, JSON 형식 오류, Bean Validation 실패 | `COMMON_VALIDATION_FAILED`, `COMMON_INVALID_REQUEST` |
| 401 | 인증 실패(토큰 없음/무효/만료/Refresh 재사용) | `AUTH_UNAUTHORIZED`, `AUTH_TOKEN_EXPIRED`, `AUTH_REFRESH_REUSED` |
| 403 | 인가 실패(권한 없음, 타인 리소스) | `ACCESS_FORBIDDEN`, `COLLECTION_ACCESS_DENIED` |
| 404 | 리소스 없음 | `*_NOT_FOUND` |
| 409 | 충돌(중복, 상태 불일치) | `USER_EMAIL_DUPLICATE`, `COMMON_CONFLICT` |
| 422 | 도메인 규칙 위반(선택 사용) | `VIDEO_NOT_AVAILABLE` 등(팀에서 400과 통일 가능) |
| 429 | 요청 제한 | `COMMON_RATE_LIMITED` |
| 500 | 서버 내부 오류 | `COMMON_INTERNAL_ERROR` |

- **원칙**: 가능하면 **4xx/5xx** 를 올바르게 쓰고, 세부 구분은 **`error.code`** 로 한다.
- **422** 사용 여부는 팀 선택; 본 프로젝트 초기에는 **400** 으로 흡수해도 되며, 명세와 구현을 일치시킨다.

---

## 5. 에러 코드 분류 기준

### 5.1 네이밍

- 형식: **`{도메인}_{이유}`** , **UPPER_SNAKE_CASE**.
- 도메인 접두: 공통 `COMMON_`, 인증 `AUTH_`, 접근 `ACCESS_`, 연동 `YOUTUBE_`, 나머지는 테이블/바운디드 컨텍스트(`USER_`, `CHANNEL_`, `VIDEO_`, `COLLECTION_`, `NOTE_`, `HIGHLIGHT_`, `TRANSCRIPT_`, `QUEUE_`, `SYNC_`).

### 5.2 분류 표

| 구분 | 설명 | HTTP 주로 |
|------|------|-----------|
| COMMON_* | 입력·포맷·내부오류·레이트리밋 | 400, 429, 500 |
| AUTH_* | 인증 | 401 |
| ACCESS_* | 인가·소유권 | 403 |
| {DOMAIN}_* | 도메인 리소스 없음/불가 | 404, 409 |
| YOUTUBE_* | 외부 API 실패·쿼터 | 502, 503, 429 등 |

### 5.3 카탈로그 초안

| code | HTTP | 설명 |
|------|------|------|
| COMMON_VALIDATION_FAILED | 400 | Bean Validation 실패 |
| COMMON_INVALID_REQUEST | 400 | JSON 파싱 실패 등 |
| COMMON_CONFLICT | 409 | 일반 충돌 |
| COMMON_RATE_LIMITED | 429 | Rate limit |
| COMMON_INTERNAL_ERROR | 500 | 처리 중 예외(민감 정보 노출 금지) |
| AUTH_UNAUTHORIZED | 401 | 인증 필요 또는 실패 |
| AUTH_TOKEN_EXPIRED | 401 | 액세스 토큰 만료 |
| AUTH_REFRESH_REUSED | 401 | Refresh 로테이션 후 재사용 등(재로그인 유도) |
| ACCESS_FORBIDDEN | 403 | 권한 없음 |
| USER_NOT_FOUND | 404 | 사용자 없음 |
| USER_EMAIL_DUPLICATE | 409 | 이메일 중복(부분 유니크 위반) |
| USER_INVALID_CREDENTIALS | 401 | 로그인 실패(메시지는 모호하게) |
| GOOGLE_ID_TOKEN_INVALID | 401 | Google ID token(credential) 검증 실패(서명/iss/aud/exp 등) |
| GOOGLE_EMAIL_NOT_VERIFIED | 401 | Google 계정 email_verified=false |
| GOOGLE_EMAIL_MISSING | 400 | Google ID token payload에 email 없음 |
| CHANNEL_NOT_FOUND | 404 | 채널 없음 |
| VIDEO_NOT_FOUND | 404 | 영상 없음 |
| COLLECTION_NOT_FOUND | 404 | 컬렉션 없음 |
| COLLECTION_NAME_DUPLICATE | 409 | 동일 사용자 내 컬렉션 이름 중복(trim + 대소문자 무시) |
| COLLECTION_ACCESS_DENIED | 403 | 타인 컬렉션 |
| COLLECTION_VIDEO_NOT_FOUND | 404 | 컬렉션에 해당 UserVideo 매핑 없음 |
| COLLECTION_VIDEO_DUPLICATE | 409 | 동일 컬렉션에 동일 UserVideo 중복 |
| COLLECTION_VIDEO_ORDER_INVALID | 400 | 순서 변경 요청 집합이 현재 멤버와 불일치 |
| NOTE_NOT_FOUND | 404 | 노트 없음 |
| HIGHLIGHT_NOT_FOUND | 404 | 하이라이트 없음 |
| TRANSCRIPT_NOT_FOUND | 404 | 자막 없음 |
| QUEUE_ITEM_NOT_FOUND | 404 | 큐 항목 없음 |
| QUEUE_USER_VIDEO_ALREADY_IN_QUEUE | 409 | 동일 UserVideo가 학습 큐에 이미 존재(`user_id`,`user_video_id` 유니크) |
| QUEUE_ORDER_INVALID | 400 | 순서 변경 요청 id 목록이 해당 큐 멤버와 불일치 또는 중복 id |
| TRANSCRIPT_ACCESS_DENIED | 403 | 내 학습 목록에 없는 Video에 Transcript API 호출 |
| TRANSCRIPT_TRACK_NOT_FOUND | 404 | TranscriptTrack 없음 또는 해당 videoId 소속 아님 |
| SYNC_JOB_NOT_FOUND | 404 | 동기화 작업 없음 |
| YOUTUBE_UPSTREAM_ERROR | 502 | YouTube API 오류 |
| YOUTUBE_QUOTA_EXCEEDED | 429 | API 할당량 |
| YOUTUBE_ACCESS_TOKEN_MISSING | 400 | YouTube 연동용 Google 액세스 토큰 없음 |
| YOUTUBE_AUTH_FAILED | 401 | YouTube/Google 토큰 무효·만료 |
| SUBSCRIPTION_NOT_FOUND | 404 | UserSubscription 없음 또는 타인 소유(404 통일) |
| SUBSCRIPTION_DUPLICATE | 409 | `(user_id, channel_id)` 유니크 위반(동시 동기화 등) |
| VIDEO_INVALID_YOUTUBE_URL | 400 | YouTube URL/ID 파싱 실패 |
| VIDEO_YOUTUBE_ID_DUPLICATE | 409 | 공용 Video `youtube_video_id` 유니크 충돌(동시 임포트 등) |
| USER_VIDEO_NOT_FOUND | 404 | UserVideo 없음 또는 타인 소유(404 통일) |
| USER_VIDEO_DUPLICATE | 409 | 동일 사용자에 이미 등록된 영상(선행 검사 또는 DB 유니크 위반) |

> 구현 시 `ErrorCode` enum(또는 상수 클래스)에 동일 문자열을 정의하고, **문서-코드 싱크**를 유지한다.

---

## 6. 글로벌 예외 처리 전략 (Spring)

### 6.1 위치

- 패키지: `com.myapp.learningtube.global.error`
- `@RestControllerAdvice` 단일(또는 도메인별 분리 시에도 **응답 envelope 동일**).

### 6.2 처리 대상(권장 매핑)

| 예외 유형 | HTTP | error.code |
|-----------|------|------------|
| 커스텀 `BusinessException`(도메인) | 예외별 | enum에 정의된 코드 |
| `MethodArgumentNotValidException` | 400 | COMMON_VALIDATION_FAILED |
| `ConstraintViolationException` | 400 | COMMON_VALIDATION_FAILED |
| `HttpMessageNotReadableException` | 400 | COMMON_INVALID_REQUEST |
| Spring Security `AuthenticationException` | 401 | AUTH_UNAUTHORIZED 등 세분화 |
| Spring Security `AccessDeniedException` | 403 | ACCESS_FORBIDDEN |
| `DataIntegrityViolationException`(유니크) | 409 | 도메인별 변환(예: USER_EMAIL_DUPLICATE) |
| 기타 `Exception` | 500 | COMMON_INTERNAL_ERROR(스택은 로그만) |

### 6.3 도메인 예외 패턴

- `BusinessException extends RuntimeException` + `ErrorCode errorCode` + 선택 `details`.
- 리소스 없음: `ResourceNotFoundException` 또는 도메인별 `XxxNotFoundException` → 404 + `XXX_NOT_FOUND`.

### 6.4 로깅

- **500**: ERROR 레벨, `requestId`, `error.code`, 예외 타입·메시지(스택은 정책에 따라).
- **4xx**: INFO 또는 WARN(반복 스팸 주의); **401/403** 은 보안 정책에 맞춰 레벨 조정.

### 6.5 DB·soft delete

- 조회 시 `deleted_at IS NULL` 누락으로 “없는 것처럼” 보이면 **404** 와 `*_NOT_FOUND` 로 통일(정보 누설 최소화).

---

## 7. Swagger(OpenAPI) — 에러 응답 규칙

> 태그·DTO·enum·예시값 등 **문서화 전반**은 `docs/backend-swagger-spec.md`(STEP 6)를 따른다.

### 7.1 공통 스키마

- OpenAPI **components.schemas** 에 `ApiErrorResponse` 정의:
  - `success`, `requestId`, `error`(nested: `code`, `message`, `details`).
- 성공 래퍼 `ApiSuccessResponse`를 제네릭으로 표현하기 어려우면, **대표 DTO**별로 `data` 스키마를 inline 하거나 `oneOf` 사용.

### 7.2 Controller 어노테이션

- 각 Operation에 **최소** 다음 응답을 문서화:
  - **401**, **403**(보호 API)
  - **404**(단일 리소스 조회·수정·삭제)
  - **409**(생성·중복 가능)
  - **400**(요청 본문 있음)
- `@ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))` 형태로 통일.
- **500** 은 전역 태그 설명 또는 공통 문구로 한 번 명시 가능(모든 메서드 중복 최소화).

### 7.3 예시값

- `examples`에 `error.code`, `error.message` 샘플을 1~2개 포함해 클라이언트 생성기 품질을 높인다.

### 7.4 보안 스키마

- JWT Bearer는 `components.securitySchemes`; 401 응답과 함께 문서화.

---

## 8. API 목록 초안

> 상세 요청/응답 DTO·에러 케이스는 후속 보강. 인증 필요 여부만 우선 고정.

| API 이름 | Method | Path | 인증 | 비고 |
|----------|--------|------|------|------|
| 헬스 체크 | GET | `/api/v1/health` | N | 인프라용, envelope 생략 가능 여부는 구현 시 결정(생략 시 문서 명시) |
| 회원가입 | POST | `/api/v1/auth/signup` | N | |
| 로그인 | POST | `/api/v1/auth/login` | N | access/refresh 발급 |
| 토큰 갱신 | POST | `/api/v1/auth/refresh` | N | refresh body 또는 cookie |
| 로그아웃 | POST | `/api/v1/auth/logout` | Y | refresh 폐기 |
| Google 로그인 | POST | `/api/auth/google/login` | N | 프론트가 보낸 Google ID token 검증 후 내부 access/refresh 발급 (호환 alias: `/api/v1/auth/google/login`) |
| 내 프로필 조회 | GET | `/api/v1/users/me` | Y | |
| 내 프로필 수정 | PATCH | `/api/v1/users/me` | Y | |
| 등록 채널 목록 | GET | `/api/v1/me/channels` | Y | |
| 채널 등록 | POST | `/api/v1/me/channels` | Y | |
| 채널 등록 해제 | DELETE | `/api/v1/me/channels/{channelId}` | Y | |
| 컬렉션 CRUD | * | `/api/v1/collections` 등 | Y | 소유권 |
| 컬렉션-영상 | * | `/api/v1/collections/{id}/videos` | Y | |
| 노트·하이라이트 | * | `/api/v1/videos/{videoId}/notes` 등 | Y | 경로는 구현 시 확정 |
| 시청 큐 | * | `/api/v1/me/watch-queue` | Y | |
| 진행률 | PUT | `/api/v1/me/progress/videos/{videoId}` | Y | |
| 동기화 작업 | GET/POST | `/api/v1/sync/jobs` 등 | Y | |
| 영상 URL 임포트 | POST | `/api/v1/videos/import-url` | Y | 공용 Video + UserVideo |
| 내 영상 목록 | GET | `/api/v1/videos` | Y | 페이징·필터·정렬 |
| UserVideo 상세 | GET | `/api/v1/videos/{userVideoId}` | Y | |
| 학습 상태 변경 | PATCH | `/api/v1/videos/{userVideoId}/learning-state` | Y | |
| 진행률/위치 | PATCH | `/api/v1/videos/{userVideoId}/progress` | Y | |
| 핀 | PATCH | `/api/v1/videos/{userVideoId}/pin` | Y | |
| 아카이브 | PATCH | `/api/v1/videos/{userVideoId}/archive` | Y | |
| 노트 CRUD | * | `/api/v1/videos/{userVideoId}/notes`, `/api/v1/notes/{noteId}` | Y | STEP 10 |
| 하이라이트 CRUD | * | `/api/v1/videos/{userVideoId}/highlights`, `/api/v1/highlights/{id}` | Y | STEP 10 |
| 컬렉션 | * | `/api/v1/collections`, `/api/v1/collections/{id}/videos` 등 | Y | STEP 11 |
| 구독 동기화·목록 | * | `/api/v1/subscriptions`, `/api/v1/subscriptions/sync` | Y | STEP 12 |
| 구독 채널 최신 업로드 | * | `/api/v1/subscriptions/channel-updates/sync`, `.../recent-videos` | Y(sync만 외부) | STEP 13 |
| 대시보드 집계 | GET | `/api/v1/dashboard` | Y | STEP 14 |
| Analytics | GET | `/api/v1/analytics/*` | Y | STEP 15 |
| 학습 큐 | * | `/api/v1/queue` | Y | STEP 16 — TODAY/WEEKLY/BACKLOG, UserVideo당 1행 |
| Transcript | * | `/api/v1/videos/{videoId}/transcript` | Y | STEP 17 — 공용 Video PK, 내 목록 검증 |

### 8.1 Video / UserVideo (STEP 9 구현 상세)

| API 이름 | Method | Path | 인증 | 요약 |
|----------|--------|------|------|------|
| URL 임포트 | POST | `/api/v1/videos/import-url` | Y | Body: `{ "url": string }` → `ImportVideoUrlResponse` |
| 목록 | GET | `/api/v1/videos` | Y | Query: `page`(기본 1), `size`(기본 20, 최대 100), `learningStatus?`, `archived?`, `q?`(제목 부분일치), `sort?`(예: `updatedAt,desc`, `video.title,asc`). 응답 `data`: 배열(0건 `[]`), `meta`: `PageMeta`(필드: `page`,`size`,`totalElements`,`totalPages`,`first`,`last`,`sort`). 요약 항목에 `videoPublishedAt`, `progressSeconds`(=`lastPositionSec`) 포함 |
| 상세 | GET | `/api/v1/videos/{userVideoId}` | Y | `UserVideoDetailResponse` — 집계·자막 메타 포함(아래 확장) |
| 학습 상태 | PATCH | `/api/v1/videos/{userVideoId}/learning-state` | Y | 요청: `learningStatus` 필수, `priority` 선택. 응답 본문은 GET 상세와 동일 스키마(집계 재계산) |
| 진행 | PATCH | `/api/v1/videos/{userVideoId}/progress` | Y | `lastPositionSec` / `watchPercent` 중 ≥1; 알려진 `durationSeconds` 초과 시 400 |
| 핀 | PATCH | `/api/v1/videos/{userVideoId}/pin` | Y | `{ "pinned": boolean }` |
| 아카이브 | PATCH | `/api/v1/videos/{userVideoId}/archive` | Y | `{ "archived": boolean }` |

**에러 (요약)**

| 상황 | HTTP | code |
|------|------|------|
| URL 파싱 실패 | 400 | VIDEO_INVALID_YOUTUBE_URL |
| 이미 내 목록에 있음 | 409 | USER_VIDEO_DUPLICATE |
| 없거나 타인 것 | 404 | USER_VIDEO_NOT_FOUND |
| 진행 필드 누락·길이 초과 등 | 400 | COMMON_VALIDATION_FAILED |
| DB 유니크(내 목록 중복, 동시 임포트) | 409 | USER_VIDEO_DUPLICATE |
| DB 유니크(공용 Video youtube id 동시 생성) | 409 | VIDEO_YOUTUBE_ID_DUPLICATE |
| DB 유니크(collection+user_video 동시 추가) | 409 | COLLECTION_VIDEO_DUPLICATE |
| 기타 무결성 위반 | 500 | COMMON_INTERNAL_ERROR |

### 8.2 Note / Highlight (STEP 10)

Base: `/api/v1`. 모두 **Bearer** 필요. `userVideoId` 경로는 **본인 UserVideo**만; `noteId`/`highlightId`는 **소유자**만(id로 조회 시 타인이면 404).

#### Note

| Method | Path | 요약 |
|--------|------|------|
| POST | `/api/v1/videos/{userVideoId}/notes` | 생성 — `CreateNoteRequest` |
| GET | `/api/v1/videos/{userVideoId}/notes` | 목록 — `page`(기본 1), `size`(기본 50, 최대 200), `meta` 페이징 |
| PATCH | `/api/v1/notes/{noteId}` | 부분 수정 |
| DELETE | `/api/v1/notes/{noteId}` | 삭제 — `data: null` |

- **NoteType**: `GENERAL` → `positionSec` 없음; `TIMESTAMP` → `positionSec` 필수(0 이상, 영상 길이 이하).
- **에러**: `USER_VIDEO_NOT_FOUND`, `NOTE_NOT_FOUND`, `COMMON_VALIDATION_FAILED`.

#### Highlight

| Method | Path | 요약 |
|--------|------|------|
| POST | `/api/v1/videos/{userVideoId}/highlights` | 생성 |
| GET | `/api/v1/videos/{userVideoId}/highlights` | 목록 — 페이징 동일 |
| PATCH | `/api/v1/highlights/{highlightId}` | 부분 수정 |
| DELETE | `/api/v1/highlights/{highlightId}` | 삭제 |

- **구간**: `0 ≤ startSec ≤ endSec`, `durationSeconds`가 있으면 `endSec` 상한.
- **에러**: `USER_VIDEO_NOT_FOUND`, `HIGHLIGHT_NOT_FOUND`, `COMMON_VALIDATION_FAILED`.

#### UserVideo 상세 확장

- `GET` 및 **`PATCH` learning-state / progress / pin / archive** 응답(`UserVideoDetailResponse`)에 공통 포함:
  - **`noteCount`**, **`highlightCount`**, **`reviewTargetCount`**(노트·하이라이트 중 `reviewTarget=true` 합)
  - **`transcriptTracksAvailable`**, **`transcriptHasSelection`**(공용 Video·자막 트랙 기준)
  - 요약 상속 필드: **`videoPublishedAt`**, `description`, `durationSeconds`, `sourceType`, `userVideoCreatedAt` 등

### 8.3 Collection / CollectionVideo (STEP 11)

Base `/api/v1/collections`. 소유권: **Collection.user_id = JWT sub**. UserVideo 추가 시 해당 UserVideo도 **동일 사용자**여야 함.

| Method | Path | 요약 |
|--------|------|------|
| POST | `/api/v1/collections` | 생성 — 이름 trim·중복 검사(대소문자 무시), 컬렉션 `sortOrder` 자동(맨 뒤) |
| GET | `/api/v1/collections` | 목록 — `videoCount`, 페이징 |
| GET | `/api/v1/collections/{collectionId}` | 상세 — `videoCount`, `previewThumbnailUrls`(최대 3) |
| PATCH | `/api/v1/collections/{collectionId}` | 이름·설명·visibility·sortOrder·coverThumbnailUrl |
| DELETE | `/api/v1/collections/{collectionId}` | 컬렉션 삭제 — **CollectionVideo cascade 물리 삭제** |
| POST | `/api/v1/collections/{collectionId}/videos` | `userVideoId` 추가 |
| GET | `/api/v1/collections/{collectionId}/videos` | 담긴 영상 목록(페이징, `position` 오름차순) |
| DELETE | `/api/v1/collections/{collectionId}/videos/{userVideoId}` | 매핑 제거 |
| PATCH | `/api/v1/collections/{collectionId}/videos/order` | `orderedUserVideoIds` — 현재 멤버와 **동일 집합** |

**확장**: `coverThumbnailUrl` 수동/배치 동기화; 다중 썸네일은 API 필드 `previewThumbnailUrls`로 확장 가능.

### 8.4 Subscriptions / Channel (STEP 12)

Base `/api/v1/subscriptions`. **조회·수정은 내부 DB만** 사용. **`POST .../sync` 만** YouTube Data API(또는 `learningtube.youtube.stub=true` 스텁)를 호출한다.

| Method | Path | 요약 |
|--------|------|------|
| POST | `/api/v1/subscriptions/sync` | 구독 목록 동기화 — 응답 `SubscriptionSyncResponse`: `syncedCount`, `createdCount`, `updatedCount`, `failedCount` |
| GET | `/api/v1/subscriptions` | 내 구독 목록 — 페이징(`page` 기본 1, `size` 기본 20·최대 100), `updatedAt` 내림차순 |
| GET | `/api/v1/subscriptions/{subscriptionId}` | 단건 — 본인만 |
| PATCH | `/api/v1/subscriptions/{subscriptionId}` | `category`, `isFavorite`, `isLearningChannel`, `note` — 전달된 필드만 갱신; 빈 문자열은 null 저장 |

**인증·토큰**: `stub=false` 일 때 `UserOAuthAccount`(provider=`GOOGLE`)에 **비어 있지 않은 `access_token`** 이 있어야 한다. 없으면 **400** `YOUTUBE_ACCESS_TOKEN_MISSING`. JWT는 기존과 동일(`Authorization: Bearer`).

**에러 (요약)**

| 상황 | HTTP | code |
|------|------|------|
| Google 액세스 토큰 없음 | 400 | YOUTUBE_ACCESS_TOKEN_MISSING |
| YouTube 401 등 | 401 | YOUTUBE_AUTH_FAILED |
| 할당량 | 429 | YOUTUBE_QUOTA_EXCEEDED |
| 기타 YouTube 오류·파싱 실패 | 502 | YOUTUBE_UPSTREAM_ERROR |
| 없거나 타인 것 | 404 | SUBSCRIPTION_NOT_FOUND |
| 유니크 충돌(항목 단위 동기화에서 흡수, 전역 핸들러는 동일 UK 문자열 시 409) | 409 | SUBSCRIPTION_DUPLICATE |
| PATCH 필드 없음 | 400 | COMMON_VALIDATION_FAILED |

**다음 확장(STEP 13 반영)**: §8.5 참고.

### 8.5 구독 채널 최근 업로드 피드 (STEP 13)

| Method | Path | 외부 API | 요약 |
|--------|------|----------|------|
| POST | `/api/v1/subscriptions/channel-updates/sync` | Y(stub 가능) | 모든 내 구독 채널에 대해 uploads 플레이리스트 기반 최근 영상 수집(기본 15건/채널, `learningtube.youtube.channel-updates-max-videos-per-channel`). 응답 `ChannelUpdatesSyncResponse`: `processedChannels`, `createdVideos`, `updatedVideos`, `failedChannels` |
| GET | `/api/v1/subscriptions/recent-videos` | N | 전 구독 합산 피드 — `publishedAt` 내림차순, 페이징 |
| GET | `/api/v1/subscriptions/{subscriptionId}/recent-videos` | N | 단일 구독 채널 피드 — 본인만 |

**데이터**: 공용 `Video` 는 `youtube_video_id` 기준 upsert. 피드 행은 `subscription_recent_videos`(`user_subscription_id`, `video_id` 유니크). **UserVideo 자동 생성 없음**.

**`isNew` / `unreadNewVideoCount`**: 해당 사용자에 대해 **아직 `UserVideo`로 담기지 않은** 피드 영상을 “새 영상”으로 본다. 구독별 `unread_new_video_count` 는 채널 업로드 sync 완료 시점에 재계산한다.

**에러**: §8.4 YouTube 코드와 동일. 채널 단위 실패는 `failedChannels` 로 집계(200 응답 가능). 사전 조건 실패(토큰 없음 등)는 4xx.

### 8.6 Dashboard (STEP 14)

| Method | Path | 인증 | 요약 |
|--------|------|------|------|
| GET | `/api/v1/dashboard` | Y | 메인 화면용 **읽기 전용** 집계. `DashboardResponse` — `todayPick`, `continueWatching`, `recentNotes`, `incompleteVideos`, `favoriteChannelUpdates`, `weeklySummary` |

- **todayPick**: **STEP 16 이후** `LearningQueueItem`(`queueType=TODAY`)를 우선 소스로 쓰는 것을 권장. 현재 구현은 Queue 미연동 **임시 추천** — `NOT_STARTED`/`IN_PROGRESS` 후보 중 우선순위(URGENT→HIGH→…) 및 `updatedAt` 기준 상위 N건(`learningtube.dashboard.today-pick-limit` 등).
- **continueWatching**: `lastPositionSec > 0` 이고 `learningStatus <> COMPLETED`.
- **recentNotes**: 본인 `Note` 중 `createdAt` 내림차순, 본문 미리보기 길이 제한.
- **incompleteVideos**: `NOT_STARTED`·`IN_PROGRESS`, 보관함 제외.
- **favoriteChannelUpdates**: `isFavorite=true` 구독 상위 N건 + 채널별 피드 영상 M건(DB).
- **weeklySummary**: `weekStartUtc`(UTC 월요일 00:00) 기준 — `inProgressCount`(해당 주에 `updatedAt`이 갱신된 `IN_PROGRESS` 영상 수), `completedCount`(`completedAt` 구간), `noteCount`/`highlightCount`(`createdAt` 구간).

**설정**: `learningtube.dashboard.*` (`application.yml` 참고).

### 8.7 Analytics (STEP 15)

Base `/api/v1/analytics`. **읽기 전용** 실시간 집계(JPQL·네이티브 SQL). 추후 `DailyLearningStat` 등 집계 엔티티·배치로 대체 가능.

| Method | Path | 요약 |
|--------|------|------|
| GET | `/api/v1/analytics/summary` | `AnalyticsSummaryResponse` — 저장 영상 총계, 진행/완료/onHold, 노트·하이라이트 총계, `estimatedLearningSeconds`(진행 초 합) |
| GET | `/api/v1/analytics/daily` | Query `rangeType`: `WEEK` / `MONTH` / `ALL`(기본 `WEEK`). `AnalyticsDailyResponse` — 일자별 완료 수(`completed_at`), 노트/하이라이트 생성 수 |
| GET | `/api/v1/analytics/status-distribution` | 보관함 제외 `UserVideo` 의 `learningStatus`별 건수 |
| GET | `/api/v1/analytics/channels` | 채널(Video 메타)별 저장·완료·노트 수; `Channel` 매핑 시 `channelId` |
| GET | `/api/v1/analytics/collections` | 컬렉션별 담긴 영상 수·완료 수 |

- **onHoldCount**: `IN_PROGRESS`·`COMPLETED` 가 아니고 보관함 제외(`NOT_STARTED`, `DROPPED` 등).
- **daily / ALL**: 사용자 최초 활동일~오늘(UTC)이나 일자 버킷은 `learningtube.analytics.max-daily-buckets` 로 상한.
- **daily / WEEK·MONTH**: `learningtube.analytics.week-days`, `month-days` 롤링 일수.
- **잘못된 rangeType**: 400 `COMMON_VALIDATION_FAILED`.

**설정**: `learningtube.analytics.*`.

### 8.8 Queue / LearningQueueItem (STEP 16)

Base `/api/v1/queue`. **Bearer** 필요. 소유권: `LearningQueueItem.user_id = JWT sub`. **UserVideo**는 반드시 동일 사용자 것.

**정책**

- 동일 사용자에 대해 **동일 `user_video_id`는 큐 테이블에 최대 1행**(TODAY/WEEKLY/BACKLOG 중 하나에만 존재).
- 동일 `(user_id, queue_type)` 내 `sort_order`(API 필드명 `position`)는 **0부터 연속 정수**. 추가·삭제·이동·재정렬 후 서비스가 compact/재부여.
- **`queueType` 변경(PATCH)**: 이전 타입 버킷은 남은 항목만으로 position 재압축; 새 타입에는 요청 `position`(없으면 맨 뒤)에 삽입.
- **`PATCH /reorder`**: 요청 `orderedQueueItemIds`는 해당 `queueType`의 **현재 멤버 id 집합과 동일**(순서만 변경). 컬렉션 `videos/order`와 동일 패턴.

| Method | Path | 요약 |
|--------|------|------|
| GET | `/api/v1/queue` | 목록. Query `queueType?` — 없으면 타입 순(TODAY→WEEKLY→BACKLOG)·position 오름차순 |
| POST | `/api/v1/queue` | `AddQueueItemRequest`: `userVideoId`, `queueType`, `position?` |
| PATCH | `/api/v1/queue/reorder` | `ReorderQueueRequest`: `queueType`, `orderedQueueItemIds` |
| PATCH | `/api/v1/queue/{queueItemId}` | `UpdateQueueItemRequest`: `queueType?`, `position?` (최소 하나) |
| DELETE | `/api/v1/queue/{queueItemId}` | 제거 후 해당 타입 compact |

**응답 DTO (`QueueItemResponse`)**: `queueItemId`, `userVideoId`, `queueType`, `position`, `addedAt`(=`createdAt`), `videoTitle`, `thumbnailUrl`, `learningStatus`, `priority`, `progressSeconds`, `durationSeconds`.

**에러 (요약)**

| 상황 | HTTP | code |
|------|------|------|
| 큐 항목 없음·타인 것 | 404 | QUEUE_ITEM_NOT_FOUND |
| 이미 큐에 있는 UserVideo 추가 | 409 | QUEUE_USER_VIDEO_ALREADY_IN_QUEUE |
| reorder 집합 불일치·중복 id | 400 | QUEUE_ORDER_INVALID |
| 내 목록에 없는 UserVideo 추가 | 404 | USER_VIDEO_NOT_FOUND |
| PATCH 필드 없음 | 400 | COMMON_VALIDATION_FAILED |
| DB 유니크 동시성 | 409 | QUEUE_USER_VIDEO_ALREADY_IN_QUEUE(글로벌 핸들러 UK 매핑) |

**Dashboard 연계**: `GET /api/v1/dashboard` 의 `todayPick`은 현재 UserVideo 후보 풀 기반 임시 로직 — **후속**으로 `queueType=TODAY` 큐(또는 별도 필드)를 우선 소스로 바꿀 수 있음(§8.6 참고).

---

## 9. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 4 초안 — 공통 응답·에러·글로벌 예외·Swagger·API 목록 초안 |
| 0.2 | 2026-03-25 | STEP 5 정합 — `AUTH_REFRESH_REUSED` 카탈로그 추가 |
| 0.3 | 2026-03-25 | STEP 6 — §7 상단에 `backend-swagger-spec.md` 참조 추가 |
| 0.4 | 2026-03-25 | STEP 9 — Video/UserVideo API·에러 코드·§8.1 상세 |
| 0.5 | 2026-03-25 | `completedAt`·`VIDEO_YOUTUBE_ID_DUPLICATE`·무결성 예외 매핑 |
| 0.6 | 2026-03-25 | STEP 10 — Note/Highlight API·UserVideo 상세 집계·§8.2 |
| 0.7 | 2026-03-25 | STEP 11 — Collection/CollectionVideo·에러 코드·§8.3 |
| 0.8 | 2026-03-25 | 컬렉션 이름 중복 정책·`COLLECTION_NAME_DUPLICATE`·항목 `position` 필드 |
| 0.9 | 2026-03-25 | STEP 12 — 구독 동기화·`SUBSCRIPTION_*`·`YOUTUBE_ACCESS_TOKEN_MISSING`·`YOUTUBE_AUTH_FAILED`·§8.4 |
| 0.10 | 2026-03-25 | STEP 13 — 채널 최근 업로드 sync·피드 조회·§8.5 |
| 0.11 | 2026-03-25 | STEP 14 — `GET /api/v1/dashboard`·§8.6 |
| 0.12 | 2026-03-25 | STEP 15 — Analytics API·§8.7 |
| 0.13 | 2026-03-25 | STEP 16 — Queue API·에러 코드·§8.8 |
| 0.14 | 2026-03-25 | STEP 17 — Transcript API·§8.9·`VIDEO_NOT_FOUND`·`TRANSCRIPT_*`·`COMMON_CONFLICT` enum 정합 |
| 0.15 | 2026-03-25 | 프론트 연동 — CORS(§1.2)·성공 envelope `message`·`frontend-backend-contract.md` |
