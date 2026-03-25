# 프론트엔드–백엔드 연동

## 개요

- 공통 레이어: `src/shared/api/` (`client`, `interceptors`, `auth`, 도메인별 모듈)
- 환경 변수: `VITE_API_BASE_URL` (선택). 비우면 브라우저 현재 오리진 기준 **상대 경로**로 `/api/...` 호출 → Vite dev에서 `vite.config.ts`의 `server.proxy`로 백엔드에 전달
- 인증: `localStorage` 키 `ylh.accessToken.v1` — `loginAndStoreToken`이 저장, `apiRequest`가 `Authorization: Bearer` 자동 첨부

## 연결 완료

| 화면 | 메서드·경로 | 비고 |
|------|-------------|------|
| 대시보드 | `GET /api/dashboard` | `DashboardPage` → `useDashboard` → `fetchDashboard` |
| 로그인 | `POST /api/auth/login` | `LoginPage` → `loginAndStoreToken` (본문 JSON: `email`, `password`) |
| 학습 자산 목록 | `GET /api/videos` | `VideosPage` → `useVideoLibrary` → `fetchVideos` |
| 영상 상세 | `GET /api/videos/{userVideoId}` | 라우트 `:videoId` = `userVideoId` → `useVideoDetail` |
| 학습 상태 | `PATCH .../learning-state` | `{ "learningStatus": "not_started" \| "in_progress" \| "completed" \| "on_hold" }` |
| 진행률 | `PATCH .../progress` | `{ "progressPercent": 0–100 }` — 상세 스크럽 디바운스 ~0.9s |

**목록 본문**: 배열 또는 `{ items \| videos \| content: [] }` — `unwrapVideoListPayload`. **상세**: `VideoDetailResponseDto` (`types/api.ts`); 스크립트·메모 등 없으면 빈 배열.

**별·우선순위·컬렉션**: 이번 단계에서 PATCH 없음 → 상세에서만 로컬 반영, 새로고침 시 서버 값.

## 다음 연결 제안 (우선순위)

1. **Notes** — 아카이브·필터
2. **Collections API** — 현재 `constants/videoCollections.ts` 정적 목록 대체
3. **Queue / Subscriptions / Analytics** — 스텁 확장
4. **영상 메타 PATCH** — 별·우선순위·컬렉션

## 공통 응답 unwrap

`parseResponseUnwrap` (`interceptors.ts`)이 다음을 처리합니다.

1. HTTP 오류(`!res.ok`): **401** → `onUnauthorized`(기본: 토큰 삭제 후 `/login` 이동, 이미 로그인 페이지면 이동 생략), **403** → `onForbidden`(기본: `console.warn`) 후 `ApiError` throw
2. 본문 JSON에서:
   - `{ success: false, message?, code? }` → `ApiError` (HTTP 200 비즈니스 실패)
   - `{ success: true, data: T }` → `T`
   - `{ data: T }`만 있는 형태 → `T` (에러 필드가 없을 때)
   - 그 외 → 본문 전체를 `T`로 간주

백엔드 계약이 다르면 `interceptors.ts`의 `unwrapEnvelope`만 조정하면 됩니다.

## 대시보드 DTO (`DashboardPayloadDto`)

`src/shared/types/api.ts` 참고. 영상 행은 **`userVideoId` 우선**, 없으면 `id`를 라우트용으로 사용합니다. 매핑은 `mapDashboardPayloadToBundle` (`src/shared/api/dashboard.ts`).

- `nextUp`이 `null`이거나 id가 비면 상단 콜아웃은 빈 상태 UI
- `quickActions`가 비어 있으면 프론트 **기본 빠른 링크**로 채움 (기존 mock과 동일 UX)

## 화면 타입 vs DTO

- 화면은 기존 `DashboardBundle`, `VideoCardModel`, `NoteCardModel` 등 **카드/UI 모델**을 유지
- API 전용 필드는 `*ResponseDto` / `*PayloadDto`에 두고, 매퍼에서 UI 모델로 변환

## 로컬 실행 팁

1. 백엔드를 `http://localhost:8080` 등에서 띄움
2. `.env`에 `VITE_API_BASE_URL`을 비우거나 생략 → Vite가 `/api`를 프록시
3. 또는 `VITE_API_BASE_URL=http://localhost:8080` 로 직접 호출 (CORS 허용 필요)

## 커스터마이징

```ts
import { configureApiClient } from '@/shared/api'

configureApiClient({
  onUnauthorized: () => { /* 기본 대신 커스텀 */ },
  onForbidden: () => { /* 토스트 등 */ },
})
```

앱 진입 전 `main.tsx` 등 한 번 호출하면 됩니다.

## 파일 역할 요약

| 파일 | 역할 |
|------|------|
| `client.ts` | `fetch` 래핑, baseURL, Authorization, `apiGet` / `apiPost` / `apiPatch` |
| `interceptors.ts` | 응답 파싱·unwrap, 401/403 훅, `configureApiClient` |
| `errors.ts` | `ApiError` |
| `auth.ts` | 로그인 POST, 토큰 저장, `logoutClient` |
| `dashboard.ts` | `GET /api/dashboard`, DTO→번들 매핑 |
| `videos.ts` | `GET /api/videos`, `GET/PATCH` 상세·상태·진행률, DTO→`VideoLibraryEntry` / `VideoDetailDocument` |
| `notes.ts` ~ `analytics.ts` | 다음 단계용 스텁 |
| `constants/videoCollections.ts` | 컬렉션 select용 정적 목록(추후 API 대체) |
| `hooks/useVideoLibrary.ts` | 목록 로딩·에러·재시도·로컬 `setItems` |
| `hooks/useVideoDetail.ts` | 상세 로딩·404·에러·`setDetail` |
| `constants/authStorage.ts` | 토큰 get/set/clear |
| `types/api.ts` | 공통·대시보드·로그인·영상 라이브러리/상세 DTO 타입 |
| `hooks/useDashboard.ts` | 대시보드 로딩·에러·재시도 |
