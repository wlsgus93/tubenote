# STEP 11 계획 — Collection / CollectionVideo

## 1. 단계 목표

- 사용자가 **UserVideo**를 폴더(컬렉션) 단위로 묶어 관리할 수 있는 **Collection / CollectionVideo** CRUD를 구현한다.

## 2. 설계 요지

- Collection은 **User 소유**; 모든 API에서 `user_id = JWT sub` 검증.
- CollectionVideo는 **Collection + UserVideo** 매핑; `(collection_id, user_video_id)` **UNIQUE**.
- **Video**가 아닌 **UserVideo** 기준으로 연결(“내 목록에 있는 영상”만 담기).
- 컬렉션 **삭제** 시 연결 행은 **JPA cascade(ALL + orphanRemoval)** 로 **물리 삭제**.

## 3. API (Base `/api/v1`)

- Collection CRUD + `videoCount`·`previewThumbnailUrls`(상세, 최대 3)·`coverThumbnailUrl`.
- CollectionVideo: 추가·목록·제거·`PATCH .../videos/order` (`orderedUserVideoIds` = 현재 멤버와 동일 집합).

## 4. 문서·코드

- `ErrorCode`: `COLLECTION_NOT_FOUND`, `COLLECTION_VIDEO_*`, DB 유니크 → `COLLECTION_VIDEO_DUPLICATE`.
- 명세: `backend-entities.md`, `backend-api-spec.md` §8.3.

## 5. 다음 단계 연결

- **Subscription / Channel**: 컬렉션과 무관하게 `UserChannel`·채널 메타; 추후 “채널별 자동 컬렉션”은 정책으로 연결 가능.
