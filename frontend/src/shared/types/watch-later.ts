import type { LearningPriority } from '@/shared/types/learning'

/** 나중에 보기 항목의 시청 의도 — 학습 계획과 휴식을 분리 */
export type WatchLaterIntent = 'learning' | 'casual'

/** 큐·목록 공통 행 — API 연동 시 필드 확장 */
export type WatchLaterEntry = {
  id: string
  title: string
  channelName: string
  durationLabel: string
  durationMinutes: number
  /** 나중에 보기에 담은 시각(ISO) — 오래됨·정렬 */
  addedAt: string
  intent: WatchLaterIntent
  priority: LearningPriority
  inTodayQueue: boolean
  /** 오늘 큐 정렬(작을수록 위) */
  todayQueueOrder: number
  /** 학습 자산 컬렉션과 동일 id — 정리·이동용 */
  collectionId: string
  /** 있으면 상세 페이지로 이동 가능(mock) */
  libraryVideoId?: string
}

export type WatchLaterSortId =
  | 'plan_order'
  | 'priority_desc'
  | 'added_asc'
  | 'added_desc'
  | 'duration_asc'

export type WatchLaterFilterState = {
  search: string
  intent: 'all' | WatchLaterIntent
  sortId: WatchLaterSortId
}
