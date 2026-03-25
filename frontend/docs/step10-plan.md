# step10-plan — 설정·온보딩·문서 마감

## 1. 단계 목표

- **설정 페이지**를 프로필·학습 관심사·알림·연동 계정(mock)으로 구현한다.
- **온보딩 플로우**를 앱 쉘 밖 단계 화면으로 제공하고, 완료 플래그는 **localStorage**에만 저장한다.
- **라우트·components·ui-decisions·프로젝트 구조 문서**를 최종 정리한다.

---

## 2. 이번 단계에서 해결할 사용자 문제

| 문제 | 대응 |
|------|------|
| “내 학습 취향이 반영된다”는 느낌 부족 | 관심사 칩·알림 토글 |
| 외부 연동이 있는지 불안 | 연동 카드에 상태·설명 |
| 신규 사용자 진입 동선 부재 | `/onboarding` 다단계 + 로그인/설정에서 진입 링크 |

---

## 3. 대상 페이지 / 컴포넌트

- **페이지**: `SettingsPage`, `OnboardingPage` (`/onboarding`).
- **피처**: `src/features/settings-hub/`, `src/features/onboarding-flow/`(스타일).
- **라우터**: `app/router/index.tsx`에 `onboarding` 추가.
- **문서**: `step10-result.md`, `docs/project-structure.md`, `components.md`, `ui-decisions.md`, `routes.md`.

---

## 4. 화면 구성 요소

### 설정

- 프로필: 표시 이름, 이메일(읽기 전용 힌트 + API 연동 예정).
- 학습 관심사: 다중 토글 칩.
- 알림: 이메일 요약·학습 리마인더·주간 요약(토글).
- 연동: YouTube/Google 등 카드 + 상태 뱃지 + mock 버튼.

### 온보딩

- 4단계: 환영 → 관심사 → 알림 → 완료 CTA.
- 완료 시 `localStorage` 키 저장 후 `/dashboard` 이동.

---

## 5. 상태 및 데이터 구조

- `shared/types/settings.ts` — 프로필·알림·연동 행 타입.
- `mocks/settingsPreferences.ts` — 기본값·관심사 옵션·연동 mock.
- `shared/constants/storage.ts` — 온보딩 완료 키.

---

## 6. UX 결정 사항

- 설정은 **섹션 카드**로 나누어 스캔 용이.
- 온보딩은 **로그인과 동일하게 앱 쉘 밖** 단순 레이아웃.
- 기본 진입 `/` → 대시보드는 유지; 온보딩은 **명시 링크**로 진입(개발 편의).

---

## 7. 구현 범위

- 위 페이지·피처·상수·문서 전부.

---

## 8. 제외 범위

- 실제 OAuth·푸시·이메일 발송.
- 서버 프로필 저장.
- 온보딩 강제 게이트(추후 인증과 함께).
