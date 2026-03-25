# STEP 10 결과 — Note / Highlight

## 1. 구현 완료 항목

- **Note**·**Highlight** 엔티티·Repository·Service·Controller·DTO·Swagger.
- **NoteType**: `GENERAL`, `TIMESTAMP` + `positionSec` 규칙·영상 길이 상한(가능 시).
- **Highlight** 구간 검증: `endSec ≥ startSec`, `durationSeconds` 있으면 `endSec` 상한.
- **Owner check**: `UserVideo`는 `findByIdAndUser_Id`; 노트/하이라이트는 `findByIdAndUserVideo_User_Id`.
- **UserVideo 상세**에 `noteCount`, `highlightCount`, `reviewTargetCount` 추가.
- **로깅**: `userId`, `userVideoId`, `noteId`/`highlightId` — 본문·memo 내용 미로그.

## 2. 생성/수정 파일 요약

| 영역 | 경로 |
|------|------|
| Note | `domain/note/Note.java`, `NoteType.java`, `NoteRepository.java`, `NoteService.java`, `NoteDtoMapper.java`, `VideoNotesController.java`, `NoteController.java`, `dto/*` |
| Highlight | `domain/highlight/Highlight.java`, `HighlightRepository.java`, `HighlightService.java`, `HighlightDtoMapper.java`, `VideoHighlightsController.java`, `HighlightController.java`, `dto/*` |
| 연동 | `UserVideoService.java`, `UserVideoDetailResponse.java`, `ErrorCode.java` |
| 문서 | `backend-step10-plan.md`, 본 파일, `backend-entities.md`, `backend-api-spec.md`, `backend-swagger-spec.md` |

## 3. API (Base `/api/v1`)

- Note: `POST/GET .../videos/{userVideoId}/notes`, `PATCH/DELETE .../notes/{noteId}`
- Highlight: `POST/GET .../videos/{userVideoId}/highlights`, `PATCH/DELETE .../highlights/{highlightId}`

## 4. Swagger·예외

- 태그 `Notes`, `Highlights`. `NOTE_NOT_FOUND`, `HIGHLIGHT_NOT_FOUND`, `USER_VIDEO_NOT_FOUND`, `COMMON_VALIDATION_FAILED`.

## 5. 다음 단계 (Collection / Subscription)

- **Collection**: `Collection` 엔티티 + `CollectionUserVideo`(또는 `CollectionItem`)로 다대다/순서 관리; API는 `POST /collections/{id}/user-videos` 형태 권장.
- **Subscription**: 채널 단위 `UserChannel` + 동기화 작업(STEP 8 비동기 명세)과 연계해 신규 영상 알림.

## 6. 아쉬운 점

- 목록 정렬·필터 고정; 검색·커서 페이징은 후속.
- Note/Highlight에 대한 감사 로그·복구(soft delete) 미도입.
