import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

/** 스크립트 한 구간(자막/요약 행) */
export type ScriptCue = {
  id: string
  startSec: number
  endSec: number
  text: string
}

/** 타임라인 메모 */
export type TimelineNote = {
  id: string
  timeSec: number
  body: string
  createdLabel: string
}

/** 하이라이트(인용 + 시점) */
export type VideoHighlight = {
  id: string
  timeSec: number
  quote: string
}

export type RelatedVideoBrief = {
  id: string
  title: string
  durationLabel: string
  learningStatus: LearningStatus
  progressPercent: number
}

/** 복습·학습 정리 포인트 */
export type ReviewPoint = {
  id: string
  title: string
  detail: string
}

/** 상세 페이지 단일 문서(mock/API) */
export type VideoDetailDocument = {
  id: string
  title: string
  channelName: string
  durationLabel: string
  durationSec: number
  progressPercent: number
  learningStatus: LearningStatus
  priority?: LearningPriority
  reviewNeeded: boolean
  tags: string[]
  isStarred: boolean
  collectionId: string
  collectionName: string
  scriptCues: ScriptCue[]
  timelineNotes: TimelineNote[]
  highlights: VideoHighlight[]
  relatedInCollection: RelatedVideoBrief[]
  reviewPoints: ReviewPoint[]
}
