import type { ReactNode } from 'react'
import type { VideoLibrarySortId } from '@/shared/types/video-library'

export type VideosToolbarProps = {
  search: string
  onSearchChange: (value: string) => void
  sortId: VideoLibrarySortId
  onSortChange: (id: VideoLibrarySortId) => void
  viewMode: 'grid' | 'list'
  onViewModeChange: (mode: 'grid' | 'list') => void
  /** URL 추가 등 보조 액션 — 툴바 오른쪽에 배치 */
  trailingActions?: ReactNode
}

const SORT_OPTIONS: { id: VideoLibrarySortId; label: string }[] = [
  { id: 'updated_desc', label: '최근 활동순' },
  { id: 'title_asc', label: '제목 가나다순' },
  { id: 'duration_asc', label: '길이 짧은 순' },
  { id: 'duration_desc', label: '길이 긴 순' },
  { id: 'progress_desc', label: '진행률 높은 순' },
]

/** 검색 · 정렬 · 그리드/리스트 전환 */
export function VideosToolbar({
  search,
  onSearchChange,
  sortId,
  onSortChange,
  viewMode,
  onViewModeChange,
  trailingActions,
}: VideosToolbarProps) {
  return (
    <div className="vlib-toolbar">
      <div className="vlib-search">
        <label htmlFor="vlib-search-input" className="visually-hidden">
          학습 자산 검색
        </label>
        <input
          id="vlib-search-input"
          type="search"
          placeholder="제목, 채널, 태그 검색…"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          autoComplete="off"
        />
      </div>
      <div className="vlib-toolbar__sort">
        <label htmlFor="vlib-sort">정렬</label>
        <select
          id="vlib-sort"
          className="vlib-select"
          value={sortId}
          onChange={(e) => onSortChange(e.target.value as VideoLibrarySortId)}
        >
          {SORT_OPTIONS.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
      </div>
      <div className="vlib-view-toggle" role="group" aria-label="보기 방식">
        <button
          type="button"
          aria-pressed={viewMode === 'grid'}
          onClick={() => onViewModeChange('grid')}
        >
          그리드
        </button>
        <button
          type="button"
          aria-pressed={viewMode === 'list'}
          onClick={() => onViewModeChange('list')}
        >
          리스트
        </button>
      </div>
      {trailingActions ? <div className="vlib-toolbar__actions">{trailingActions}</div> : null}
    </div>
  )
}
