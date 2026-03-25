# STEP 12 계획 — Channel / UserSubscription · YouTube 구독 동기화

## 1. 단계 목표

- 공용 **Channel** 과 사용자별 **UserSubscription** 을 구현하고, **YouTube 구독 목록 동기화**를 실사용 가능한 수준으로 연결한다.
- 조회 API는 **내부 DB만** 사용하고, **동기화 API만** 외부 YouTube Data API(또는 스텁)를 호출한다.

## 2. 이번 단계에서 해결할 문제

- 사용자가 구독한 채널을 플랫폼에 반영하고, 카테고리·즐겨찾기·학습 채널·메모로 관리할 수 있게 한다.
- Google OAuth로 저장된 **액세스 토큰**과 YouTube API 클라이언트를 **infra 계층**으로 분리한다.
- 동기화 시 **항목 단위 트랜잭션**으로 일부 실패가 전체를 망가뜨리지 않게 한다.

## 3. 설계 대상

- 엔티티: `Channel`, `UserSubscription`(기존 후보 UserChannel 명칭 정리).
- 저장소: `ChannelRepository`, `UserSubscriptionRepository`.
- 애플리케이션: `SubscriptionService`, `SubscriptionSyncItemService`, `SubscriptionController`, DTO·Mapper.
- 인프라: `YoutubeSubscriptionsPort`, `YoutubeSubscriptionsStubAdapter`, `YoutubeSubscriptionsRestAdapter`, `YoutubeApiProperties`.

## 4. 주요 결정 사항

- `(user_id, channel_id)` **UNIQUE** — 중복 구독 행 방지.
- `learningtube.youtube.stub` 기본 **true** — CI·로컬에서 토큰 없이 동작.
- `stub=false` 일 때 `UserOAuthAccount`(GOOGLE)의 **access_token** 필수; 없으면 `YOUTUBE_ACCESS_TOKEN_MISSING`.
- **채널 최신 영상 전량 동기화**는 이번 단계 **제외**(주석·문서에 확장 포인트만 명시).

## 5. 생성/수정 예정 파일

- `domain/subscription/**`, `domain/channel/**`(이미 존재 시 보강), `infra/youtube/**`, `global/error/ErrorCode`, `GlobalExceptionHandler`, `LearningTubeApplication`, `application.yml`.
- 문서: `backend-step12-plan.md`, `backend-step12-result.md`, `backend-entities.md`, `backend-api-spec.md`, `backend-auth-spec.md`.

## 6. 구현 범위

- API: `POST /api/v1/subscriptions/sync`, `GET /api/v1/subscriptions`, `GET .../{id}`, `PATCH .../{id}`.
- 동기화 응답: `syncedCount`, `createdCount`, `updatedCount`, `failedCount`.
- Swagger·로깅·공통 예외·소유권(JWT sub) 검증.

## 7. 제외 범위

- 채널 **recent videos** 배치/전량 동기화.
- OAuth Refresh 자동 갱신 파이프라인(문서에 후속 과제로만 기술).

## 8. 다음 단계 연결 포인트

- `SubscriptionSyncItemService.merge` 이후 또는 별도 `ChannelRecentVideosSyncJob` 에서 `search.list` / `playlistItems` 등으로 확장.
- `SyncJob` 엔티티 도입 시 동기화를 비동기 큐로 이전.
