# STEP 16 결과 — Learning Queue

## 1. 구현 완료 항목

- `LearningQueueItem` + `QueueType`(TODAY, WEEKLY, BACKLOG) + `learning_queue_items` 스키마(JPA/Hibernate 생성 기준).
- Repository(FETCH 조인 목록), Service(추가·수정·삭제·compact·reorder), Controller `/api/v1/queue`.
- DTO 분리(`QueueItemResponse` 등), Swagger 문서, `ErrorCode`·글로벌 무결성 매핑.
- 소유권: `user_id` 일치 검증.

## 2. 생성/수정한 파일 목록

| 경로 | 역할 |
|------|------|
| `domain/queue/QueueType.java` | 큐 구분 enum + `@Schema` |
| `domain/queue/LearningQueueItem.java` | JPA 엔티티, UK `(user_id,user_video_id)`, 인덱스 |
| `domain/queue/LearningQueueItemRepository.java` | 소유 조회·타입별 목록(FETCH) |
| `domain/queue/QueueDtoMapper.java` | Entity → `QueueItemResponse` |
| `domain/queue/QueueService.java` | 비즈니스 규칙·로깅·position 정책 |
| `domain/queue/QueueController.java` | REST + Swagger |
| `domain/queue/dto/*.java` | 요청/응답 DTO |
| `global/error/ErrorCode.java` | `QUEUE_*` 코드 |
| `global/error/GlobalExceptionHandler.java` | `UK_LEARNING_QUEUE_ITEMS_USER_USER_VIDEO` → 409 |
| `docs/backend-step16-plan.md` / `backend-step16-result.md` | 단계 문서 |
| `docs/backend-entities.md` | §3.11 `LearningQueueItem` |
| `docs/backend-api-spec.md` | §8.8 Queue, 에러 카탈로그, Dashboard 안내 |
| `docs/backend-db-spec.md` | `learning_queue_items` DDL·FK |

## 3. 핵심 클래스/구조 설명

- **`QueueService`**: POST 시 타입 버킷에 삽입 후 전체 `saveAll`로 id 발급·position 연속화. DELETE·타입 변경 시 `compactPositions`. PATCH 동일 타입이면 `moveWithinBucket`.
- **`QueueController`**: `PATCH /reorder`를 `/{queueItemId:\\d+}` 보다 위에 두어 경로 충돌 방지. 실제 매칭은 리터럴 `/reorder`가 숫자 패턴과 구분됨.

## 4. 반영된 설계 원칙

- Entity API 비노출, UserVideo 기준 연결, 사용자 소유 데이터, 추적 가능 로그.

## 5. Swagger 반영 내용

- 태그 `Queue`, 각 Operation summary/description, 401/404/409/400 응답 스키마 일부.

## 6. 로깅 반영 내용

- 추가·수정·삭제·reorder 시 `userId`·식별자·`queueType` (민감정보 없음).

## 7. 아쉬운 점 / 개선 포인트

- `User` 삭제 시 FK CASCADE는 Hibernate ddl-auto 기본에 의존 — 운영 DB 마이그레이션에서 `ON DELETE` 명시 권장.
- 목록 API는 전체 조회(페이징 없음) — 큐 길이가 커지면 `queueType` 필수·페이징 도입 검토.

## 8. 다음 단계 TODO

1. **`DashboardService.load`**: `learningQueueItemRepository.findByUserIdAndQueueTypeWithVideo(userId, QueueType.TODAY)`로 카드 목록을 구성하고, **비어 있을 때만** 기존 `findTodayPickCandidates` 폴백(또는 정책에 따라 큐만 사용).
2. **`DashboardResponse` / `DashboardVideoCardDto`**: 큐 기반일 때 `queueItemId`·`position`을 내려줄지 결정 후 DTO 확장.
3. **프론트**: `GET /api/v1/queue?queueType=TODAY`와 대시보드 데이터 소스 통일.
