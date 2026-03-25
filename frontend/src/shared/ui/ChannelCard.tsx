import type { ChannelCardModel } from '@/shared/types/cards'

export type ChannelCardProps = {
  channel: ChannelCardModel
  onOpen?: (id: string) => void
}

/** 구독 채널 한 덩어리 — 학습 소스 단위 */
export function ChannelCard({ channel, onOpen }: ChannelCardProps) {
  const { id, name, initial, videoCount, lastActivityLabel } = channel
  const letter = initial ?? name.slice(0, 1).toUpperCase()

  const metaParts: string[] = []
  if (typeof videoCount === 'number') metaParts.push(`영상 ${videoCount}개`)
  if (lastActivityLabel) metaParts.push(lastActivityLabel)

  return (
    <button type="button" className="ui-channel-card" onClick={() => onOpen?.(id)}>
      <div className="ui-channel-card__avatar" aria-hidden>
        {letter}
      </div>
      <div>
        <h3 className="ui-channel-card__name">{name}</h3>
        {metaParts.length > 0 ? <p className="ui-channel-card__meta">{metaParts.join(' · ')}</p> : null}
      </div>
    </button>
  )
}
