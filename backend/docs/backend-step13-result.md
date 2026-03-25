# STEP 13 결과 — 구독 채널 최신 업로드 동기화·피드

## 1. 구현 완료 항목

- **YouTube**: `YoutubeChannelUploadsPort` + Stub / Rest(`channels` → uploads playlist → `playlistItems`).
- **도메인**: `SubscriptionRecentVideo`, `Video.publishedAt` + upsert용 setter, `UserSubscription` 의 unread·채널 동기화 시각.
- **서비스**: `SubscriptionChannelUpdatesService`(전체 오케스트레이션), `SubscriptionChannelUpdatesSyncItemService`(채널별 `REQUIRES_NEW`).
- **API**: sync + 전체/단일 구독 `recent-videos` 조회, `SubscriptionResponse` 확장.
- **설정**: `learningtube.youtube.channel-updates-max-videos-per-channel`(기본 15).

## 2. 생성/수정한 파일 목록 (요약)

| 구분 | 경로 |
|------|------|
| 엔티티·리포 | `SubscriptionRecentVideo.java`, `SubscriptionRecentVideoRepository.java`, `Video.java`, `UserSubscription.java`, `UserSubscriptionRepository.java` |
| 서비스 | `SubscriptionChannelUpdatesService.java`, `SubscriptionChannelUpdatesSyncItemService.java` |
| DTO | `ChannelUpdatesSyncResponse`, `SubscriptionRecentVideoResponse`, `SubscriptionResponse` 보강 |
| 매퍼 | `SubscriptionRecentVideoMapper.java`, `SubscriptionDtoMapper.java` |
| HTTP | `SubscriptionController.java` |
| Infra | `YoutubeUploadVideoItem`, `YoutubeChannelUploadsPort`, `*StubAdapter`, `*RestAdapter`, `YoutubeApiProperties` |
| 설정 | `application.yml` |
| 문서 | `backend-step13-*.md`, `backend-entities.md`, `backend-api-spec.md`, `backend-async-spec.md` |

## 3. 핵심 클래스/구조 설명

- **`SubscriptionChannelUpdatesSyncItemService`**: 한 구독(채널)에 대해 API 호출 → `Video` upsert → 피드 행 정리·저장 → `unread_new_video_count` 재계산.
- **`SubscriptionChannelUpdatesService`**: 내 구독 전체 루프, 실패 채널 집계, 피드 목록은 JPA Page + `UserVideo` 존재 여부로 `isNew` 계산.

## 4. 반영된 설계 원칙

- 외부 연동은 infra, 조회는 DB, UserVideo 자동 생성 없음, Entity 비노출.

## 5. Swagger

- Subscriptions 태그에 sync·피드 엔드포인트 및 DTO 필드 설명 추가.

## 6. 로깅

- 요약: `userId`, `processedChannels`, `createdVideos`, `updatedVideos`, `failedChannels`.
- 채널 단위 DEBUG: `subscriptionId`, `channelId`, 피드 건수·unread.

## 7. 아쉬운 점 / 개선 포인트

- 영상 **길이(duration)** 는 playlist 단계에서 오지 않아 null 유지 — 필요 시 `videos.list` 배치 호출.
- 피드 **읽음 처리** 전용 API는 없음(정책은 UserVideo 추가 시 자연 감소).

## 8. 다음 단계 TODO

- Analytics/Dashboard: 사용자·기간별 신규 피드 수, 임포트 전환.
- 스케줄러/메시지로 `channel-updates/sync` 분리, 쿼터 백오프.
