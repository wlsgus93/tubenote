/** 구독 채널을 학습 리소스로 정리할 때 사용하는 유형 */
export type ChannelFocus = 'learning' | 'general'

/** 채널 목록 정렬 키 */
export type ChannelListSortId = 'name_asc' | 'new_desc' | 'upload_desc'

/** 구독 채널 한 행 — API 연동 시 동일 필드로 매핑 */
export type ChannelSubscription = {
  id: string
  name: string
  /** 아바타 글자 (없으면 name 첫 글자) */
  initial?: string
  categoryId: string
  focus: ChannelFocus
  isFavorite: boolean
  /** 최근 N일 내 신규로 집계한 영상 수(mock) */
  newVideoCount: number
  /** 최근 업로드 시각 — 정렬·표시용 */
  lastUploadAt: string
  /** 구독 이유·학습 목적 등 사용자 메모 */
  memo: string
  /** 학습 자산에 저장해 둔 해당 채널 영상 수(mock) */
  savedVideoCount: number
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
