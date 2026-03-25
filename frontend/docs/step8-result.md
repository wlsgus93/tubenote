# step8-result — 메모·하이라이트 아카이브

## 1. 구현 완료 항목

- **`/notes` 페이지**에서 메모·하이라이트를 **학습 흔적** 단위로 통합 목록화.
- **필터**: 전체/메모만/하이라이트만, **복습 필요만**, **태그별**.
- **정렬**: 최근 작성순, 중요도순(동순위는 최근 작성).
- **보기**: 카드 / 리스트 전환.
- **영상 링크**: 상세만 열기 / **`?t=초` 시점 점프** — `VideoDetailPage`에서 메모 탭·재생 시각 반영 후 쿼리 제거.

---

## 2. 생성/수정한 파일 목록

| 구분 | 경로 |
|------|------|
| 신규 | `docs/step8-plan.md`, `docs/step8-result.md` |
| 신규 | `src/shared/types/note-archive.ts` |
| 신규 | `src/mocks/noteArchive.ts` |
| 신규 | `src/features/note-archive-management/filterNoteArchive.ts` |
| 신규 | `src/features/note-archive-management/NoteArchiveToolbar.tsx` |
| 신규 | `src/features/note-archive-management/NoteArchiveCard.tsx` |
| 신규 | `src/features/note-archive-management/NoteArchiveListRow.tsx` |
| 신규 | `src/features/note-archive-management/note-archive.css` |
| 신규 | `src/features/note-archive-management/index.ts` |
| 수정 | `src/pages/notes/NotesPage.tsx` |
| 수정 | `src/pages/video-detail/VideoDetailPage.tsx` (`t` 쿼리) |
| 수정 | `src/shared/types/index.ts` |
| 수정 | `docs/components.md`, `docs/routes.md`, `docs/ui-decisions.md` |

---

## 3. 핵심 컴포넌트 설명

- **`NoteArchiveToolbar`**: 검색, 정렬, 카드/리스트 토글.
- **`NoteArchiveCard`**: eyebrow「학습 흔적」, 영상·채널·타임코드, 본문, 태그·중요도·복습 뱃지, 시점/영상 버튼.
- **`NoteArchiveListRow`**: 스캔용 밀도 높은 행 + 동일 액션.
- **`filterAndSortNoteArchive`**: 필터·정렬 순수 함수.

---

## 4. mock 데이터 구조 설명

- **`NoteArchiveEntry`**: `kind`, `videoId`, `videoTitle`, `channelName`, `timeSec`/`timeLabel`, `body`, `tags`, `reviewNeeded`, `importance`, `createdAt`.
- **`NOTE_ARCHIVE_MOCK`**: `lib-1`~`lib-11` 등 라이브러리 id와 맞춘 12건.
- **`NOTE_ARCHIVE_TAGS`**: 필터 칩용 태그 목록.

---

## 5. UX 반영 사항

- **학습 흔적** 카피(헤더·카드 eyebrow)로 단순 메모장이 아닌 맥락 강조.
- 복습은 **필터 + 뱃지** 이중 진입.
- 타임코드는 **primary 톤**으로 점프 액션과 시각적 연결.

---

## 6. 아쉬운 점 / 이후 개선점

- mock 정적 데이터; 실제로는 상세 `timelineNotes`·`highlights`와 단일 소스 동기화 필요.
- 하이라이트 진입 시 메모 탭만 열림 — 추후 `tab` 쿼리로 하이라이트 구역 포커스 가능.
- 편집·삭제·보내기 없음.

---

## 7. 다음 단계 TODO

- 아카이브 ↔ 상세 **단일 데이터 파생**(빌드 타임 또는 API).
- **기간 필터**(이번 주 작성 등).
