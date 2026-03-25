# step7-result — 나중에 보기 학습 큐

## 1. 구현 완료 항목

- **`/watch-later` 페이지**를 학습 계획·오늘 큐 중심으로 구현.
- **학습용/비학습용** 의도, **우선순위**, **오늘 볼 영상 큐**(순서 ↑↓), **오래됨** 배지(21일+), **일괄 액션 바**, **컬렉션 이동**(행 단위·일괄).
- Mock 데이터·필터·정렬·`libraryVideoId`가 있을 때 **학습 자산 상세** 이동.

---

## 2. 생성/수정한 파일 목록

| 구분 | 경로 |
|------|------|
| 신규 | `docs/step7-plan.md`, `docs/step7-result.md` |
| 신규 | `src/shared/types/watch-later.ts` |
| 신규 | `src/mocks/watchLater.ts` |
| 신규 | `src/features/watch-later-management/filterWatchLater.ts` |
| 신규 | `src/features/watch-later-management/queueOrder.ts` |
| 신규 | `src/features/watch-later-management/WatchLaterToolbar.tsx` |
| 신규 | `src/features/watch-later-management/TodayQueueStrip.tsx` |
| 신규 | `src/features/watch-later-management/WatchLaterListRow.tsx` |
| 신규 | `src/features/watch-later-management/BulkActionBar.tsx` |
| 신규 | `src/features/watch-later-management/watch-later.css` |
| 신규 | `src/features/watch-later-management/index.ts` |
| 수정 | `src/pages/watch-later/WatchLaterPage.tsx` |
| 수정 | `src/shared/types/index.ts` |
| 수정 | `docs/components.md`, `docs/routes.md`, `docs/ui-decisions.md` |

---

## 3. 핵심 컴포넌트 설명

- **`TodayQueueStrip`**: 오늘 큐만 순번·메타·순서 변경·큐 제거·학습 화면 이동; 비어 있을 때 부담 완화 카피 + 앵커 링크.
- **`WatchLaterListRow`**: 체크박스, 의도 토글, 우선순위·컬렉션 select, 오늘 큐 토글, 오래됨·큐 뱃지.
- **`BulkActionBar`**: 선택 시 하단 고정 — 큐 넣기/빼기, 의도·우선순위, 컬렉션 이동, 목록 제거.
- **`filterAndSortWatchLater` / 큐 유틸**: 계획 순(큐 우선)·우선순위·담은 날·길이 정렬, 큐 순서 정규화.

---

## 4. mock 데이터 구조 설명

- **`WatchLaterEntry`**: `intent`, `priority`, `inTodayQueue`, `todayQueueOrder`, `collectionId`, `addedAt`, 선택적 `libraryVideoId`.
- **`WATCH_LATER_STALE_DAYS`**: 21일 초과 시 `오래됨` 배지.
- **`WATCH_LATER_MOCK`**: 학습/비학습·오래된 항목·큐 3개 시드.

---

## 5. UX 반영 사항

- 헤더·빈 큐에서 **“전부 비울 필요 없음”**, **1~3개** 언급으로 압박 완화.
- 정렬 라벨 **「학습 계획 순」** 등 계획 언어 사용.
- 오래됨은 **muted 배지**로 경고 과다 사용 억제.

---

## 6. 아쉬운 점 / 이후 개선점

- 드래그 앤 드롭 없음(↑↓ 버튼).
- 나중에 보기 ↔ 학습 자산 **실제 동기화** 없음(`libraryVideoId` 수동).
- 일괄 작업 **실행 취소** 없음.

---

## 7. 다음 단계 TODO

- 나중에 보기에서 **학습 자산으로 승격** 단일 액션(백엔드 설계 후).
- 오늘 큐 **예상 소요 시간** 합계 표시.
