import { apiGet, apiPatch } from '@/shared/api/client'
import { VIDEO_COLLECTIONS } from '@/shared/constants/videoCollections'
import type {
  PatchLearningStateRequestDto,
  PatchProgressRequestDto,
  RelatedVideoBriefDto,
  ReviewPointDto,
  ScriptCueDto,
  TimelineNoteDto,
  VideoDetailResponseDto,
  VideoHighlightDto,
  VideoLibraryItemDto,
} from '@/shared/types/api'
import type {
  RelatedVideoBrief,
  ReviewPoint,
  ScriptCue,
  TimelineNote,
  VideoDetailDocument,
  VideoHighlight,
} from '@/shared/types/video-detail'
import type { VideoLibraryEntry } from '@/shared/types/video-library'
import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

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

function userVideoIdFrom(d: { userVideoId?: string; id?: string }): string {
  return String(d.userVideoId ?? d.id ?? '').trim()
}

function durationMinutesFromDto(d: VideoLibraryItemDto | VideoDetailResponseDto): number {
  if (typeof d.durationMinutes === 'number' && Number.isFinite(d.durationMinutes)) {
    return Math.max(0, d.durationMinutes)
  }
  if (typeof d.durationSec === 'number' && Number.isFinite(d.durationSec)) {
    return Math.max(1, Math.round(d.durationSec / 60))
  }
  return 1
}

function durationSecFromDto(d: VideoDetailResponseDto): number {
  if (typeof d.durationSec === 'number' && d.durationSec > 0) return Math.floor(d.durationSec)
  return Math.max(120, durationMinutesFromDto(d) * 60)
}

function collectionNameFromId(collectionId: string, fallback?: string): string {
  if (fallback) return fallback
  return VIDEO_COLLECTIONS.find((c) => c.id === collectionId)?.name ?? '미분류'
}

/** GET /api/videos 본문이 배열 또는 { items | videos | content } 인 경우 */
export function unwrapVideoListPayload(data: unknown): VideoLibraryItemDto[] {
  if (Array.isArray(data)) return data as VideoLibraryItemDto[]
  if (data !== null && typeof data === 'object') {
    const o = data as Record<string, unknown>
    const items = o.items ?? o.videos ?? o.content
    if (Array.isArray(items)) return items as VideoLibraryItemDto[]
  }
  return []
}

export function mapVideoLibraryItemDto(dto: VideoLibraryItemDto): VideoLibraryEntry | null {
  const id = userVideoIdFrom(dto)
  if (!id) return null
  const durationMinutes = durationMinutesFromDto(dto)
  return {
    id,
    title: dto.title ?? '',
    channelName: dto.channelName ?? '',
    thumbnailUrl: dto.thumbnailUrl,
    durationLabel: dto.durationLabel ?? `${durationMinutes}분`,
    durationMinutes,
    progressPercent: Math.min(100, Math.max(0, Number(dto.progressPercent) || 0)),
    learningStatus: parseLearningStatus(dto.learningStatus),
    priority: parsePriority(dto.priority),
    reviewNeeded: Boolean(dto.reviewNeeded),
    tags: Array.isArray(dto.tags) ? dto.tags.map(String) : [],
    isStarred: Boolean(dto.isStarred),
    collectionId: dto.collectionId ?? 'col-inbox',
    updatedAt: dto.updatedAt ?? new Date().toISOString(),
  }
}

function mapScriptCue(d: ScriptCueDto, i: number): ScriptCue {
  const start = typeof d.startSec === 'number' ? d.startSec : 0
  const end = typeof d.endSec === 'number' ? d.endSec : start + 60
  return {
    id: d.id ?? `cue-${i}`,
    startSec: start,
    endSec: end,
    text: d.text ?? '',
  }
}

function mapTimelineNote(d: TimelineNoteDto, i: number): TimelineNote {
  return {
    id: d.id ?? `note-${i}`,
    timeSec: typeof d.timeSec === 'number' ? d.timeSec : 0,
    body: d.body ?? '',
    createdLabel: d.createdLabel ?? '',
  }
}

