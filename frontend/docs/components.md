# components.md — 컴포넌트 목록

형식: 컴포넌트명 · 책임 · props · 재사용 여부 · 사용 페이지

---

## 레이아웃 (`src/components/layout`)

| 컴포넌트명 | 책임 | props | 재사용 | 사용 페이지 |
|------------|------|-------|--------|-------------|
| `AppShell` | Topbar·Sidebar·MainContent·Outlet, 모바일 드로어 상태 | — | 예 | 앱 쉘 내 전역 |
| `Topbar` | 브랜딩·햄버거(≤900px)·검색 자리·유저 자리 | `onMenuClick?` | 예 | 동일 |
| `Sidebar` | 그룹 내비·활성 링크·배지(mock)·로그인 하단 | `groups?`, `mobileOpen?`, `onMobileClose?` | 예 | 동일 |
| `MainContent` | 본문 패딩·스크롤·max-width | `children`, `constrained?` (미지정 시 `/videos/:id` 만 전폭) | 예 | 동일 |
| `PageHeader` | 페이지 제목·설명·액션 슬롯 | `title`, `description?`, `actions?` | 예 | 기능 페이지 |

**구현 파일**: `AppShell.tsx`, `Topbar.tsx`, `Sidebar.tsx`, `MainContent.tsx`, `PageHeader.tsx`

**내비 데이터**: `src/mocks/navigation.ts` (`SIDEBAR_NAV_MOCK`). `src/shared/constants/navigation.ts`는 동일 데이터를 `SIDEBAR_NAV_GROUPS`로 재export.

---

## 공통 UI (`src/shared/ui`)

| 컴포넌트명 | 책임 | props | 재사용 | 사용 페이지 |
|------------|------|-------|--------|-------------|
| `Button` | primary/secondary/ghost/danger, sm/md | `variant?`, `size?`, 표준 `button` 속성 | 예 | 전역 |
| `StatusBadge` | 학습 상태·우선순위·복습 표시 | `StatusBadgeProps`(discriminated union) | 예 | 카드·목록 |
| `SectionHeader` | 페이지 내 구역 제목·eyebrow·설명·액션 | `eyebrow?`, `title`, `description?`, `actions?` | 예 | 대시보드·목록 |
| `StatCard` | KPI 숫자·힌트·톤 | `label`, `value`, `hint?`, `tone?` | 예 | 대시보드·통계 |
| `VideoCard` | 썸네일·제목·채널·`contextHint`·진행률·배지 | `video: VideoCardModel`, `onOpen?` | 예 | 대시보드·영상 |
| `ChannelCard` | 아바타·채널명·메타 | `channel: ChannelCardModel`, `onOpen?` | 예 | 구독·대시보드 |
| `NoteCard` | 영상 참조·타임코드·발췌·복습 | `note: NoteCardModel`, `onOpen?` | 예 | 메모·대시보드 |
| `FilterBar` | 필터 칩 행·trailing 슬롯 | `filters`, `activeId`, `onChange`, `trailing?` | 예 | 목록·대시보드 |
| `EmptyState` | 빈 데이터 안내·CTA | `title`, `description?`, `action?` | 예 | 큐·목록 |
| `TabMenu` | 세그먼트 탭(a11y tab 역할) | `tabs`, `activeId`, `onChange`, `ariaLabel?` | 예 | 상세 등 |

**배럴**: `src/shared/ui/index.ts`

**스타일**: `src/shared/styles/ui.css` (클래스 접두어 `ui-`)

---

## 페이지 (`src/pages`)

