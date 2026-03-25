import { Button } from '@/shared/ui'
import type { LearningPriority } from '@/shared/types/learning'
import type { VideoCollection } from '@/shared/types/video-library'
import type { WatchLaterIntent } from '@/shared/types/watch-later'

export type BulkActionBarProps = {
  count: number
  collections: VideoCollection[]
  bulkCollectionId: string
  onBulkCollectionIdChange: (id: string) => void
  onAddToTodayQueue: () => void
  onRemoveFromTodayQueue: () => void
  onSetIntent: (intent: WatchLaterIntent) => void
  onSetPriority: (p: LearningPriority) => void
  onMoveToCollection: () => void
  onRemoveFromList: () => void
  onClearSelection: () => void
}

/** 선택된 항목 일괄 정리 — 하단 고정, 압박 대신 ‘한 번에 계획’ 톤 */
export function BulkActionBar({
  count,
  collections,
  bulkCollectionId,
  onBulkCollectionIdChange,
  onAddToTodayQueue,
  onRemoveFromTodayQueue,
  onSetIntent,
  onSetPriority,
  onMoveToCollection,
  onRemoveFromList,
  onClearSelection,
}: BulkActionBarProps) {
  if (count === 0) return null

  return (
    <div className="wl-bulk" role="region" aria-label="선택 항목 일괄 작업">
      <div className="wl-bulk__inner">
        <p className="wl-bulk__summary">
          <strong>{count}개</strong> 선택됨 — 같은 규칙으로 한 번에 적용할 수 있어요
        </p>
        <div className="wl-bulk__actions">
          <Button variant="secondary" size="sm" type="button" onClick={onAddToTodayQueue}>
            오늘 큐에 넣기
          </Button>
          <Button variant="ghost" size="sm" type="button" onClick={onRemoveFromTodayQueue}>
            오늘 큐에서 빼기
          </Button>
          <span className="wl-bulk__sep" aria-hidden />
          <Button variant="ghost" size="sm" type="button" onClick={() => onSetIntent('learning')}>
            학습용으로
          </Button>
          <Button variant="ghost" size="sm" type="button" onClick={() => onSetIntent('casual')}>
            비학습용으로
          </Button>
          <span className="wl-bulk__sep" aria-hidden />
          <Button variant="ghost" size="sm" type="button" onClick={() => onSetPriority('high')}>
            우선 높음
          </Button>
          <Button variant="ghost" size="sm" type="button" onClick={() => onSetPriority('normal')}>
            우선 보통
          </Button>
          <Button variant="ghost" size="sm" type="button" onClick={() => onSetPriority('low')}>
            우선 낮음
          </Button>
          <span className="wl-bulk__sep" aria-hidden />
          <label className="wl-bulk__collection">
            <span className="visually-hidden">일괄 컬렉션</span>
            <select
              className="wl-bulk__select"
              value={bulkCollectionId}
              onChange={(e) => onBulkCollectionIdChange(e.target.value)}
              aria-label="이동할 컬렉션"
            >
              {collections.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <Button variant="secondary" size="sm" type="button" onClick={onMoveToCollection}>
            컬렉션으로 이동
          </Button>
          <span className="wl-bulk__sep" aria-hidden />
          <Button variant="danger" size="sm" type="button" onClick={onRemoveFromList}>
            목록에서 제거
          </Button>
          <Button variant="ghost" size="sm" type="button" onClick={onClearSelection}>
            선택 해제
          </Button>
        </div>
      </div>
    </div>
  )
}
