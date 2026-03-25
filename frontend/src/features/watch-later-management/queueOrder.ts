import type { WatchLaterEntry } from '@/shared/types/watch-later'

/** 오늘 큐에 넣은 항목만 순서대로 */
export function getTodayQueueSorted(entries: WatchLaterEntry[]): WatchLaterEntry[] {
  return [...entries].filter((e) => e.inTodayQueue).sort((a, b) => a.todayQueueOrder - b.todayQueueOrder)
}

/** 큐 id 순서에서 인접 항목과 교환 */
export function reorderQueueIds(order: string[], id: string, direction: -1 | 1): string[] {
  const i = order.indexOf(id)
  if (i < 0) return order
  const j = i + direction
  if (j < 0 || j >= order.length) return order
  const next = [...order]
  const a = next[i]!
  const b = next[j]!
  next[i] = b
  next[j] = a
  return next
}

/** orderedIds 순서대로 todayQueueOrder 부여 (0부터) */
export function applyTodayQueueOrder(entries: WatchLaterEntry[], orderedIds: string[]): WatchLaterEntry[] {
  const map = new Map(orderedIds.map((id, idx) => [id, idx]))
  return entries.map((e) => {
    if (!e.inTodayQueue) return { ...e, todayQueueOrder: 99 }
    const o = map.get(e.id)
    return o !== undefined ? { ...e, todayQueueOrder: o } : { ...e, inTodayQueue: false, todayQueueOrder: 99 }
  })
}

/** 큐에서 빠진 뒤 남은 항목 order를 0..n-1로 다시 매김 */
export function normalizeWatchLaterQueue(entries: WatchLaterEntry[]): WatchLaterEntry[] {
  const queued = getTodayQueueSorted(entries)
  return applyTodayQueueOrder(entries, queued.map((e) => e.id))
}
