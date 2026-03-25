import type { VideoCardModel } from '@/shared/types/cards'
import { StatusBadge } from '@/shared/ui/StatusBadge'

export type VideoCardProps = {
  video: VideoCardModel
  onOpen?: (id: string) => void
}

/** 영상 요약 카드 — 진행률·상태·우선순위·복습 메타 */
export function VideoCard({ video, onOpen }: VideoCardProps) {
  const {
    id,
    title,
    channelName,
    thumbnailUrl,
    progressPercent,
    durationLabel,
    learningStatus,
    priority,
    reviewNeeded,
    contextHint,
  } = video

  return (
    <button
      type="button"
      className="ui-video-card"
      onClick={() => onOpen?.(id)}
    >
      <div className="ui-video-card__thumb-wrap">
        {thumbnailUrl ? (
          <img className="ui-video-card__thumb" src={thumbnailUrl} alt="" />
        ) : null}
        {durationLabel ? <span className="ui-video-card__duration">{durationLabel}</span> : null}
      </div>
      <div className="ui-video-card__body">
        <h3 className="ui-video-card__title">{title}</h3>
        <p className="ui-video-card__meta">{channelName}</p>
        {contextHint ? <p className="ui-video-card__hint">{contextHint}</p> : null}
        {typeof progressPercent === 'number' ? (
          <div className="ui-video-card__progress" aria-hidden>
            <div className="ui-video-card__progress-bar" style={{ width: `${Math.min(100, Math.max(0, progressPercent))}%` }} />
          </div>
        ) : null}
        <div className="ui-video-card__badges">
          <StatusBadge type="learning" status={learningStatus} />
          {priority ? <StatusBadge type="priority" level={priority} /> : null}
          {reviewNeeded ? <StatusBadge type="review" needed /> : null}
        </div>
      </div>
    </button>
  )
}
