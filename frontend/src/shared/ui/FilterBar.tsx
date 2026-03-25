import type { ReactNode } from 'react'

export type FilterOption = {
  id: string
  label: string
}

export type FilterBarProps = {
  filters: FilterOption[]
  activeId: string
  onChange: (id: string) => void
  /** 정렬·뷰 전환 등 우측 슬롯 */
  trailing?: ReactNode
}

/** 목록 상단 필터 칩 — 상태·태그 등 */
export function FilterBar({ filters, activeId, onChange, trailing }: FilterBarProps) {
  return (
    <div className="ui-filter-bar" role="toolbar" aria-label="필터">
      {filters.map((f) => {
        const active = f.id === activeId
        return (
          <button
            key={f.id}
            type="button"
            className={`ui-filter-bar__chip${active ? ' ui-filter-bar__chip--active' : ''}`}
            aria-pressed={active}
            onClick={() => onChange(f.id)}
          >
            {f.label}
          </button>
        )
      })}
      {trailing ? (
        <>
          <span className="ui-filter-bar__spacer" aria-hidden />
          {trailing}
        </>
      ) : null}
    </div>
  )
}
