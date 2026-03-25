# STEP 14 결과 — Dashboard 집계 API

## 1. 구현 완료 항목

- `GET /api/v1/dashboard` — `DashboardResponse` 및 섹션별 DTO.
- `DashboardService` — Repository 조합 + todayPick 메모리 정렬 + 즐겨찾기 채널별 피드 N건.
- `DashboardProperties` / `learningtube.dashboard` 설정.
- Repository JPQL 보강(주간 카운트·대시보드용 목록).

## 2. 생성/수정한 파일 목록 (요약)

| 구분 | 경로 |
|------|------|
| API | `DashboardController.java`, `DashboardService.java`, `DashboardMapper.java` |
| 설정 | `DashboardProperties.java`, `LearningTubeApplication.java`, `application.yml` |
| DTO | `domain/dashboard/dto/Dashboard*.java` |
| Repository | `UserVideoRepository`, `NoteRepository`, `HighlightRepository`, `UserSubscriptionRepository` |
| 문서 | `backend-step14-*.md`, `backend-api-spec.md` §8.6 |

## 3. 핵심 클래스/구조 설명

- **`DashboardController`**: JWT `userId` 기준 단일 GET.
- **`DashboardService`**: 섹션별 쿼리 실행·한도 적용·`weeklySummary`용 `startOfWeekUtc()`(UTC 월요일).
- **`DashboardMapper`**: `UserVideo`/`Note`/`SubscriptionRecentVideo` → DTO(노트 본문 미리보기·피드 `isNew`는 `UserVideo` 존재 여부).

## 4. 반영된 설계 원칙

- 조회 전용, Entity 미노출, 단순 쿼리 우선, 카드 친화 필드.

## 5. Swagger

- 태그 **Dashboard**, Operation 요약·섹션 설명.

## 6. 로깅

- INFO: `userId`, 각 섹션 리스트 size, 주간 `noteCount`·`highlightCount`(민감값 없음).

## 7. 아쉬운 점 / 개선 포인트

- `favoriteChannelUpdates`가 구독마다 추가 쿼리 — 구독 수가 많아지면 배치 조회·DTO projection 검토.
- `todayPick`은 도메인 규칙이 커지면 전용 **Recommendation/Queue** 서비스로 이전 권장.

## 8. 다음 단계 TODO

- **Analytics 도메인**: 일/주/월 롤업 테이블 또는 이벤트 적재 후 `weeklySummary` 데이터 소스 교체.
- **Queue 도메인**: 오늘의 학습 큐를 저장하면 `todayPick`을 큐 기반으로 전환.
