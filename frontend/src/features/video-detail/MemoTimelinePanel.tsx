import { useMemo } from 'react'
import type { TimelineNote } from '@/shared/types/video-detail'
import { formatTimecode } from '@/features/video-detail/timecode'

export type MemoTimelinePanelProps = {
  notes: TimelineNote[]
  currentTimeSec: number
  onSeekTo: (sec: number) => void
}

/** 타임라인 메모 — 시각 오름차순, 클릭 시 seek */
export function MemoTimelinePanel({ notes, currentTimeSec, onSeekTo }: MemoTimelinePanelProps) {
  const sorted = useMemo(() => [...notes].sort((a, b) => a.timeSec - b.timeSec), [notes])

  if (sorted.length === 0) {
    return (
      <p className="page-header__description" style={{ margin: 0 }}>
        아직 이 영상에 연결된 메모가 없습니다. 재생 중 기록하면 타임라인에 쌓입니다.
      </p>
    )
  }

  return (
    <div role="list">
      {sorted.map((note) => {
        const near = Math.abs(currentTimeSec - note.timeSec) < 3
        return (
          <button
            key={note.id}
            type="button"
            role="listitem"
            className="vd-memo-item"
            style={
              near
                ? { borderColor: 'var(--color-primary)', background: 'var(--color-primary-muted)' }
                : undefined
            }
            onClick={() => onSeekTo(note.timeSec)}
          >
            <div className="vd-memo-item__time">{formatTimecode(note.timeSec)}</div>
            <p className="vd-memo-item__body">{note.body}</p>
            <span className="vd-memo-item__date">{note.createdLabel}</span>
          </button>
        )
      })}
    </div>
  )
}