function mapHighlight(d: VideoHighlightDto, i: number): VideoHighlight {
  return {
    id: d.id ?? `hl-${i}`,
    timeSec: typeof d.timeSec === 'number' ? d.timeSec : 0,
    quote: d.quote ?? '',
  }
}

function mapRelatedBrief(d: RelatedVideoBriefDto): RelatedVideoBrief | null {
  const id = userVideoIdFrom(d)
  if (!id) return null
  return {
    id,
    title: d.title ?? '',
    durationLabel: d.durationLabel ?? '',
    learningStatus: parseLearningStatus(d.learningStatus),
    progressPercent: Math.min(100, Math.max(0, Number(d.progressPercent) || 0)),
  }
}

function mapReviewPoint(d: ReviewPointDto, i: number): ReviewPoint {
  return {
    id: d.id ?? `rp-${i}`,
    title: d.title ?? '',
    detail: d.detail ?? '',
  }
}

export function mapVideoDetailDto(dto: VideoDetailResponseDto): VideoDetailDocument | null {
  const id = userVideoIdFrom(dto)
  if (!id) return null
  const durationSec = durationSecFromDto(dto)
  const durationMinutes = durationMinutesFromDto(dto)
  const collectionId = dto.collectionId ?? 'col-inbox'

  return {
    id,
    title: dto.title ?? '',
    channelName: dto.channelName ?? '',
    durationLabel: dto.durationLabel ?? `${durationMinutes}분`,
    durationSec,
    progressPercent: Math.min(100, Math.max(0, Number(dto.progressPercent) || 0)),
    learningStatus: parseLearningStatus(dto.learningStatus),
    priority: parsePriority(dto.priority),
    reviewNeeded: Boolean(dto.reviewNeeded),
    tags: Array.isArray(dto.tags) ? dto.tags.map(String) : [],
    isStarred: Boolean(dto.isStarred),
    collectionId,
    collectionName: collectionNameFromId(collectionId, dto.collectionName),
    scriptCues: (dto.scriptCues ?? []).map(mapScriptCue),
    timelineNotes: (dto.timelineNotes ?? []).map(mapTimelineNote),
    highlights: (dto.highlights ?? []).map(mapHighlight),
    relatedInCollection: (dto.relatedInCollection ?? [])
      .map(mapRelatedBrief)
      .filter((x): x is RelatedVideoBrief => x !== null),
    reviewPoints: (dto.reviewPoints ?? []).map(mapReviewPoint),
  }
}

export async function fetchVideos(): Promise<VideoLibraryEntry[]> {
  const raw = await apiGet<unknown>('/api/videos')
  const rows = unwrapVideoListPayload(raw)
  return rows.map(mapVideoLibraryItemDto).filter(Boolean) as VideoLibraryEntry[]
}

export async function fetchVideoDetail(userVideoId: string): Promise<VideoDetailDocument> {
  const dto = await apiGet<VideoDetailResponseDto>(`/api/videos/${encodeURIComponent(userVideoId)}`)
  const doc = mapVideoDetailDto(dto)
  if (!doc) {
    throw new Error('영상 상세 응답을 해석할 수 없습니다.')
  }
  return doc
}

export async function patchVideoLearningState(userVideoId: string, learningStatus: LearningStatus): Promise<void> {
  const body: PatchLearningStateRequestDto = { learningStatus }
  await apiPatch<void>(`/api/videos/${encodeURIComponent(userVideoId)}/learning-state`, body)
}

export async function patchVideoProgress(userVideoId: string, progressPercent: number): Promise<void> {
  const clamped = Math.min(100, Math.max(0, Math.round(progressPercent)))
  const body: PatchProgressRequestDto = { progressPercent: clamped }
  await apiPatch<void>(`/api/videos/${encodeURIComponent(userVideoId)}/progress`, body)
}
