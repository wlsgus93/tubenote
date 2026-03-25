# step6-plan — 학습 친화적 구독 채널 정리 페이지

## 1. 단계 목표

- **`/subscriptions` 페이지**를 유튜브 구독 피드가 아니라 **학습 리소스·의도 관리** 화면으로 구현한다.
- **카테고리·학습/일반·즐겨찾기·검색·정렬**로 채널을 빠르게 좁히고, **상세 패널**에서 메모·통계·영상 목록 진입을 한 번에 본다.

---

## 2. 이번 단계에서 해결할 사용자 문제

| 문제 | 대응 |
|------|------|
| 채널이 쌓이면 왜 구독했는지 잊는다 | 채널별 **메모(구독 이유)** + 상세 패널에서 전체 편집(mock) |
| 학습용·가벼운 구독이 섞인다 | **학습용 / 일반** 배지·필터 |
| 최근 올라온 곳을 놓친다 | **신규 영상 수** + **최근 업로드** 문구·정렬 |
| 다음 행동이 불명확 | **이 채널 영상 보기** → 학습 자산 목록 `?q=` 연동 |

---

## 3. 대상 페이지 / 컴포넌트

- **페이지**: `SubscriptionsPage`.
- **피처** `src/features/subscription-management/`: `filterChannels`, `ChannelsToolbar`, `SubscriptionChannelCard`, `ChannelDetailPanel`, `channel-library.css`.
- **재사용**: `PageHeader`, `FilterBar`, `Button`, `EmptyState`, `TabMenu`(불필요 시 생략).

---

## 4. 화면 구성 요소

1. **PageHeader** — “구독 채널” 대신 **학습 채널** 톤 설명.
2. **툴바** — 검색(이름·메모), 정렬(이름 / 신규 많은 순 / 최근 업로드).
3. **필터** — 카테고리 칩, **학습용·일반·전체**, **즐겨찾기만**.
4. **채널 그리드** — 카드에 배지·별·신규 수·메모 한 줄.
5. **상세 패널** — 선택 채널: 메모 textarea, 통계, 유형·즐겨찾기 토글, **영상 목록으로** 링크.

---

## 5. 상태 및 데이터 구조

- `ChannelCategory`, `ChannelSubscription` (`shared/types/channel-library.ts`): `categoryId`, `focus: learning|general`, `isFavorite`, `newVideoCount`, `lastUploadAt`, `memo`, `savedVideoCount` 등.
- Mock: `CHANNEL_CATEGORIES`, `CHANNEL_SUBSCRIPTIONS_MOCK`.
- 페이지: `channels` 로컬 상태(즐겨찾기·메모·유형·카테고리 변경 mock).

---

## 6. UX 결정 사항

- **리소스 카탈로그** 톤: 카드에 카테고리·유형·신규가 먼저 보이게.
- **선택 유지**: 카드 클릭 시 우측(넓은 화면)·하단(좁은 화면) 패널 갱신.
- **흐름 끊김 최소화**: 상세에서 바로 학습 자산으로 `?q=채널명` 이동.

---

## 7. 구현 범위

- `step6-plan` / `step6-result`, `components.md`·`routes.md` 갱신.
- `VideosPage`에 `q` 쿼리 1회 반영(구독 → 목록 진입).

---

## 8. 제외 범위

- 실제 YouTube API·동기화.
- 썸네일 이미지·채널 구독/해지 API.
- 무한 스크롤·푸시 알림.
