import { formatLearningMinutes } from '@/mocks/analytics'
import type { AnalyticsCategoryShare } from '@/shared/types/analytics'

export type CategoryShareSectionProps = {
  items: AnalyticsCategoryShare[]
}

/** 카테고리(컬렉션)별 학습 비중 — 수평 막대만 사용 */
export function CategoryShareSection({ items }: CategoryShareSectionProps) {
  return (
    <section className="an-panel" aria-labelledby="an-cat-title">
      <h3 id="an-cat-title" className="an-panel__title">
        카테고리별 학습 비중
      </h3>
      <p className="an-panel__desc">어느 주제에 시간을 썼는지 한눈에 봅니다.</p>
      <ul className="an-cat-list">
        {items.map((c, i) => (
          <li key={c.collectionId} className="an-cat-item">
            <div className="an-cat-item__row">
              <span className="an-cat-item__name">{c.name}</span>
              <span className="an-cat-item__meta">
                {c.percent}% · {formatLearningMinutes(c.minutes)}
              </span>
            </div>
            <div
              className="an-cat-item__track"
              role="img"
              aria-label={`${c.name} ${c.percent}퍼센트, ${formatLearningMinutes(c.minutes)}`}
            >
              <div
                className={`an-cat-item__fill an-cat-item__fill--${(i % 3) + 1}`}
                style={{ width: `${Math.min(100, c.percent)}%` }}
              />
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}
