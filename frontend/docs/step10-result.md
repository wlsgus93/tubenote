# step10-result — 설정·온보딩·문서 마감

## 1. 구현 완료 항목

- **`docs/step10-plan.md`** · **`docs/project-structure.md`** · 본 **`step10-result.md`**
- **설정 페이지**: 프로필(이름 편집·이메일 읽기 전용)·학습 관심사 칩·알림 3종 토글·연동 계정 카드(mock)·저장 메시지 mock·온보딩 재진입
- **온보딩 페이지** (`/onboarding`): 4단계(환영 → 관심사 → 알림 → 완료), 완료/건너뛰기 시 **`localStorage`** (`ylh.onboarding.v1`)
- **라우터**: `/onboarding` 추가, 로그인 페이지에 온보딩 링크
- **문서**: `routes.md`, `components.md`, `ui-decisions.md` 최종 반영

---

## 2. 생성/수정한 파일 목록 (요약)

| 구분 | 경로 |
|------|------|
| 신규 | `docs/step10-plan.md`, `docs/step10-result.md`, `docs/project-structure.md` |
| 신규 | `src/shared/types/settings.ts`, `src/shared/constants/storage.ts` |
| 신규 | `src/mocks/settingsPreferences.ts` |
| 신규 | `src/features/settings-hub/*`, `src/features/onboarding-flow/onboarding.css` |
| 신규 | `src/pages/onboarding/OnboardingPage.tsx` |
| 수정 | `src/pages/settings/SettingsPage.tsx`, `src/pages/auth/LoginPage.tsx` |
| 수정 | `src/app/router/index.tsx`, `src/shared/types/index.ts` |
| 수정 | `docs/routes.md`, `docs/components.md`, `docs/ui-decisions.md` |

---

## 3. mock 기반인 부분 (명시)

- **모든 학습 콘텐츠·통계·구독·나중에 보기·메모 아카이브**: `src/mocks/*.ts` 정적 데이터 및 로컬 `useState` 갱신(페이지 새로고침 시 초기화되는 항목 다수).
- **설정 저장**: “저장했습니다 (로컬 mock)” 메시지만; **서버·localStorage 영속화 없음**(온보딩 플래그 제외).
- **연동 계정**: YouTube/Google **상태·버튼 비활성** mock; OAuth 없음.
- **알림**: 토글만 존재, **실제 푸시/이메일 없음**.
- **온보딩에서 고른 관심사·알림**: 완료 후 **설정 페이지와 동기화되지 않음**(각각 독립 상태; API 시 단일 프로필로 합칠 예정).

---

## 4. 추후 API 연결 포인트 (제안)

| 영역 | 연결 시 후보 |
|------|----------------|
| 인증 | `POST /auth/*`, 세션·토큰; `LoginPage` 폼 |
| 프로필·설정 | `GET/PATCH /me`, `PATCH /settings/notifications`, `PATCH /settings/interests` |
| 온보딩 완료 | `PATCH /users/me`의 `onboardingDoneAt` 등 → `localStorage` 제거 |
| YouTube 연동 | OAuth 콜백 후 `GET /integrations/youtube/status` |
| 학습 자산·상세 | `videoLibrary`·`getVideoDetailMock` → REST/GraphQL |
| 통계 | `ANALYTICS_MOCK` → 집계 API(시청 로그·메모 이벤트) |
| 사이드바 배지 | `SIDEBAR_NAV_MOCK` → 알림·큐 카운트 API |

---

## 5. MVP 핵심 페이지 (우선순위 관점)

1. **`/dashboard`** — 오늘 할 일·이어보기(학습 허브 진입)  
2. **`/videos` + `/videos/:videoId`** — 자산 목록·학습 상세(메모·스크립트·시크)  
3. **`/watch-later`** — 학습 큐·오늘 편성  
4. **`/notes`** — 학습 흔적 아카이브·복습 필터  
5. **`/subscriptions`** — 채널을 학습 리소스로 정리  
6. **`/analytics`** — 동기 부여형 요약(데이터 연동 후 가치 증가)  
7. **`/settings`**, **`/onboarding`**, **`/login`** — 계정·취향·첫 경험(백엔드 붙은 뒤 필수도 상승)

---

## 6. 추후 개발 순서 제안

1. **인증 + 사용자 설정 API** — 로그인, 프로필, 온보딩 플래그 서버화  
2. **영상·진행도 API** — 라이브러리·상세·상태 변경의 영속화  
3. **YouTube 읽기 연동** — 구독·나중에 보기 동기화(읽기 전용부터)  
4. **메모/하이라이트 API** — 아카이브를 상세와 단일 소스로  
5. **시청 로그·집계** — 통계 페이지를 mock에서 실데이터로  
6. **알림 채널** — 설정 토글과 FCM/이메일 워커 연결  

---

## 7. 아쉬운 점 / 이후 개선점

- 온보딩 강제 게이트 없음(의도적; 인증 도입 시 라우트 가드와 함께 추가 권장).  
- 설정과 온보딩 상태 **단일화** 필요.  
- `PagePlaceholder`는 **현재 어떤 라우트 페이지에서도 사용하지 않음**(`grep` 기준). 신규 스캐폴드용으로만 유지.

---

## 8. 다음 단계 TODO

- `src/services/` 도입 및 mock 어댑터 패턴 통일.  
- E2E·스토리북(선택)으로 핵심 플로우 검증.
