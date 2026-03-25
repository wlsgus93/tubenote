# 백엔드 비동기·메시징 명세

> STEP 8 산출물. 작업 영속화는 `sync_jobs`(`backend-db-spec.md`)를 사용한다.  
> 로그 MDC·`syncJobId` 는 `backend-logging-spec.md` §10과 **정합**한다.

---

## 1. 비동기 처리 대상

| 구분 | 작업 예 | 동기 처리 시 문제 | 비동기 권장 |
|------|---------|-------------------|-------------|
| **동기화** | 채널/영상 메타 갱신, 자막 수집 | YouTube API 지연·쿼터 | Y |
| **통계 집계** | 일별 시청·학습 지표(향후) | 집계 쿼리로 응답 지연 | Y |
| **알림** | 이메일/푸시(향후) | 외부 SMTP 지연 | Y |
| **외부 API 후처리** | 업로드 후 썸네일 생성 등 | 사용자 대기 시간 증가 | Y |
| **로그성 이벤트** | 감사 로그 적재(대용량) | 메인 트랜잭션 부하 | 선택 |

### 1.1 동기로 충분한 경우

- 단건 DB 읽기/쓰기, **1회 YouTube 호출 이내**이고 p95 SLA 이내인 경우.
- 사용자가 **즉시 결과**를 봐야 하는 검색(캐시 히트 전제) 등.

### 1.2 MVP 우선순위

1. **채널/영상 메타데이터 동기화** (`CHANNEL_SYNC`, `VIDEO_SYNC`)
2. **자막 수집** (`TRANSCRIPT_FETCH`)
3. 그 외(통계·알림)는 **이벤트만 정의**하고 구현은 후속.

### 1.3 STEP 13 (동기 API, 비동기 확장 예정)

- **현재**: `POST /api/v1/subscriptions/channel-updates/sync` 는 요청 스레드에서 **구독 채널 루프 + YouTube 호출**을 수행한다. 채널 단위는 `REQUIRES_NEW` 트랜잭션으로 격리된다.
- **후속**: `@Scheduled` 또는 `sync_jobs` + 워커로 **사용자·채널 단위 Job**을 큐잉하면 쿼터·타임아웃을 사용자 경험과 분리할 수 있다. 이벤트 타입 예: `SubscriptionChannelUpdatesRequested` (`userId`, 선택 `subscriptionId`).

---

## 2. 메시징 도입 여부

| 단계 | 브로커 | 설명 |
|------|--------|------|
| **MVP** | 없음 | `@Async` + `ThreadPoolTaskExecutor` + `sync_jobs` |
| **확장 1** | Redis Stream 또는 RabbitMQ | 단일 인스턴스 한계·재시작 유실 방지 |
| **확장 2** | Kafka | 고처리량·다중 컨슈머·리플레이 |

- MVP에서도 **이벤트 JSON 스키마**를 브로커 메시지 본문과 **동일하게** 가져가면 마이그레이션 비용이 줄어든다.

---

## 3. 이벤트 구조

### 3.1 애플리케이션 이벤트(내부)

도메인에서 발행하는 **불변** 레코드(가칭 `DomainEvent`).

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `eventId` | UUID string | Y | 중복 소비 탐지(브로커 도입 시) |
| `eventType` | string | Y | 예: `ChannelSyncRequested` |
| `schemaVersion` | int | Y | 시작값 `1` |
| `occurredAt` | Instant | Y | 발생 시각(UTC) |
| `correlationId` | string | Y | 원 요청 `requestId` 또는 `syncJobId` |
| `causationId` | string | N | 부모 `eventId` |
| `payload` | object | Y | 도메인별 데이터(아래 예) |

### 3.2 payload 예시

**ChannelSyncRequested**

```json
{
  "internalChannelId": 12,
  "youtubeChannelId": "UCxxxxxxxx",
  "requestedByUserId": 42
}
```

**TranscriptFetchRequested**

```json
{
  "internalVideoId": 99,
  "youtubeVideoId": "dQw4w9WgXcQ",
  "languageCode": "ko"
}
```

### 3.3 sync_jobs 와의 관계

- HTTP/API에서 “작업 등록” 시 **`sync_jobs` 행 생성**(`PENDING`) 후 비동기 실행기가 처리.
- `payload` 컬럼(JSONB)에는 **위 payload 부분집합**을 저장해 재시도·디버깅에 사용(민감정보·토큰 금지 — `backend-logging-spec.md` §8).

---

## 4. 실패 처리 정책

| 결과 | `sync_jobs.status` | `error_message` | 후속 |
|------|-------------------|-----------------|------|
| 성공 | `SUCCESS` | NULL | — |
| 비즈니스/클라이언트 오류 | `FAILED` | 요약 메시지 | 수동 재시도 API 또는 UI |
| 일시적 오류(재시도 소진 전) | `RUNNING` 유지 또는 `PENDING` 복귀 | 마지막 시도 메시지 | 스케줄러/백오프 |
| 취소 | `CANCELLED` | 선택 | 사용자 취소 |

