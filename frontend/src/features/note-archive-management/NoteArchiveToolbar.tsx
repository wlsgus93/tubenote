import type { NoteArchiveSortId } from '@/shared/types/note-archive'

export type NoteArchiveToolbarProps = {
  search: string
  onSearchChange: (v: string) => void
  sortId: NoteArchiveSortId
  onSortChange: (id: NoteArchiveSortId) => void
  viewMode: 'grid' | 'list'
  onViewModeChange: (mode: 'grid' | 'list') => void
}

const SORT_OPTIONS: { id: NoteArchiveSortId; label: string }[] = [
  { id: 'recent_desc', label: '최근 작성순' },
  { id: 'importance_desc', label: '중요도순' },
]

export function NoteArchiveToolbar({
  search,
  onSearchChange,
  sortId,
  onSortChange,
  viewMode,
  onViewModeChange,
}: NoteArchiveToolbarProps) {
  return (
    <div className="na-toolbar" role="search">
      <label className="na-toolbar__search-wrap">
        <span className="visually-hidden">메모·하이라이트 검색</span>
        <input
          type="search"
          className="na-toolbar__search"
          placeholder="본문, 영상 제목, 채널, 태그…"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          autoComplete="off"
        />
      </label>
      <label className="na-toolbar__sort">
        <span className="na-toolbar__sort-label">정렬</span>
        <select
          className="na-toolbar__select"
          value={sortId}
          onChange={(e) => onSortChange(e.target.value as NoteArchiveSortId)}
          aria-label="정렬"
        >
          {SORT_OPTIONS.map((o) => (
            <option key={o.id} value={o.id}>
              {o.label}
            </option>
          ))}
        </select>
      </label>
      <div className="na-view-toggle" role="group" aria-label="보기 방식">
        <button
          type="button"
          className={viewMode === 'grid' ? 'is-active' : ''}
          aria-pressed={viewMode === 'grid'}
          onClick={() => onViewModeChange('grid')}
        >
          카드
        </button>
        <button
          type="button"
          className={viewMode === 'list' ? 'is-active' : ''}
          aria-pressed={viewMode === 'list'}
          onClick={() => onViewModeChange('list')}
        >
          리스트
        </button>
      </div>
    </div>
  )
}
