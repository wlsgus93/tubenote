# STEP 9 결과 — Video / UserVideo 구현

## 1. 구현 완료 항목

- **Video / UserVideo** JPA 연동 및 **CRUD 수준** API (임포트·목록·상세·PATCH 4종).
- **Enum**: `LearningStatus`, `Priority`, `VideoSourceType` (엔티티에서 사용).
- **Repository**: `VideoRepository`, `UserVideoRepository` + `JpaSpecificationExecutor`, 제목 검색용 `UserVideoSpecs`, 상세용 `JOIN FETCH` 쿼리.
- **메타**: `VideoMetadataPort` + `StubVideoMetadataAdapter` (YouTube API 대체).
- **URL 파싱**: `YoutubeUrlParser`.
- **페이징**: `PageMeta` + 목록 `meta`에 반영 (1-based `page`).
- **에러 코드**: `VIDEO_INVALID_YOUTUBE_URL`, `USER_VIDEO_NOT_FOUND`, `USER_VIDEO_DUPLICATE`.
- **시드**: 빈 DB일 때 `User` 1명 삽입 → JWT 테스트 로그인 `sub=1`과 FK 정합.
- **검증**: `@RequestParam` 위반 시 `ConstraintViolationException` → `COMMON_VALIDATION_FAILED`.

## 2. 생성/수정한 파일 목록

| 구분 | 경로 |
|------|------|
| Repository | `domain/video/VideoRepository.java`, `UserVideoRepository.java` |
| Spec | `domain/video/UserVideoSpecs.java` |
| Service | `domain/video/UserVideoService.java` |
| Controller | `domain/video/VideoController.java` |
| Mapper | `domain/video/UserVideoDtoMapper.java` |
| DTO | `domain/video/dto/*.java` |
| 메타 포트 | `domain/video/support/VideoMetadataPort.java`, `VideoMetadataSnapshot.java`, `StubVideoMetadataAdapter.java` |
| 유틸 | `global/util/YoutubeUrlParser.java` |
| 응답 | `global/response/PageMeta.java` |
| 설정 | `global/config/DataInitializer.java`, `application.yml` (`default_batch_fetch_size`) |
| 예외 | `global/error/ErrorCode.java`, `GlobalExceptionHandler.java` |

## 3. 핵심 클래스/구조 설명

- **UserVideoService**: 소유자 `userId`로만 조회·수정; 임포트 시 `Video` get-or-create 후 `UserVideo` 생성.
- **VideoController**: Swagger 태그 `Videos`, Bearer 필수.
- **UserVideoDtoMapper**: Entity → 응답 DTO만 담당 (API에 Entity 미노출).

## 4. 반영된 설계 원칙

- 공용 메타(**Video**) vs 사용자 상태(**UserVideo**) 분리.
- DTO 분리, 소유 검증은 Repository 쿼리로 일원화(미소유는 404).

## 5. Swagger 반영 내용

- 각 연산 summary/description, 요청 DTO 필드 `@Schema`, 401/404/409 등 ApiErrorResponse 연계.

## 6. 로깅 반영 내용

- 임포트·진행·핀·아카이브·학습상태: **userId**, **userVideoId**, 필요 시 **youtubeVideoId** / enum 값 (URL 전문은 로그에 남기지 않음).

## 7. 아쉬운 점 / 개선 포인트

- `UserVideo.completedAt`: `learningStatus`가 **COMPLETED**로 **최초** 바뀔 때만 시각 기록, 다른 상태로 되돌리면 **null**. `DataIntegrityViolationException`은 `uk_user_videos_user_video` / `uk_videos_youtube_video_id` 를 **409**로 매핑.
- 목록 N+1 완화용 `default_batch_fetch_size`만 적용; 대량 트래픽 시 fetch join·프로젝션 검토.
- `Video` 메타 갱신(재임포트·동기화) 정책 미정.
- DB에 이미 데이터만 있고 `users`가 비어 있지 않으면 시드가 스킵되어 JWT `sub=1`과 불일치할 수 있음 → 로컬에서는 DB 초기화 또는 수동 유저 생성 필요.

## 8. 다음 단계 TODO (Note 도메인)

- Note의 기준 키: **`userVideoId` 단일 FK**(권장: User+Video 소유 일관성) vs **`userId` + `videoId`** 복합.
- `Note` 엔티티에 `UserVideo` optional FK를 두면 “내 목록에 없는 영상” 노트 정책과 충돌하므로 **제품 정책을 먼저 확정** 후 스키마 반영.
- API 경로 초안: `GET/PATCH /api/v1/videos/{userVideoId}/notes` 또는 `/api/v1/user-videos/{userVideoId}/notes`.
