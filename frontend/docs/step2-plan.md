# step2-plan — 공통 UI 시스템·앱 쉘 완성

## 1. 단계 목표

- **디자인 토큰**을 spacing·radius·typography·시맨틱 컬러까지 정리해 코드(`tokens.css`)와 문서(`ui-decisions.md`)를 일치시킨다.
- **공통 UI 컴포넌트**를 `shared/ui`에 구현해 대시보드·목록·상세 등 전역에서 재사용 가능하게 한다.
- **앱 쉘**을 `Topbar` + `Sidebar` 명명으로 정리하고, **반응형 최소 대응**(좁은 화면에서 드로어형 사이드바)을 넣는다.
- **mock 내비게이션 데이터**로 사이드바 배지 등 샘플 메타를 분리한다.

---

## 2. 이번 단계에서 해결할 사용자 문제

| 문제 | 대응 |
|------|------|
| 화면마다 간격·타이포가 달라 집중이 깨진다 | 토큰 + 공통 카드·섹션 헤더로 시각 위계 통일 |
| 학습 상태·복습 필요가 한눈에 안 들어온다 | `StatusBadge`·카드 내 진행률·복습 표시 규칙 |
| 장시간 사용 시 눈이 피로하다 | 낮은 채도 배경·얇은 테두리·과한 그림자 지양(문서+토큰) |
| 모바일에서 사이드바가 본문을 침범한다 | 햄버거 + 오버레이 사이드바 |

---

## 3. 대상 페이지 / 대상 컴포넌트

- **전 페이지**: 앱 쉘(`AppShell`·`Topbar`·`Sidebar`) 적용 범위는 기존과 동일.
- **검증용**: `DashboardPage`에 StatCard·VideoCard·FilterBar·TabMenu·EmptyState 등을 **조합 샘플**로만 배치(비즈니스 로직 없음).
- **구현 컴포넌트**: `AppShell`, `Sidebar`, `Topbar`, `SectionHeader`, `StatCard`, `VideoCard`, `ChannelCard`, `NoteCard`, `StatusBadge`, `FilterBar`, `EmptyState`, `TabMenu`, 보조로 `Button`.

---

## 4. 화면 구성 요소

- **Topbar**: 브랜딩, (모바일) 메뉴 토글, 검색 자리, 사용자 자리.
- **Sidebar**: 그룹 라벨 + 링크 + **선택 배지(mock)**.
- **본문**: 기존 `MainContent` 유지; 섹션은 `SectionHeader`로 구분.
- **카드 위계**: `StatCard`(KPI) < `VideoCard`/`ChannelCard`/`NoteCard`(콘텐츠) — 그림자보다 테두리·패딩으로 단계 표현.

---

## 5. 상태 및 데이터 구조

- `LearningStatus`, `LearningPriority` — `src/shared/types/learning.ts`.
- 카드용 경량 모델은 컴포넌트 `props` 타입으로 각 TSX 옆 또는 `shared/types/cards.ts`에 정의(API 대체 용이).
- 내비: `src/mocks/navigation.ts`에 `SidebarNavItem` 확장 필드(`badgeCount?` 등).

---

## 6. UX 결정 사항

- **차분·집중**: primary 블루는 액션·현재 내비에만 제한, 본문은 중립 회색 톤.
- **위계**: 페이지 제목(`PageHeader`) > 섹션(`SectionHeader`) > 카드 제목 타이포 단계 유지.
- **학습 메타**: 상태(미시청·진행·완료·보류) + 우선순위(낮음·보통·높음) + 복습 필요는 배지 색·라벨로 구분.
- **피로**: 배경 `#f5f6f8`대 안쪽 서피스 흰색 대비를 유지, 대비는 WCAG 본문 위주만 확보.

---

## 7. 구현 범위

- `docs/step2-plan.md` / `step2-result.md`, `components.md`·`ui-decisions.md` 갱신.
- `tokens.css` 확장, `ui.css` 신설(공통 컴포넌트 스타일).
- `shared/ui/*` 컴포넌트, `layout`의 `Topbar`·`Sidebar`, `AppShell` 상태 연동.
- `mocks/navigation.ts`, `constants/navigation.ts`는 mock 재export.
- `DashboardPage` 샘플 조합.
- 영상 상세 보조 패널·실데이터 CRUD·API는 제외.

---

## 8. 제외 범위

- React Query / 전역 상태 / 실제 검색 동작.
- 페이지별 필터 상태 persistence.
- 다크 모드 토글 구현(토큰 확장만 문서에 예고 가능).
- 단위 테스트·스토리북.
