import type { LearningStatus } from '@/shared/types/learning'
import type {
  VideoLengthFilterId,
  VideoLibraryEntry,
  VideoLibrarySortId,
} from '@/shared/types/video-library'

export type VideoLibraryFilterState = {
  search: string
  statusId: LearningStatus | 'all'
  selectedTags: string[]
  lengthId: VideoLengthFilterId
  sortId: VideoLibrarySortId
}

function matchesLength(minutes: number, lengthId: VideoLengthFilterId): boolean {
  if (lengthId === 'all') return true
  if (lengthId === 'short') return minutes < 15
  if (lengthId === 'medium') return minutes >= 15 && minutes <= 45
  return minutes > 45
}

/** 제목·채널·태그 텍스트 검색(대소문자 무시) */
function matchesSearch(entry: VideoLibraryEntry, q: string): boolean {
  const s = q.trim().toLowerCase()
  if (!s) return true
  if (entry.title.toLowerCase().includes(s)) return true
  if (entry.channelName.toLowerCase().includes(s)) return true
  return entry.tags.some((t) => t.toLowerCase().includes(s))
}

/** 필터 + 정렬 — 순수 함수 */
export function filterAndSortVideos(
  items: VideoLibraryEntry[],
  state: VideoLibraryFilterState,
): VideoLibraryEntry[] {
  let list = items.filter((v) => {
    if (state.statusId !== 'all' && v.learningStatus !== state.statusId) return false
    if (!matchesLength(v.durationMinutes, state.lengthId)) return false
    if (!matchesSearch(v, state.search)) return false
    if (state.selectedTags.length > 0) {
      const hit = state.selectedTags.some((t) => v.tags.includes(t))
      if (!hit) return false
    }
    return true
  })

  const { sortId } = state
  list = [...list].sort((a, b) => {
    if (sortId === 'title_asc') return a.title.localeCompare(b.title, 'ko')
    if (sortId === 'duration_asc') return a.durationMinutes - b.durationMinutes
    if (sortId === 'duration_desc') return b.durationMinutes - a.durationMinutes
    if (sortId === 'progress_desc') return b.progressPercent - a.progressPercent
    return new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
  })

  return list
}
