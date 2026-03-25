import { formatTimecode } from '@/features/video-detail/timecode'

export type VideoPlayerPanelProps = {
  title: string
  durationSec: number
  currentTimeSec: number
  onSeek: (sec: number) => void
}

/**
 * mock 플레이어 — 스크럽으로 재생 위치만 시뮬레이션
 * 실서비스에서는 iframe/비디오 엘리먼트와 동기화
 */
export function VideoPlayerPanel({ title, durationSec, currentTimeSec, onSeek }: VideoPlayerPanelProps) {
  const safeDur = Math.max(1, durationSec)
  const t = Math.min(safeDur, Math.max(0, currentTimeSec))

  return (
    <div className="vd-player">
      <div className="vd-player__screen" aria-label="영상 재생 영역(목업)">
        <div className="vd-player__placeholder">
          학습용 플레이어 자리입니다. 스크립트·메모의 시각을 누르면 이 위치가 맞춰집니다.
        </div>
        <span className="vd-player__time">{formatTimecode(t)}</span>
      </div>
      <div className="vd-player__controls">
        <label htmlFor="vd-scrub" className="visually-hidden">
          재생 위치
        </label>
        <input
          id="vd-scrub"
          type="range"
          className="vd-player__scrub"
          min={0}
          max={safeDur}
          step={1}
          value={t}
          onChange={(e) => onSeek(Number(e.target.value))}
          aria-valuetext={`${formatTimecode(t)} / ${formatTimecode(safeDur)}`}
        />
        <div className="vd-player__labels">
          <span>{formatTimecode(t)}</span>
          <span aria-hidden>
            {title.slice(0, 28)}
            {title.length > 28 ? '…' : ''}
          </span>
          <span>{formatTimecode(safeDur)}</span>
        </div>
      </div>
    </div>
  )
}
