# STEP 15 결과 — Analytics 집계 API

## 1. 구현 완료 항목

- `GET /api/v1/analytics/summary|daily|status-distribution|channels|collections`
- `AnalyticsService` + `AnalyticsAggregationRepository`(네이티브 일·채널·컬렉션 집계)
- `AnalyticsProperties` / `learningtube.analytics`
- DTO: `AnalyticsSummaryResponse`, `AnalyticsDailyResponse`(+ points), `AnalyticsStatusBucketDto`, `AnalyticsChannelStatDto`, `AnalyticsCollectionStatDto`
- Repository 보강: `UserVideoRepository`, `NoteRepository`, `HighlightRepository`, `ChannelRepository`

## 2. 생성/수정한 파일 목록 (요약)

| 구분 | 경로 |
|------|------|
| API | `AnalyticsController.java`, `AnalyticsService.java`, `AnalyticsAggregationRepository.java` |
| 모델/설정 | `AnalyticsRangeType.java`, `AnalyticsProperties.java`, `dto/*` |
| 연동 | `LearningTubeApplication.java`, `application.yml` |
| Repository | `UserVideoRepository`, `NoteRepository`, `HighlightRepository`, `ChannelRepository` |
| 문서 | `backend-step15-*.md`, `backend-api-spec.md` §8.7, `backend-entities.md` §3.14 |

## 3. 핵심 클래스/구조 설명

- **`AnalyticsAggregationRepository`**: `CAST(... AS DATE)` 기준 일별 집계 및 채널·컬렉션 GROUP BY. PostgreSQL/H2(UTC) 기준으로 맞춤.
- **`AnalyticsService`**: 요약은 JPQL 카운트·SUM, 일별은 빈 버킷 채운 뒤 세 집계 결과 병합, 채널은 노트 수 맵 + `Channel` id 조회.

## 4. 반영된 설계 원칙

- CRUD 없음, DTO만 응답, 실시간 집계 우선, 기간·버킷 설정으로 확장.

## 5. Swagger

- 태그 **Analytics**, `rangeType` 설명, 일별 메타 필드(`rangeStartUtc` 등).

## 6. 로깅

- 요약/일별/채널/컬렉션별 `userId` + 건수·합계(민감정보 없음).

## 7. 아쉬운 점 / 개선 포인트

- DB별 `CAST(x AS DATE)`·타임존 정책이 다를 수 있음 — 운영 DB 전환 시 네이티브 SQL 점검.
- 채널 제목이 Video 스냅샷 기준이라 동일 YouTube ID에 제목 불일치 시 GROUP BY가 갈라질 수 있음(장기적으로 `channel_id` FK 정규화).

## 8. 다음 단계 TODO

- **`DailyLearningStat` 엔티티** + 야간 배치로 `daily` 소스 교체(`backend-entities.md` §3.14).
- **Queue 도메인**: 큐 길이·처리율을 `summary`에 옵션 필드로 추가.
- **Transcript**: 시청 구간·자막 기반 “실학습 시간” 추정 시 `estimatedLearningSeconds`와 병행 또는 대체.