| 페이지 | 책임 | 주요 구성 |
|--------|------|-----------|
| `DashboardPage` | 학습 허브 — **GET `/api/v1/dashboard`** (`useDashboard`), 로딩·에러·401 안내; 큐·이어보기·메모·주간 요약 | `DashboardNextUp`, `DashboardQuickActions`, `DashboardSection`, `VideoCard`, `NoteCard`, `StatCard`, `EmptyState` |
| `LoginPage` | Google OAuth 리다이렉트(백엔드 `/oauth2/...`) — API 로그인은 **POST `/api/v1/auth/test-login`** 등 | `GoogleSignInButton`, `Button` |
| `VideosPage` | 학습 자산 — **GET `/api/v1/videos`**, **POST import-url**(URL/ID 추가), 상태 **PATCH …/learning-state**; 검색·필터·정렬·그리드/리스트; `?q=` 프리필 | `useVideoLibrary`, `VideosToolbar`, `AddVideoByUrlDialog`, `VideoTagFilterBar`, `FilterBar`, `VideoLibraryGridCard`, `VideoLibraryListRow`, `EmptyState` |
| `VideoDetailPage` | 학습 상세 — **GET `/api/v1/videos/:userVideoId`**, **PATCH learning-state / progress**; 플레이어 스크럽·스크립트/메모·필기·하이라이트·관련·복습; `?t=초` 시 메모 탭·시각 시크 | `useVideoDetail`, `VideoInfoHeader`, `VideoPlayerPanel`, `VideoDetailMetaBar`, `ScriptPanel`, `MemoTimelinePanel`, `VideoScratchpadPanel`, `TabMenu`, 하단 섹션들 |
| `SubscriptionsPage` | 구독 채널 — 카드·상세 정리(mock **최근 업로드**에서 import-url), **리스트·일괄** | `ChannelsToolbar`, `FilterBar`, `SubscriptionChannelCard`, `ChannelDetailPanel`, `SubscriptionBulkListHeader`, `SubscriptionBulkListRow`, `SubscriptionBulkActionBar`, `EmptyState` |
| `WatchLaterPage` | 나중에 보기 학습 큐 — 오늘 큐·의도·우선순위·오래됨·일괄 정리·컬렉션 | `TodayQueueStrip`, `WatchLaterToolbar`, `FilterBar`, `WatchLaterListRow`, `BulkActionBar`, `EmptyState` |
| `NotesPage` | 메모·하이라이트 아카이브 — 학습 흔적 목록·태그·복습·정렬·카드/리스트·시점 점프 | `NoteArchiveToolbar`, `FilterBar`, `NoteArchiveCard`, `NoteArchiveListRow`, `EmptyState` |
| `AnalyticsPage` | 학습 통계 — 피드백 인트로·KPI·카테고리/주간 막대·선호 채널·길이 | `FeedbackIntro`, `AnalyticsKpiRow`, `CategoryShareSection`, `WeeklyActivityBars`, `PreferenceColumns`, `StatCard` |
| `SettingsPage` | 설정 — 프로필·관심사·알림·연동 mock, 온보딩 재진입 | `ProfileSection`, `InterestsSection`, `NotificationsSection`, `ConnectionsSection`, `PageHeader`, `Button` |
| `OnboardingPage` | 온보딩 — 4단계(환영·관심사·알림·완료), `localStorage` 완료 플래그 | `Button`, `onboarding.css`, (알림 토글은 `settings-hub.css` 토글 클래스 재사용) |

---

## 대시보드 피처 (`src/features/dashboard`)

| 컴포넌트명 | 책임 | props | 재사용 | 사용 페이지 |
|------------|------|-------|--------|-------------|
| `DashboardNextUp` | 최우선 “지금 이어서” 콜아웃 + CTA | `video`(null 가능), `onContinue`, `onAddNote?`, `onPickVideo?` | 부분 | 대시보드 |
| `DashboardQuickActions` | 빠른 이동 버튼 행(API `quickActions` 또는 기본 링크) | `actions: QuickActionItem[]` | 부분 | 대시보드 |
| `DashboardSection` | 섹션 래퍼(`focus`/`standard`/`support`) + `SectionHeader` | `level?`, `eyebrow?`, `title`, `description?`, `actions?`, `children` | 부분 | 대시보드 |

**스타일**: `dashboard.css`  
**배럴**: `index.ts`

---

## 영상 관리 피처 (`src/features/video-management`)

