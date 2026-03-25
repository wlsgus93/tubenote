import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

/** 학습 자산 단위 — 목록·필터·mock/API 공통 */
export type VideoLibraryEntry = {
  id: string
  title: string
  channelName: string
  thumbnailUrl?: string
  durationLabel: string
  /** 길이 필터·정렬용(분) */
  durationMinutes: number
  progressPercent: number
  learningStatus: LearningStatus
  priority?: LearningPriority
  reviewNeeded?: boolean
  tags: string[]
  /** 중요(즐겨 찾는 학습 자산) */
  isStarred: boolean
  collectionId: string
  /** 최근 활동 기준 정렬 */
  updatedAt: string
}

export type VideoCollection = {
  id: string
  name: string
}

export type VideoLibrarySortId =
  | 'updated_desc'
  | 'title_asc'
  | 'duration_asc'
  | 'duration_desc'
  | 'progress_desc'

export type VideoLengthFilterId = 'all' | 'short' | 'medium' | 'long'
