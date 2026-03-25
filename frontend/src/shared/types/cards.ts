import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

/** VideoCard — mock/API 대체 용이한 경량 모델 */
export type VideoCardModel = {
  id: string
  title: string
  channelName: string
  thumbnailUrl?: string
  /** 0~100, 없으면 진행 바 숨김 */
  progressPercent?: number
  durationLabel?: string
  learningStatus: LearningStatus
  priority?: LearningPriority
  /** 복습 권장 여부 */
  reviewNeeded?: boolean
  /** 큐 순위·학습 맥락 한 줄 (대시보드 등) */
  contextHint?: string
}

export type ChannelCardModel = {
  id: string
  name: string
  /** 아바타 대체 문자 1글자 */
  initial?: string
  videoCount?: number
  lastActivityLabel?: string
}

export type NoteCardModel = {
  id: string
  videoTitle: string
  timecode: string
  excerpt: string
  createdAtLabel?: string
  reviewSuggested?: boolean
}
