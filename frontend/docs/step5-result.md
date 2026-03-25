# step5-result — 5단계 구현 결과

## 1. 구현 완료 항목

- **`/videos/:videoId` 학습 상세**: 좌측 정보·mock 플레이어·메타 액션, 우측 **스크립트 \| 메모** 탭, 하단 하이라이트·같은 컬렉션·복습 포인트.
- **타임스탬프 mock**: 스크립트 구간·메모·하이라이트·스크럽 바가 동일 `currentTimeSec` 상태를 갱신.
- **활성 스크립트 행**: 재생 위치가 구간 안에 있을 때 시각 강조.
- **mock 데이터**: `getVideoDetailMock` — 라이브러리 항목 병합, `lib-1`·`lib-2` 풍부 스크립트/메모, 나머지는 generic.
- **레이아웃 폭**: `MainContent`가 `/videos/:id` 에서만 `constrained` 해제.
- **대시보드 연동**: `nextUp`/큐 첫 항목 id를 `lib-2`로 맞춰 상세 mock과 이어지도록 조정.

---

## 2. 생성/수정한 파일 목록

### 문서

- `docs/step5-plan.md`, `docs/step5-result.md`

### 타입

- `src/shared/types/video-detail.ts`
- `src/shared/types/index.ts`

### Mock

- `src/mocks/videoDetail.ts`
- `src/mocks/dashboard.ts` (`lib-2` 정렬)

### 피처 `src/features/video-detail/`

- `timecode.ts`, `video-detail.css`
- `VideoInfoHeader.tsx`, `VideoPlayerPanel.tsx`, `VideoDetailMetaBar.tsx`
- `ScriptPanel.tsx`, `MemoTimelinePanel.tsx`
- `HighlightSection.tsx`, `RelatedInCollection.tsx`, `ReviewPointsSection.tsx`
- `index.ts`

### 페이지·레이아웃

- `src/pages/video-detail/VideoDetailPage.tsx`
- `src/components/layout/MainContent.tsx`

---

## 3. 핵심 컴포넌트

| 컴포넌트 | 역할 |
|----------|------|
| `VideoPlayerPanel` | 16:9 목업 + range 스크럽 |
| `ScriptPanel` | 구간 클릭 → `seekTo` |
| `MemoTimelinePanel` | 시각 정렬 메모, 근접 시 강조 |
| `HighlightSection` | 인용 + 시점 이동 버튼 |
| `RelatedInCollection` | 같은 컬렉션 카드 → 라우팅 |
| `ReviewPointsSection` | 복습·정리 목록 |
| `VideoDetailMetaBar` | 상태·우선순위·컬렉션·중요(로컬) |

---

## 4. mock 데이터

- `VideoDetailDocument`: 스크립트 큐, 타임라인 메모, 하이라이트, 복습 포인트, `relatedInCollection`(라이브러리에서 계산).
- 미등록 `videoId` → 상세 대신 `EmptyState` + 목록/대시보드 이동.

---

## 5. UX 반영

- 페이지 톤: **학습 화면**(플레이스홀더 카피, 메타·탭·복습 블록).
- 스크립트/메모 **탭 전환**으로 시야 분리, 하단에 인용·연속 학습·정리.
- 라이브러리에 없는 대시보드 영상 id는 여전히 404 상세 가능 — 주요 진입은 `lib-*` 권장.

---

## 6. 아쉬운 점 / 이후 개선점

- 실제 `<video>`/YouTube iframe·자막 트랙 동기화 없음.
- 메모/메타 변경 **persist** 없음(새로고침 시 mock 초기값).
- 스크립트·메모 **분할 패널**(탭 대신) 옵션은 미구현.

---

## 7. 다음 단계 TODO (step6 제안)

1. 메모 작성/수정 UI 및 저장 레이어 목업.
2. 플레이어와 `timeupdate` 이벤트로 `currentTime` 양방향 동기화.
3. `docs/step6-plan.md` 작성.
