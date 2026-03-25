import type { VideoDetailDocument } from '@/shared/types/video-detail'
import { formatTimecode } from '@/features/video-detail/timecode'

export type VideoInfoHeaderProps = {
  detail: VideoDetailDocument
}

/** 상단 영상 정보 — 학습 자산 맥락 */
export function VideoInfoHeader({ detail }: VideoInfoHeaderProps) {
  return (
    <header>
      <h1 className="vd-info__title">{detail.title}</h1>
      <div className="vd-info__row">
        <p className="vd-info__channel">{detail.channelName}</p>
        <span className="vd-info__meta" aria-hidden>
          ·
        </span>
        <span className="vd-info__meta">
          {detail.durationLabel} · 진행 {detail.progressPercent}% · 총 {formatTimecode(detail.durationSec)}
        </span>
      </div>
      <div className="vd-info__tags">
        <span className="vd-pill">컬렉션 · {detail.collectionName}</span>
        {detail.tags.map((t) => (
          <span key={t} className="vd-pill">
            {t}
          </span>
        ))}
      </div>
    </header>
  )
}
