# 백엔드 엔티티 명세

> STEP 2 기준 초안. DDL·컬럼 길이·DB 벤더별 타입은 `backend-db-spec.md`(STEP 3)에서 확정한다.  
> **원칙**: HTTP에서 **Entity 직접 노출 금지** — 아래 “API 노출”은 **응답 DTO에 넣을 수 있는지** 수준의 가이드다.

---

## 0. 데이터 소유 구분 (요약)

| 구분 | 의미 | 엔티티 예 |
|------|------|-----------|
| **USER_SCOPED** | 특정 사용자 소유·권한 검증 필수 | User, RefreshToken, UserChannel, Collection, CollectionVideo(간접), Note, Highlight, LearningQueueItem, UserVideoProgress, SyncJob |
| **SHARED_REFERENCE** | 플랫폼 전역 1행(또는 언어별)·YouTube ID 유니크 | Channel, Video, TranscriptTrack, TranscriptSegment |
| **SECURITY** | 인증·세션·토큰 | RefreshToken |

---

## 1. 엔티티 후보 목록

| 엔티티명 | 목적(한 줄) | 소유 구분 |
|----------|-------------|-----------|
| User | 로컬 계정·프로필 | USER_SCOPED |
| RefreshToken | JWT Refresh 저장(해시) | SECURITY |
| Channel | YouTube 채널 메타데이터 캐시 | SHARED_REFERENCE |
| UserChannel / **UserSubscription** | 사용자가 등록/구독한 채널 연결 — **STEP 12 구현 엔티티명 `UserSubscription`** | USER_SCOPED |
| Video | YouTube 영상 메타데이터 캐시 | SHARED_REFERENCE |
| Collection | 학습용 폴더(재생목록) | USER_SCOPED |
| CollectionVideo | 컬렉션-영상 소속·순서 | USER_SCOPED(간접) |
| Note | 영상에 대한 사용자 메모 | USER_SCOPED |
| Highlight | 구간 강조 | USER_SCOPED |
| TranscriptTrack | 공용 Video당 자막 트랙(언어·자동/수동) | SHARED_REFERENCE |
| TranscriptSegment | 트랙 내 시간순 자막 큐 | SHARED_REFERENCE |
| LearningQueueItem | 오늘/주간/백로그 학습 큐(UserVideo당 1행); 구 명칭 WatchQueueItem | USER_SCOPED |
| UserVideoProgress | 시청 진행률·이어보기 | USER_SCOPED |
| SyncJob | 동기화/수집 작업 단위 | USER_SCOPED |

**후속 검토 후보(본 명세에 필수 필드 미기재)**: `UserYoutubeCredential` — YouTube Data API OAuth 토큰 암호화 저장, 별도 보안 규정 필요 시 도입.

---

## 2. 공통 규칙 (모든 엔티티)

- **PK**: `id` — `BIGINT` 자동 증가(또는 UUID 전략은 STEP 3에서 결정).
- **감사**: `created_at`, `updated_at` — `TIMESTAMP`, `updated_at`은 수정 시 갱신.
- **타임존**: UTC 저장 권장.
- **API**: 응답은 전용 DTO; 아래 “API 노출”은 DTO 설계 가이드.
- **Swagger**: DTO 필드에 `@Schema` 적용; Entity는 OpenAPI에 직접 등록하지 않음.

---

## 3. 엔티티별 상세

### 3.1 User

- **목적**: 이메일/비밀번호 기반 로컬 사용자. JWT subject·소유권 검증의 기준.
- **연관관계**: RefreshToken(1:N), UserChannel(1:N), Collection(1:N), Note(1:N), Highlight(1:N), LearningQueueItem(1:N), UserVideoProgress(1:N), SyncJob(1:N).
- **생성/수정 정책**: 가입 시 생성; 프로필·비밀번호 변경 시 `updated_at` 갱신.
- **soft delete 여부**: **권장(Y)** — `deleted_at` NULL이면 활성. 로그인·조회 시 활성만 기본 조회.
- **인덱스 여부**: `email` **UNIQUE**(활성 계정만 유니크할 경우 partial unique는 STEP 3에서 DB별 정의).
- **unique 여부**: 논리적 `email` 유일(soft delete 시 정책: 동일 이메일 재가입 허용 여부는 비즈니스 규칙으로 결정).

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | (PK) | 식별자 |
| email | VARCHAR(320) | N | - | 로그인 ID |
| password_hash | VARCHAR(255) | N | - | BCrypt 등 |
| nickname | VARCHAR(100) | N | - | 표시명 |
| role | VARCHAR(32) | N | MEMBER | MEMBER, ADMIN 등 (enum 고정값은 STEP 3) |
| created_at | TIMESTAMP | N | now() | 생성 시각 |
| updated_at | TIMESTAMP | N | now() | 수정 시각 |
| deleted_at | TIMESTAMP | Y | NULL | soft delete |

