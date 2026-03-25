# project-structure.md — 최종 프로젝트 구조

학습용 유튜브 허브 프론트엔드의 **디렉터리 역할**과 **데이터 흐름 원칙**을 정리한다. (파일 단위 전수 목록은 생략하고, 확장 시 참고할 골격을 적는다.)

---

## 1. 최상위

| 경로 | 역할 |
|------|------|
| `src/` | 애플리케이션 소스 전부 |
| `public/` | 정적 자산 |
| `docs/` | 단계 계획·결과·라우트·컴포넌트·UI·본 문서 |
| `.cursor/rules/` | 에이전트용 프로젝트 규칙 |

---

## 2. `src/app/`

| 경로 | 역할 |
|------|------|
| `router/index.tsx` | `createBrowserRouter` — `/`, `/login`, `/onboarding`, 앱 쉘 하위 기능 라우트 |
| `providers/AppProviders.tsx` | RouterProvider 마운트 |

---

## 3. `src/components/`

| 경로 | 역할 |
|------|------|
| `layout/` | `AppShell`, `Topbar`, `Sidebar`, `MainContent`, `PageHeader` — 전역 레이아웃 |
| `common/` | `PagePlaceholder` 등 공용 조각 |

**원칙**: 페이지 조합은 가볍게, 비즈니스 UI는 `features/`로.

---

## 4. `src/pages/`

라우트 1:1 진입점. 로직·목록·필터는 가능 한 **피처 컴포넌트**로 위임.

| 디렉터리 | 라우트(요약) |
|-----------|----------------|
| `auth/LoginPage` | `/login` |
| `onboarding/OnboardingPage` | `/onboarding` |
| `dashboard/` | `/dashboard` |
| `videos/` | `/videos` |
| `video-detail/` | `/videos/:videoId` |
| `watch-later/` | `/watch-later` |
| `notes/` | `/notes` |
| `subscriptions/` | `/subscriptions` |
| `analytics/` | `/analytics` |
| `settings/` | `/settings` |
| `not-found/` | `*` |

---

## 5. `src/features/`

기능 단위 UI·스타일·(필요 시) 순수 유틸. 페이지가 import 하는 주된 구현층.

| 디렉터리 | 역할 |
|-----------|------|
| `dashboard/` | 대시보드 섹션·이어보기 등 |
| `video-management/` | 학습 자산 목록 툴바·카드·필터 |
| `video-detail/` | 상세 플레이어·스크립트·메모·하이라이트 |
| `subscription-management/` | 구독 채널 카드·필터·상세 패널 |
| `watch-later-management/` | 나중에 보기 큐·일괄 바 |
| `note-archive-management/` | 메모·하이라이트 아카이브 |
| `learning-analytics/` | 통계 KPI·막대 시각화 |
| `settings-hub/` | 설정 섹션(프로필·관심사·알림·연동) |
| `onboarding-flow/` | 온보딩 전용 스타일(`onboarding.css`) |

각 피처는 보통 `*.css` + `index.ts` 배럴을 둔다.

---

## 6. `src/shared/`

| 경로 | 역할 |
|------|------|
| `ui/` | 버튼·카드·필터바 등 **도메인 무관** 재사용 컴포넌트 |
| `types/` | 전역 타입·도메인 타입 (`video-library`, `note-archive`, `settings` 등) |
| `styles/` | `tokens.css`, `global.css`, `ui.css` |
| `constants/` | `navigation.ts`, `storage.ts`(온보딩 키·헬퍼) |
| `hooks/`, `utils/` | (필요 시 확장) |

---

## 7. `src/mocks/`

API 전 **`단일 소스처럼** 쓰는 정적 데이터. 교체 시 서비스 레이어에서 동일 스키마로 치환.

| 파일(예) | 데이터 |
|-----------|--------|
| `navigation.ts` | 사이드바 그룹 |
| `dashboard.ts` | 대시보드 번들 |
| `videoLibrary.ts` | 학습 자산·컬렉션·태그 |
| `videoDetail.ts` | `getVideoDetailMock` |
| `channels.ts` | 구독 채널 |
| `watchLater.ts` | 나중에 보기 |
| `noteArchive.ts` | 메모 아카이브 |
| `analytics.ts` | 통계 번들 |
| `settingsPreferences.ts` | 설정 기본값·관심사·연동 mock |

---

## 8. 데이터 흐름 원칙

1. **타입** (`shared/types`) 먼저 정의 → mock·컴포넌트가 동일 필드 사용.  
2. **페이지**는 상태를 “화면 단위”로만 가짐; 재사용 로직은 **피처 유틸**(예: `filterAndSortVideos`).  
3. **API 연결 시**: `services/`(추가 예정)에서 fetch → 동일 타입으로 변환 → 페이지/스토어에 주입.  
4. **온보딩 완료**만 `localStorage` (`shared/constants/storage.ts`); 나머지 설정 저장은 현재 **UI mock**.

---

## 9. 문서 매핑

| 문서 | 용도 |
|------|------|
| `docs/routes.md` | 경로·페이지·구현 파일 |
| `docs/components.md` | 컴포넌트·피처·타입·mock 목록 |
| `docs/ui-decisions.md` | 색·간격·예외 UI 규칙 |
| `docs/stepN-plan.md` / `stepN-result.md` | 단계별 기록 |

---

## 10. 갱신 이력

- step10: 본 문서 신설 — 구조 마감·온보딩·설정 반영.
