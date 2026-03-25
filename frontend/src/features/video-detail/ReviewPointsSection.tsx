import type { ReviewPoint } from '@/shared/types/video-detail'

export type ReviewPointsSectionProps = {
  points: ReviewPoint[]
}

/** 복습·학습 정리 포인트 */
export function ReviewPointsSection({ points }: ReviewPointsSectionProps) {
  if (points.length === 0) return null

  return (
    <ol className="vd-review-list">
      {points.map((p) => (
        <li key={p.id}>
          <strong>{p.title}</strong>
          {p.detail}
        </li>
      ))}
    </ol>
  )
}
