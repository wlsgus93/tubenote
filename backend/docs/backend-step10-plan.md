# STEP 10 계획 — Note / Highlight (UserVideo 종속)

## 1. 단계 목표

- 영상 상세·학습 기록의 핵심인 **Note**·**Highlight**를 **UserVideo** 하위 리소스로 CRUD 가능 수준까지 구현한다.

## 2. 해결할 문제

- “내 영상(UserVideo)” 단위로 메모·구간 강조를 저장·조회·수정·삭제한다.
- **소유권**: JWT `sub`와 `UserVideo.user_id` 일치만 허용한다.

## 3. 설계 대상

- 엔티티: `Note`(NoteType, body, positionSec, reviewTarget, pinned), `Highlight`(start/end, memo, flags).
- API: `/api/v1/videos/{userVideoId}/notes|highlights` + id 기준 `/api/v1/notes/{noteId}`, `/api/v1/highlights/{highlightId}`.
- `UserVideo` 상세에 `noteCount`, `highlightCount`, `reviewTargetCount` 집계.

## 4. 주요 결정

| 항목 | 결정 |
|------|------|
| FK | `user_video_id` → UserVideo (User·Video 직접 FK 아님) |
| NoteType | `GENERAL`, `TIMESTAMP`(+ `positionSec`) |
| 삭제 | 물리 DELETE |
| 목록 정렬 | `pinned DESC`, `updatedAt DESC` |
| 페이징 | page/size 선택적 확장(기본 50, 최대 200) |

## 5. 예정 파일

- `domain/note/*`, `domain/highlight/*`
- `ErrorCode` 확장, `UserVideoService`·`UserVideoDetailResponse` 보강
- `docs/backend-step10-result.md`, 명세 문서 갱신

## 6. 구현 범위

- CRUD, 검증, Swagger, 로깅(id 수준), `NOTE_NOT_FOUND` / `HIGHLIGHT_NOT_FOUND`.

## 7. 제외

- Collection·Subscription, 노트 본문 검색·전문 인덱스, soft delete.

## 8. 다음 단계 연결

- **Collection**: `Collection` ↔ `UserVideo` 또는 `Video` 연결 테이블로 “폴더에 넣기” UX.
- **Subscription**: `UserChannel`/채널 구독과 알림·동기화 트리거.
