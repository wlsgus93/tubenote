import { useEffect, useRef } from 'react'
import type { ChannelSubscription } from '@/shared/types/channel-library'

export type SubscriptionBulkListHeaderProps = {
  filtered: ChannelSubscription[]
  selectedIds: Set<string>
  onSelectAllFiltered: () => void
  onClearSelection: () => void
}

/** 리스트 뷰 헤더 — 현재 필터 결과 기준 전체 선택 */
export function SubscriptionBulkListHeader({
  filtered,
  selectedIds,
  onSelectAllFiltered,
  onClearSelection,
}: SubscriptionBulkListHeaderProps) {
  const ref = useRef<HTMLInputElement>(null)
  const allSelected = filtered.length > 0 && filtered.every((c) => selectedIds.has(c.id))
  const someSelected = filtered.some((c) => selectedIds.has(c.id)) && !allSelected

  useEffect(() => {
    if (ref.current) ref.current.indeterminate = someSelected
  }, [someSelected])

  return (
    <div className="chlib-bulk-head" role="row">
      <label className="chlib-bulk-head__check">
        <input
          ref={ref}
          type="checkbox"
          checked={allSelected}
          onChange={(e) => {
            if (e.target.checked) onSelectAllFiltered()
            else onClearSelection()
          }}
          aria-label="필터된 목록 전체 선택"
        />
      </label>
      <span className="chlib-bulk-head__col chlib-bulk-head__col--main">채널</span>
      <span className="chlib-bulk-head__col chlib-bulk-head__col--meta">분류</span>
      <span className="chlib-bulk-head__col chlib-bulk-head__col--action">작업</span>
    </div>
  )
}