- **API 노출**: `id`, `nickname`, `email`(마스킹 가능), `created_at` 정도만 DTO 허용. **`password_hash` 절대 비노출**.
- **Swagger 설명 필요 항목**: DTO 기준 — `email` 형식, `nickname` 길이.
- **로깅 시 마스킹 필요 여부**: `password_hash` **로그 금지**; `email`은 **부분 마스킹** 권장.

---

### 3.2 RefreshToken

- **목적**: Refresh JWT 또는 Opaque 토큰의 **해시** 저장·폐기·로테이션.
- **연관관계**: User(N:1).
- **생성/수정 정책**: 로그인·재발급 시 행 추가 또는 토큰 버전 갱신; 로그아웃·재설정 시 `revoked_at` 설정.
- **soft delete 여부**: **N** — 폐기는 `revoked_at`로 관리.
- **인덱스 여부**: `(user_id, revoked_at)`, 조회 최적화용 `(token_hash)` UNIQUE(해시 길이 고정 시).
- **unique 여부**: `token_hash` UNIQUE(동시에 하나의 활성 해시만 유효하게 설계 가능).

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | (PK) | 식별자 |
| user_id | BIGINT | N | - | FK → User |
| token_hash | VARCHAR(128) | N | - | 원문 토큰 저장 금지 |
| expires_at | TIMESTAMP | N | - | 만료 |
| revoked_at | TIMESTAMP | Y | NULL | 폐기 시각 |
| created_at | TIMESTAMP | N | now() | 발급 시각 |
| user_agent | VARCHAR(512) | Y | NULL | 선택, 디바이스 구분 |
| ip_address | VARCHAR(45) | Y | NULL | 선택 |

- **API 노출**: **전부 비노출**(관리 API 제외 시 별도 보안 검토).
- **Swagger**: 일반 사용자 API 스키마에 포함하지 않음.
- **로깅**: `token_hash`·토큰 원문 **절대 금지**; `ip_address`는 정책에 따라 마스킹.

---

### 3.3 Channel

- **목적**: YouTube `channelId` 기준 **전역 메타데이터 캐시**. API 할당량 절약·목록 응답 일관성.
- **연관관계**: UserChannel(1:N), Video(1:N).
- **생성/수정 정책**: 최초 수집 시 insert; 주기적 동기화로 `title`, `thumbnail_url` 등 갱신.
- **soft delete 여부**: **N(기본)** — YouTube 삭제 시 `availability` 등 상태 컬럼으로 표현(선택). 필요 시 `deleted_at` 추가 검토.
- **인덱스 여부**: `youtube_channel_id` **UNIQUE**; 동기화 조회 `(last_synced_at)`.
- **unique 여부**: `youtube_channel_id` UNIQUE.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | (PK) | 내부 PK |
| youtube_channel_id | VARCHAR(64) | N | - | YouTube 채널 ID |
| title | VARCHAR(500) | N | - | 캐시 제목 (**STEP 12 구현: 500**) |
| description | TEXT | Y | NULL | 선택 |
| thumbnail_url | VARCHAR(2048) | Y | NULL | 썸네일 URL |
| custom_url | VARCHAR(255) | Y | NULL | **STEP 12 컬럼 추가** |
| subscriber_count | BIGINT | Y | NULL | 스냅샷 (**미구현**, nullable 유지) |
| last_synced_at | TIMESTAMP | Y | NULL | 마지막 메타 동기화 |
| created_at | TIMESTAMP | N | now() | |
| updated_at | TIMESTAMP | N | now() | |

