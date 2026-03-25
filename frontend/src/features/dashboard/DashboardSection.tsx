import type { ReactNode } from 'react'
import { SectionHeader } from '@/shared/ui/SectionHeader'

export type DashboardSectionProps = {
  /** focus: 오늘 큐 / standard: 기본 / support: 보조 톤 */
  level?: 'focus' | 'standard' | 'support'
  eyebrow?: string
  title: string
  description?: string
  actions?: ReactNode
  children: ReactNode
}

/** 대시보드 구역 — 우선순위별 시각 톤 */
export function DashboardSection({
  level = 'standard',
  eyebrow,
  title,
  description,
  actions,
  children,
}: DashboardSectionProps) {
  return (
    <section className={`dash-section dash-section--${level}`}>
      <div className="dash-section__inner">
        <div className="dash-section__header">
          <SectionHeader eyebrow={eyebrow} title={title} description={description} actions={actions} />
        </div>
        {children}
      </div>
    </section>
  )
}
