import { learningStatusFromBackend, priorityFromBackend } from '@/shared/api/backendContract'
import { apiGet } from '@/shared/api/client'
import { API_V1_PREFIX } from '@/shared/constants/apiPaths'
import type { NoteCardModel, VideoCardModel } from '@/shared/types/cards'
import type { DashboardBundle, NewUploadFromFavorite, QuickActionItem, WeeklyLearningSummary } from '@/shared/types/dashboard'
import type {
  DashboardPayloadDto,
  NoteCardResponseDto,
  VideoCardResponseDto,
  WeeklySummaryResponseDto,
} from '@/shared/types/api'
import type { LearningPriority } from '@/shared/types/learning'

const DEFAULT_QUICK_ACTIONS: QuickActionItem[] = [
  { id: 'qa-queue', label: '나중에 보기 큐', to: '/watch-later', variant: 'secondary' },
  { id: 'qa-notes', label: '메모 허브', to: '/notes', variant: 'ghost' },
  { id: 'qa-videos', label: '전체 영상', to: '/videos', variant: 'ghost' },
  { id: 'qa-analytics', label: '학습 통계', to: '/analytics', variant: 'ghost' },
  { id: 'qa-subs', label: '구독 채널', to: '/subscriptions', variant: 'ghost' },
]

const PRIORITIES: LearningPriority[] = ['low', 'normal', 'high']

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
    learningStatus: learningStatusFromBackend(dto.learningStatus),
    priority: priorityFromBackend(dto.priority) ?? parsePriority(dto.priority),
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

function formatDurationLabelFromSeconds(sec: unknown): string | undefined {
  if (typeof sec !== 'number' || !Number.isFinite(sec) || sec <= 0) return undefined
  const m = Math.max(1, Math.round(sec / 60))
  return `${m}분`
}

/** 백엔드 `DashboardVideoCardDto` 한 행 → 카드 DTO */
function mapBackendDashboardVideoRow(row: unknown): VideoCardResponseDto {
  if (!row || typeof row !== 'object') {
    return { title: '', channelName: '' }
  }
  const r = row as Record<string, unknown>
  const uid = r.userVideoId
  const idStr = uid !== undefined && uid !== null ? String(uid) : ''
  return {
    userVideoId: idStr || undefined,
    id: idStr || undefined,
    title: String(r.title ?? '') || '',
    channelName: String(r.channelTitle ?? r.channelName ?? '') || '',
    thumbnailUrl: r.thumbnailUrl != null ? String(r.thumbnailUrl) : undefined,
    progressPercent: typeof r.watchPercent === 'number' ? r.watchPercent : undefined,
    durationLabel:
      (typeof r.durationLabel === 'string' ? r.durationLabel : undefined) ??
      formatDurationLabelFromSeconds(r.durationSeconds),
    learningStatus: typeof r.learningStatus === 'string' ? r.learningStatus : undefined,
    priority: typeof r.priority === 'string' ? r.priority : undefined,
    contextHint: typeof r.contextHint === 'string' ? r.contextHint : undefined,
    reviewNeeded: typeof r.reviewNeeded === 'boolean' ? r.reviewNeeded : undefined,
    uploadedAtLabel: r.uploadedAtLabel != null ? String(r.uploadedAtLabel) : undefined,
  }
}

function asVideoCardRowArray(x: unknown): VideoCardResponseDto[] {
  if (!Array.isArray(x)) return []
  return x.map(mapBackendDashboardVideoRow)
}

function mapBackendNoteRow(row: unknown, i: number): NoteCardResponseDto {
  if (!row || typeof row !== 'object') {
    return { id: `n-${i}`, videoTitle: '', timecode: '', excerpt: '' }
  }
  const r = row as Record<string, unknown>
  return {
    id: r.noteId != null ? String(r.noteId) : `n-${i}`,
    videoTitle: String(r.videoTitle ?? ''),
    timecode: String(r.noteType ?? ''),
    excerpt: String(r.bodyPreview ?? ''),
    createdAtLabel: r.createdAt != null ? String(r.createdAt) : undefined,
  }
}