- **API 노출**: DTO로 `youtube_channel_id`, `title`, `thumbnail_url` 등 **메타만**. 내부 PK는 클라이언트에 필수 아니면 비노출 가능.
- **Swagger**: 채널 응답 DTO에 설명.
- **로깅**: 일반 필드 OK; 외부 URL 과다 출력은 DEBUG 제한.

---

### 3.4 UserChannel

- **목적**: 사용자가 **자신의 학습 공간에 등록한 채널** 연결. 동일 Channel을 여러 사용자가 참조.
- **연관관계**: User(N:1), Channel(N:1).
- **생성/수정 정책**: 등록 시 생성; 별칭 변경 시 `updated_at`.
- **soft delete 여부**: **선택** — 등록 취소를 물리 삭제로 할지 soft delete로 할지 정책 결정. **권장: 물리 삭제 또는 `unfollowed_at`**.
- **인덱스 여부**: `(user_id, channel_id)` **UNIQUE**; 목록 `(user_id, created_at DESC)`.
- **unique 여부**: `(user_id, channel_id)` UNIQUE.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | (PK) | |
| user_id | BIGINT | N | - | FK → User |
| channel_id | BIGINT | N | - | FK → Channel |
| display_name_override | VARCHAR(255) | Y | NULL | 사용자 지정 별칭 |
| created_at | TIMESTAMP | N | now() | 등록 시각 |
| updated_at | TIMESTAMP | N | now() | |

- **API 노출**: 채널 메타 + `display_name_override`, 등록일.
- **Swagger**: “등록 채널” 응답에 별칭 필드 설명.
- **로깅**: user_id·channel_id는 추적용으로 허용; 민감도 낮음.

---

### 3.4.1 UserSubscription (STEP 12 구현)

> 명세 초안의 **UserChannel**과 동일 역할. 구현·테이블명은 **`user_subscriptions`**.

- **목적**: 사용자별 **YouTube 구독 채널** 연결 및 학습용 메타(카테고리·즐겨찾기·메모). 공용 `Channel` 을 참조.
- **연관관계**: User(N:1), Channel(N:1).
- **생성/수정 정책**: `POST /api/v1/subscriptions/sync` 로 YouTube 목록 반영 시 upsert; 사용자 필드는 PATCH로만 변경.
- **soft delete 여부**: **N(MVP)** — 물리 행 유지.
- **인덱스·unique**: `uk_user_subscriptions_user_channel` (`user_id`, `channel_id`); `idx_user_subscriptions_user_updated` (`user_id`, `updated_at`).
- **API 노출**: Entity 직접 금지 — `SubscriptionResponse`·`ChannelSummaryResponse` 사용.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | |
| user_id | BIGINT | N | - | FK → User |
| channel_id | BIGINT | N | - | FK → Channel |
| youtube_subscription_id | VARCHAR(128) | Y | NULL | YouTube `subscriptions.list` 항목 id |
| category | VARCHAR(100) | Y | NULL | 사용자 지정 |
| is_favorite | BOOLEAN | N | false | |
| is_learning_channel | BOOLEAN | N | false | |
| note | TEXT | Y | NULL | |
| last_synced_at | TIMESTAMP | Y | NULL | 마지막 YouTube 구독 동기화 반영 시각 |
| unread_new_video_count | INT | N | 0 | 피드에 있으나 UserVideo 미등록인 영상 수(채널 업로드 sync 후 재계산) |
| last_channel_videos_synced_at | TIMESTAMP | Y | NULL | 마지막 채널 최신 업로드 피드 동기화 시각 |
| created_at | TIMESTAMP | N | now() | |
| updated_at | TIMESTAMP | N | now() | |

- **Swagger**: PATCH 요청에 `isFavorite` / `isLearningChannel`(JSON camelCase). 목록/단건 응답에 `unreadNewVideoCount`, `lastChannelVideosSyncedAt`.
- **로깅**: `userId`, `subscriptionId`, 내부 `channelId`, 동기화 카운트 허용; **토큰·PII 과다 금지**.

---

### 3.4.2 SubscriptionRecentVideo (STEP 13)

- **목적**: 구독 채널별 **최근 업로드 피드** 행. `UserVideo` 자동 생성 없이 공용 `Video` 와 연결.
- **연관관계**: `UserSubscription`(N:1), `Video`(N:1).
- **unique**: (`user_subscription_id`, `video_id`) — `uk_subscription_recent_sub_video`.
- **정책**: sync 시 API에서 가져온 최근 N건(설정값)만 피드에 유지; 그 외 피드 행은 삭제. 메타는 `Video` upsert로 반영.

