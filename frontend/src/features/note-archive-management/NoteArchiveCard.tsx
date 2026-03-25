import { useNavigate } from 'react-router-dom'
import { Button } from '@/shared/ui'
import type { NoteArchiveEntry } from '@/shared/types/note-archive'

export type NoteArchiveCardProps = {
  entry: NoteArchiveEntry
}

function importanceLabel(p: NoteArchiveEntry['importance']) {
  if (p === 'high') return '중요 높음'
  if (p === 'normal') return '보통'
  return '낮음'
}

/** 그리드용 카드 — 학습 흔적 맥락 + 시점 점프 */
export function NoteArchiveCard({ entry }: NoteArchiveCardProps) {
  const navigate = useNavigate()
  const href = `/videos/${entry.videoId}?t=${entry.timeSec}`

  const openAtTime = () => navigate(href)

  return (
    <article className="na-card">
      <p className="na-card__eyebrow">
        학습 흔적 · {entry.kind === 'memo' ? '메모' : '하이라이트'}
      </p>
      <h3 className="na-card__video">{entry.videoTitle}</h3>
      <p className="na-card__meta">
        {entry.channelName} · <span className="na-card__time">{entry.timeLabel}</span>
      </p>
      <p className="na-card__body">{entry.body}</p>
      <div className="na-card__tags">
        {entry.tags.map((t) => (
          <span key={t} className="na-tag">
            {t}
          </span>
        ))}
      </div>
      <div className="na-card__footer">
        <span className={`na-importance na-importance--${entry.importance}`}>{importanceLabel(entry.importance)}</span>
        {entry.reviewNeeded ? <span className="na-review-badge">복습 필요</span> : null}
      </div>
      <div className="na-card__actions">
        <Button variant="primary" size="sm" type="button" onClick={openAtTime}>
          이 시점으로 학습 화면 열기
        </Button>
        <Button variant="ghost" size="sm" type="button" onClick={() => navigate(`/videos/${entry.videoId}`)}>
          영상만 열기
        </Button>
      </div>
    </article>
  )
}
