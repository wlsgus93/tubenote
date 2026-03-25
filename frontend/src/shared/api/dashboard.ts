import { apiGet } from '@/shared/api/client'
import type { NoteCardModel, VideoCardModel } from '@/shared/types/cards'
import type { DashboardBundle, NewUploadFromFavorite, QuickActionItem, WeeklyLearningSummary } from '@/shared/types/dashboard'
import type {
  DashboardPayloadDto,
  NoteCardResponseDto,
  VideoCardResponseDto,
} from '@/shared/types/api'
import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

const DEFAULT_QUICK_ACTIONS: QuickActionItem[] = [
  { id: 'qa-queue', label: '나중에 보기 큐', to: '/watch-later', variant: 'secondary' },
  { id: 'qa-notes', label: '메모 허브', to: '/notes', variant: 'ghost' },
  { id: 'qa-videos', label: '전체 영상', to: '/videos', variant: 'ghost' },
  { id: 'qa-analytics', label: '학습 통계', to: '/analytics', variant: 'ghost' },
  { id: 'qa-subs', label: '구독 채널', to: '/subscriptions', variant: 'ghost' },
]

const LEARNING_STATUSES: LearningStatus[] = ['not_started', 'in_progress', 'completed', 'on_hold']
const PRIORITIES: LearningPriority[] = ['low', 'normal', 'high']

function parseLearningStatus(v: unknown): LearningStatus {
  return typeof v === 'string' && LEARNING_STATUSES.includes(v as LearningStatus)
    ? (v as LearningStatus)
    : 'not_started'
}

function parsePriority(v: unknown): LearningPriority | undefined {
  if (typeof v !== 'string' || !PRIORITIES.includes(v as LearningPriority)) return undefined
  return v as LearningPriority
}

function mapVideoCard(dto: VideoCardResponseDto): VideoCardModel {
  const id = String(dto.userVideoId ?? dto.id ?? '').trim()
  return {
    id,
    title: dto.title ?? '',
    channelName: dto.channelName ?? '',
    thumbnailUrl: dto.thumbnailUrl,
    progressPercent: typeof dto.progressPercent === 'number' ? dto.progressPercent : undefined,
    durationLabel: dto.durationLabel,
    learningStatus: parseLearningStatus(dto.learningStatus),
    priority: parsePriority(dto.priority),
    reviewNeeded: Boolean(dto.reviewNeeded),
    contextHint: dto.contextHint,
  }
}

function mapNewFromFavorite(dto: VideoCardResponseDto): NewUploadFromFavorite {
  const base = mapVideoCard(dto)
  return {
    ...base,
    uploadedAtLabel: dto.uploadedAtLabel ?? '',
  }
}

function mapNoteCard(dto: NoteCardResponseDto): NoteCardModel {
  return {
    id: dto.id,
    videoTitle: dto.videoTitle ?? '',
    timecode: dto.timecode ?? '',
    excerpt: dto.excerpt ?? '',
    createdAtLabel: dto.createdAtLabel,
    reviewSuggested: Boolean(dto.reviewSuggested),
  }
}

function mapWeekly(dto: DashboardPayloadDto['weekly']): WeeklyLearningSummary {
  return {
    completedCount: Number(dto.completedCount) || 0,
    minutesTotal: Number(dto.minutesTotal) || 0,
    streakDays: Number(dto.streakDays) || 0,
    reviewDueCount: Number(dto.reviewDueCount) || 0,
  }
}

function mapQuickActions(rows: DashboardPayloadDto['quickActions']): QuickActionItem[] {
  if (!rows?.length) return DEFAULT_QUICK_ACTIONS
  return rows.map((r) => ({
    id: r.id,
    label: r.label,
    to: r.to,
    variant: r.variant,
  }))
}

/** DTO → 화면용 `DashboardBundle` (카드 id는 userVideoId 우선) */
export function mapDashboardPayloadToBundle(dto: DashboardPayloadDto): DashboardBundle {
  const filterVideos = (list: VideoCardResponseDto[]) =>
    list.map(mapVideoCard).filter((v) => v.id.length > 0)

  const next = dto.nextUp ? mapVideoCard(dto.nextUp) : null
  return {
    nextUp: next && next.id ? next : null,
    todayQueue: filterVideos(dto.todayQueue ?? []),
    continueWatching: filterVideos(dto.continueWatching ?? []),
    recentNotes: (dto.recentNotes ?? []).map(mapNoteCard),
    incompleteVideos: filterVideos(dto.incompleteVideos ?? []),
    newFromFavorites: (dto.newFromFavorites ?? []).map(mapNewFromFavorite).filter((v) => v.id.length > 0),
    weekly: dto.weekly ? mapWeekly(dto.weekly) : mapWeekly({ completedCount: 0, minutesTotal: 0, streakDays: 0, reviewDueCount: 0 }),
    quickActions: mapQuickActions(dto.quickActions),
  }
}

/** GET /api/dashboard — unwrap 된 페이로드를 받아 번들로 변환 */
export async function fetchDashboard(): Promise<DashboardBundle> {
  const payload = await apiGet<DashboardPayloadDto>('/api/dashboard')
  return mapDashboardPayloadToBundle(payload)
}
