# ui-decisions.md — 공통 UI 시스템

## 1. 컬러 시스템

### 1.1 기본(구조)

| 토큰 | 용도 | 값 |
|------|------|-----|
| `--color-bg` | 앱 배경 | `#f4f5f7` (장시간 피로 완화) |
| `--color-surface` | 카드·패널 | `#ffffff` |
| `--color-surface-elevated` | Topbar·Sidebar | `#ffffff` |
| `--color-surface-subtle` | 탭 트랙·빈 상태 배경 | `#f9fafb` |
| `--color-border` | 기본 구분선 | `#e4e7ec` |
| `--color-border-strong` | 호버·강조 테두리 | `#d0d4dd` |
| `--color-text` | 본문 | `#1c1f26` |
| `--color-text-muted` | 보조 | `#5f6673` |
| `--color-text-subtle` | 메타·eyebrow | `#8b929e` |

### 1.2 액션·포커스

| 토큰 | 용도 |
|------|------|
| `--color-primary` / `--color-primary-hover` / `--color-primary-muted` | CTA·현재 내비·포커스 배경 |
| `--color-focus-ring` | `:focus-visible` 외곽선 |

### 1.3 시맨틱 피드백

| 토큰 | 용도 |
|------|------|
| `--color-success` + `--color-success-muted` | 완료·긍정 KPI |
| `--color-warning` + `--color-warning-muted` | 진행·주의 KPI |
| `--color-danger` + `--color-danger-muted` | 보류·파괴적 액션 |
| `--color-info-muted` | 중립 강조 배경 |

### 1.4 학습 도메인(배지)

| 구분 | 배경/글자 토큰 | 의미 |
|------|----------------|------|
| 미시청 | `--color-learning-not-started-*` | 중립 |
| 진행 | `--color-learning-in-progress-*` | 주의(과포화 방지 위해 muted) |
| 완료 | `--color-learning-completed-*` | 성공 |
| 보류 | `--color-learning-on-hold-*` | 위험/중단 |
| 우선순위 낮음·보통·높음 | `--color-priority-*` | 큐 정렬 시각화 |
| 복습 권장 | `--color-review-*` | 보라 톤으로 소비 앱과 차별 |

다크 모드: 토큰만 확장 가능, step2에서는 미구현.

---

## 2. 타이포 시스템

| 토큰 | 용도 | 비고 |
|------|------|------|
| `--font-sans` | 전역 본문 | 시스템 스택 |
| `--font-weight-medium` / `--font-weight-semibold` | 라벨·제목 | |
| `--text-display` | Stat 수치 강조 | ~1.75rem |
| `--text-page-title` | 페이지 제목 | PageHeader |
| `--text-section` | 섹션·Topbar 제목 | SectionHeader 제목 |
| `--text-body` | 본문 | 기본 `body` |
| `--text-small` | 메타·칩 | |
| `--text-micro` | 배지·eyebrow | |
| `--line-height-body` | 1.55 | 장문 가독성 |
| `--line-height-tight` | 1.35 | 제목·카드 |

위계: **PageHeader > SectionHeader > 카드 제목 > 메타 > 배지**.

---

## 3. 간격 시스템

- 그리드: **4px**, 토큰 `--space-1` … `--space-9` (0.25rem ~ 3rem).
- 페이지 본문 패딩: `--space-6` 좌우(좁은 화면 `--space-4`).
- 카드 내부: `--space-4` ~ `--space-5`.
- 섹션 간: `--space-5` ~ `--space-7`.

---

## 4. Radius

| 토큰 | 용도 |
|------|------|
| `--radius-sm` | 칩 내부·작은 요소 |
| `--radius-md` | 버튼·입력·사이드 링크 |
| `--radius-lg` | 카드(기본과 동일 계열) |
| `--radius-xl` | 큰 패널 |
| `--radius-pill` | 배지·필터 칩 |
| `--radius-card` | 카드 = `--radius-lg` |

---

## 5. 카드/패널 규칙

- 기본: `1px solid var(--color-border)`, 배경 `--color-surface`, 그림자 `--shadow-xs`만.
- 호버: 테두리 `--color-border-strong`, `--shadow-card-hover`(은은하게).
- **VideoCard**: 16:9 썸네일 영역, 진행 바 4px primary.
- **NoteCard**: 좌측 3px primary 보더로 메모 성격 표시.

---

## 6. 버튼 규칙

| 변형 | 용도 |
|------|------|
| primary | 주요 CTA |
| secondary | 보조 확인 |
| ghost | 툴바·덜 강조 |
| danger | 삭제·큐 제거 |

최소 높이 **36px**(sm 32px), `:focus-visible` 링 필수.

