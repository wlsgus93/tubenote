import type { ChannelListSortId } from '@/shared/types/channel-library'

export type ChannelsToolbarProps = {
  search: string
  onSearchChange: (v: string) => void
  sortId: ChannelListSortId
  onSortChange: (id: ChannelListSortId) => void
}

const SORT_OPTIONS: { id: ChannelListSortId; label: string }[] = [
  { id: 'upload_desc', label: '최근 업로드순' },
  { id: 'new_desc', label: '신규 영상 많은 순' },
  { id: 'name_asc', label: '이름순' },
]

/** 검색·정렬 — 학습 자산 툴바와 동일 패턴 */
export function ChannelsToolbar({ search, onSearchChange, sortId, onSortChange }: ChannelsToolbarProps) {
  return (
    <div className="chlib-toolbar" role="search">
      <label className="chlib-toolbar__search-label">
        <span className="visually-hidden">채널·메모 검색</span>
        <input
          type="search"
          className="chlib-toolbar__search"
          placeholder="채널명 또는 메모로 검색…"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
          autoComplete="off"
        />
      </label>
      <label className="chlib-toolbar__sort">
        <span className="chlib-toolbar__sort-label">정렬</span>
        <select
          className="chlib-toolbar__select"
          value={sortId}
          onChange={(e) => onSortChange(e.target.value as ChannelListSortId)}
          aria-label="채널 정렬"
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
