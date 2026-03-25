import { Button } from '@/shared/ui'
import type { LearningPriority } from '@/shared/types/learning'
import type { VideoCollection } from '@/shared/types/video-library'
import type { WatchLaterEntry, WatchLaterIntent } from '@/shared/types/watch-later'

export type WatchLaterListRowProps = {
  entry: WatchLaterEntry
  collectionName: string
  collections: VideoCollection[]
  stale: boolean
  selected: boolean
  onToggleSelect: (id: string) => void
  onToggleTodayQueue: (id: string) => void
  onPriorityChange: (id: string, p: LearningPriority) => void
  onIntentChange: (id: string, intent: WatchLaterIntent) => void
  onCollectionChange: (id: string, collectionId: string) => void
  onOpenLibrary?: (libraryVideoId: string) => void
}

const PRIORITY_OPTIONS: { id: LearningPriority; label: string }[] = [
  { id: 'high', label: '높음' },
  { id: 'normal', label: '보통' },
  { id: 'low', label: '낮음' },
]

/** 나중에 보기 한 줄 — 선택·의도·우선순위·오늘 큐·컬렉션 */
export function WatchLaterListRow({
  entry,
  collectionName,
  collections,
  stale,
  selected,
  onToggleSelect,
  onToggleTodayQueue,
  onPriorityChange,
  onIntentChange,
  onCollectionChange,
  onOpenLibrary,
}: WatchLaterListRowProps) {
  const added = new Date(entry.addedAt).toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric',
  })

  return (
    <div className={`wl-row${selected ? ' wl-row--selected' : ''}`}>
      <div className="wl-row__check">
        <input
          type="checkbox"
          checked={selected}
          onChange={() => onToggleSelect(entry.id)}
          aria-label={`${entry.title} 선택`}
        />
      </div>
      <div className="wl-row__main">
        <div className="wl-row__title-line">
          <h3 className="wl-row__title">{entry.title}</h3>
          {stale ? (
            <span className="wl-row__stale" title="한동안 미시청으로 표시">
              오래됨
            </span>
          ) : null}
          {entry.inTodayQueue ? <span className="wl-row__queue-pill">오늘 큐</span> : null}
        </div>
        <p className="wl-row__meta">
          {entry.channelName} · {entry.durationLabel} · 담은 날 {added}
        </p>
        <div className="wl-row__chips">
          <span className={entry.intent === 'learning' ? 'wl-chip wl-chip--learn' : 'wl-chip wl-chip--casual'}>
            {entry.intent === 'learning' ? '학습용' : '비학습용'}
          </span>
          <span className="wl-chip wl-chip--muted">{collectionName}</span>
        </div>
      </div>
      <div className="wl-row__controls">
        <label className="wl-row__field-label">
          우선순위
          <select
            className="wl-row__select"
            value={entry.priority}
            onChange={(e) => onPriorityChange(entry.id, e.target.value as LearningPriority)}
            aria-label={`${entry.title} 우선순위`}
          >
            {PRIORITY_OPTIONS.map((o) => (
              <option key={o.id} value={o.id}>
                {o.label}
              </option>
            ))}
          </select>
        </label>
        <div className="wl-row__intent" role="group" aria-label="의도">
          <button
            type="button"
            className={entry.intent === 'learning' ? 'wl-seg is-on' : 'wl-seg'}
            onClick={() => onIntentChange(entry.id, 'learning')}
          >
            학습
          </button>
          <button
            type="button"
            className={entry.intent === 'casual' ? 'wl-seg is-on' : 'wl-seg'}
            onClick={() => onIntentChange(entry.id, 'casual')}
          >
            비학습
          </button>
        </div>
        <label className="wl-row__field-label">
          컬렉션
          <select
            className="wl-row__select"
            value={entry.collectionId}
            onChange={(e) => onCollectionChange(entry.id, e.target.value)}
            aria-label={`${entry.title} 컬렉션`}
          >
            {collections.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </label>
        <Button
          variant={entry.inTodayQueue ? 'secondary' : 'ghost'}
          size="sm"
          type="button"
          onClick={() => onToggleTodayQueue(entry.id)}
        >
          {entry.inTodayQueue ? '오늘 큐 빼기' : '오늘 큐에 넣기'}
        </Button>
        {entry.libraryVideoId ? (
          <Button variant="ghost" size="sm" type="button" onClick={() => onOpenLibrary?.(entry.libraryVideoId!)}>
            학습 자산 상세
          </Button>
        ) : (
          <span className="wl-row__no-lib">자산 미연결</span>
        )}
      </div>
    </div>
  )
}
