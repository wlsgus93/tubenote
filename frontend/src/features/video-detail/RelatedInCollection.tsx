import { useNavigate } from 'react-router-dom'
import type { RelatedVideoBrief } from '@/shared/types/video-detail'
import { StatusBadge } from '@/shared/ui/StatusBadge'

export type RelatedInCollectionProps = {
  items: RelatedVideoBrief[]
  collectionName: string
}

/** 같은 컬렉션 학습 자산 */
export function RelatedInCollection({ items, collectionName }: RelatedInCollectionProps) {
  const navigate = useNavigate()

  if (items.length === 0) {
    return (
      <p className="page-header__description">
        이 컬렉션({collectionName})에 다른 영상이 없습니다. 라이브러리에서 같은 주제를 묶어 보세요.
      </p>
    )
  }

  return (
    <div className="vd-related">
      {items.map((v) => (
        <button
          key={v.id}
          type="button"
          className="vd-related-card"
          onClick={() => navigate(`/videos/${v.id}`)}
        >
          <h3 className="vd-related-card__title">{v.title}</h3>
          <div className="vd-related-card__meta">
            {v.durationLabel} · 진행 {v.progressPercent}%
          </div>
          <div style={{ marginTop: 'var(--space-2)' }}>
            <StatusBadge type="learning" status={v.learningStatus} />
          </div>
        </button>
      ))}
    </div>
  )
}