| 필드명 | 타입 | nullable | 설명 |
|--------|------|----------|------|
| id | BIGINT | N | PK |
| user_subscription_id | BIGINT | N | FK |
| video_id | BIGINT | N | FK |
| feed_synced_at | TIMESTAMP | N | 마지막으로 피드에 반영된 시각 |
| created_at / updated_at | TIMESTAMP | N | 감사 |

- **API**: `SubscriptionRecentVideoResponse` — `isNew` 는 동일 사용자에 `UserVideo` 없으면 true.
- **로깅**: userId·subscriptionId·channelId·집계 수치; 토큰 금지.

---

### 3.5 Video

- **목적**: YouTube `videoId` 기준 **전역 메타데이터 캐시**.
- **연관관계**: Channel(N:1), CollectionVideo(1:N), Note(1:N), Highlight(1:N), LearningQueueItem(1:N), UserVideoProgress(1:N), TranscriptTrack(1:N).
- **생성/수정 정책**: 최초 등록·동기화 시 upsert 성격; 메타 필드 갱신.
- **soft delete 여부**: **N(기본)** — `availability` VARCHAR/ENUM로 REMOVED 등 표현 권장.
- **인덱스 여부**: `youtube_video_id` **UNIQUE**; `(channel_id, published_at DESC)`; 제목 검색 시 DB full-text는 STEP 3·별도 검색 엔진 검토.
- **unique 여부**: `youtube_video_id` UNIQUE.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | (PK) | |
| channel_id | BIGINT | N | - | FK → Channel |
| youtube_video_id | VARCHAR(32) | N | - | YouTube video ID |
| title | VARCHAR(500) | N | - | |
| description | TEXT | Y | NULL | 선택 |
| duration_seconds | INT | Y | NULL | 재생 길이(초) |
| thumbnail_url | VARCHAR(2048) | Y | NULL | |
| published_at | TIMESTAMP | Y | NULL | YouTube 공개 시각 |
| availability | VARCHAR(32) | N | AVAILABLE | AVAILABLE, PRIVATE, REMOVED 등 |
| last_synced_at | TIMESTAMP | Y | NULL | |
| created_at | TIMESTAMP | N | now() | |
| updated_at | TIMESTAMP | N | now() | |

- **API 노출**: 메타 필드 위주; `description`은 길이 큼 → 요약 DTO 분리 검토.
- **Swagger**: 영상 상세·목록 DTO 구분.
- **로깅**: 일반 OK.

> **구현 참고**: MVP `Video` 는 `channel_title`·`channel_youtube_id`·`source_type` 를 사용한다. **STEP 13**에서 `published_at` 컬럼을 추가해 피드 정렬·동기화에 사용한다. `channel_id`·`availability`·`last_synced_at` 는 장기 스키마와 병합 시 검토.

---

### 3.5.1 UserVideo (MVP 구현)

- **목적**: 특정 사용자의 **학습 상태·진행·핀·아카이브**를 `Video`와 분리해 보관한다.
- **소유 구분**: **USER_SCOPED** (API에서 JWT `sub`와 `user_id` 일치 필수).
- **연관관계**: `User`(N:1), `Video`(N:1), `LearningQueueItem`(0..1, 동일 사용자당 `user_video_id` 유니크로 최대 1행).
- **unique**: `(user_id, video_id)` — 동일 영상 중복 등록 방지.
- **인덱스**: `(user_id, updated_at)`, `(user_id, archived)`, `(user_id, learning_status)` (엔티티 `@Index`와 동일).
- **soft delete**: **N** (삭제 API는 후속 단계).

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | UserVideo 식별자 (**API의 userVideoId**) |
| user_id | BIGINT | N | FK | 사용자 |
| video_id | BIGINT | N | FK | 공용 Video |
| learning_status | VARCHAR(32) | N | NOT_STARTED | LearningStatus enum |
| priority | VARCHAR(32) | N | NORMAL | Priority enum |
| last_position_sec | INT | N | 0 | 이어보기 위치(초) |
| watch_percent | INT | Y | NULL | 0–100 |
| pinned | BOOLEAN | N | false | 핀 고정 |
| archived | BOOLEAN | N | false | 보관함 |
| completed_at | TIMESTAMP | Y | NULL | `learningStatus=COMPLETED`로 **최초** 전환된 시각; 다시 미완료 등으로 바꾸면 **NULL** |
| created_at | TIMESTAMP | N | 감사 | |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: 전용 DTO(`UserVideoSummaryResponse`, `UserVideoDetailResponse` 등). Entity 직접 반환 금지.
- **Swagger**: enum 값·필터·정렬 파라미터 설명.
- **로깅**: `user_id`, `user_video_id`, `youtube_video_id` 수준 허용; 비밀번호·토큰 금지.

