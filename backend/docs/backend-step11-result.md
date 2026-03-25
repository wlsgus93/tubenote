# STEP 11 결과 — Collection / CollectionVideo

## 1. 구현 완료

- **이름**: 동일 사용자 내 **trim + `LOWER` 비교**로 중복 금지 → `COLLECTION_NAME_DUPLICATE`(409).
- **CollectionVideo 순서**: 도메인/API 필드 **`position`**(DB 컬럼 `sort_order` 유지).
- **Collection** 엔티티: `name`, `description`, `CollectionVisibility`, `sortOrder`, `coverThumbnailUrl`, `User` FK, `@OneToMany` cascade.
- **CollectionVideo** 엔티티: `Collection`·`UserVideo` FK, `sortOrder`, 유니크 `(collection_id, user_video_id)`.
- **Repository**: 목록·집계 쿼리(`countGroupedByCollectionIds`), `findMaxSortOrder*`, `findTop3` 미리보기용.
- **CollectionService**: 생성 시 사용자별 `sortOrder` 자동 증가, 추가 시 UserVideo 소유 검증, 순서 재배치 검증, 삭제 시 cascade.
- **CollectionController**: 단일 클래스에 컬렉션 + 중첩 videos API.
- **DTO / Mapper / Swagger / 로깅** (`collectionId`, `userId`, `userVideoId`).

## 2. 삭제 정책

- **컬렉션 DELETE**: 연결된 `collection_videos` 행은 **부모 삭제 시 cascade**로 제거(MVP는 soft delete 없음).
- **항목만 제거**: `DELETE .../videos/{userVideoId}` 로 매핑 행만 삭제.

## 3. 확장 포인트

- `previewThumbnailUrls`: 현재는 정렬 상위 3개 **Video 썸네일** URL 리스트; 별도 JSON 컬럼 없이 API/DTO 확장 가능.
- `coverThumbnailUrl`: 수동 설정 후 배치로 첫 영상 썸네일 동기화 등.

## 4. Subscription / Channel 연결

- **Channel/UserChannel** 도입 시: 공용 `Channel`·`Video`와는 별개; 컬렉션은 여전히 **UserVideo** 단위.
- 구독 기반 “새 영상 자동 담기”는 **SyncJob / 이벤트**에서 UserVideo 생성 후 `CollectionService.addVideo` 호출 패턴으로 연계 가능.

## 5. 파일 역할 요약

| 파일 | 역할 |
|------|------|
| `Collection`, `CollectionVideo` | JPA 모델·제약 |
| `CollectionVisibility` | PRIVATE / PUBLIC |
| `CollectionRepository`, `CollectionVideoRepository` | 영속성·집계 |
| `CollectionService` | 소유권·중복·순서·미리보기 |
| `CollectionDtoMapper` | Entity → DTO |
| `CollectionController` | REST + Swagger |
| `dto/*` | 요청/응답 모델 |