| 컴포넌트명 | 책임 | props | 재사용 | 사용 페이지 |
|------------|------|-------|--------|-------------|
| `VideosToolbar` | 검색 입력·정렬 select·그리드/리스트 토글·`trailingActions` | 위 + `trailingActions?` | 부분 | 영상 목록 |
| `AddVideoByUrlDialog` | 모달 — YouTube URL/11자 ID → `importVideoByUrl` | `open`, `onClose`, `onSuccess(userVideoId)` | 부분 | 영상 목록 |
| `VideoTagFilterBar` | 태그 다중 토글(OR)·전체 초기화 | `tagOptions`, `selectedTags`, `onToggleTag`, `onClearTags` | 부분 | 영상 목록 |
| `VideoLibraryGridCard` | 학습 자산 그리드 카드 + 상태·중요·컬렉션 | `video`, `collections`, `collectionName`, 콜백 | 부분 | 영상 목록 |
| `VideoLibraryListRow` | 리스트 행 레이아웃 + 동일 액션 | 동일 | 부분 | 영상 목록 |
| `filterAndSortVideos` | 검색·상태·태그·길이·정렬 순수 함수 | `items`, `VideoLibraryFilterState` | 유틸 | 영상 목록 |

**스타일**: `video-library.css`  
**배럴**: `index.ts`

---

## 구독·채널 정리 피처 (`src/features/subscription-management`)

| 컴포넌트명 | 책임 | props / 비고 | 재사용 | 사용 페이지 |
|------------|------|----------------|--------|-------------|
| `ChannelsToolbar` | 채널·메모 검색, 정렬 select | `search`, `onSearchChange`, `sortId`, `onSortChange` | 부분 | 구독 |
| `SubscriptionChannelCard` | 학습/일반·즐겨찾기·신규·메모 미리보기 카드 | `channel`, `categoryName`, `selected`, `onSelect`, `onToggleFavorite` | 부분 | 구독 |
| `ChannelDetailPanel` | 최근 업로드(API 피드)·라이브러리 추가·메모(blur 저장)·유형·카테고리·통계·학습 자산 이동 | + `recentFeedLoading?`, `recentFeedLoadError?`, `feedSource?`, `onMemoBlur?` | 부분 | 구독 |
| `filterAndSortChannels` | 검색·카테고리·유형·즐겨찾기·정렬 | `ChannelListFilterState` | 유틸 | 구독 |
| `SubscriptionBulkListHeader` | 리스트 뷰 헤더·필터 결과 전체 선택 | `filtered`, `selectedIds`, 콜백 | 부분 | 구독 |
| `SubscriptionBulkListRow` | 체크·채널 메타·행 단위 구독 취소 | `channel`, `categoryName`, 콜백 | 부분 | 구독 |
| `SubscriptionBulkActionBar` | 선택 일괄 구독 취소·하단 고정 | `selectedCount`, 콜백 | 부분 | 구독 |

**스타일**: `channel-library.css` · **mock**: `mocks/channels.ts`  
**배럴**: `index.ts`

---

## 나중에 보기 피처 (`src/features/watch-later-management`)

| 컴포넌트명 | 책임 | 비고 | 사용 페이지 |
|------------|------|------|-------------|
| `TodayQueueStrip` | 오늘 볼 영상 순서·큐 제거·학습 화면 링크 | 비어 있을 때 완화 카피·`#watch-later-list` 앵커 | 나중에 보기 |
| `WatchLaterToolbar` | 검색·정렬 select | `WatchLaterSortId` | 동일 |
| `WatchLaterListRow` | 선택·의도·우선순위·컬렉션·오늘 큐·오래됨 | `VIDEO_COLLECTIONS` 연동 | 동일 |
| `BulkActionBar` | 선택 시 하단 고정 일괄 액션 | `position: fixed` | 동일 |
| `filterAndSortWatchLater` | 검색·의도·정렬 | 계획 순·우선순위 등 | 동일 |
| 큐 유틸 (`queueOrder.ts`) | 순서 교환·정규화 | `normalizeWatchLaterQueue` | 동일 |

**스타일**: `watch-later.css` · **mock**: `mocks/watchLater.ts`  
**배럴**: `index.ts`

---

## 메모 아카이브 피처 (`src/features/note-archive-management`)

| 컴포넌트명 | 책임 | 비고 | 사용 페이지 |
|------------|------|------|-------------|
| `NoteArchiveToolbar` | 검색·정렬·카드/리스트 전환 | `NoteArchiveSortId` | 메모 |
| `NoteArchiveCard` | 학습 흔적 카드·시점/영상 이동 | `navigate` + `?t=` | 동일 |
| `NoteArchiveListRow` | 고밀도 리스트 행 | 동일 액션 | 동일 |
| `filterAndSortNoteArchive` | 종류·태그·복습·검색·정렬 | 순수 함수 | 동일 |

