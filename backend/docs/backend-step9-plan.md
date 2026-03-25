# STEP 9 계획 — Video / UserVideo 도메인 (CRUD·학습 상태)

## 1. 단계 목표

- 공용 **Video**와 사용자별 **UserVideo**를 분리해, 유튜브 학습 영상 관리의 첫 핵심 도메인을 **DB 저장·조회·상태 변경**까지 완성한다.
- YouTube Data API는 **연동하지 않고** `VideoMetadataPort` 스텁으로 메타를 채운다.

## 2. 이번 단계에서 해결할 문제

- URL(또는 11자 id)로 영상을 “내 목록”에 넣는 흐름.
- `userVideoId` 기준 조회·부분 갱신(학습 상태, 진행, 핀, 아카이브).
- 목록 **페이징**, 최소 **필터·검색·정렬** 확장 포인트.

## 3. 설계 대상

- 엔티티: `Video`, `UserVideo` (이미 스키마 반영된 필드 유지).
- Enum: `LearningStatus`, `Priority`, `VideoSourceType`.
- 계층: Repository + Specification, Service(owner=JWT `sub` ↔ `userId`), Controller + DTO, Swagger.
- 유틸: `YoutubeUrlParser` (watch / youtu.be / shorts / embed / live).

## 4. 주요 결정 사항

| 항목 | 결정 |
|------|------|
| API 베이스 | `/api/v1/videos` (프로젝트 공통 `/api/v1` 유지) |
| 소유 검증 | `UserVideo` 조회 시 `user_id = principal.id` (없으면 404로 통일) |
| 중복 등록 | 동일 `(user_id, video_id)` → **409** `USER_VIDEO_DUPLICATE` |
| URL 오류 | **400** `VIDEO_INVALID_YOUTUBE_URL` |
| 목록 식별자 | 응답·경로 모두 **userVideoId** |
| 메타 소스 | `StubVideoMetadataAdapter` + 향후 `YoutubeDataVideoMetadataAdapter` 교체 가능 |

## 5. 생성/수정 예정 파일

- `VideoRepository`, `UserVideoRepository`, `UserVideoSpecs`, `UserVideoService`, `VideoController`
- `domain/video/dto/*`, `UserVideoDtoMapper`
- `VideoMetadataPort`, `VideoMetadataSnapshot`, `StubVideoMetadataAdapter`
- `YoutubeUrlParser`, `PageMeta`, `ErrorCode` 확장, `DataInitializer`(빈 DB 시 시드 유저)
- `docs/backend-step9-result.md`, `backend-entities.md`, `backend-api-spec.md` 갱신

## 6. 구현 범위

- API: `POST /import-url`, `GET /`, `GET /{userVideoId}`, PATCH learning-state / progress / pin / archive.
- 공통 예외·로깅·Swagger 반영.

## 7. 제외 범위

- YouTube API 실호출, Channel 엔티티 FK, Note/Collection 연동.

## 8. 다음 단계 연결 포인트

- **Note** 도메인: `User` + `Video`(또는 `UserVideo`)와의 FK·소유권 정책 확정 후 API 경로 설계 (`userVideoId` vs `videoId`).
