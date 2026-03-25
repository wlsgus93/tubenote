# STEP 15 계획 — Analytics 집계 API

## 1. 단계 목표

- 저장된 학습 데이터를 기반으로 **통계 전용 읽기 API**를 제공해 프론트 통계 화면에서 바로 사용할 수 있게 한다.

## 2. 이번 단계에서 해결할 문제

- Dashboard가 “메인 카드” 중심이라면, Analytics는 **요약·일별 추세·분포·채널/컬렉션 뷰** 등 분석 축을 분리한다.

## 3. 설계 대상

- Base path: **`/api/v1/analytics`** (프로젝트 규칙과 통일).
- 엔드포인트: `summary`, `daily?rangeType=`, `status-distribution`, `channels`, `collections`.
- 구현: **실시간 집계**(JPQL + 네이티브 GROUP BY). 추후 `DailyLearningStat`·배치로 치환 가능하게 `AnalyticsAggregationRepository` 경계 유지.

## 4. 주요 결정 사항

- **estimatedLearningSeconds**: `last_position_sec` 합(보관함 제외).
- **onHoldCount**: `IN_PROGRESS`·`COMPLETED` 제외, 보관함 제외.
- **daily / ALL**: 사용자 최초 활동일~오늘(UTC)이나 **일자 버킷 상한**(`max-daily-buckets`).
- **rangeType** 오류: `COMMON_VALIDATION_FAILED`(400).

## 5. 생성/수정 예정 파일

- `domain/analytics/*`, Repository 보강, `ChannelRepository.findByYoutubeChannelIdIn`, `application.yml`, `LearningTubeApplication`, 문서.

## 6. 구현 범위

- Swagger, 로깅(`userId`, 행 수·합계 등).

## 7. 제외 범위

- BI급 다차원 분석, 실시간 스트림, 외부 웨어하우스 연동.

## 8. 다음 단계 연결 포인트

- **Queue**: 오늘의 학습 큐 지표는 Queue 도입 후 `summary` 또는 별도 엔드포인트 확장.
- **Transcript**: 자막 수·학습 시간 추정에 반영 시 `estimatedLearningSeconds` 정책 재정의.