**스타일**: `note-archive.css` · **mock**: `mocks/noteArchive.ts`  
**배럴**: `index.ts`

---

## 학습 통계 피처 (`src/features/learning-analytics`)

| 컴포넌트명 | 책임 | 비고 | 사용 페이지 |
|------------|------|------|-------------|
| `FeedbackIntro` | 상단 동기·피드백 카피 | `headline`, `body` | 통계 |
| `AnalyticsKpiRow` | 총 시간·완료·메모·복습 KPI | `StatCard` + hint | 동일 |
| `CategoryShareSection` | 컬렉션별 비중 수평 막대 | CSS `width:%` | 동일 |
| `WeeklyActivityBars` | 7일 세로 막대 | 정규화 높이 | 동일 |
| `PreferenceColumns` | 선호 채널 순위 + 길이 분포 | 2컬럼 그리드 | 동일 |

**스타일**: `analytics.css` · **mock**: `mocks/analytics.ts` (`formatLearningMinutes`)  
**배럴**: `index.ts`

---

## 설정 허브 (`src/features/settings-hub`)

| 컴포넌트명 | 책임 | 비고 | 사용 페이지 |
|------------|------|------|-------------|
| `ProfileSection` | 표시 이름·이메일(읽기 전용) | 추후 `/me` | 설정 |
| `InterestsSection` | 학습 관심사 다중 칩 | `LEARNING_INTEREST_OPTIONS` | 설정·온보딩(칩 UI는 별도) |
| `NotificationsSection` | 이메일·리마인더·주간 리포트 토글 | 추후 알림 API | 설정·온보딩 |
| `ConnectionsSection` | 연동 계정 카드·상태 뱃지 | OAuth mock | 설정 |

**스타일**: `settings-hub.css` · **mock**: `mocks/settingsPreferences.ts`  
**배럴**: `index.ts`

---

## 온보딩 플로우 (`src/features/onboarding-flow`)

| 항목 | 책임 |
|------|------|
| `onboarding.css` | 전체 화면 카드·진행 점·칩 스타일 |
| (페이지) `OnboardingPage` | 단계 상태·`writeOnboardingDone` / `clearOnboardingDone` |

---

## 영상 상세 피처 (`src/features/video-detail`)

| 컴포넌트명 | 책임 | 비고 |
|------------|------|------|
| `VideoInfoHeader` | 제목·채널·길이·진행·태그·컬렉션 표시 | |
| `VideoPlayerPanel` | mock 플레이어 + 스크럽 range | `formatTimecode` |
| `VideoDetailMetaBar` | 상태·우선순위·컬렉션·중요 | 로컬 상태 |
| `ScriptPanel` | 스크립트 구간 목록·활성 구간·클릭 seek | |
| `MemoTimelinePanel` | 타임라인 메모·클릭 seek | |
| `HighlightSection` | 인용 + 시점 버튼 | |
| `RelatedInCollection` | 같은 컬렉션 카드 | `VIDEO_LIBRARY_MOCK` 기반 |
| `ReviewPointsSection` | 복습·정리 목록 | |
| `VideoScratchpadPanel` | 영상별 자유 필기 `textarea` · `localStorage` 자동 저장(디바운스)·수동 저장·비우기 | `videoId` |
| `formatTimecode` | 초 → 표시 문자열 | |

**스타일**: `video-detail.css` · **데이터**: `GET /api/v1/videos/{userVideoId}` (`videos.ts` 매퍼)

---

## 페이지 조합 (`src/components/common`)

| 컴포넌트명 | 책임 | props | 재사용 | 사용 페이지 |
|------------|------|-------|--------|-------------|
| `PagePlaceholder` | 단계별 플레이스홀더 | `title`, `description`, `bullets?` | 예 | 현재 라우트에서는 미사용 · 신규 페이지 스캐폴드용 |

---

## 타입 (`src/shared/types`)

