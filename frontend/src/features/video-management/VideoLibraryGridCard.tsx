import type { LearningStatus } from '@/shared/types/learning'
import type { VideoCollection, VideoLibraryEntry } from '@/shared/types/video-library'
import { StatusBadge } from '@/shared/ui/StatusBadge'

export type VideoLibraryGridCardProps = {
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

/** 학습 자산 그리드 카드 — 본문 클릭은 상세, 하단은 분류 액션 */
export function VideoLibraryGridCard({
  video,
  collections,
  collectionName,
  onOpen,
  onStatusChange,
  onStarToggle,
  onCollectionChange,
}: VideoLibraryGridCardProps) {
  const { id, title, channelName, thumbnailUrl, durationLabel, progressPercent, learningStatus, priority, reviewNeeded, tags, isStarred } =
    video

  return (
    <article className={`vlib-card${isStarred ? ' vlib-card--starred' : ''}`}>
      <button type="button" className="vlib-card__main" onClick={() => onOpen(id)}>
        <div className="vlib-card__thumb-wrap">
          {thumbnailUrl ? <img className="vlib-card__thumb" src={thumbnailUrl} alt="" /> : null}
          {durationLabel ? <span className="vlib-card__duration">{durationLabel}</span> : null}
        </div>
        <div className="vlib-card__body">
          <h3 className="vlib-card__title">{title}</h3>
          <p className="vlib-card__meta">{channelName}</p>
          <p className="vlib-card__collection">컬렉션 · {collectionName}</p>
          {tags.length > 0 ? (
            <div className="vlib-card__tags">
              {tags.map((t) => (
                <span key={t} className="vlib-pill">
                  {t}
                </span>
              ))}
            </div>
          ) : null}
          <div className="vlib-card__progress" aria-hidden>
            <div
              className="vlib-card__progress-bar"
              style={{ width: `${Math.min(100, Math.max(0, progressPercent))}%` }}
            />
          </div>
          <div className="vlib-card__badges">
            <StatusBadge type="learning" status={learningStatus} />
            {priority ? <StatusBadge type="priority" level={priority} /> : null}
            {reviewNeeded ? <StatusBadge type="review" needed /> : null}
          </div>
        </div>
      </button>
      <div
        className="vlib-card__actions"
        onClick={(e) => e.stopPropagation()}
        onKeyDown={(e) => e.stopPropagation()}
      >
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
    </article>
  )
}
