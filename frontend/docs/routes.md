# routes.md — 라우트 명세

형식: 경로 | 페이지명 | 목적 | 주요 구성 요소 | 비고

---

## 공개·진입

| 경로 | 페이지명 | 목적 | 주요 구성 요소 | 비고 |
|------|----------|------|----------------|------|
| `/login` | 로그인 | 계정 진입(추후) | 안내 문구, 대시보드·온보딩 링크 | OAuth 전 |
| `/onboarding` | 온보딩 | 신규 사용자 단계 안내 | 4단계 마법사, 관심사·알림, 완료 시 `localStorage` | 앱 쉘 밖, step10 |
| `/` | 홈(리다이렉트) | 기본 진입을 대시보드로 통일 | — | `Navigate` to `/dashboard` |

---

## 앱 쉘 내부 (메인 레이아웃)

| 경로 | 페이지명 | 목적 | 주요 구성 요소 | 비고 |
|------|----------|------|----------------|------|
| `/dashboard` | 대시보드 | 오늘의 학습·이어보기·요약 | 이어보기 카드, 큐 미리보기, 빠른 링크 | IA 1차 |
| `/videos` | 학습 자산 목록 | 저장 영상 탐색·분류 | 검색, 상태·태그·길이 필터, 정렬, 그리드/리스트, 빠른 상태·중요·컬렉션 | step4 구현 |
| `/videos/:videoId` | 영상 상세(학습) | 플레이어·스크립트/메모·하이라이트·관련·복습 | mock 플레이어+스크럽, 탭 패널, 타임코드 seek | `getVideoDetailMock`, step5 |
| `/watch-later` | 나중에 보기 | 학습 계획·오늘 큐 | 의도·우선순위·오래됨·일괄 정리·컬렉션, 순서 조정 | step7 |
| `/notes` | 메모·하이라이트 | 학습 흔적 아카이브 | 메모/하이라이트·태그·복습·정렬·카드·리스트, `?t=` 상세 연동 | step8 |
| `/subscriptions` | 구독 채널 | 채널을 학습 리소스로 분류·메모 | 카테고리·유형·즐겨찾기·검색·정렬, 카드+상세 패널, 학습 자산 `?q=` 이동 | step6 |
| `/analytics` | 학습 통계 | 피드백형 요약·패턴 | KPI·카테고리/주간 막대·선호 채널·길이 | step9 |
| `/settings` | 설정 | 프로필·관심사·알림·연동 mock | `settings-hub` 섹션, 저장 UI mock, 온보딩 재진입 | step10 |

---

## 예외·향후

| 경로 | 페이지명 | 목적 | 주요 구성 요소 | 비고 |
|------|----------|------|----------------|------|
| `*` | NotFound | 잘못된 URL 안내 | 링크로 대시보드 복귀 | 옵션 |

**React Router 구성 (1단계 구현)**

- 정의 파일: `src/app/router/index.tsx` — `createBrowserRouter` 사용.  
- 부모 **pathless** 레이아웃: `AppShell`이 `/dashboard` … `/settings`·`/videos/:videoId`를 감쌈.  
- `/login`, `/onboarding`은 앱 쉘 **외부** 단독 페이지.  
- 정의되지 않은 경로: 앱 쉘 내부 `NotFoundPage` (`path: '*'`).  
- `/videos/:videoId`는 목록과 동일 레이아웃 하위 형제 라우트.

| 경로 | 페이지 컴포넌트(구현) |
|------|------------------------|
| `/` | `<Navigate to="/dashboard" />` |
| `/login` | `pages/auth/LoginPage` |
| `/onboarding` | `pages/onboarding/OnboardingPage` |
| `/dashboard` | `pages/dashboard/DashboardPage` |
| `/videos` | `pages/videos/VideosPage` |
| `/videos/:videoId` | `pages/video-detail/VideoDetailPage` |
| `/watch-later` | `pages/watch-later/WatchLaterPage` |
| `/notes` | `pages/notes/NotesPage` |
| `/subscriptions` | `pages/subscriptions/SubscriptionsPage` |
| `/analytics` | `pages/analytics/AnalyticsPage` |
| `/settings` | `pages/settings/SettingsPage` |
| `*` | `pages/not-found/NotFoundPage` |
