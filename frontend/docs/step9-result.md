# step9-result — 학습 통계 페이지

## 1. 구현 완료 항목

- **`/analytics` 페이지**를 mock **`ANALYTICS_MOCK`** 기반으로 구현.
- **총 학습 시간·완료 영상·메모·복습** KPI (`StatCard` + hint).
- **카테고리별 비중** 수평 막대, **주간 활동** 7일 세로 막대(CSS만).
- **선호 채널** 순위 리스트, **선호 길이** 분포 막대.
- 상단 **피드백 인트로** + 하단 **동기 한 줄**로 숫자 너머 톤 유지.

---

## 2. 생성/수정한 파일 목록

| 구분 | 경로 |
|------|------|
| 신규 | `docs/step9-plan.md`, `docs/step9-result.md` |
| 신규 | `src/shared/types/analytics.ts` |
| 신규 | `src/mocks/analytics.ts` |
| 신규 | `src/features/learning-analytics/FeedbackIntro.tsx` |
| 신규 | `src/features/learning-analytics/AnalyticsKpiRow.tsx` |
| 신규 | `src/features/learning-analytics/CategoryShareSection.tsx` |
| 신규 | `src/features/learning-analytics/WeeklyActivityBars.tsx` |
| 신규 | `src/features/learning-analytics/PreferenceColumns.tsx` |
| 신규 | `src/features/learning-analytics/analytics.css` |
| 신규 | `src/features/learning-analytics/index.ts` |
| 수정 | `src/pages/analytics/AnalyticsPage.tsx` |
| 수정 | `src/shared/types/index.ts` |
| 수정 | `docs/components.md`, `docs/routes.md`, `docs/ui-decisions.md` |

---

## 3. 핵심 컴포넌트 설명

- **`FeedbackIntro`**: 피드백 헤드라인·본문(학습 동기).
- **`AnalyticsKpiRow`**: 4개 `StatCard`, 각 `hint`로 의미 설명.
- **`CategoryShareSection`**: 컬렉션명·%·시간 + `width:%` 막대.
- **`WeeklyActivityBars`**: 요일별 분 수 정규화 세로 막대, `aria-label` 요약.
- **`PreferenceColumns`**: 채널 TOP5 + 길이 버킷 3단.

---

## 4. mock 데이터 구조 설명

- **`AnalyticsBundle`**: 기간 라벨, 총 분·완료·메모·복습 수, `categoryShares`, `weekActivity`, `channelPreferences`, `lengthPreferences`, `feedbackHeadline`/`Body`.
- **`formatLearningMinutes`**: 분 → 한글 시간 문자열.

---

## 5. UX 반영 사항

- KPI **hint**와 **피드백 블록**으로 “측정판”이 아닌 **되돌아보기** 톤.
- 차트 라이브러리 없이 **막대만** 사용해 복잡도 제한.
- 하단 문장으로 **지속 행동** 제안(대시보드 연결).

---

## 6. 아쉬운 점 / 이후 개선점

- 기간 선택·비교 주간은 UI만 문구로 표시, 동작 없음.
- 수치는 전부 mock; 실제는 시청 로그·메모 이벤트 집계 필요.
- 카테고리 막대 색 3단 반복은 시각적 구분용(토큰 확장 여지).

---

## 7. 다음 단계 TODO

- **전주 대비** 증감 화살표(Δ분, Δ완료).
- **설정**에서 주간 시작 요일·목표 시간 반영.
