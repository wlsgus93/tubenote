import type { LearningPriority, LearningStatus } from '@/shared/types/learning'
import type { VideoCollection } from '@/shared/types/video-library'
import { StatusBadge } from '@/shared/ui/StatusBadge'

export type VideoDetailMetaBarProps = {
  learningStatus: LearningStatus
  priority?: LearningPriority
  reviewNeeded: boolean
  isStarred: boolean
  collectionId: string
  collections: VideoCollection[]
  onStatusChange: (s: LearningStatus) => void
  onPriorityChange: (p: LearningPriority | undefined) => void
  onCollectionChange: (id: string) => void
  onStarToggle: () => void
}

const STATUS_OPTIONS: { value: LearningStatus; label: string }[] = [
  { value: 'not_started', label: '미시청' },
  { value: 'in_progress', label: '진행' },
  { value: 'completed', label: '완료' },
  { value: 'on_hold', label: '보류' },
]

const PRIORITY_OPTIONS: { value: LearningPriority | ''; label: string }[] = [
  { value: '', label: '우선순위 없음' },
  { value: 'low', label: '낮음' },
  { value: 'normal', label: '보통' },
  { value: 'high', label: '높음' },
]

/** 학습 상태·우선순위·컬렉션·중요 — 빠른 분류 */
export function VideoDetailMetaBar({
  learningStatus,
  priority,
  reviewNeeded,
  isStarred,
  collectionId,
  collections,
  onStatusChange,
  onPriorityChange,
  onCollectionChange,
  onStarToggle,
}: VideoDetailMetaBarProps) {
  return (
    <div className="vd-meta-bar">
      <div className="vd-meta-bar__group">
        <span className="vd-meta-bar__label">상태</span>
        <select
          className="vd-meta-select"
          aria-label="학습 상태"
          value={learningStatus}
          onChange={(e) => onStatusChange(e.target.value as LearningStatus)}
        >
          {STATUS_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <StatusBadge type="learning" status={learningStatus} />
        {reviewNeeded ? <StatusBadge type="review" needed /> : null}
      </div>
      <div className="vd-meta-bar__group">
        <span className="vd-meta-bar__label">우선순위</span>
        <select
          className="vd-meta-select"
          aria-label="우선순위"
          value={priority ?? ''}
          onChange={(e) =>
            onPriorityChange(
              e.target.value ? (e.target.value as LearningPriority) : undefined,
            )
          }
        >
          {PRIORITY_OPTIONS.map((o) => (
            <option key={o.label} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>
      <div className="vd-meta-bar__group">
        <span className="vd-meta-bar__label">컬렉션</span>
        <select
          className="vd-meta-select"
          aria-label="컬렉션 이동"
          value={collectionId}
          onChange={(e) => onCollectionChange(e.target.value)}
        >
          {collections.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>
      <button
        type="button"
        className="vlib-icon-btn"
        aria-label={isStarred ? '중요 해제' : '중요 표시'}
        aria-pressed={isStarred}
        onClick={onStarToggle}
      >
        {isStarred ? '★' : '☆'}
      </button>
    </div>
  )
}
