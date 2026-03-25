import type { LearningPriority } from '@/shared/types/learning'

/** 아카이브에서 다루는 한 줄 기록의 종류 */
export type NoteArchiveKind = 'memo' | 'highlight'

/** 영상을 가로지르는 메모·하이라이트 통합 행 — API/mock 공통 */
export type NoteArchiveEntry = {
  id: string
  kind: NoteArchiveKind
  videoId: string
  videoTitle: string
  channelName: string
  timeSec: number
  /** 표시용 타임코드 */
  timeLabel: string
  /** 메모 본문 또는 하이라이트 인용 */
  body: string
  tags: string[]
  /** 복습 큐에 올릴지 여부(mock) */
  reviewNeeded: boolean
  importance: LearningPriority
  /** 최근 작성순 정렬용 */
  createdAt: string
}

export type NoteArchiveSortId = 'recent_desc' | 'importance_desc'

export type NoteArchiveFilterState = {
  search: string
  kind: 'all' | NoteArchiveKind
  tagId: string
  /** true면 reviewNeeded만 */
  reviewOnly: boolean
  sortId: NoteArchiveSortId
}