---

## 7. 상태·복습 표시 규칙

- **학습 상태**: `StatusBadge` `type="learning"` — 네 가지 상태 고정 라벨(미시청·진행·완료·보류).
- **우선순위**: `type="priority"` — 낮음/보통/높음, 고우선만 강조색이 눈에 띄게.
- **복습**: `type="review"` — `needed === true`일 때만 카드에 노출(노이즈 감소). `NoteCard`는 `reviewSuggested` 시 동일 배지.

---

## 8. 학습 UX 원칙

- **집중**: primary 색은 액션·현재 위치에만, 본문은 중립 톤.
- **위계**: Section eyebrow(uppercase micro) → 제목 → 설명 → 카드 그리드.
- **장시간 사용**: 배경·보더 대비 완화, 순색 대신 subtle 서피스.
- **반응형**: `--breakpoint-nav` **900px** 이하에서 사이드바 드로어 + 백드롭.

---

## 9. 예외 규칙

- 영상 상세 보조 패널: step3에서 레이아웃 그리드로 확장.
- 검색 필드: 기능 연결 전까지 disabled 스타일 유지.
- 로그인: 앱 쉘 외부 단순 카드.
- **구독 채널(step6)**: `channel-library.css` 전용 — 카드 선택 테두리는 `--color-primary`, 학습용 칩은 primary 계열, 신규 수 배지는 녹색 계열(고정 hex), **960px↑**에서 상세 패널 `sticky` 우측 컬럼.
- **나중에 보기(step7)**: `watch-later.css` — 오늘 큐 영역은 `primary-muted` 그라데이션으로 ‘계획’ 구역만 은은히 구분; **일괄 액션 바**는 뷰포트 하단 `fixed`, 본문은 `.wl-page--has-bulk`로 하단 패딩 확보; **오래됨**은 subtle 보더·muted 텍스트.
- **메모 아카이브(step8)**: `note-archive.css` — 카드 **eyebrow**로「학습 흔적」구분; **타임코드**는 primary색·tabular 숫자; 메모/하이라이트는 **칩 색**(`na-kind--memo` / `na-kind--hl`)으로 구분; 복습 뱃지는 `--color-review-*`.
- **학습 통계(step9)**: `analytics.css` — **피드백 블록** 좌측 primary 보더 + 은은한 그라데이션; KPI는 기존 `StatCard`; 막대는 **primary / success** 토큰 위주(일부 보조 hex는 막대 단계 구분용); 주간 막대는 세로 그라데이션으로 “쌓임” 표현.
- **설정(step10)**: `settings-hub.css` — **섹션 카드** 단위로 스캔; 칩·토글은 **primary-muted** 선택 상태; 연동 카드는 **subtle 서피스** + 상태 뱃지(`success`/`muted`/`danger`).
- **온보딩(step10)**: `onboarding.css` — 로그인과 동일하게 **전폭 배경 그라데이션** + 중앙 **단일 카드**; 상단 **진행 점 4개**; 하단 “건너뛰기”는 텍스트 버튼 스타일로 secondary 액션.

---

## 10. 마감 정리 (step10)

- **공통**: 학습 도메인 색은 토큰·`ui.css` 배지 규칙을 우선하고, 피처 전용 CSS는 해당 폴더에만 둔다.
- **폼**: 설정·온보딩 입력은 네이티브 `input`/`checkbox` + 프로젝트 보더·radius 토큰을 맞춘다.
- **앱 쉘 밖**: 로그인·온보딩은 `AppShell` 없이 최소 UI로 집중도를 높인다.

---

## 구현 매핑

| 영역 | 파일 |
|------|------|
| 토큰 | `src/shared/styles/tokens.css` |
| 리셋·앱 쉘·Topbar·Sidebar | `src/shared/styles/global.css` |
| 공통 UI 컴포넌트 | `src/shared/styles/ui.css` |
| 진입 | `main.tsx`에서 `global.css` → `ui.css` 순 import |

---

## 갱신 이력

- step1: 초안 토큰.  
- step2: 시맨틱 컬러·spacing·radius·타이포·배지·반응형 브레이크포인트 반영.  
- step6: 구독 채널 페이지 예외(채널 카드·sticky 패널·학습/일반 칩).  
- step7: 나중에 보기 예외(오늘 큐·하단 일괄 바·오래됨 배지).  
- step8: 메모 아카이브 예외(학습 흔적 eyebrow·종류 칩·타임코드 강조).  
- step9: 통계 페이지 예외(피드백 인트로·막대 시각화·푸터 동기 문구).  
- step10: 설정·온보딩 예외(섹션 카드·온보딩 진행 점·연동 상태 뱃지).
