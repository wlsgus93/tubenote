import { useNavigate } from 'react-router-dom'
import { Button } from '@/shared/ui'
import type { NoteArchiveEntry } from '@/shared/types/note-archive'

export type NoteArchiveListRowProps = {
  entry: NoteArchiveEntry
}

function importanceShort(p: NoteArchiveEntry['importance']) {
  if (p === 'high') return '높음'
  if (p === 'normal') return '보통'
  return '낮음'
}

/** 리스트 행 — 스캔에 유리한 한 줄 요약 + 동일 액션 */
export function NoteArchiveListRow({ entry }: NoteArchiveListRowProps) {
  const navigate = useNavigate()

  return (
    <div className="na-row">
      <div className="na-row__kind">
        <span className={entry.kind === 'memo' ? 'na-kind na-kind--memo' : 'na-kind na-kind--hl'}>
          {entry.kind === 'memo' ? '메모' : '하이라이트'}
        </span>
      </div>
      <div className="na-row__main">
        <p className="na-row__title">{entry.videoTitle}</p>
        <p className="na-row__body">{entry.body}</p>
        <div className="na-row__tags">
          {entry.tags.map((t) => (
            <span key={t} className="na-tag na-tag--sm">
              {t}
            </span>
          ))}
        </div>
      </div>
      <div className="na-row__meta">
        <p className="na-row__channel">{entry.channelName}</p>
        <p className="na-row__timecode" title="영상 내 시점">
          {entry.timeLabel}
        </p>
        <p className="na-row__imp">{importanceShort(entry.importance)}</p>
        {entry.reviewNeeded ? <span className="na-review-badge na-review-badge--sm">복습</span> : null}
      </div>
      <div className="na-row__actions">
        <Button variant="primary" size="sm" type="button" onClick={() => navigate(`/videos/${entry.videoId}?t=${entry.timeSec}`)}>
          시점 이동
        </Button>
        <Button variant="ghost" size="sm" type="button" onClick={() => navigate(`/videos/${entry.videoId}`)}>
          영상
        </Button>
      </div>
    </div>
  )
}