---

### 3.6 Collection

- **목적**: 사용자 학습 **컬렉션(폴더/플레이리스트)**.
- **STEP 11 MVP**: User 소유; **soft delete 미적용**(DELETE 시 물리 삭제 + 하위 CollectionVideo cascade).
- **이름 정책**: 저장 시 **trim**; **동일 user_id 내 `LOWER(TRIM(name))` 유일**(Spring/spring/SPRING 중복 불가). DB 유니크 인덱스는 JPQL 검증으로 대체(마이그레이션 시 함수 인덱스 권장).
- **연관관계**: User(N:1), CollectionVideo(1:N).
- **인덱스**: `(user_id, sort_order)`, `(user_id, updated_at)`.
- **대표 썸네일**: `cover_thumbnail_url` — 수동/배치; 상세 API는 하위 영상에서 최대 3개 `previewThumbnailUrls` 조합.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | |
| user_id | BIGINT | N | - | FK → User |
| name | VARCHAR(200) | N | - | |
| description | VARCHAR(2000) | Y | NULL | |
| visibility | VARCHAR(32) | N | PRIVATE | PRIVATE, PUBLIC |
| sort_order | INT | N | 0 | 사용자 컬렉션 탭 정렬 |
| cover_thumbnail_url | VARCHAR(2048) | Y | NULL | 대표 썸네일 |
| created_at | TIMESTAMP | N | 감사 | |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: DTO + 목록 `videoCount`, 상세 `previewThumbnailUrls`.
- **로깅**: `collectionId`, `userId` 허용.

---

### 3.7 CollectionVideo

- **목적**: 컬렉션에 포함된 **UserVideo** 및 **표시 순서**.
- **STEP 11 MVP**: **UserVideo** FK(동일 사용자 소유 검증은 서비스). Video 직접 FK 아님.
- **unique**: `(collection_id, user_video_id)`.
- **인덱스**: `(collection_id, sort_order)`.
- **삭제**: 컬렉션에서 제거 시 행 삭제; 컬렉션 삭제 시 **cascade**로 일괄 삭제.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | |
| collection_id | BIGINT | N | - | FK → Collection |
| user_video_id | BIGINT | N | - | FK → UserVideo |
| sort_order | INT | N | 0 | 컬렉션 내 순서 |
| created_at | TIMESTAMP | N | 감사 | |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: UserVideo·Video 요약 DTO + `sortOrder`.
- **Swagger**: 순서 변경 API는 멤버 집합 일치 조건 명시.

---

### 3.8 Note

- **목적(장기)**: 영상에 대한 사용자 메모. (초기 명세는 User+Video 직접 FK 기준이었음.)
- **STEP 10 MVP 구현**: **UserVideo(N:1)** 에 종속 — 소유권은 `UserVideo.user_id` 로 일원화.
- **생성/수정 정책**: JPA Auditing `created_at` / `updated_at`.
- **soft delete**: MVP는 **물리 삭제**(DELETE API).
- **인덱스**: `(user_video_id, updated_at)`, `(user_video_id, review_target)`.
- **NoteType**: `GENERAL`(일반), `TIMESTAMP`(`position_sec` 필수, 재생 시각 기준 메모).

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | |
| user_video_id | BIGINT | N | - | FK → UserVideo |
| note_type | VARCHAR(32) | N | - | GENERAL, TIMESTAMP |
| body | TEXT | N | - | 본문 |
| position_sec | INT | Y | NULL | TIMESTAMP일 때만(초) |
| review_target | BOOLEAN | N | false | 복습 대상 |
| pinned | BOOLEAN | N | false | 핀 |
| created_at | TIMESTAMP | N | 감사 | |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: DTO(`NoteResponse` 등). `body` 직접 로그 금지·길이 제한 권장.
- **Swagger**: 타입별 `position_sec` 규칙.

