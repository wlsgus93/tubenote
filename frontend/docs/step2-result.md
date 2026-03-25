# step2-result — 2단계 구현 결과

## 1. 구현 완료 항목

- **문서**: `step2-plan.md`, `step2-result.md`, `components.md`·`ui-decisions.md` 갱신.
- **토큰**: spacing·radius·typography·시맨틱(학습·우선순위·복습·피드백) 정리 (`tokens.css`).
- **스타일**: 공통 UI 전용 `ui.css` 추가, `global.css`를 Topbar·Sidebar·반응형 드로어 구조로 정리.
- **레이아웃**: `AppHeader`/`AppSidebar` → **`Topbar`** / **`Sidebar`** 명명, `AppShell`에 모바일 사이드바 상태·백드롭.
- **공통 UI**: `Button`, `StatusBadge`, `SectionHeader`, `StatCard`, `VideoCard`, `ChannelCard`, `NoteCard`, `FilterBar`, `EmptyState`, `TabMenu`.
- **타입**: `LearningStatus`, `LearningPriority`, 카드용 `*CardModel`.
- **Mock**: `mocks/navigation.ts`(배지), `mocks/dashboardUi.ts`(카드 샘플) → **step3에서 `mocks/dashboard.ts`로 통합·삭제**.
- **검증 화면**: `DashboardPage`에서 위 컴포넌트 조합(비즈니스 로직 없음).

---

## 2. 생성/수정한 파일 목록

### 문서

- `docs/step2-plan.md`, `docs/step2-result.md`
- `docs/components.md`, `docs/ui-decisions.md`

### 스타일

- `src/shared/styles/tokens.css` (대폭 수정)
- `src/shared/styles/global.css` (Topbar·Sidebar·반응형)
- `src/shared/styles/ui.css` (신규)

### 타입·Mock·상수

- `src/shared/types/learning.ts`, `cards.ts`, `index.ts`
- `src/mocks/navigation.ts`, `dashboardUi.ts`(후속 step3에서 `dashboard.ts`로 대체)
- `src/shared/constants/navigation.ts` (mock 재export)

### 공통 UI

- `src/shared/ui/Button.tsx`, `StatusBadge.tsx`, `SectionHeader.tsx`, `StatCard.tsx`, `VideoCard.tsx`, `ChannelCard.tsx`, `NoteCard.tsx`, `FilterBar.tsx`, `EmptyState.tsx`, `TabMenu.tsx`, `index.ts`

### 레이아웃

- `src/components/layout/Topbar.tsx`, `Sidebar.tsx`, `AppShell.tsx` (수정)
- `src/components/layout/PageHeader.tsx` (마크업)
- 삭제: `AppHeader.tsx`, `AppSidebar.tsx`

### 페이지·진입

- `src/pages/dashboard/DashboardPage.tsx`
- `src/main.tsx` (`ui.css` import)

### 기타

- 삭제: `src/shared/ui/.gitkeep`, `src/mocks/.gitkeep`

---

## 3. 핵심 컴포넌트 설명

- **Topbar + Sidebar**: 생산성형 앱 쉘; 900px 이하에서 햄버거·오버레이·슬라이드 인 사이드바.
- **StatusBadge**: 유니온 타입으로 학습/우선순위/복습 구분, 토큰 기반 배경색.
- **VideoCard**: 16:9 영역, 진행률 바, 복습 배지는 `reviewNeeded`일 때만 표시.
- **FilterBar / TabMenu**: 툴바·탭 패턴으로 목록·대시보드 세그먼트 확장 예정.
- **EmptyState + Button**: 빈 데이터 시 다음 경로(`useNavigate`)로 유도 가능.

---

## 4. mock 데이터 구조 설명

- **navigation**: `SidebarNavItem.badgeCount` — 사이드바 우측 숫자 배지(데모).
- **dashboardUi**(폐기): step2 당시 카드 스냅샷 — step3 `DASHBOARD_MOCK`로 대체.

---

## 5. UX 반영 사항

- 차분한 배경·얇은 테두리·약한 호버 그림자로 **장시간 열람** 전제.
- **위계**: PageHeader → TabMenu → Stat 그리드 → SectionHeader → 필터 → 카드 그리드.
- **학습 메타**: 상태·우선순위·복습을 배지로 분리해 스캔 가능성 확보.

---

## 6. 아쉬운 점 / 이후 개선점

- 키보드로 TabMenu **화살표 이동(roving tabindex)** 미구현(현재 클릭 중심).
- `Button` as `Link` 패턴 미제공 — CTA에서 라우터와 중복 스타일 가능.
- 다크 모드·고대비 테마 없음.
- `npm audit` moderate 이슈는 step1과 동일 잔존.

---

## 7. 다음 단계 TODO (step3 제안)

1. 영상 목록 페이지: `FilterBar` + `VideoCard` 그리드 + `EmptyState`를 실데이터(mock)와 연결.
2. 영상 상세: 메인 + **보조 패널** 그리드, `TabMenu`로 스크립트/메모 전환.
3. 메모 허브: `NoteCard` 리스트 + 필터 상태.
4. 접근성: TabMenu roving tabindex, 스킵 링크.
5. `docs/step3-plan.md` 작성.
