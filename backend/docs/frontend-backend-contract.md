# 프론트엔드 — 백엔드 API 계약 (연동 기준)

> Base URL 예: `http://localhost:8080`  
> API prefix: **`/api/v1`** (경로에 `v1` 생략 불가)  
> 본 문서는 Dashboard·**Videos 목록·Video 상세·학습상태/진행률 PATCH** 연동을 우선으로 정리한다.

---

## 1. CORS

| 항목 | 값 |
|------|-----|
| 허용 Origin | `application.yml` → `learningtube.cors.allowed-origins` (기본: localhost/127.0.0.1 + LAN 예: `http://192.168.0.5:5173`, `:3000`) |
| Credentials | `allow-credentials: true` — `fetch(..., { credentials: 'include' })` 사용 시 origin 은 와일드카드 불가(명시 목록만) |
| 허용 메서드 | GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD |
| 허용 헤더 | `*` (Authorization 포함) |
| 노출 헤더 | `X-Request-Id`, `X-Trace-Id` |

프론트가 다른 포트를 쓰면 **백엔드 yml에 origin 추가** 후 재기동.

---

## 2. 공통 응답 Envelope

### 2.1 성공 (`ApiSuccessResponse`)

```json
{
  "success": true,
  "requestId": "uuid",
  "data": { },
  "meta": null,
  "message": null
}
```

| 필드 | 필수 | 설명 |
|------|------|------|
| success | Y | 항상 `true` |
| requestId | Y | 상관 ID; 응답 헤더 `X-Request-Id` 와 동일 값 권장 |
| data | Y | 비즈니스 본문. 없으면 `null` |
| meta | N | 페이징 등 (`PageMeta`). **목록 API에서만** 주로 사용 |
| message | N | 성공 부가 문구. 대부분 API에서 생략(`null` 미직렬화) |

### 2.2 실패 (`ApiErrorResponse`)

```json
{
  "success": false,
  "requestId": "uuid",
  "error": {
    "code": "AUTH_UNAUTHORIZED",
    "message": "사람이 읽을 메시지",
    "details": null
  }
}
```

프론트는 **`error.code`** 로 분기하고, UI에는 **`error.message`** 를 표시하는 패턴을 권장한다.

---

## 3. 인증

| API | 인증 |
|-----|------|
| `POST /api/v1/auth/test-login` | 불필요 |
| `GET /api/v1/health` | 불필요 |
| `GET /api/v1/dashboard` | **필요** |
| `GET /api/v1/videos` 및 하위 | **필요** |

헤더: `Authorization: Bearer <accessToken>`

### 테스트 로그인

- `POST /api/v1/auth/test-login`  
- Body: `{ "username": "test@learningtube.local", "password": "test" }` (`username`은 이메일 형식 필수)  
- 응답: `data.accessToken`, `data.tokenType` (`Bearer`), `data.expiresIn` (초)

### HTTP 상태와 에러 코드 (요약)

| 상황 | HTTP | error.code (예) |
|------|------|-----------------|
| 토큰 없음·만료·무효(필터 통과 실패) | **401** | `AUTH_UNAUTHORIZED` |
| Spring Security 접근 거부 | **403** | `ACCESS_FORBIDDEN` |
| 비즈니스 검증 실패 | **400** | `COMMON_VALIDATION_FAILED` 등 |
| 리소스 없음·소유 아님(통일 정책) | **404** | `USER_VIDEO_NOT_FOUND` 등 |

---

## 4. Dashboard (`GET /api/v1/dashboard`)

- **인증:** Bearer 필수  
- **응답 `data`:** `DashboardResponse`

### 필드 정책

| 필드 | 타입 | 비어 있을 때 |
|------|------|----------------|
| todayPick | 배열 | `[]` |
| continueWatching | 배열 | `[]` |
| recentNotes | 배열 | `[]` |
| incompleteVideos | 배열 | `[]` |
| favoriteChannelUpdates | 배열 | `[]` |
| weeklySummary | 객체 | **항상 존재**(숫자 필드는 0 가능) |

### 비디오 카드 (`DashboardVideoCardDto`)

- `userVideoId`, `videoId`, `youtubeVideoId`, `title`, `thumbnailUrl`, `channelTitle`
- `learningStatus`, `priority`: **문자열 enum 이름** (예: `IN_PROGRESS`, `HIGH`)
- `progressSeconds`: 진행 초
- `durationSeconds`: 영상 길이(초), 없으면 `null`
- `watchPercent`, `updatedAt`

### 목록 API와의 필드 정렬