- `started_at` / `finished_at` 으로 소요 시간 추적.
- **부분 성공**(예: 영상 100개 중 3개 실패)은 `payload`에 결과 요약 또는 자식 작업 분할로 표현(구현 세부).

---

## 5. 재시도 정책

### 5.1 재시도 대상

| 유형 | 재시도 |
|------|--------|
| 네트워크 타임아웃, 연결 실패 | Y |
| HTTP 429(YouTube quota) | Y, **Retry-After** 또는 지수 백오프 |
| HTTP 5xx | Y |
| HTTP 4xx(인증·not found 등) | **N** → `FAILED` |
| 검증 오류·도메인 불변식 위반 | **N** |

### 5.2 파라미터(권장 기본)

| 항목 | 값 |
|------|-----|
| 최대 시도 횟수 | 3~5(작업 유형별 설정 가능) |
| 백오프 | 지수: 1s, 2s, 4s(상한 캡) |
| 전역 레이트 | YouTube 클라이언트 단에서 동시성·QPS 제한 |

### 5.3 구현 패턴

- **동기 루프 내 재시도**: 인프라 클라이언트 레이어.
- **작업 단위 재시도**: 스케줄러가 `FAILED` 중 재시도 가능 태그가 있는 행만 `PENDING` 으로 되돌리거나, **지연 큐**(Redis 등)에 재투입.

---

## 6. 멱등성 정책

### 6.1 목표

- 동일 **비즈니스 키**에 대해 **중복 실행**해도 결과가 일관되고, **외부 부작용**(API 호출, DB 중복 행)을 최소화한다.

### 6.2 전략

| 레벨 | 방법 |
|------|------|
| **작업 등록** | 동일 `job_type` + `dedupKey`(payload 또는 별도 컬럼)로 **이미 `PENDING`/`RUNNING`/`최근 SUCCESS`** 이면 새 행 생성 생략 또는 기존 `id` 반환 |
| **DB** | `videos.youtube_video_id` UNIQUE 등으로 upsert |
| **외부 API** | YouTube는 **읽기 위주** 동기화 → 멱등에 가깝; 쓰기 도입 시 **idempotency-key** 헤더 패턴 검토 |

### 6.3 eventId

- 브로커 도입 후 **중복 전달** 시 `eventId` 로 **소비측 스킵**(in-memory 또는 DB 테이블 `processed_events`).

---

## 7. 추후 RabbitMQ / Kafka / Redis Stream 확장

### 7.1 공통 계약

- 메시지 본문 = **§3 이벤트 JSON** + 선택 **헤더**: `eventType`, `schemaVersion`, `correlationId`.
- **버전 업**: `schemaVersion` 증가, 구버전 컨슈머 병행 기간.

### 7.2 포트(헥사고날)

| 포트(인터페이스) | 역할 |
|------------------|------|
| `JobEnqueuePort` | 동기 API → 작업 등록(`sync_jobs` + 큐 push) |
| `OutboundEventPublisher` | 도메인 이벤트 → 브로커(초기는 No-op 또는 로그) |

| 어댑터(인프라) | 구현 |
|-----------------|------|
| `InProcessAsyncJobAdapter` | MVP: `@Async` 실행 |
| `RabbitJobAdapter` | 큐: `learningtube.sync`, routing by `job_type` |
| `KafkaEventAdapter` | 토픽: `learningtube.domain.events` |
| `RedisStreamAdapter` | Stream: `jobs:sync` |

### 7.3 선택 가이드

| 브로커 | 적합 |
|--------|------|
| **Redis Stream** | 이미 Redis 캐시 사용, 중간 규모, 운영 단순 |
| **RabbitMQ** | 라우팅·DLQ·지연 큐, 전통적 태스크 큐 |
| **Kafka** | 이벤트 소싱·다수 컨슈머·리텐션·분석 파이프라인 |

### 7.4 Outbox (선택)

- DB 커밋과 메시지 발행 **원자성**이 필요하면 **Transactional Outbox** 테이블 + 폴러 퍼블리셔 검토.

---

## 8. 로깅 연계

| 항목 | 규칙 |
|------|------|
| 작업 시작 | INFO: `syncJobId`, `jobType`, `correlationId`(= `requestId` 가능) |
| 재시도 | WARN: 시도 횟수, `error.code` 수준 |
| 최종 실패 | ERROR: `syncJobId`, 요약 메시지, 스택 1회 |
| MDC | `backend-logging-spec.md` §10 — 비동기 스레드에 `requestId` 복사 + `syncJobId` 설정, `finally` 클리어 |
| 금지 | `payload` 내 토큰·PII 전량, YouTube 응답 본문 전체 |

---

## 9. API와의 연동(권장)

| 패턴 | 설명 |
|------|------|
| **202 Accepted** | 작업만 등록하고 `data: { "syncJobId": 1 }` 반환(명세는 `backend-api-spec.md` 보강 시 반영) |
| **200 + 폴링** | 즉시 `PENDING` 행 반환, 클라이언트가 `GET /sync/jobs/{id}` 조회 |

---

## 10. 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 8 초안 |
