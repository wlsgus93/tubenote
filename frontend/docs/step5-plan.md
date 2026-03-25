# step5-plan — 학습용 영상 상세(플레이어·스크립트·메모 연동)

## 1. 단계 목표

- **영상·스크립트·메모·하이라이트·복습**이 한 흐름으로 이어지는 **`/videos/:videoId` 상세 페이지**를 구현한다.
- 실제 YouTube 임베드 없이 **mock 재생 위치(`currentTime`)** 로 타임라인 인터랙션을 검증한다.
- 레이아웃은 **좌: 정보+플레이어+메타 액션**, **우: 스크립트/메모 탭**, **하단: 하이라이트·같은 컬렉션·복습 포인트**를 따른다.

---

## 2. 이번 단계에서 해결할 사용자 문제

| 문제 | 대응 |
|------|------|
| 영상만 보고 끝나 버린다 | 스크립트·메모·복습 블록을 같은 페이지에 두어 **학습 모드** 유지 |
| 스크립트와 메모 왔다 갔다 불편 | 우측 **탭(스크립트 \| 메모)** 으로 전환 부담 최소화 |
| 타임라인과 싱크가 안 맞는 느낌 | 큐/메모/하이라이트 **타임코드 클릭 → mock seek** |
| 다음에 무엇을 볼지 끊긴다 | **같은 컬렉션 관련 영상** 스트립 |

---

## 3. 대상 페이지 / 컴포넌트

- **페이지**: `VideoDetailPage`.
- **피처** `src/features/video-detail/`: 정보 헤더, 플레이어 패널, 메타 액션 바, `ScriptPanel`, `MemoTimelinePanel`, `HighlightSection`, `RelatedInCollection`, `ReviewPointsSection`, `formatTimecode` 유틸.
- **재사용**: `TabMenu`, `Button`, `StatusBadge`, `EmptyState`, `VIDEO_COLLECTIONS` / `VIDEO_LIBRARY_MOCK` (관련 영상).

---

## 4. 화면 구성 요소

1. **상단 정보**: 제목, 채널, 길이·진행률, 태그 pill.
2. **플레이어 영역**: 16:9 플레이스홀더, 현재 시각/총 길이, **range 스크럽(mock)**.
3. **메타 액션**: 학습 상태·우선순위·컬렉션 변경(select), 중요 토글 — 로컬 상태.
4. **우측 패널**: `TabMenu` — **스크립트** | **메모(타임라인)**.
5. **하단**: 하이라이트 목록 → 관련 영상(같은 컬렉션) → 복습 포인트.

---

## 5. 상태 및 데이터 구조

- `VideoDetailDocument` (`shared/types/video-detail.ts`): 메타 + `scriptCues`, `timelineNotes`, `highlights`, `relatedInCollection`, `reviewPoints`.
- `getVideoDetailMock(videoId)` — `VIDEO_LIBRARY_MOCK`와 병합, 없으면 `null` → 빈/404 유사 UI.
- 페이지 로컬 상태: `currentTimeSec`, `rightTab`, 메타 필드 복사본.

---

## 6. UX 결정 사항

- **학습 화면** 톤: 페이지 제목 대신 **영상 제목**을 최상단에 두고, 부가 설명은 최소화.
- **활성 큐**: `currentTime`이 스크립트 구간에 들어가면 해당 행 **시각 강조**.
- **탭 기본값**: 첫 진입 **스크립트**; 메모 탭은 타임라인 순 정렬.
- **본문 폭**: 상세만 `MainContent` **비제한 폭**으로 좌우 2열 여유 확보.

---

## 7. 구현 범위

- `step5-plan` / `step5-result`, `components.md`·`routes.md` 소폭 갱신.
- mock·타입·피처 컴포넌트·`video-detail.css`·`VideoDetailPage` 전면 구현.
- `MainContent`에서 `/videos/:id` 경로일 때 `constrained={false}`.

---

## 8. 제외 범위

- 실제 비디오 임베드·스트리밍.
- 메모/하이라이트 **서버 저장**·협업.
- STT 동기 스크립트 정밀 싱크.
- 키보드 단축키 전체 세트.
