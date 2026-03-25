export type StatCardProps = {
  label: string
  value: string | number
  hint?: string
  tone?: 'default' | 'primary' | 'success' | 'warning'
}

/** KPI·요약 숫자 — 대시보드·통계 */
export function StatCard({ label, value, hint, tone = 'default' }: StatCardProps) {
  const toneClass =
    tone === 'primary'
      ? ' ui-stat-card--tone-primary'
      : tone === 'success'
        ? ' ui-stat-card--tone-success'
        : tone === 'warning'
          ? ' ui-stat-card--tone-warning'
          : ''
  return (
    <article className={`ui-stat-card${toneClass}`}>
      <p className="ui-stat-card__label">{label}</p>
      <p className="ui-stat-card__value">{value}</p>
      {hint ? <p className="ui-stat-card__hint">{hint}</p> : null}
    </article>
  )
}
