# step4-plan — 학습 영상 목록(라이브러리) 페이지

## 1. 단계 목표

- **저장된 학습 영상**을 검색·필터·정렬·뷰 전환으로 탐색하는 **`/videos` 페이지**를 구현한다.
- 데이터는 **mock + 로컬 상태**로 빠른 분류(상태·중요·폴더)를 시뮬레이션하고, 이후 API로 치환 가능한 타입을 유지한다.
- 톤은 **“유튜브 구독 목록”이 아니라 “학습 자산 카탈로그”**가 되도록 메타(상태·태그·컬렉션)를 전면에 둔다.

---

## 2. 이번 단계에서 해결할 사용자 문제

| 문제 | 대응 |
|------|------|
| 영상이 쌓이면 찾기 어렵다 | 검색 + 상태·태그·길이 필터 + 정렬 |
| 정리가 안 된 느낌 | 컬렉션 이동·중요 표시·상태를 카드/행에서 바로 변경 |
| 소비 앱처럼 느껴진다 | 제목 옆 메타 우선, 썸네일은 보조; “학습 자산” 카피 |
| 필터 조합 결과가 헷갈린다 | 빈 결과 시 안내 + 필터 초기화 CTA |

---

## 3. 대상 페이지 / 컴포넌트

- **페이지**: `VideosPage` (`src/pages/videos/VideosPage.tsx`).
- **피처** (`src/features/video-management/`): `VideosToolbar`, `VideoTagFilterBar`, `VideoLengthFilterBar`, `VideoLibraryGridCard`, `VideoLibraryListRow`, `filterAndSortVideos` 유틸.
- **재사용**: `PageHeader`, `FilterBar`(학습 상태), `Button`, `EmptyState`, `StatusBadge`(내부).

---

## 4. 화면 구성 요소

1. **PageHeader** — 제목·한 줄 설명.
2. **검색창** (`VideosToolbar`) — 제목·채널·태그 텍스트 매칭.
3. **학습 상태** — `FilterBar` 단일 선택(전체·미시청·진행·완료·보류).
4. **태그** — 다중 토글 칩(선택 없음 = 전체, 1개 이상 = **OR** 매칭).
5. **길이** — 짧음/중간/김/전체(분 단위 구간).
6. **정렬** — 최근 업데이트·제목·길이↑↓·진행률.
7. **뷰** — 그리드 / 리스트 토글.
8. **결과 영역** — `VideoLibraryGridCard` 또는 `VideoLibraryListRow`.
9. **카드/행 액션** — 상태 `<select>`, 중요(별) 토글, 컬렉션 `<select>`.

---

## 5. 상태 및 데이터 구조

- `VideoLibraryEntry` — `tags`, `durationMinutes`, `isStarred`, `collectionId`, `updatedAt` 등 (`shared/types/video-library.ts`).
- `VideoCollection` — id, name (mock 목록).
- 클라이언트 상태: `items: VideoLibraryEntry[]` (mock 초기화 후 갱신), 필터·검색·정렬·뷰 모드.

---

## 6. UX 결정 사항

- **정리감**: 툴바 한 덩어리 + 필터는 시각적 그룹(라벨 micro + 칩).
- **빠른 분류**: 그리드에서도 본문 클릭은 상세 이동, **액션 줄은 클릭 전파 차단**.
- **중요 표시**: 별 아이콘(텍스트 ★/☆), `aria-pressed`.
- **빈 상태**: 라이브러리 0건 vs **필터로 0건** 문구 구분.

---

## 7. 구현 범위

- `step4-plan` / `step4-result`, `components.md` 갱신.
- mock `VIDEO_LIBRARY_MOCK`, `VIDEO_COLLECTIONS`, `VIDEO_TAG_OPTIONS`.
- 목록 페이지 전체 UI + 로컬 필터/정렬/뷰.
- 서버 저장·실검색 API·무한 스크롤·키보드 단축키 제외.

---

## 8. 제외 범위

- 실제 YouTube 동기화·서버 persist.
- 드래그앤드롭 일괄 이동.
- 고급 검색(연산자).
- 단위 테스트.
