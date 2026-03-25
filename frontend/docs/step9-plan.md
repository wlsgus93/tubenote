# step9-plan — 학습 통계(피드백형)

## 1. 단계 목표

- **`/analytics` 페이지**에서 학습 기록을 **숫자 나열이 아닌 피드백·동기**로 읽히게 구현한다.
- **총 학습 시간·완료 수·카테고리 비중·주간 활동·메모·복습·채널/길이 선호**를 한 화면에 담는다.

---

## 2. 이번 단계에서 해결할 사용자 문제

| 문제 | 대응 |
|------|------|
| 통계가 냉정하게만 느껴짐 | 상단 **피드백 문장** + 카드 **hint** |
| 무엇이 잘되고 있는지 모름 | 카테고리·주간 막대로 **패턴 가시화** |
| 다음에 무엇을 할지 | 완료·복습 수와 연계한 **짧은 제안** 카피(mock) |

---

## 3. 대상 페이지 / 컴포넌트

- **페이지**: `AnalyticsPage`.
- **피처** `src/features/learning-analytics/`: `FeedbackIntro`, `AnalyticsKpiRow`, `CategoryShareSection`, `WeeklyActivityBars`, `PreferenceColumns`, `analytics.css`.
- **재사용**: `PageHeader`, `StatCard`, `Button`, `SectionHeader`(있으면) 또는 간단 제목 블록.

---

## 4. 화면 구성 요소

1. **PageHeader** — 기간(mock)·“이번 주 요약” 톤.
2. **피드백 인트로** — 헤드라인 + 한 단락.
3. **KPI 행** — 총 시간, 완료 영상, 메모, 복습 (`StatCard` + hint).
4. **카테고리 비중** — 수평 막대(퍼센트), 라벨·분 수.
5. **주간 활동** — 7일 세로 막대(분 기준 정규화), 외부 차트 라이브러리 없음.
6. **선호 채널 / 길이** — 2컬럼: 순위 리스트 + 길이 버킷 비율 막대.

---

## 5. 상태 및 데이터 구조

- `AnalyticsBundle` 및 하위 타입 (`shared/types/analytics.ts`).
- Mock: `ANALYTICS_MOCK` (`mocks/analytics.ts`).
- 페이지는 읽기 전용(mock), 상태 없음 또는 기간 토글만 placeholder.

---

## 6. UX 결정 사항

- **과도한 차트 금지**: CSS `width`/`height` % 막대만 사용.
- **색**: 기존 토큰(`--color-primary`, success, muted) 위주.
- **접근성**: 막대에 `aria-label`로 수치 요약.

---

## 7. 구현 범위

- `step9-plan` / `step9-result`, `components.md`·`routes.md`·`ui-decisions.md` 갱신.

---

## 8. 제외 범위

- 실제 시청 로그·API.
- 기간 선택 UI 동작(칩은 mock 고정 문구만 가능).
- 정교한 SVG/캔버스 차트·애니메이션 과다.