- `GET /api/v1/videos` 의 각 항목(`UserVideoSummaryResponse`)은 동일 의미로 **`lastPositionSec`** 와 **`progressSeconds`** 를 **둘 다** 내려준다(값 동일). 프론트는 하나만 써도 됨.

---

## 5. 내 영상 목록 (`GET /api/v1/videos`)

### 5.1 경로·쿼리

| Query | 기본 | 설명 |
|-------|------|------|
| `page` | `1` | **1-based** 현재 페이지 |
| `size` | `20` | 페이지 크기 (최대 100) |
| `learningStatus` | (없음) | `NOT_STARTED` \| `IN_PROGRESS` \| `COMPLETED` \| `DROPPED` |
| `archived` | (없음) | `true` \| `false` — 미전달 시 아카이브 여부 필터 없음(전체) |
| `q` | (없음) | 제목 부분 일치(대소문자 무시) |
| `sort` | `updatedAt,desc` | `필드,asc\|desc`. 허용 필드 예: `updatedAt`, `createdAt`, `completedAt`, `lastPositionSec`, `pinned`, `learningStatus`, `priority`, `video.title` |

### 5.2 응답 형태

- **`data`**: `UserVideoSummaryResponse[]` — **항상 배열**(0건이면 `[]`)
- **`meta`**: `PageMeta` (아래 표). 목록에서만 사용.

### 5.3 `PageMeta` (JSON 필드명)

| 필드 | 타입 | 설명 |
|------|------|------|
| `page` | number | 현재 페이지 (1부터) |
| `size` | number | 페이지 크기 |
| `totalElements` | number | 전체 행 수 |
| `totalPages` | number | 전체 페이지 수 |
| `first` | boolean | 첫 페이지 여부 |
| `last` | boolean | 마지막 페이지 여부 |
| `sort` | string | 적용된 정렬 표현 (예: `updatedAt,desc`) |

> 프론트에서 흔한 실수: Spring의 `Page` 기본 키(`totalElements` 등)와 **이 프로젝트의 `PageMeta`가 동일**함. `page`는 **0-base가 아님**.

### 5.4 `UserVideoSummaryResponse` (카드 렌더링용)

| 필드 | 타입 | 비고 |
|------|------|------|
| `userVideoId` | number | **상세·PATCH 경로에 사용** |
| `videoId` | number | 공용 Video PK |
| `youtubeVideoId` | string | YouTube id |
| `title` | string | |
| `thumbnailUrl` | string \| null | |
| `channelTitle` | string \| null | |
| `learningStatus` | enum 문자열 | §9 참고 |
| `priority` | enum 문자열 | §9 참고 |
| `lastPositionSec` | number | 정수 초 |
| `progressSeconds` | number | **`lastPositionSec`와 동일**(getter만 존재, JSON에 둘 다 출력) |
| `watchPercent` | number \| null | 0–100 |
| `pinned` | boolean | |
| `archived` | boolean | |
| `completedAt` | string \| null | ISO-8601 UTC (`Instant`) |
| `updatedAt` | string | ISO-8601 UTC |
| `videoPublishedAt` | string \| null | YouTube 업로드 시각(공용 메타), 없으면 null |

---

## 6. UserVideo 상세 (`GET /api/v1/videos/{userVideoId}`)

- 경로 `{userVideoId}` = **UserVideo PK** (공용 `videoId` 아님).
- **응답 `data`:** `UserVideoDetailResponse` = 위 요약 필드 전부 + 아래 확장 필드.

### 6.1 상세 전용 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `description` | string \| null | 영상 설명(길 수 있음) |
| `durationSeconds` | number \| null | 재생 길이(초), 미상 시 null |
| `sourceType` | string | 현재 `YOUTUBE` 등 (`VideoSourceType`) |
| `userVideoCreatedAt` | string | UserVideo 생성 시각(UTC) |
| `noteCount` | number | 이 UserVideo의 노트 개수 |
| `highlightCount` | number | 하이라이트 개수 |
| `reviewTargetCount` | number | 노트·하이라이트 중 `reviewTarget===true` 합 |
| `transcriptTracksAvailable` | boolean | 공용 Video에 자막 트랙이 DB에 1건 이상 있는지 |
| `transcriptHasSelection` | boolean | 선택된 자막 트랙 존재 여부(자막 탭 기본 노출 판단) |

`GET` / `PATCH .../learning-state` / `PATCH .../progress` / `PATCH .../pin` / `PATCH .../archive` 의 **`UserVideoDetailResponse`는 동일 스키마**로 집계·자막 메타를 채운다(상세 화면에서 PATCH 직후 재조회 없이 갱신 가능).

