# STEP 14 계획 — Dashboard 집계 API

## 1. 단계 목표

- 기존 도메인(`UserVideo`, `Note`, `Highlight`, `UserSubscription`·피드)을 **조합**해 프론트 메인 화면에서 바로 쓸 수 있는 **단일 Dashboard API**를 제공한다.

## 2. 이번 단계에서 해결할 문제

- 메인 진입 시 여러 API를 순차 호출하는 비용을 줄이고, 카드 UI에 맞게 **가공된 DTO**로 한 번에 내려준다.

## 3. 설계 대상

- `GET /api/v1/dashboard` — CRUD 없음, 읽기 전용.
- 섹션: `todayPick`, `continueWatching`, `recentNotes`, `incompleteVideos`, `favoriteChannelUpdates`, `weeklySummary`.
- 설정: `learningtube.dashboard.*` 로 섹션별 최대 건수 조정.

## 4. 주요 결정 사항

- **todayPick**: Queue 없음 → 우선순위 + 미완료(`NOT_STARTED`/`IN_PROGRESS`) 후보 풀에서 메모리 정렬 후 상위 N건(임시 추천).
- **weeklySummary**: UTC **월요일 00:00** 기준 주간; `inProgressCount`는 해당 주에 `updatedAt`이 갱신된 `IN_PROGRESS` 스냅샷, 완료/노트/하이라이트는 `created`/`completedAt` 기준.
- Entity 비노출, Repository에 **단순 JPQL** 위주(추후 projection·캐시 확장 여지).

## 5. 생성/수정 예정 파일

- `domain/dashboard/*` — Controller, Service, Properties, Mapper, `dto/*`.
- Repository 메서드 보강: `UserVideoRepository`, `NoteRepository`, `HighlightRepository`, `UserSubscriptionRepository`.
- `LearningTubeApplication`, `application.yml`, `backend-api-spec.md`, `backend-step14-*`.

## 6. 구현 범위

- Swagger·로깅(`userId`, 섹션별 size·주간 카운트 일부).

## 7. 제외 범위

- Analytics 도메인·실시간 집계·Watch History 전용 API.

## 8. 다음 단계 연결 포인트

- **Queue / TodayPick** 전용 도메인으로 교체 시 `DashboardService`의 todayPick 분기만 스왑.
- **Analytics**: 주간 지표를 이벤트 스트림·배치 테이블로 이전하면 `weeklySummary` 소스만 변경.
