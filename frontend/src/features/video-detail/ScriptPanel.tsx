import type { ScriptCue } from '@/shared/types/video-detail'
import { formatTimecode } from '@/features/video-detail/timecode'

export type ScriptPanelProps = {
  cues: ScriptCue[]
  currentTimeSec: number
  onSeekTo: (sec: number) => void
}

/** 스크립트 구간 목록 — 클릭 시 mock seek */
export function ScriptPanel({ cues, currentTimeSec, onSeekTo }: ScriptPanelProps) {
  return (
    <div role="list">
      {cues.map((cue) => {
        const active = currentTimeSec >= cue.startSec && currentTimeSec < cue.endSec
        return (
          <button
            key={cue.id}
            type="button"
            role="listitem"
            className={`vd-script-line${active ? ' vd-script-line--active' : ''}`}
            onClick={() => onSeekTo(cue.startSec)}
          >
            <span className="vd-script-line__time">
              {formatTimecode(cue.startSec)} — {formatTimecode(cue.endSec)}
            </span>
            <p className="vd-script-line__text">{cue.text}</p>
          </button>
        )
      })}
    </div>
  )
}
