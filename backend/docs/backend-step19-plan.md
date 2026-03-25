# STEP 19 계획 — Videos / Video Detail 프론트 연동 계약 점검·보완

## 1. 단계 목표

영상 목록·상세 화면이 mock 없이 실제 API로 안정적으로 동작하도록 **응답 DTO**, **path variable(`userVideoId`)**, **학습 상태·진행률 PATCH** 계약을 정리하고 Swagger·연동 문서와 일치시킨다.

## 2. 이번 단계에서 해결할 문제

- 목록 `PageMeta`·요약 DTO 필드명이 프론트 카드·페이지네이션과 맞는지
- 상세에 노트/하이라이트/복습 대상 수·자막 가용성 메타가 포함되는지
- PATCH 후 응답이 GET 상세와 동일 수준의 집계를 갖는지(상세 화면 갱신 시 0으로 깨지는 문제)
- Swagger 설명·`docs/frontend-backend-contract.md`가 실제 동작과 어긋나지 않는지

## 3. 설계 대상

- `GET /api/v1/videos`, `GET /api/v1/videos/{userVideoId}`
- `PATCH .../learning-state`, `PATCH .../progress`
- DTO: `UserVideoSummaryResponse`, `UserVideoDetailResponse`, `PageMeta`, 요청 DTO
- 문서: `frontend-backend-contract.md`, `backend-api-spec.md`(UserVideo 구간), 필요 시 `VideoController` Swagger 주석

## 4. 주요 결정 사항

- **식별자**: 목록·상세·PATCH 모두 경로/참조는 **`userVideoId`(UserVideo PK)**. 공용 `videoId`는 응답 필드로만 제공.
- **일관성**: 상세 및 PATCH 응답은 동일하게 `enrichDetailAggregates`(노트·하이라이트·복습·자막 플래그) 적용.
- **목록**: `lastPositionSec`와 `progressSeconds` 동값 유지(대시보드와 동일 패턴). `videoPublishedAt`으로 정렬/표시 가능.
- **날짜·enum**: Jackson 기본 — `Instant`는 ISO-8601 UTC 문자열, enum은 이름 문자열.

## 5. 생성/수정 예정 파일

- `UserVideoSummaryResponse`, `UserVideoDetailResponse`, `UserVideoDtoMapper`, `UserVideoService`, `VideoController`
- `docs/frontend-backend-contract.md`, `docs/backend-api-spec.md`
- `docs/backend-step19-plan.md`(본 문서), `docs/backend-step19-result.md`

## 6. 구현 범위

- DTO 필드 보강·매핑·서비스 집계 로직
- Controller/Swagger 설명 보강
- 연동 문서·API 스펙 갱신

## 7. 제외 범위

- 새 도메인·새 엔드포인트 추가
- Note/Highlight/Transcript 본문 API 구현 확장

## 8. 다음 단계 연결 포인트

- STEP 20: 노트·하이라이트 목록/작성 화면 연동, 또는 자막 API·플레이어 연동
