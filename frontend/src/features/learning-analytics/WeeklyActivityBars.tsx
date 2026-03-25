import type { AnalyticsWeekDay } from '@/shared/types/analytics'

export type WeeklyActivityBarsProps = {
  days: AnalyticsWeekDay[]
}

/** 주간 활동량 — 7일 세로 막대, 외부 차트 없음 */
export function WeeklyActivityBars({ days }: WeeklyActivityBarsProps) {
  const maxM = Math.max(1, ...days.map((d) => d.learningMinutes))

  return (
    <section className="an-panel" aria-labelledby="an-week-title">
      <h3 id="an-week-title" className="an-panel__title">
        주간 활동량
      </h3>
      <p className="an-panel__desc">요일별 학습 시간(분). 가장 붐빈 날을 기준으로 높이를 맞췄어요.</p>
      <div className="an-week" role="list">
        {days.map((d) => {
          const hPct = (d.learningMinutes / maxM) * 100
          const label = `${d.label}요일 ${d.learningMinutes}분, 세션 ${d.sessionCount}회`
          return (
            <div key={d.label} className="an-week__col" role="listitem">
              <div className="an-week__bar-area" aria-label={label}>
                <div className="an-week__bar-wrap">
                  <div className="an-week__bar" style={{ height: `${hPct}%` }} title={label} />
                </div>
                <span className="an-week__value" aria-hidden>
                  {d.learningMinutes > 0 ? d.learningMinutes : '—'}
                </span>
              </div>
              <span className="an-week__label">{d.label}</span>
            </div>
          )
        })}
      </div>
    </section>
  )
}