---

### 3.9 Highlight

- **목적**: 재생 구간 **하이라이트**.
- **STEP 10 MVP**: **UserVideo(N:1)**. `color` 등 UI 확장 필드는 미도입.
- **soft delete**: MVP **물리 삭제**.
- **인덱스**: `(user_video_id, updated_at)`, `(user_video_id, review_target)`.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | |
| user_video_id | BIGINT | N | - | FK → UserVideo |
| start_sec | INT | N | - | 시작(초) |
| end_sec | INT | N | - | 종료(초), ≥ start |
| memo | TEXT | Y | NULL | 구간 메모 |
| review_target | BOOLEAN | N | false | 복습 대상 |
| pinned | BOOLEAN | N | false | 핀 |
| created_at | TIMESTAMP | N | 감사 | |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: 구간·메모·플래그 DTO.
- **Swagger**: `end_sec` ≤ 영상 길이(알려진 경우).
- **로깅**: `memo`는 본문 취급 — 과다 출력 지양.

---

### 3.10 TranscriptTrack (STEP 17)

- **목적**: 공용 **Video**당 **언어·자동생성 여부**별 자막 **트랙** 메타. 세그먼트 본문은 `TranscriptSegment`에 분리.
- **연관관계**: Video(N:1), TranscriptSegment(1:N).
- **정책**: `(video_id, language_code, is_auto_generated)` **UNIQUE**. 동일 Video에서 **선택(`selected`) 트랙은 서비스 레벨에서 최대 1개**(동기화·선택 API에서 정리).
- **soft delete 여부**: **N**.
- **인덱스**: `(video_id, selected)` 등.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | |
| video_id | BIGINT | N | - | FK → Video |
| language_code | VARCHAR(16) | N | - | BCP47 유사(ko, en…) |
| is_auto_generated | BOOLEAN | N | false | YouTube 자동 자막 등 |
| source | VARCHAR(32) | N | YOUTUBE_AUTO | TranscriptSourceType |
| selected | BOOLEAN | N | false | UI 표시용 활성 트랙 |
| created_at | TIMESTAMP | N | 감사 | |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: `TranscriptTrackSummaryResponse` 등 — Entity 비노출.
- **로깅**: `videoId`, `trackId`, `languageCode`, `segmentCount` 허용; **세그먼트 본문 대량 로그 금지**.

### 3.10.1 TranscriptSegment (STEP 17)

- **목적**: 트랙 내 **시간순 자막 한 줄**. 향후 전문 검색·인덱스는 별도 마이그레이션.
- **연관관계**: TranscriptTrack(N:1).
- **unique**: `(transcript_track_id, line_index)`.
- **인덱스**: `(transcript_track_id, start_seconds)`.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | |
| transcript_track_id | BIGINT | N | - | FK → TranscriptTrack |
| line_index | INT | N | 0 | 트랙 내 순번 |
| start_seconds | DOUBLE | N | - | 시작(초) |
| end_seconds | DOUBLE | N | - | 종료(초), ≥ start |
| text | TEXT | N | - | 한 큐 텍스트 |
| created_at | TIMESTAMP | N | 감사 | |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: `TranscriptSegmentResponse` — 목록/뷰에서만.
- **로깅**: 본문 전체 출력 금지 권장.

> **구 명세 단일 `Transcript`/`content` 행**: STEP 17에서 **트랙+세그먼트**로 대체. 구 `transcripts` DDL은 `backend-db-spec.md`에 초안으로 잔존.

---

### 3.11 LearningQueueItem (STEP 16 구현)

