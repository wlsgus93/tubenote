# STEP 13 계획 — 구독 채널 최신 업로드 동기화·피드 조회

## 1. 단계 목표

- 구독 채널별 **최근 업로드 영상**을 YouTube API(또는 스텁)로 수집하고, 공용 `Video` 메타를 upsert 하며, 사용자별 **피드**를 DB에 유지한다.
- **조회 API는 DB만** 사용하고, **동기화 API만** 외부를 호출한다.
- **UserVideo 자동 생성은 하지 않는다** — “새 영상”은 UserVideo 부재로 판별한다.

## 2. 이번 단계에서 해결할 문제

- 구독만으로는 “채널에 무엇이 올라왔는지”가 보이지 않으므로, 최근 업로드 스냅샷과 통합 조회가 필요하다.
- 할당량을 고려해 **채널당 N건(10~20)** 만 수집한다.

## 3. 설계 대상

- `Video.published_at` — 정렬·응답용.
- `UserSubscription.unread_new_video_count`, `last_channel_videos_synced_at`.
- `SubscriptionRecentVideo` — 피드 매핑(`user_subscription_id`, `video_id` 유니크).
- Infra: `YoutubeChannelUploadsPort` — `channels.list`(uploads playlist id) + `playlistItems.list`.
- 애플리케이션: `SubscriptionChannelUpdatesService`, `SubscriptionChannelUpdatesSyncItemService`(채널 단위 `REQUIRES_NEW`).

## 4. 주요 결정 사항

- 피드는 **동기화 결과 집합으로 덮어쓰기**(최근 N건 외 피드 행 삭제).
- `isNew` / `unread` = **동일 사용자·동일 Video에 UserVideo 없음**.
- 채널 단위 실패는 `failedChannels` 로 집계, 가능한 한 나머지 채널은 계속 처리.

## 5. 생성/수정 예정 파일

- `domain/subscription/SubscriptionRecentVideo*`, `SubscriptionChannelUpdates*`, DTO, `Video`, `UserSubscription`.
- `infra/youtube/YoutubeChannelUploads*`, `YoutubeApiProperties`, `application.yml`.
- 문서: `backend-step13-*`, `backend-entities.md`, `backend-api-spec.md`, `backend-async-spec.md`.

## 6. 구현 범위

- `POST /api/v1/subscriptions/channel-updates/sync`
- `GET /api/v1/subscriptions/recent-videos`
- `GET /api/v1/subscriptions/{subscriptionId}/recent-videos`

## 7. 제외 범위

- Watch Later, 자막, 추천, 대용량 배치 최적화.

## 8. 다음 단계 연결 포인트

- 대시보드·analytics: 구독별 `unreadNewVideoCount` 합계, 피드→임포트 전환율.
- `@Scheduled` / `sync_jobs` 로 동기화 비동기화(`backend-async-spec.md` §1.3).
