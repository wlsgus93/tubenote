import type { ChannelSubscription } from '@/shared/types/channel-library'

export type SubscriptionChannelCardProps = {
  channel: ChannelSubscription
  categoryName: string
  selected: boolean
  onSelect: (id: string) => void
  onToggleFavorite: (id: string) => void
}

function formatRelativeUpload(iso: string) {
  const d = new Date(iso)
  const now = new Date()
  const diffMs = now.getTime() - d.getTime()
  const days = Math.floor(diffMs / (86400 * 1000))
  if (days <= 0) return '오늘 업로드 있음'
  if (days === 1) return '어제 업로드'
  return `${days}일 전 업로드`
}

/** 구독 페이지 전용 카드 — 유형·즐겨찾기·신규·메모 한 줄 */
export function SubscriptionChannelCard({
  channel,
  categoryName,
  selected,
  onSelect,
  onToggleFavorite,
}: SubscriptionChannelCardProps) {
  const letter = channel.initial ?? channel.name.slice(0, 1).toUpperCase()
  const memoPreview =
    channel.memo.length > 56 ? `${channel.memo.slice(0, 54)}…` : channel.memo

  return (
    <article
      className={`chlib-card${selected ? ' chlib-card--selected' : ''}`}
      aria-current={selected ? 'true' : undefined}
    >
      <button type="button" className="chlib-card__main" onClick={() => onSelect(channel.id)}>
        <div className="chlib-card__avatar" aria-hidden>
          {letter}
        </div>
        <div className="chlib-card__body">
          <div className="chlib-card__title-row">
            <h3 className="chlib-card__name">{channel.name}</h3>
            {channel.newVideoCount > 0 ? (
              <span className="chlib-card__new-badge" title="최근 구간 신규 영상 수">
                +{channel.newVideoCount}
              </span>
            ) : null}
          </div>
          <div className="chlib-card__chips">
            <span className="chlib-card__chip chlib-card__chip--muted">{categoryName}</span>
            <span
              className={
                channel.focus === 'learning'
                  ? 'chlib-card__chip chlib-card__chip--learning'
                  : 'chlib-card__chip chlib-card__chip--general'
              }
            >
              {channel.focus === 'learning' ? '학습용' : '일반'}
            </span>
          </div>
          <p className="chlib-card__upload">{formatRelativeUpload(channel.lastUploadAt)}</p>
          {memoPreview ? <p className="chlib-card__memo">{memoPreview}</p> : null}
        </div>
      </button>
      <button
        type="button"
        className={`chlib-card__star${channel.isFavorite ? ' chlib-card__star--on' : ''}`}
        aria-pressed={channel.isFavorite}
        aria-label={channel.isFavorite ? '즐겨찾기 해제' : '즐겨찾기'}
        onClick={(e) => {
          e.stopPropagation()
          onToggleFavorite(channel.id)
        }}
      >
        ★
      </button>
    </article>
  )
}
