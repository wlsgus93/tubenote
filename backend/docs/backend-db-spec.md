# DB 명세 (LearningTube 백엔드)

> STEP 3 산출물. 논리 모델은 `backend-entities.md`와 정합해야 한다.  
> **DDL 1차 기준**: PostgreSQL **15+**. MySQL **8.0+** 차이는 [§11](#11-mysql-80-차이-요약) 참고.

---

## 1. 규약

| 항목 | 규칙 |
|------|------|
| 테이블·컬럼명 | `snake_case` |
| 테이블 복수형 | `users`, `videos`, `collections` 등 |
| PK 컬럼명 | `id` (`BIGINT`, 자동 증가) |
| FK 컬럼명 | `{참조테이블단수}_id` (예: `user_id`, `video_id`) |
| 제약 이름 | `pk_{table}`, `fk_{table}_{col}_{ref_table}`, `uk_{table}_{cols}`, `idx_{table}_{purpose}` |
| 문자 집합 | PostgreSQL: DB `UTF8` / MySQL: `utf8mb4` + `utf8mb4_unicode_ci` |
| 시간 | **UTC** 저장; 애플리케이션·JDBC 타임존 UTC 고정 권장 |

---

## 2. 정규화 수준

- **기본 3NF**: 반복 그룹 제거, 비키 속성은 키에 종속. 채널·영상 메타는 `channels` / `videos`에만 두고 사용자 연결은 `user_channels` 등으로 분리.
- **의도적 비정규화**: `channels.subscriber_count`, `videos`의 썸네일·제목 등은 YouTube API **스냅샷**으로 중복·캐시 성격 허용(출처는 외부 동기화).
- **집계 비정규화**: 컬렉션별 영상 개수 등은 **쿼리/COUNT**로 처리; 초기에는 별도 카운트 컬럼 두지 않음(필요 시 캐시·머티리얼라이즈드 뷰는 추후).

---

## 3. 공용 데이터 vs 사용자별 데이터

| 구분 | 테이블 | 설명 |
|------|--------|------|
| **SHARED_REFERENCE** | `channels`, `videos`, `transcript_tracks`, `transcript_segments`(초안 `transcripts` 병행 검토) | YouTube ID·내부 `id` 기준 전역 |
| **USER_SCOPED** | `user_channels`, `collections`, `collection_videos`, `notes`, `highlights`, `learning_queue_items`, `user_subscriptions`, `subscription_recent_videos`, `user_video_progress`, `sync_jobs` 등 | 항상 `user_id` 또는 `user_id`를 가진 상위를 통해 소유권 검증 |
| **SECURITY / 계정** | `users`, `refresh_tokens` | 인증·인가와 직접 연계 |

FK 원칙: **사용자 행이 공용 테이블을 직접 참조하지 않음**(예: `notes` → `videos` O, `notes` → `users` O, `notes` → `channels` X).

---

## 4. 감사 필드 및 soft delete 정책

### 4.1 감사 필드

| 컬럼 | 타입(PostgreSQL) | 적용 테이블 | 설명 |
|------|------------------|-------------|------|
| `created_at` | `TIMESTAMPTZ NOT NULL DEFAULT now()` | 전 테이블(아래 예외 참고) | 최초 생성 |
| `updated_at` | `TIMESTAMPTZ NOT NULL DEFAULT now()` | 변경 빈번 테이블 | 애플리케이션에서 갱신; DB 트리거는 선택 |
| `updated_at` 생략 가능 | — | **없음** — `user_video_progress`에도 `created_at`을 두어 감사 일관성 유지(STEP 2 대비 필드 추가) | |

> 구현 시 JPA `@PreUpdate` 등으로 `updated_at` 자동 갱신 권장.

### 4.2 soft delete

| 테이블 | 컬럼 | 정책 |
|--------|------|------|
| `users` | `deleted_at TIMESTAMPTZ NULL` | 활성 사용자만 조회·로그인. **이메일 유일**은 “활성”에 대해서만 DB 강제(§6.1 부분 유니크). |
| `collections` | `deleted_at` | 휴지통·복구 UX. 목록 기본은 `deleted_at IS NULL`. |
| `notes` | `deleted_at` | 선택적; 명세상 컬럼 유지 시 목록에서 제외. MVP에서 미사용 시 NULL만 유지 가능. |
| 그 외 | — | 물리 삭제 또는 상태 컬럼(`refresh_tokens.revoked_at`, `videos`는 `availability` 문자열). |

**물리 삭제 사용자(가입 철회 완료)** 시: CASCADE로 사용자 소유 행 정리(§5).

---

## 5. FK 및 ON DELETE 정책

| 자식 테이블 | FK | 참조 | ON DELETE | 비고 |
|-------------|-----|------|-----------|------|
| `refresh_tokens` | `user_id` | `users.id` | **CASCADE** | 사용자 영구 삭제 시 토큰 제거 |
| `user_channels` | `user_id` | `users.id` | **CASCADE** | |
| `user_channels` | `channel_id` | `channels.id` | **RESTRICT** | 채널 행이 참조 중이면 삭제 불가(일반적으로 채널 삭제 안 함) |
| `collections` | `user_id` | `users.id` | **CASCADE** | |
| `collection_videos` | `collection_id` | `collections.id` | **CASCADE** | 컬렉션 삭제 시 항목 제거 |
| `collection_videos` | `video_id` | `videos.id` | **RESTRICT** | 공용 영상 행 보존 |
| `videos` | `channel_id` | `channels.id` | **RESTRICT** | |
| `notes` | `user_id` | `users.id` | **CASCADE** | |
| `notes` | `video_id` | `videos.id` | **RESTRICT** | |
| `highlights` | `user_id` | `users.id` | **CASCADE** | |
| `highlights` | `video_id` | `videos.id` | **RESTRICT** | |
| `transcripts` | `video_id` | `videos.id` | **CASCADE** | (초안) |
| `transcript_tracks` | `video_id` | `videos.id` | **CASCADE** | STEP 17 |
| `transcript_segments` | `transcript_track_id` | `transcript_tracks.id` | **CASCADE** | |
| `learning_queue_items` | `user_id` | `users.id` | **CASCADE** | |
| `learning_queue_items` | `user_video_id` | `user_videos.id` | **CASCADE** | UserVideo 삭제 시 큐 행 제거 |
| `user_video_progress` | `user_id` | `users.id` | **CASCADE** | |
| `user_video_progress` | `video_id` | `videos.id` | **RESTRICT** | |
| `sync_jobs` | `user_id` | `users.id` | **SET NULL** | 사용자 삭제 후에도 작업 이력 보존 시 NULL; **CASCADE**로 통일할지 운영 정책 선택 가능 — 본 명세는 **SET NULL** |

> `sync_jobs.user_id`를 **CASCADE**로 바꾸면 사용자 삭제 시 이력도 삭제된다. 규제·감사 보관이 필요하면 **SET NULL** 유지.

---

## 6. PostgreSQL DDL

### 6.1 users

```sql
CREATE TABLE users (
  id            BIGSERIAL    PRIMARY KEY,
  email         VARCHAR(320) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nickname      VARCHAR(100) NOT NULL,
  role          VARCHAR(32)  NOT NULL DEFAULT 'MEMBER',
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  deleted_at    TIMESTAMPTZ  NULL,
  CONSTRAINT ck_users_role CHECK (role IN ('MEMBER', 'ADMIN'))
);

-- 활성 계정만 email 유일 (soft delete 대응)
CREATE UNIQUE INDEX uk_users_email_active ON users (email) WHERE deleted_at IS NULL;

CREATE INDEX idx_users_deleted_at ON users (deleted_at);
```

### 6.2 refresh_tokens

```sql
CREATE TABLE refresh_tokens (
  id          BIGSERIAL   PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  token_hash  VARCHAR(128) NOT NULL,
  expires_at  TIMESTAMPTZ NOT NULL,
  revoked_at  TIMESTAMPTZ NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  user_agent  VARCHAR(512) NULL,
  ip_address  VARCHAR(45) NULL,
  CONSTRAINT fk_refresh_tokens_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user_revoked ON refresh_tokens (user_id, revoked_at);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
```

### 6.3 channels

```sql
CREATE TABLE channels (
  id                  BIGSERIAL PRIMARY KEY,
  youtube_channel_id  VARCHAR(64)  NOT NULL,
  title               VARCHAR(255) NOT NULL,
  description         TEXT         NULL,
  thumbnail_url       VARCHAR(2048) NULL,
  subscriber_count    BIGINT       NULL,
  last_synced_at      TIMESTAMPTZ  NULL,
  created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT uk_channels_youtube_channel_id UNIQUE (youtube_channel_id)
);

CREATE INDEX idx_channels_last_synced_at ON channels (last_synced_at);
```

### 6.4 user_channels

```sql
CREATE TABLE user_channels (
  id                      BIGSERIAL PRIMARY KEY,
  user_id                 BIGINT       NOT NULL,
  channel_id              BIGINT       NOT NULL,
  display_name_override   VARCHAR(255) NULL,
  created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_user_channels_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_channels_channel_id
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE RESTRICT,
  CONSTRAINT uk_user_channels_user_channel UNIQUE (user_id, channel_id)
);

CREATE INDEX idx_user_channels_user_created ON user_channels (user_id, created_at DESC);
```

### 6.5 videos

```sql
CREATE TABLE videos (
  id                 BIGSERIAL PRIMARY KEY,
  channel_id         BIGINT       NOT NULL,
  youtube_video_id   VARCHAR(32)  NOT NULL,
  title              VARCHAR(500) NOT NULL,
  description        TEXT         NULL,
  duration_seconds   INT          NULL,
  thumbnail_url      VARCHAR(2048) NULL,
  published_at       TIMESTAMPTZ  NULL,
  availability       VARCHAR(32)  NOT NULL DEFAULT 'AVAILABLE',
  last_synced_at     TIMESTAMPTZ  NULL,
  created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_videos_channel_id
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE RESTRICT,
  CONSTRAINT uk_videos_youtube_video_id UNIQUE (youtube_video_id),
  CONSTRAINT ck_videos_availability CHECK (availability IN ('AVAILABLE', 'PRIVATE', 'REMOVED', 'UNKNOWN'))
);

CREATE INDEX idx_videos_channel_published ON videos (channel_id, published_at DESC NULLS LAST);
CREATE INDEX idx_videos_last_synced_at ON videos (last_synced_at);
```

### 6.6 collections

```sql
CREATE TABLE collections (
  id           BIGSERIAL PRIMARY KEY,
  user_id      BIGINT       NOT NULL,
  name         VARCHAR(200) NOT NULL,
  description  VARCHAR(2000) NULL,
  visibility   VARCHAR(32)  NOT NULL DEFAULT 'PRIVATE',
  sort_order   INT          NOT NULL DEFAULT 0,
  created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
  deleted_at   TIMESTAMPTZ  NULL,
  CONSTRAINT fk_collections_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT ck_collections_visibility CHECK (visibility IN ('PRIVATE', 'PUBLIC', 'UNLISTED'))
);

CREATE INDEX idx_collections_user_updated ON collections (user_id, updated_at DESC);
CREATE INDEX idx_collections_user_name ON collections (user_id, name);
CREATE INDEX idx_collections_deleted_at ON collections (deleted_at);
```

### 6.7 collection_videos

```sql
CREATE TABLE collection_videos (
  id             BIGSERIAL PRIMARY KEY,
  collection_id  BIGINT      NOT NULL,
  video_id       BIGINT      NOT NULL,
  sort_order     INT         NOT NULL DEFAULT 0,
  added_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_collection_videos_collection_id
    FOREIGN KEY (collection_id) REFERENCES collections (id) ON DELETE CASCADE,
  CONSTRAINT fk_collection_videos_video_id
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE RESTRICT,
  CONSTRAINT uk_collection_videos_collection_video UNIQUE (collection_id, video_id)
);

CREATE INDEX idx_collection_videos_collection_sort ON collection_videos (collection_id, sort_order);
```

> **STEP 11 구현 편차**: `collection_videos.user_video_id` + `uk_collection_videos_collection_user_video` 가 코드 기준이다. 위 DDL은 초안(`video_id`) 잔존 — 마이그레이션 시 코드와 맞출 것.

### 6.7.1 learning_queue_items (STEP 16)

```sql
CREATE TABLE learning_queue_items (
  id              BIGSERIAL PRIMARY KEY,
  user_id         BIGINT       NOT NULL,
  user_video_id   BIGINT       NOT NULL,
  queue_type      VARCHAR(32)  NOT NULL,
  sort_order      INT          NOT NULL DEFAULT 0,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_learning_queue_items_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_learning_queue_items_user_video_id
    FOREIGN KEY (user_video_id) REFERENCES user_videos (id) ON DELETE CASCADE,
  CONSTRAINT uk_learning_queue_items_user_user_video UNIQUE (user_id, user_video_id),
  CONSTRAINT ck_learning_queue_items_queue_type
    CHECK (queue_type IN ('TODAY', 'WEEKLY', 'BACKLOG'))
);

CREATE INDEX idx_learning_queue_user_type_sort
  ON learning_queue_items (user_id, queue_type, sort_order);
```

### 6.8 notes

```sql
CREATE TABLE notes (
  id                   BIGSERIAL PRIMARY KEY,
  user_id              BIGINT      NOT NULL,
  video_id             BIGINT      NOT NULL,
  body                 TEXT        NOT NULL,
  video_timestamp_sec  INT         NULL,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at           TIMESTAMPTZ NULL,
  CONSTRAINT fk_notes_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_notes_video_id
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE RESTRICT
);

CREATE INDEX idx_notes_user_video_created ON notes (user_id, video_id, created_at DESC);
CREATE INDEX idx_notes_video_timestamp ON notes (video_id, video_timestamp_sec);
```

### 6.9 highlights

```sql
CREATE TABLE highlights (
  id         BIGSERIAL PRIMARY KEY,
  user_id    BIGINT      NOT NULL,
  video_id   BIGINT      NOT NULL,
  start_sec  INT         NOT NULL,
  end_sec    INT         NOT NULL,
  memo       VARCHAR(500) NULL,
  color      VARCHAR(32) NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_highlights_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_highlights_video_id
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE RESTRICT,
  CONSTRAINT ck_highlights_range CHECK (start_sec >= 0 AND end_sec >= start_sec)
);

CREATE INDEX idx_highlights_user_video_start ON highlights (user_id, video_id, start_sec);
```

### 6.10 transcripts

```sql
CREATE TABLE transcripts (
  id             BIGSERIAL PRIMARY KEY,
  video_id       BIGINT       NOT NULL,
  language_code  VARCHAR(16)  NOT NULL,
  source         VARCHAR(32)  NOT NULL DEFAULT 'YOUTUBE_AUTO',
  content        TEXT         NOT NULL,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_transcripts_video_id
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
  CONSTRAINT uk_transcripts_video_language UNIQUE (video_id, language_code),
  CONSTRAINT ck_transcripts_source CHECK (source IN ('YOUTUBE_AUTO', 'IMPORTED', 'USER_PASTE'))
);
```

> 대용량 `content`는 TOAST 저장. 검색이 필요하면 향후 `tsvector`·외부 검색 엔진 검토.

> **STEP 17 구현**: 단일 `content` 대신 **`transcript_tracks` + `transcript_segments`** 를 사용한다(아래 §6.10.1). 위 `transcripts` 테이블은 초안·마이그레이션 시 병합 또는 폐기 검토.

### 6.10.1 transcript_tracks / transcript_segments (STEP 17)

```sql
CREATE TABLE transcript_tracks (
  id                 BIGSERIAL PRIMARY KEY,
  video_id           BIGINT       NOT NULL,
  language_code      VARCHAR(16)  NOT NULL,
  is_auto_generated  BOOLEAN      NOT NULL DEFAULT false,
  source             VARCHAR(32)  NOT NULL DEFAULT 'YOUTUBE_AUTO',
  selected           BOOLEAN      NOT NULL DEFAULT false,
  created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_transcript_tracks_video_id
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE CASCADE,
  CONSTRAINT uk_transcript_tracks_video_lang_auto
    UNIQUE (video_id, language_code, is_auto_generated)
);

CREATE INDEX idx_transcript_tracks_video_selected
  ON transcript_tracks (video_id, selected);

CREATE TABLE transcript_segments (
  id                   BIGSERIAL PRIMARY KEY,
  transcript_track_id  BIGINT       NOT NULL,
  line_index           INT          NOT NULL,
  start_seconds        DOUBLE PRECISION NOT NULL,
  end_seconds          DOUBLE PRECISION NOT NULL,
  text                 TEXT         NOT NULL,
  created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_transcript_segments_track_id
    FOREIGN KEY (transcript_track_id) REFERENCES transcript_tracks (id) ON DELETE CASCADE,
  CONSTRAINT uk_transcript_segments_track_line
    UNIQUE (transcript_track_id, line_index)
);

CREATE INDEX idx_transcript_segments_track_start
  ON transcript_segments (transcript_track_id, start_seconds);
```

### 6.11 watch_queue_items

```sql
CREATE TABLE watch_queue_items (
  id          BIGSERIAL PRIMARY KEY,
  user_id     BIGINT      NOT NULL,
  video_id    BIGINT      NOT NULL,
  sort_order  INT         NOT NULL DEFAULT 0,
  added_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_watch_queue_items_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_watch_queue_items_video_id
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE RESTRICT,
  CONSTRAINT uk_watch_queue_items_user_video UNIQUE (user_id, video_id)
);

CREATE INDEX idx_watch_queue_items_user_sort ON watch_queue_items (user_id, sort_order);
```

### 6.12 user_video_progress

```sql
CREATE TABLE user_video_progress (
  id                 BIGSERIAL PRIMARY KEY,
  user_id            BIGINT      NOT NULL,
  video_id           BIGINT      NOT NULL,
  last_position_sec  INT         NOT NULL DEFAULT 0,
  percent_complete   SMALLINT    NULL,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT fk_user_video_progress_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_video_progress_video_id
    FOREIGN KEY (video_id) REFERENCES videos (id) ON DELETE RESTRICT,
  CONSTRAINT uk_user_video_progress_user_video UNIQUE (user_id, video_id),
  CONSTRAINT ck_user_video_progress_percent CHECK (percent_complete IS NULL OR (percent_complete >= 0 AND percent_complete <= 100))
);

CREATE INDEX idx_user_video_progress_user_updated ON user_video_progress (user_id, updated_at DESC);
```

### 6.13 sync_jobs

```sql
CREATE TABLE sync_jobs (
  id             BIGSERIAL PRIMARY KEY,
  user_id        BIGINT       NULL,
  job_type       VARCHAR(64)  NOT NULL,
  status         VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
  payload        JSONB        NULL,
  error_message  VARCHAR(2000) NULL,
  started_at     TIMESTAMPTZ  NULL,
  finished_at    TIMESTAMPTZ  NULL,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT fk_sync_jobs_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
  CONSTRAINT ck_sync_jobs_status CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED'))
);

CREATE INDEX idx_sync_jobs_user_created ON sync_jobs (user_id, created_at DESC);
CREATE INDEX idx_sync_jobs_status_created ON sync_jobs (status, created_at);
CREATE INDEX idx_sync_jobs_type_status ON sync_jobs (job_type, status);
```

---

## 7. 테이블별 제약·인덱스 요약표

| 테이블 | PK | 주요 UNIQUE | 주요 보조 인덱스 |
|--------|-----|-------------|------------------|
| users | `id` | `uk_users_email_active`(부분) | `idx_users_deleted_at` |
| refresh_tokens | `id` | `token_hash` | `(user_id, revoked_at)`, `expires_at` |
| channels | `id` | `youtube_channel_id` | `last_synced_at` |
| user_channels | `id` | `(user_id, channel_id)` | `(user_id, created_at DESC)` |
| videos | `id` | `youtube_video_id` | `(channel_id, published_at DESC)`, `last_synced_at` |
| collections | `id` | — | `(user_id, updated_at DESC)`, `(user_id, name)`, `deleted_at` |
| collection_videos | `id` | `(collection_id, video_id)` | `(collection_id, sort_order)` |
| notes | `id` | — | `(user_id, video_id, created_at DESC)`, `(video_id, video_timestamp_sec)` |
| highlights | `id` | — | `(user_id, video_id, start_sec)` |
| transcripts | `id` | `(video_id, language_code)` | — |
| transcript_tracks | `id` | `(video_id, language_code, is_auto_generated)` | `(video_id, selected)` |
| transcript_segments | `id` | `(transcript_track_id, line_index)` | `(transcript_track_id, start_seconds)` |
| watch_queue_items | `id` | `(user_id, video_id)` | `(user_id, sort_order)` |
| user_video_progress | `id` | `(user_id, video_id)` | `(user_id, updated_at DESC)` |
| sync_jobs | `id` | — | `(user_id, created_at)`, `(status, created_at)`, `(job_type, status)` |

---

## 8. 조회 패턴 ↔ 인덱스

| 유스케이스 | WHERE / ORDER BY | 인덱스 |
|------------|------------------|--------|
| 로그인 | `email` + `deleted_at IS NULL` | `uk_users_email_active` |
| 내 등록 채널 목록 | `user_id` ORDER BY `created_at` DESC | `idx_user_channels_user_created` |
| 채널별 영상 피드 | `channel_id` ORDER BY `published_at` DESC | `idx_videos_channel_published` |
| 내 컬렉션 목록 | `user_id`, `deleted_at IS NULL` ORDER BY `updated_at` | `idx_collections_user_updated` + 앱 조건 |
| 컬렉션 내 영상 정렬 | `collection_id` ORDER BY `sort_order` | `idx_collection_videos_collection_sort` |
| 영상별 내 노트 타임라인 | `user_id`, `video_id` ORDER BY `created_at` | `idx_notes_user_video_created` |
| 동기화 대기 작업 | `status = 'PENDING'` ORDER BY `created_at` | `idx_sync_jobs_status_created` |
| 최근 이어보기 | `user_id` ORDER BY `updated_at` DESC | `idx_user_video_progress_user_updated` |

---

## 9. 비정규화·확장 포인트

| 항목 | 현재 | 추후 |
|------|------|------|
| 컬렉션 영상 개수 | `COUNT(*)` 쿼리 | `collections.video_count` + 트리거/이벤트 |
| 영상 제목 검색 | LIKE(비효율) | `pg_trgm`, Elasticsearch 등 |
| 자막 전문 검색 | 미도입 | `tsvector` 또는 외부 인덱스 |
| 다중 리전 | 단일 DB | 읽기 복제·파티셔닝 별도 설계 |

---

## 10. JPA·마이그레이션 가이드(요약)

- Flyway/Liquibase 버전 관리, **환경별 동일 스키마** 원칙.
- `users.deleted_at` 필터: 기본 Repository 쿼리에 조건 포함 또는 Hibernate `@SQLRestriction`(버전 호환 확인).
- `JSONB` 매핑: Hibernate `@JdbcTypeCode(SqlTypes.JSON)` 등.
- 동시성: `user_video_progress` 등은 `UPDATE ... SET last_position_sec = ? WHERE id = ?` 단건 위주; 충돌 시 마지막 쓰기 우선(LWW) 정책 명시 가능.

---

## 11. MySQL 8.0 차이 요약

| 항목 | PostgreSQL | MySQL 8.0 |
|------|------------|-----------|
| 시각 타입 | `TIMESTAMPTZ` | `DATETIME(6)` + 앱 UTC |
| JSON | `JSONB` | `JSON` |
| 활성 이메일 유일 | `UNIQUE INDEX ... WHERE deleted_at IS NULL` | **함수 인덱스 + 유니크**: `UNIQUE` on `(email, (IF(deleted_at IS NULL, 1, NULL)))` 패턴 또는 **`email` + `deleted_at` 복합 유니크** + 앱에서 삭제 시 `deleted_at`을 고유 값으로 치환하는 우회 |
| 권장 우회(MySQL) | — | 컬럼 `email_normalized_active VARCHAR(320) GENERATED ALWAYS AS (CASE WHEN deleted_at IS NULL THEN email ELSE NULL END) STORED` + `UNIQUE (email_normalized_active)` (버전·문법 검증 필요) **또는** 탈퇴 시 email에 suffix 부여 후 유니크 `(email)` 단순 유지 |

> MySQL 채택 시 **이메일 유일성** 패턴을 스키마 초기에 하나로 고정하고 `backend-db-spec.md` 개정 이력에 남길 것.

---

## 12. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 3 초안 — PostgreSQL DDL, FK, 인덱스, 정규화·감사·soft delete, MySQL 차이 |
