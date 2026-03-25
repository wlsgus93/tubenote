import type { WatchLaterSortId } from '@/shared/types/watch-later'

export type WatchLaterToolbarProps = {
  search: string
  onSearchChange: (v: string) => void
  sortId: WatchLaterSortId
  onSortChange: (id: WatchLaterSortId) => void
}

const SORT_OPTIONS: { id: WatchLaterSortId; label: string }[] = [
  { id: 'plan_order', label: '학습 계획 순(오늘 큐 먼저)' },
  { id: 'priority_desc', label: '우선순위 높은 순' },
  { id: 'added_asc', label: '담은 날 오래된 순' },
  { id: 'added_desc', label: '담은 날 최근 순' },
  { id: 'duration_asc', label: '짧은 영상 순' },
]

export function WatchLaterToolbar({ search, onSearchChange, sortId, onSortChange }: WatchLaterToolbarProps) {
  return (
    <div className="wl-toolbar" role="search">
      <label className="wl-toolbar__search-wrap">
        <span className="visually-hidden">나중에 보기 검색</span>
        <input
          type="search"
          className="wl-toolbar__search"
          placeholder="제목·채널로 검색…"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          autoComplete="off"
        />
      </label>
      <label className="wl-toolbar__sort">
        <span className="wl-toolbar__sort-label">정렬</span>
        <select
          className="wl-toolbar__select"
          value={sortId}
          onChange={(e) => onSortChange(e.target.value as WatchLaterSortId)}
          aria-label="목록 정렬"
        >
          {SORT_OPTIONS.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}
