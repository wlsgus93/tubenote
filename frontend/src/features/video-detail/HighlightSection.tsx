import type { VideoHighlight } from '@/shared/types/video-detail'
import { formatTimecode } from '@/features/video-detail/timecode'

export type HighlightSectionProps = {
  highlights: VideoHighlight[]
  onSeekTo: (sec: number) => void
}

/** 하이라이트 인용 + 시점 이동 */
export function HighlightSection({ highlights, onSeekTo }: HighlightSectionProps) {
  if (highlights.length === 0) {
    return <p className="page-header__description">저장된 하이라이트가 없습니다.</p>
  }

  return (
    <div>
      {highlights.map((h) => (
        <div key={h.id} className="vd-highlight">
          <blockquote className="vd-highlight__quote">{h.quote}</blockquote>
          <button type="button" className="vd-time-btn" onClick={() => onSeekTo(h.timeSec)}>
            {formatTimecode(h.timeSec)} 로 이동
          </button>
        </div>
      ))}
    </div>
  )
}
