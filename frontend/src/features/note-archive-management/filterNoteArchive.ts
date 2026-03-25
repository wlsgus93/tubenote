import type { LearningPriority } from '@/shared/types/learning'
import type { NoteArchiveEntry, NoteArchiveFilterState, NoteArchiveKind } from '@/shared/types/note-archive'

function importanceRank(p: LearningPriority): number {
  if (p === 'high') return 3
  if (p === 'normal') return 2
  return 1
}

function matchesSearch(e: NoteArchiveEntry, q: string): boolean {
  const s = q.trim().toLowerCase()
  if (!s) return true
  if (e.body.toLowerCase().includes(s)) return true
  if (e.videoTitle.toLowerCase().includes(s)) return true
  if (e.channelName.toLowerCase().includes(s)) return true
  return e.tags.some((t) => t.toLowerCase().includes(s))
}

function matchesKind(kind: 'all' | NoteArchiveKind, e: NoteArchiveEntry): boolean {
  if (kind === 'all') return true
  return e.kind === kind
}

function matchesTag(tagId: string, e: NoteArchiveEntry): boolean {
  if (tagId === 'all') return true
  return e.tags.includes(tagId)
}

/** 검색·종류·태그·복습 필터 후 정렬 */
export function filterAndSortNoteArchive(
  items: NoteArchiveEntry[],
  state: NoteArchiveFilterState,
): NoteArchiveEntry[] {
  let list = items.filter(
    (e) =>
      matchesSearch(e, state.search) &&
      matchesKind(state.kind, e) &&
      matchesTag(state.tagId, e) &&
      (!state.reviewOnly || e.reviewNeeded),
  )

  list = [...list].sort((a, b) => {
    if (state.sortId === 'importance_desc') {
      const ir = importanceRank(b.importance) - importanceRank(a.importance)
      if (ir !== 0) return ir
    }
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })

  return list
}
