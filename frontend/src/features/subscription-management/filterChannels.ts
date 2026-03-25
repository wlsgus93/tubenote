import type { ChannelListFilterState, ChannelSubscription } from '@/shared/types/channel-library'

function matchesSearch(ch: ChannelSubscription, q: string) {
  if (!q.trim()) return true
  const s = q.trim().toLowerCase()
  return ch.name.toLowerCase().includes(s) || ch.memo.toLowerCase().includes(s)
}

/** 카테고리·유형·즐겨찾기·검색 후 정렬 */
export function filterAndSortChannels(
  items: ChannelSubscription[],
  state: ChannelListFilterState,
): ChannelSubscription[] {
  let out = items.filter((ch) => {
    if (!matchesSearch(ch, state.search)) return false
    if (state.categoryId !== 'all' && ch.categoryId !== state.categoryId) return false
    if (state.focus !== 'all' && ch.focus !== state.focus) return false
    if (state.favoritesOnly && !ch.isFavorite) return false
    return true
  })

  const { sortId } = state
  out = [...out].sort((a, b) => {
    if (sortId === 'name_asc') return a.name.localeCompare(b.name, 'ko')
    if (sortId === 'new_desc') return b.newVideoCount - a.newVideoCount
    return new Date(b.lastUploadAt).getTime() - new Date(a.lastUploadAt).getTime()
  })

  return out
}
