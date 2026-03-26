import { Button } from '@/shared/ui'
import type { ChannelCategory, ChannelSubscription } from '@/shared/types/channel-library'

export type SubscriptionBulkListRowProps = {
  channel: ChannelSubscription
  categoryName: string
  /** 전달 시 카테고리 칩 대신 선택 상자(카드·상세 없이 리스트에서만 분류할 때) */
  categories?: ChannelCategory[]
  onCategoryChange?: (id: string, categoryId: string) => void
  categoryNameById?: (id: string) => string
  selected: boolean
  onToggleSelect: (id: string) => void
  /** 미전달 시 구독 취소 버튼 숨김(API 미제공 등) */
  onUnsubscribe?: (id: string) => void
}

/** 대량 구독 정리용 한 줄 — 체크 + 구독 취소 */
export function SubscriptionBulkListRow({
  channel,
  categoryName,
  categories,
  onCategoryChange,
  categoryNameById,
  selected,
  onToggleSelect,
  onUnsubscribe,
}: SubscriptionBulkListRowProps) {
  const letter = channel.initial ?? channel.name.slice(0, 1).toUpperCase()
  const resolveCatLabel = categoryNameById ?? (() => categoryName)

  return (
    <div className={`chlib-bulk-row${selected ? ' chlib-bulk-row--selected' : ''}`} role="row">
      <div className="chlib-bulk-row__check">
        <input
          type="checkbox"
          checked={selected}
          onChange={() => onToggleSelect(channel.id)}
          aria-label={`${channel.name} 선택`}
        />
      </div>
      <div className="chlib-bulk-row__main">
        <div className="chlib-bulk-row__title-line">
          <span className="chlib-bulk-row__avatar" aria-hidden>
            {letter}
          </span>
          <span className="chlib-bulk-row__name">{channel.name}</span>
        </div>
        <span className="chlib-bulk-row__sub">
          신규 +{channel.newVideoCount} · 자산 {channel.savedVideoCount}개
        </span>
      </div>
      <div className="chlib-bulk-row__meta">
        {categories && onCategoryChange ? (
          <select
            className="chlib-bulk-row__cat-select"
            aria-label={`${channel.name} 카테고리`}
            value={channel.categoryId}
            onChange={(e) => onCategoryChange(channel.id, e.target.value)}
            onClick={(e) => e.stopPropagation()}
          >
            {!categories.some((c) => c.id === channel.categoryId) && channel.categoryId ? (
              <option value={channel.categoryId}>{resolveCatLabel(channel.categoryId)}</option>
            ) : null}
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        ) : (
          <span className="chlib-bulk-row__chip">{categoryName}</span>
        )}
        <span
          className={
            channel.focus === 'learning' ? 'chlib-bulk-row__chip chlib-bulk-row__chip--learn' : 'chlib-bulk-row__chip'
          }
        >
          {channel.focus === 'learning' ? '학습' : '일반'}
        </span>
      </div>
      <div className="chlib-bulk-row__action">
        {onUnsubscribe ? (
          <Button variant="danger" size="sm" type="button" onClick={() => onUnsubscribe(channel.id)}>
            구독 취소
          </Button>
        ) : null}
      </div>
    </div>
  )
}
