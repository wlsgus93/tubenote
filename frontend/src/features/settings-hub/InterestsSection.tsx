import type { LearningInterestOption } from '@/mocks/settingsPreferences'

export type InterestsSectionProps = {
  options: LearningInterestOption[]
  selectedIds: Set<string>
  onToggle: (id: string) => void
}

/** 학습 관심사 — 추천·통계 개인화에 쓰일 태그(mock 저장) */
export function InterestsSection({ options, selectedIds, onToggle }: InterestsSectionProps) {
  return (
    <section className="settings-section" aria-labelledby="settings-interest-title">
      <h2 id="settings-interest-title" className="settings-section__title">
        학습 관심사
      </h2>
      <p className="settings-section__desc">관심 있는 주제를 골라 두면 대시보드와 추천(추후)에 반영할 수 있어요.</p>
      <div className="settings-chips" role="group" aria-label="관심 주제">
        {options.map((o) => {
          const on = selectedIds.has(o.id)
          return (
            <button
              key={o.id}
              type="button"
              className={on ? 'settings-chip settings-chip--on' : 'settings-chip'}
              aria-pressed={on}
              onClick={() => onToggle(o.id)}
            >
              {o.label}
            </button>
          )
        })}
      </div>
    </section>
  )
}