- **목적**: 사용자가 저장한 **UserVideo**를 **오늘(TODAY) / 주간(WEEKLY) / 백로그(BACKLOG)** 큐로 나누어 순서까지 관리한다.
- **연관관계**: `User`(N:1), `UserVideo`(N:1).
- **정책**: 동일 `(user_id, user_video_id)` **UNIQUE** — 한 `UserVideo`는 큐 전체에서 **최대 1행**(한 시점에 하나의 `queue_type`만). 동일 `(user_id, queue_type)` 내 `sort_order`는 **0부터 연속**.
- **생성/수정 정책**: 추가·삭제·타입 이동·단건 position 변경·`reorder` 후 서비스가 필요 시 compact.
- **soft delete 여부**: **N** — 제거 시 물리 삭제.
- **인덱스**: `(user_id, queue_type, sort_order)`.
- **확장 후보(미구현 컬럼)**: `scheduled_date`, 큐 메모, 큐 전용 완료 플래그 등 — nullable 컬럼 추가로 확장 가능.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | PK | API `queueItemId` |
| user_id | BIGINT | N | - | FK → User |
| user_video_id | BIGINT | N | - | FK → UserVideo |
| queue_type | VARCHAR(32) | N | - | TODAY, WEEKLY, BACKLOG |
| sort_order | INT | N | 0 | API 응답 필드명 `position` |
| created_at | TIMESTAMP | N | 감사 | API `addedAt` |
| updated_at | TIMESTAMP | N | 감사 | |

- **API 노출**: `QueueItemResponse` — 큐 메타 + `UserVideo`/`Video` 요약(제목·썸네일·학습상태·우선순위·진행초·길이).
- **Swagger**: `Queue` 태그, reorder 집합 일치·유니크 정책 설명.
- **로깅**: `userId`, `queueItemId`, `userVideoId`, `queueType` 허용; 토큰·PII 금지.

> **구 명칭 `WatchQueueItem` / `watch_queue_items`**: STEP 2 초안과의 정합을 위해 목차에 남겼으나, **구현 테이블명은 `learning_queue_items`**, FK는 **`user_videos`** 기준이다.

---

### 3.12 UserVideoProgress

- **목적**: **이어보기·진행률**(사용자×영상당 1행).
- **연관관계**: User(N:1), Video(N:1).
- **생성/수정 정책**: 시청 리포트 시 upsert; `updated_at` 갱신.
- **soft delete 여부**: **N** — 초기화 시 삭제 또는 progress 0.
- **인덱스 여부**: `(user_id, video_id)` **UNIQUE**; 최근 시청 `(user_id, updated_at DESC)`.
- **unique 여부**: `(user_id, video_id)` UNIQUE.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | (PK) | |
| user_id | BIGINT | N | - | FK → User |
| video_id | BIGINT | N | - | FK → Video |
| last_position_sec | INT | N | 0 | 마지막 재생 위치 |
| percent_complete | TINYINT 또는 SMALLINT | Y | NULL | 0–100 |
| created_at | TIMESTAMP | N | now() | 감사(STEP 3 DB 명세와 정합) |
| updated_at | TIMESTAMP | N | now() | |

- **API 노출**: 위치·퍼센트·갱신 시각.
- **Swagger**: 진행률 범위.
- **로깅**: 일반 OK.

---

### 3.13 SyncJob

- **목적**: 채널/영상/자막 **동기화·배치 작업** 추적(비동기 STEP과 연계).
- **연관관계**: User(N:1, nullable 시 시스템 작업).
- **생성/수정 정책**: 생성 시 PENDING; 실행·종료 시 status·시각 갱신.
- **soft delete 여부**: **N** — 보관 정책은 오래된 행 아카이브/삭제 배치.
- **인덱스 여부**: `(user_id, created_at DESC)`; `(status, created_at)`; `(job_type, status)`.
- **unique 여부**: 없음.

| 필드명 | 타입 | nullable | 기본값 | 설명 |
|--------|------|----------|--------|------|
| id | BIGINT | N | (PK) | |
| user_id | BIGINT | Y | NULL | 트리거 사용자; 시스템이면 NULL |
| job_type | VARCHAR(64) | N | - | CHANNEL_SYNC, VIDEO_SYNC, TRANSCRIPT_FETCH 등 |
| status | VARCHAR(32) | N | PENDING | PENDING, RUNNING, SUCCESS, FAILED |
| payload | JSON 또는 TEXT | Y | NULL | 대상 ID 등(비민감만) |
| error_message | VARCHAR(2000) | Y | NULL | 실패 시 |
| started_at | TIMESTAMP | Y | NULL | |
| finished_at | TIMESTAMP | Y | NULL | |
| created_at | TIMESTAMP | N | now() | |
| updated_at | TIMESTAMP | N | now() | |

