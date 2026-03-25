import { Button } from '@/shared/ui'
import type { ChannelSubscription } from '@/shared/types/channel-library'

export type SubscriptionBulkListRowProps = {
  channel: ChannelSubscription
  categoryName: string
  selected: boolean
  onToggleSelect: (id: string) => void
  onUnsubscribe: (id: string) => void
}

/** 대량 구독 정리용 한 줄 — 체크 + 구독 취소 */
export function SubscriptionBulkListRow({
  channel,
  categoryName,
  selected,
  onToggleSelect,
  onUnsubscribe,
}: SubscriptionBulkListRowProps) {
  const letter = channel.initial ?? channel.name.slice(0, 1).toUpperCase()

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
        <span className="chlib-bulk-row__chip">{categoryName}</span>
        <span
          className={
            channel.focus === 'learning' ? 'chlib-bulk-row__chip chlib-bulk-row__chip--learn' : 'chlib-bulk-row__chip'
          }
        >
          {channel.focus === 'learning' ? '학습' : '일반'}
        </span>
      </div>
      <div className="chlib-bulk-row__action">
        <Button variant="danger" size="sm" type="button" onClick={() => onUnsubscribe(channel.id)}>
          구독 취소
        </Button>
      </div>
    </div>
  )
}
