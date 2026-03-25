# step3-plan — 학습 중심 메인 대시보드

## 1. 단계 목표

- **대시보드**를 “지금 무엇을 할지”가 즉시 보이는 **학습 허브**로 구현한다.
- 요청된 **7개 섹션**과 **빠른 액션**을 mock 데이터로 채우고, 이후 API로 치환 가능한 구조를 유지한다.
- **섹션·카드 우선순위**를 시각 언어(강조 밴드, 스크롤 행, 2열 배치)로 구분한다.

---

## 2. 이번 단계에서 해결할 사용자 문제

| 문제 | 대응 |
|------|------|
| 들어오자마자 할 일이 안 보인다 | 상단 **다음 학습 콜아웃** + 주요 CTA |
| 정보가 많아 압도된다 | **focus / standard / support** 섹션 단계 + 주간 요약은 Stat 카드로 압축 |
| 이어보기까지 클릭이 많다 | `nextUp` 영상에 **이어서 학습** 1클릭 |
| 메모·복습이 끊긴다 | **최근 메모** 섹션 + 복습 배지 + 메모/통계 **빠른 액션** |

---

## 3. 대상 페이지 / 대상 컴포넌트

- **페이지**: `DashboardPage` (`src/pages/dashboard/DashboardPage.tsx`).
- **피처(신규)**: `DashboardSection`, `DashboardNextUp`, `DashboardQuickActions` (`src/features/dashboard/`).
- **재사용**: `PageHeader`, `VideoCard`, `NoteCard`, `StatCard`, `SectionHeader`, `Button`, `EmptyState`.

---

## 4. 화면 구성 요소 (섹션 순서 = 우선순위)

1. **PageHeader** — 제목·한 줄 설명·보조 액션(메모 허브 등).
2. **다음 학습 콜아웃** (`DashboardNextUp`) — `nextUp` 영상, 진행률·배지, **이어서 학습**.
3. **빠른 액션** (`DashboardQuickActions`) — 큐·메모·영상·통계·구독.
4. **오늘의 학습 큐** — `focus` 섹션, 가로 스크롤 카드 행.
5. **이어보기 영상** — `standard`, 그리드(콜아웃와 중복 제외 가능).
6. **2열(넓은 화면)**: **최근 메모/하이라이트** | **미완료 학습 영상**.
7. **즐겨찾기 채널의 신규 업로드** — 업로드 시각 라벨.
8. **주간 학습 통계 요약** — 완료 수·시간·연속 일수·복습 대기.

빈 배열이면 해당 섹션에 `EmptyState` + CTA.

---

## 5. 상태 및 데이터 구조

- `DashboardBundle` (`src/shared/types/dashboard.ts`): `nextUp`, `todayQueue`, `continueWatching`, `recentNotes`, `incompleteVideos`, `newFromFavorites`, `weekly`, `quickActions`.
- `NewUploadFromFavorite` = `VideoCardModel` + `uploadedAtLabel`.
- `VideoCardModel`에 선택 필드 `contextHint` — 큐 우선순위 등 한 줄 설명.
- 전역 상태 없음; mock은 `src/mocks/dashboard.ts`.

---

## 6. UX 결정 사항

- **3초 규칙**: 첫 화면에 “지금 이어서” + 제목 + CTA.
- **위계**: 콜아웃 > 오늘 큐 > 이어보기 > 메모·미완료 > 신규 > 주간 요약.
- **피로**: 강조는 **왼쪽 악센트 보더·연한 primary 배경** 위주, 과한 원색 블록 지양.
- **복습**: 메모 카드 `reviewSuggested`·영상 `reviewNeeded` 유지.

---

## 7. 구현 범위

- `step3-plan` / `step3-result`, `components.md` 갱신.
- 타입·mock·피처 컴포넌트·`dashboard.css`·`DashboardPage` 전면 교체.
- 기존 `dashboardUi.ts`는 `dashboard.ts`로 통합 후 제거.

---

## 8. 제외 범위

- 실시간 API·인증·검색 동작.
- 큐 순서 편집·드래그앤드롭.
- 차트(막대/라인); 주간 요약은 수치 카드만.