| 파일 | 내용 |
|------|------|
| `learning.ts` | `LearningStatus`, `LearningPriority` |
| `cards.ts` | `VideoCardModel`(`contextHint?`), `ChannelCardModel`, `NoteCardModel` |
| `dashboard.ts` | `DashboardBundle`(`nextUp` null 가능), `WeeklyLearningSummary`, `QuickActionItem`, `NewUploadFromFavorite` |
| `api.ts` | 공통·로그인·대시보드·영상 라이브러리/상세 DTO (`VideoDetailResponseDto`, `VideoLibraryItemDto`, `ApiEnvelope` 등) |
| `video-library.ts` | `VideoLibraryEntry`, `VideoCollection`, `VideoLibrarySortId`, `VideoLengthFilterId` |
| `video-detail.ts` | `VideoDetailDocument`, `ScriptCue`, `TimelineNote`, `VideoHighlight`, `ReviewPoint`, `RelatedVideoBrief` |
| `channel-library.ts` | `ChannelSubscription`, `ChannelCategory`, `ChannelFocus`, `ChannelListFilterState`, `ChannelListSortId` |
| `watch-later.ts` | `WatchLaterEntry`, `WatchLaterIntent`, `WatchLaterFilterState`, `WatchLaterSortId` |
| `note-archive.ts` | `NoteArchiveEntry`, `NoteArchiveKind`, `NoteArchiveFilterState`, `NoteArchiveSortId` |
| `analytics.ts` | `AnalyticsBundle`, `AnalyticsCategoryShare`, `AnalyticsWeekDay`, `AnalyticsChannelRank`, `AnalyticsLengthBucket` |
| `settings.ts` | `SettingsProfile`, `SettingsNotificationPrefs`, `LinkedAccountRow`, `LinkedAccountStatus` |
| `index.ts` | 재export |

---

## Mock (`src/mocks`)

| 파일 | 내용 |
|------|------|
| `navigation.ts` | 사이드바 그룹·`badgeCount` |
| `channels.ts` | `CHANNEL_CATEGORIES` (구독 목록은 API) |
| `watchLater.ts` | `WATCH_LATER_MOCK`, `WATCH_LATER_STALE_DAYS`, `isWatchLaterStale` |
| `noteArchive.ts` | `NOTE_ARCHIVE_MOCK`, `NOTE_ARCHIVE_TAGS` |
| `analytics.ts` | `ANALYTICS_MOCK`, `formatLearningMinutes` |
| `settingsPreferences.ts` | `LEARNING_INTEREST_OPTIONS`, `DEFAULT_*`, `LINKED_ACCOUNTS_MOCK` |

---

## 상수 (`src/shared/constants`)

| 파일 | 내용 |
|------|------|
| `navigation.ts` | `SIDEBAR_NAV_GROUPS` 등 내비 재export |
| `storage.ts` | `ONBOARDING_DONE_STORAGE_KEY`, `read/write/clearOnboardingDone` |
| `authStorage.ts` | `ACCESS_TOKEN_STORAGE_KEY`, `get/set/clearAccessToken` |
| `videoCollections.ts` | `VIDEO_COLLECTIONS` — 컬렉션 select UI용(추후 API 대체) |

---

## 앱 진입

| 컴포넌트명 | 책임 | 구현 |
|------------|------|------|
| `AppProviders` | Router 마운트 | `src/app/providers/AppProviders.tsx` |
| `router` | 라우트 트리 | `src/app/router/index.tsx` |

---

## 갱신 이력

- step1: 레이아웃·플레이스홀더·라우터.  
- step2: `Topbar`/`Sidebar`로 명칭 정리, `shared/ui` 전 컴포넌트·mock 내비·토큰 연동.  
- step3: 학습 대시보드 피처·`dashboard` mock·`VideoCard.contextHint`.  
- step4: 영상 라이브러리 피처·`video-library` 타입·`videoLibrary` mock.  
- step5: 영상 상세 피처·`video-detail` 타입·`videoDetail` mock·`MainContent` 상세 폭.  
- step6: 구독 채널 피처·`channel-library` 타입·`channels` mock·`/videos?q=` 연동.  
- step7: 나중에 보기 피처·`watch-later` 타입·`watchLater` mock·일괄 바·오늘 큐.  
- step8: 메모 아카이브 피처·`note-archive` 타입·`noteArchive` mock·상세 `?t=` 시크.  
- step9: 학습 통계 피처·`analytics` 타입·`analytics` mock·CSS 막대 시각화.  
- step10: 설정 허브·온보딩·`settings` 타입·`settingsPreferences` mock·`storage` 상수·라우트 `/onboarding`.
