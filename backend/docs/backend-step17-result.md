# STEP 17 결과 — Transcript

## 1. 구현 완료 항목

- `TranscriptTrack` / `TranscriptSegment` 엔티티·리포지토리·서비스·컨트롤러·DTO·Swagger.
- `YoutubeTranscriptPort` + `YoutubeTranscriptStubAdapter`(빈 리스트 반환).
- 내 목록 검증(`UserVideo` 존재)·선택 트랙 단일화·동기화 후 기본 선택(수동 트랙 우선).
- `ErrorCode`: `VIDEO_NOT_FOUND`, `TRANSCRIPT_ACCESS_DENIED`, `TRANSCRIPT_TRACK_NOT_FOUND`, `COMMON_CONFLICT`; 무결성 UK 문자열 매핑.

## 2. 생성/수정한 파일 목록

| 경로 | 역할 |
|------|------|
| `domain/transcript/TranscriptSourceType.java` | 출처 enum |
| `domain/transcript/TranscriptTrack.java` | 트랙 엔티티 |
| `domain/transcript/TranscriptSegment.java` | 세그먼트 엔티티 |
| `domain/transcript/*Repository.java` | JPA |
| `domain/transcript/TranscriptService.java` | 비즈니스·로깅 |
| `domain/transcript/TranscriptDtoMapper.java` | Entity→DTO |
| `domain/transcript/TranscriptController.java` | REST |
| `domain/transcript/dto/*.java` | 요청/응답 |
| `infra/youtube/transcript/*` | Port·Stub·fetch DTO |
| `global/error/ErrorCode.java` | 코드 추가 |
| `global/error/GlobalExceptionHandler.java` | transcript UK → 409 |
| `docs/backend-step17-plan.md` / `backend-step17-result.md` | 단계 문서 |
| `docs/backend-entities.md` | §3.10 트랙·세그먼트 |
| `docs/backend-api-spec.md` | §8.9 |
| `docs/backend-db-spec.md` | §6.10.1 DDL·요약표·FK |

## 3. 핵심 클래스/구조 설명

- **TranscriptService.syncFromUpstream**: `fetchTracks` 결과 중 **큐가 비어 있지 않은 트랙만** upsert; 기존 세그먼트 `deleteByTranscriptTrack_Id` 후 `saveAll`.
- **TranscriptViewResponse**: 자막이 전혀 없거나 선택이 없어도 **200**으로 플래그만 구분.

## 4. 반영된 설계 원칙

- 공용 Video 데이터·사용자 메모 분리·세그먼트 단위 확장(검색은 후속).

## 5. Swagger 반영 내용

- 태그 `Transcript`, 403/404/200(빈 자막) 설명.

## 6. 로깅 반영 내용

- view/sync/select 시 식별자·건수 위주; Stub은 `debug` 한 줄.

## 7. 아쉬운 점 / 개선 포인트

- 목록 API에 페이징 없음 — 세그먼트 수만 건 이상이면 스트리밍·커서 페이징 필요.
- `selected` DB 제약 없음 — 애플리케이션만 단일화; 운영 DB에서 partial unique 검토 가능.

## 8. 다음 단계 TODO (배치·자동화 연계)

1. **`@Scheduled` / `SyncJob`**: 미동기화 Video·오래된 트랙에 대해 `syncFromUpstream` 호출(쿼터·백오프).
2. **`UserVideoService.importFromUrl` 후처리**: `@Async` 또는 메시지로 transcript sync 큐잉.
3. **`YoutubeTranscriptRestAdapter`**: `captions.list` + 자막 파일 fetch·파싱 후 `FetchedTranscriptTrack` 생성; `ConditionalOnProperty`로 Stub 대체.
4. **관측**: sync 실패율·세그먼트 수 메트릭.
5. **검색**: `transcript_segments` 에 GIN/tsvector 또는 OpenSearch 동기화.
