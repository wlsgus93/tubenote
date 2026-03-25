# STEP 16 계획 — Learning Queue (오늘/주간/백로그)

## 1. 단계 목표

- 사용자가 저장한 **UserVideo**를 **TODAY / WEEKLY / BACKLOG** 큐로 나누고, **순서(position)** 까지 CRUD·재정렬할 수 있는 API를 제공한다.

## 2. 이번 단계에서 해결할 문제

- 대시보드 `todayPick`이 임시 휴리스틱에만 의존하는 상태에서, **실제 “오늘 큐” 데이터 소스**로 전환할 수 있는 토대를 만든다.
- 큐와 컬렉션·UserVideo 간 **중복·소유권·정렬** 규칙을 코드와 문서로 고정한다.

## 3. 설계 대상

- 엔티티 **`LearningQueueItem`**: `user_id`, `user_video_id`, `queue_type`, `sort_order`(API: `position`), 감사 필드.
- **유니크 `(user_id, user_video_id)`**: 한 UserVideo는 큐 전체에서 **한 번만**(초기 정책 — 여러 `queue_type` 동시 소속 불가).
- **정렬**: 타입별 `position` **0..n-1 연속** — 추가·삭제·타입 이동·reorder 후 compact.
- Base path: **`/api/v1/queue`** (프로젝트 규칙).

## 4. 주요 결정 사항

- **Owner check**: `findByIdAndUser_Id` 등으로 일원화; 타인 리소스는 **404** (`QUEUE_ITEM_NOT_FOUND`).
- **POST 중복**: 선행 `exists` + **409** `QUEUE_USER_VIDEO_ALREADY_IN_QUEUE`; 동시성은 UK + `DataIntegrityViolationException` 매핑.
- **PATCH `queueType` 변경**: 이전 버킷 compact → 새 버킷에 삽입( `position` 없으면 맨 뒤).
- **PATCH `/reorder`**: 컬렉션 `videos/order`와 동일하게 **ordered id 집합 = 현재 멤버 집합**.
- **로깅**: `userId`, `queueItemId`, `userVideoId`, `queueType` — 토큰·비밀번호 금지.

## 5. 생성/수정 예정 파일

- `domain/queue/*` — Entity, Enum, Repository, Service, Controller, DTO, Mapper
- `ErrorCode`, `GlobalExceptionHandler`
- `docs/backend-step16-plan.md`, `docs/backend-step16-result.md`, `backend-entities.md`, `backend-api-spec.md`, `backend-db-spec.md`

## 6. 구현 범위

- Swagger(`Queue` 태그), 공통 응답·예외 연동.

## 7. 제외 범위

- 플래너급 **scheduledDate·메모·완료 상태** 컬럼/API (확장 여지만 문서·엔티티 주석).
- Dashboard `todayPick`을 큐로 **실제 교체**하는 코드(연계 포인트만 문서화).

## 8. 다음 단계 연결 포인트

- **`DashboardService`**: `todayPick`을 `queueType=TODAY` 목록(또는 비었을 때만 폴백)으로 채우기.
- **Analytics `summary`**: “오늘 큐 길이” 등 지표 추가 여부 검토.
