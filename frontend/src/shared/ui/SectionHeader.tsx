import type { ReactNode } from 'react'

export type SectionHeaderProps = {
  /** 섹션 위 작은 라벨 */
  eyebrow?: string
  title: string
  description?: string
  actions?: ReactNode
}

/** 페이지 내 구역 위계 — PageHeader보다 한 단계 낮음 */
export function SectionHeader({ eyebrow, title, description, actions }: SectionHeaderProps) {
  return (
    <div className="ui-section-header">
      <div>
        {eyebrow ? <p className="ui-section-header__eyebrow">{eyebrow}</p> : null}
        <h2 className="ui-section-header__title">{title}</h2>
        {description ? <p className="ui-section-header__description">{description}</p> : null}
      </div>
      {actions ? <div>{actions}</div> : null}
    </div>
  )
}
