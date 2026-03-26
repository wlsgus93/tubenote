/** 구독 채널을 학습 리소스로 정리할 때 사용하는 유형 */
export type ChannelFocus = 'learning' | 'general'

/** 채널 목록 정렬 키 */
export type ChannelListSortId = 'name_asc' | 'new_desc' | 'upload_desc'

/** 채널 상세의 최근 피드 한 줄 — GET `/api/v1/subscriptions/{id}/recent-videos` 매핑 */
export type ChannelRecentFeedItem = {
  youtubeVideoId: string
  title: string
  publishedAtLabel?: string
}

/** 구독 채널 한 행 — API 연동 시 동일 필드로 매핑 */
export type ChannelSubscription = {
  id: string
  name: string
  /** 아바타 글자 (없으면 name 첫 글자) */
  initial?: string
  categoryId: string
  focus: ChannelFocus
  isFavorite: boolean
  /** `unreadNewVideoCount`(아직 학습 자산에 안 담은 피드 영상 수) */
  newVideoCount: number
  /** 최근 업로드 시각 — 정렬·표시용 */
  lastUploadAt: string
  /** 구독 이유·학습 목적 등 사용자 메모 */
  memo: string
  /** 백엔드 미제공 시 UI용 0 */
  savedVideoCount: number
  /** 목록만 로드할 때는 비움 — 상세에서 `recent-videos` 로 채울 수 있음 */
  recentFeed?: ChannelRecentFeedItem[]
}

export type ChannelCategory = {
  id: string
  name: string
}

/** 목록 툴바·필터 상태 */
export type ChannelListFilterState = {
  search: string
  categoryId: string | 'all'
  focus: 'all' | ChannelFocus
  favoritesOnly: boolean
  sortId: ChannelListSortId
}
