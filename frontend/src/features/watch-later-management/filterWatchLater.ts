import type { LearningPriority } from '@/shared/types/learning'
import type { WatchLaterEntry, WatchLaterFilterState } from '@/shared/types/watch-later'

function priorityRank(p: LearningPriority): number {
  if (p === 'high') return 3
  if (p === 'normal') return 2
  return 1
}

function matchesSearch(e: WatchLaterEntry, q: string): boolean {
  const s = q.trim().toLowerCase()
  if (!s) return true
  return e.title.toLowerCase().includes(s) || e.channelName.toLowerCase().includes(s)
}

/** 검색·의도 필터 후 정렬 */
export function filterAndSortWatchLater(
  items: WatchLaterEntry[],
  state: WatchLaterFilterState,
): WatchLaterEntry[] {
  let list = items.filter((e) => {
    if (!matchesSearch(e, state.search)) return false
    if (state.intent !== 'all' && e.intent !== state.intent) return false
    return true
  })

  list = [...list].sort((a, b) => {
    const sortId = state.sortId
    if (sortId === 'plan_order') {
      const aq = a.inTodayQueue
      const bq = b.inTodayQueue
      if (aq && !bq) return -1
      if (!aq && bq) return 1
      if (aq && bq) return a.todayQueueOrder - b.todayQueueOrder
      const pr = priorityRank(b.priority) - priorityRank(a.priority)
      if (pr !== 0) return pr
      return new Date(a.addedAt).getTime() - new Date(b.addedAt).getTime()
    }
    if (sortId === 'priority_desc') {
      const pr = priorityRank(b.priority) - priorityRank(a.priority)
      if (pr !== 0) return pr
      return new Date(b.addedAt).getTime() - new Date(a.addedAt).getTime()
    }
    if (sortId === 'added_asc') return new Date(a.addedAt).getTime() - new Date(b.addedAt).getTime()
    if (sortId === 'added_desc') return new Date(b.addedAt).getTime() - new Date(a.addedAt).getTime()
    return a.durationMinutes - b.durationMinutes
  })

  return list
}
