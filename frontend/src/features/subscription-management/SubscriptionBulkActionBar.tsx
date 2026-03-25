import { Button } from '@/shared/ui'

export type SubscriptionBulkActionBarProps = {
  selectedCount: number
  onBulkUnsubscribe: () => void
  onClearSelection: () => void
}

/** 선택 채널 일괄 구독 취소 — 하단 고정 */
export function SubscriptionBulkActionBar({
  selectedCount,
  onBulkUnsubscribe,
  onClearSelection,
}: SubscriptionBulkActionBarProps) {
  if (selectedCount === 0) return null

  return (
    <div className="chlib-bulk-bar" role="region" aria-label="선택 채널 일괄 작업">
      <div className="chlib-bulk-bar__inner">
        <p className="chlib-bulk-bar__text">
          <strong>{selectedCount}개</strong> 선택됨 — 구독 목록에서 제거합니다(mock)
        </p>
        <div className="chlib-bulk-bar__actions">
          <Button variant="danger" size="sm" type="button" onClick={onBulkUnsubscribe}>
            선택 항목 구독 취소
          </Button>
          <Button variant="ghost" size="sm" type="button" onClick={onClearSelection}>
            선택 해제
          </Button>
        </div>
      </div>
    </div>
  )
}
