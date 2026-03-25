import type { LearningStatus } from '@/shared/types/learning'
import type { VideoCollection, VideoLibraryEntry } from '@/shared/types/video-library'
import { StatusBadge } from '@/shared/ui/StatusBadge'

export type VideoLibraryListRowProps = {
  video: VideoLibraryEntry
  collections: VideoCollection[]
  collectionName: string
  onOpen: (id: string) => void
  onStatusChange: (id: string, status: LearningStatus) => void
  onStarToggle: (id: string) => void
  onCollectionChange: (id: string, collectionId: string) => void
}

const STATUS_OPTIONS: { value: LearningStatus; label: string }[] = [
  { value: 'not_started', label: '미시청' },
  { value: 'in_progress', label: '진행' },
  { value: 'completed', label: '완료' },
  { value: 'on_hold', label: '보류' },
]

/** 리스트 뷰 — 스캔에 유리한 한 행 레이아웃 */
export function VideoLibraryListRow({
  video,
  collections,
  collectionName,
  onOpen,
  onStatusChange,
  onStarToggle,
  onCollectionChange,
}: VideoLibraryListRowProps) {
  const {
    id,
    title,
    channelName,
    thumbnailUrl,
    durationLabel,
    progressPercent,
    learningStatus,
    priority,
    reviewNeeded,
    tags,
    isStarred,
  } = video

  return (
    <article className={`vlib-row${isStarred ? ' vlib-row--starred' : ''}`}>
      <div className="vlib-row__thumb">
        <button type="button" onClick={() => onOpen(id)} aria-label={`${title} 상세`}>
          {thumbnailUrl ? <img src={thumbnailUrl} alt="" /> : null}
          {durationLabel ? (
            <span className="vlib-card__duration" style={{ position: 'absolute', right: 6, bottom: 6 }}>
              {durationLabel}
            </span>
          ) : null}
        </button>
      </div>
      <div className="vlib-row__main">
        <button type="button" className="vlib-row__title" onClick={() => onOpen(id)}>
          {title}
        </button>
        <p className="vlib-card__meta" style={{ margin: 0 }}>
          {channelName}
        </p>
        <div className="vlib-meta-line">
          <span className="vlib-pill">컬렉션 · {collectionName}</span>
          <span className="vlib-meta-line__sep">·</span>
          <span className="vlib-pill">진행 {progressPercent}%</span>
          {tags.slice(0, 3).map((t) => (
            <span key={t} className="vlib-pill">
              {t}
            </span>
          ))}
        </div>
        <div className="vlib-card__badges" style={{ marginTop: 'var(--space-2)' }}>
          <StatusBadge type="learning" status={learningStatus} />
          {priority ? <StatusBadge type="priority" level={priority} /> : null}
          {reviewNeeded ? <StatusBadge type="review" needed /> : null}
        </div>
        <div className="vlib-row__actions" onClick={(e) => e.stopPropagation()}>
          <select
            className="vlib-select"
            aria-label="학습 상태 변경"
            value={learningStatus}
            onChange={(e) => onStatusChange(id, e.target.value as LearningStatus)}
          >
            {STATUS_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
          <button
            type="button"
            className="vlib-icon-btn"
            aria-label={isStarred ? '중요 해제' : '중요 표시'}
            aria-pressed={isStarred}
            onClick={() => onStarToggle(id)}
          >
            {isStarred ? '★' : '☆'}
          </button>
          <select
            className="vlib-select"
            aria-label="컬렉션 이동"
            value={video.collectionId}
            onChange={(e) => onCollectionChange(id, e.target.value)}
          >
            {collections.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
      </div>
    </article>
  )
}