---

## 7. PATCH 학습 상태 (`PATCH /api/v1/videos/{userVideoId}/learning-state`)

**인증:** Bearer 필수  

### 요청 본문 (`UpdateLearningStateRequest`)

```json
{
  "learningStatus": "IN_PROGRESS",
  "priority": "HIGH"
}
```

| 필드 | 필수 | 설명 |
|------|------|------|
| `learningStatus` | Y | §9 `LearningStatus` |
| `priority` | N | null이면 **기존 값 유지** |

### 동작 요약

- `COMPLETED`로 **최초** 전환 시 `completedAt` 설정.
- `COMPLETED`가 아니면 `completedAt`은 **null**로 초기화.

### 응답

- **200** + `data`: `UserVideoDetailResponse` (§6와 동일 구조)

### 오류

| HTTP | code (예) | 상황 |
|------|-----------|------|
| 400 | `COMMON_VALIDATION_FAILED` | `learningStatus` 누락 등 검증 실패 |
| 404 | `USER_VIDEO_NOT_FOUND` | 없거나 타인 소유 |

---

## 8. PATCH 진행률 (`PATCH /api/v1/videos/{userVideoId}/progress`)

**인증:** Bearer 필수  

### 요청 본문 (`UpdateProgressRequest`)

`lastPositionSec`, `watchPercent` 중 **최소 하나** 필수.

```json
{
  "lastPositionSec": 120,
  "watchPercent": 45
}
```

| 필드 | 제약 |
|------|------|
| `lastPositionSec` | null 허용. 0 이상 정수. **`durationSeconds`가 알려져 있으면 그 값을 초과 불가** |
| `watchPercent` | null 허용. 0–100 정수 |

한쪽만내면 **나머지 필드는 기존 DB 값 유지**.

### 응답

- **200** + `data`: `UserVideoDetailResponse`

### 오류

| HTTP | code (예) | 상황 |
|------|-----------|------|
| 400 | `COMMON_VALIDATION_FAILED` | 둘 다 null, 위치가 길이 초과, 범위 위반 |
| 404 | `USER_VIDEO_NOT_FOUND` | 없거나 타인 소유 |

---

## 9. Enum·날짜 JSON (연동 시 주의)

### 9.1 Enum

요청·응답 모두 **Jackson 기본: enum 이름 문자열**(대문자 스네이크 스타일).

**LearningStatus:** `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `DROPPED`  

**Priority:** `LOW`, `NORMAL`, `HIGH`, `URGENT`

프론트 TypeScript는 `as const` 배열 또는 enum과 1:1 매핑 권장. 숫자·소문자는 서버가 받지 않음.

### 9.2 날짜 (`Instant`)

ISO-8601 **UTC** 문자열 (예: `2025-01-15T08:30:00Z`). 로컬 타임존 가정 금지.

### 9.3 자주 깨지는 포인트

- 목록에서 **`videoId`로 상세 호출** → 404. 반드시 **`userVideoId`**.
- **`page`를 0부터** 보내기 → 백엔드는 **1-based**.
- PATCH progress에서 **둘 다 생략** → 400.
- 상세 화면에서 PATCH 응답을 무시하고 이전 state만 유지 → 현재 API는 **집계 포함 상세**를 돌려주므로 응답으로 state 갱신 가능.

---

## 10. Swagger

- UI: `/swagger-ui.html`
- Videos 태그: 목록/상세/PATCH에 path parameter·에러 코드 설명 반영.

---

## 11. 다음 연동 우선순위 (제안)

1. **Auth:** test-login → 토큰 저장 → 인터셉터에 Bearer  
2. **Dashboard:** 메인 데이터 단일화  
3. **Videos:** `GET .../videos` + 카드 클릭 시 `GET .../videos/{userVideoId}`  
4. **플레이어/상세:** `PATCH .../progress` 디바운스 저장, 상태 변경은 `PATCH .../learning-state`  
5. **에러:** 401 → 로그인, `error.code` → 토스트  
6. 이후: Note/Highlight 목록·작성, Transcript API, Queue

---

## 12. 개정 이력

| 일자 | 내용 |
|------|------|
| 2026-03-25 | 초안 — CORS·envelope·Dashboard/Videos/Detail·인증 정리 |
| 2026-03-25 | Videos: `PageMeta`·요약/상세 필드표·`videoPublishedAt`·자막 플래그·PATCH 요청/응답·enum/날짜 주의사항 |
