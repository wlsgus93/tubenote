import type { NoteCardModel, VideoCardModel } from '@/shared/types/cards'

/** 즐겨찾기 채널 신규 업로드 행 */
export type NewUploadFromFavorite = VideoCardModel & {
  uploadedAtLabel: string
}

export type QuickActionItem = {
  id: string
  label: string
  to: string
  variant?: 'primary' | 'secondary' | 'ghost'
}

export type WeeklyLearningSummary = {
  completedCount: number
  minutesTotal: number
  streakDays: number
  reviewDueCount: number
}

/** 대시보드 API 매핑 후 화면 단위 — `nextUp` 없으면 상단 콜아웃 숨김 */
export type DashboardBundle = {
  nextUp: VideoCardModel | null
  todayQueue: VideoCardModel[]
  /** 콜아웃 제외한 이어보기 후보 */
  continueWatching: VideoCardModel[]
  recentNotes: NoteCardModel[]
  incompleteVideos: VideoCardModel[]
  newFromFavorites: NewUploadFromFavorite[]
  weekly: WeeklyLearningSummary
  quickActions: QuickActionItem[]
}