function mapBackendFavoriteToNewUploads(rows: unknown): VideoCardResponseDto[] {
  if (!Array.isArray(rows)) return []
  const out: VideoCardResponseDto[] = []
  for (const ch of rows) {
    if (!ch || typeof ch !== 'object') continue
    const c = ch as Record<string, unknown>
    const recent = c.recentVideos
    if (!Array.isArray(recent)) continue
    const channelTitle = String(c.channelTitle ?? '')
    for (const v of recent) {
      if (!v || typeof v !== 'object') continue
      const r = v as Record<string, unknown>
      const yid = r.youtubeVideoId != null ? String(r.youtubeVideoId) : ''
      if (!yid) continue
      out.push({
        id: yid,
        userVideoId: yid,
        title: String(r.title ?? ''),
        channelName: channelTitle,
        thumbnailUrl: r.thumbnailUrl != null ? String(r.thumbnailUrl) : undefined,
        uploadedAtLabel: r.publishedAt != null ? String(r.publishedAt) : undefined,
      })
    }
  }
  return out
}

function mapBackendWeekly(w: unknown): WeeklySummaryResponseDto {
  if (!w || typeof w !== 'object') {
    return { completedCount: 0, minutesTotal: 0, streakDays: 0, reviewDueCount: 0 }
  }
  const o = w as Record<string, unknown>
  const note = Number(o.noteCount) || 0
  const hl = Number(o.highlightCount) || 0
  return {
    completedCount: Number(o.completedCount) || 0,
    minutesTotal: 0,
    streakDays: 0,
    reviewDueCount: note + hl,
  }
}

const EMPTY_WEEKLY: WeeklySummaryResponseDto = {
  completedCount: 0,
  minutesTotal: 0,
  streakDays: 0,
  reviewDueCount: 0,
}

/**
 * GET /api/v1/dashboard unwrap 결과 또는 구 목/mock 형태를 `DashboardPayloadDto` 로 통일
 */
export function normalizeDashboardPayload(data: unknown): DashboardPayloadDto {
  if (data === null || typeof data !== 'object') {
    return {
      nextUp: null,
      todayQueue: [],
      continueWatching: [],
      recentNotes: [],
      incompleteVideos: [],
      newFromFavorites: [],
      weekly: EMPTY_WEEKLY,
    }
  }
  const o = data as Record<string, unknown>
  const legacyShape = ('nextUp' in o || 'todayQueue' in o) && !('todayPick' in o)
  if (legacyShape) {
    return data as DashboardPayloadDto
  }

  const todayPick = asVideoCardRowArray(o.todayPick)
  return {
    nextUp: todayPick[0] ?? null,
    todayQueue: todayPick,
    continueWatching: asVideoCardRowArray(o.continueWatching),
    recentNotes: Array.isArray(o.recentNotes) ? o.recentNotes.map((row, i) => mapBackendNoteRow(row, i)) : [],
    incompleteVideos: asVideoCardRowArray(o.incompleteVideos),
    newFromFavorites: mapBackendFavoriteToNewUploads(o.favoriteChannelUpdates),
    weekly: mapBackendWeekly(o.weeklySummary),
    quickActions: Array.isArray(o.quickActions) ? (o.quickActions as DashboardPayloadDto['quickActions']) : undefined,
  }
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

/** GET /api/v1/dashboard — unwrap 후 백엔드 DTO 정규화·번들 변환 */
export async function fetchDashboard(): Promise<DashboardBundle> {
  const raw = await apiGet<unknown>(`${API_V1_PREFIX}/dashboard`)
  const payload = normalizeDashboardPayload(raw)
  return mapDashboardPayloadToBundle(payload)
}