- **API 노출**: 상태·타입·시각·사용자에게 보여줄 에러 요약. `payload`는 **내부 필드** 위주, 클라이언트에는 축소 DTO.
- **Swagger**: 작업 타입·상태 enum 문서화.
- **로깅**: `error_message`는 INFO 이상 허용 가능; `payload`에 토큰·PII 넣지 말 것, 로그 출력 시 마스킹.

---

### 3.14 DailyLearningStat (후보 엔티티 — STEP 15 연계, 미구현)

> **현재(STEP 15)**: Analytics API는 트랜잭션 DB에 대한 **실시간 집계**(`AnalyticsAggregationRepository` 네이티브 SQL 등).  
> **확장 시**: 일별 버킷을 **사전 집계**하면 대시보드·통계 API의 읽기 부하와 쿼리 복잡도를 줄일 수 있다.

| 항목 | 제안 |
|------|------|
| 목적 | 사용자·UTC 일자별 완료 수, 노트/하이라이트 생성 수 스냅샷 |
| 키 후보 | `(user_id, stat_date)` **UNIQUE** |
| 갱신 | `@Scheduled` 또는 `SyncJob` 파이프라인에서 전일 마감 후 upsert |
| API | Entity 비노출 — 기존 `AnalyticsDailyResponse` 소스만 집계 테이블로 교체 |

---

## 4. Swagger·로그 요약 매트릭스

| 엔티티 | Entity를 스키마로 노출 | DTO 노출 권장 | 로그 마스킹/금지 |
|--------|------------------------|---------------|------------------|
| User | 금지 | id, nickname, email(부분) | password_hash 금지, email 마스킹 |
| RefreshToken | 금지 | 없음 | 전부 금지 수준 |
| Channel | 금지 | 메타 필드 | 일반 |
| UserChannel | 금지 | 메타+별칭 | 일반 |
| UserSubscription | 금지 | `SubscriptionResponse` | OAuth 토큰 금지 |
| SubscriptionRecentVideo | 금지 | `SubscriptionRecentVideoResponse` | 일반 |
| Video | 금지 | 메타 | 일반 |
| Collection | 금지 | 이름·설명·visibility | 일반 |
| CollectionVideo | 금지 | 영상 요약+순서 | 일반 |
| Note | 금지 | 본문(정책) | body/memo 주의 |
| Highlight | 금지 | 구간·메모 | memo 주의 |
| TranscriptTrack / TranscriptSegment | 금지 | `Transcript*Response` | segment text 대량 로그 금지 |
| LearningQueueItem | 금지 | `QueueItemResponse` | 일반 |
| UserVideoProgress | 금지 | 진행 정보 | 일반 |
| UserVideo | 금지 | 요약/상세 DTO | user_id·진행 정보 일반 |
| SyncJob | 금지 | 상태·요약 | payload·error 주의 |

---

## 5. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 2 초안 — 엔티티·관계·인덱스·soft delete·API/로그 가이드 |
| 0.2 | 2026-03-25 | UserVideoProgress에 `created_at` 반영(STEP 3 `backend-db-spec.md` 정합) |
| 0.3 | 2026-03-25 | STEP 9 — `UserVideo` MVP 명세·`Video` 구현 편차 주석·매트릭스에 UserVideo 추가 |
| 0.4 | 2026-03-25 | UserVideo `completed_at`·완료 시각 정책 반영 |
| 0.5 | 2026-03-25 | STEP 10 — Note/Highlight를 UserVideo FK 기준으로 명세 정합 |
| 0.6 | 2026-03-25 | STEP 11 — Collection/CollectionVideo를 UserVideo 매핑·MVP 삭제 정책으로 정합 |
| 0.7 | 2026-03-25 | 컬렉션 이름 중복(trim+대소문자 무시)·CollectionVideo 순서 필드 `position` 명시 |
| 0.8 | 2026-03-25 | STEP 15 — `DailyLearningStat` 후보(§3.14)·Analytics 확장 포인트 |
| 0.9 | 2026-03-25 | STEP 16 — `LearningQueueItem`·§3.11 교체·Dashboard todayPick 연계 안내 |
| 1.0 | 2026-03-25 | STEP 17 — `TranscriptTrack`·`TranscriptSegment`·§3.10 교체 |
