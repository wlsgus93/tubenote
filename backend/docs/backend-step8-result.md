# STEP 8 결과 — 비동기·메시징 구조 고정

## 1. 구현 완료 항목

- **비동기 처리 대상**(동기화·집계·알림·후처리)과 **동기로 충분한 경우**를 구분함.
- **MVP는 브로커 없음** + `@Async` + **`sync_jobs`** 를 기본으로 정함.
- **이벤트 envelope**(`eventId`, `eventType`, `schemaVersion`, `occurredAt`, `correlationId`, `payload`)와 예시 payload를 정의함.
- **실패 처리**(`SUCCESS`/`FAILED`/`CANCELLED`, `error_message`)를 `sync_jobs` 상태와 연계함.
- **재시도**(대상 오류 유형, 횟수·백오프 권장, 4xx 비재시도)를 정의함.
- **멱등성**(작업 dedup, DB 유니크, `eventId` 중복 소비)을 정의함.
- **RabbitMQ/Kafka/Redis Stream** 확장을 위한 **포트·어댑터** 개념과 브로커 선택 가이드를 문서화함.
- **로깅**은 `backend-logging-spec.md` §10과 교차 참조함.
- **코드·브로커 설정 미작성**.

## 2. 생성/수정한 파일 목록

| 파일 | 비고 |
|------|------|
| `docs/backend-step8-plan.md` | 신규 |
| `docs/backend-async-spec.md` | 신규 |
| `docs/backend-step8-result.md` | 신규 |

## 3. 핵심 클래스/구조 설명

- 구현 시(가칭): `JobEnqueuePort`, `SyncJobService`, `@Async` 실행기, `YoutubeSyncWorker`, (추후) `RabbitJobAdapter` 등.

## 4. 반영된 설계 원칙

- 무거운 작업은 HTTP에서 분리; **작업 이력은 DB**에 남김.
- 브로커 전에도 **메시지와 동일한 이벤트 스키마**로 확장 비용 절감.

## 5. Swagger 반영 내용

- 해당 없음. 작업 생성 API 도입 시 **202** 또는 `syncJobId` 응답을 `backend-api-spec.md`에 추가 예정.

## 6. 로깅 반영 내용

- §8에서 `syncJobId`·`correlationId`·금지 필드를 명시; STEP 7 명세와 일치.

## 7. 아쉬운 점 / 개선 포인트

- `sync_jobs`에 `dedup_key`·`attempt_count` 컬럼 추가 여부는 구현/마이그레이션에서 결정.
- Outbox 테이블 도입 시점은 트래픽·정합 요구에 따라 별도 결정.

## 8. 다음 단계 TODO

| 순서 | 작업 |
|------|------|
| STEP 9 | 도메인 API 설계 시 동기 vs 비동기·폴링 계약 반영 |
| 구현 | `ThreadPoolTaskExecutor` 빈, `SyncJob` 처리 루프, YouTube 클라이언트 재시도 |

---

## 개정 이력

| 버전 | 일자 | 내용 |
|------|------|------|
| 0.1 | 2026-03-25 | STEP 8 완료 기준 초안 |
