import type { ReactNode } from 'react'

export type PageHeaderProps = {
  title: string
  description?: string
  actions?: ReactNode
}

/** 페이지 상단 — 제목·설명·(선택) 액션으로 3초 이해 목적 */
export function PageHeader({ title, description, actions }: PageHeaderProps) {
  return (
    <header className="page-header">
      <div className="page-header__row">
        <div>
          <h1 className="page-header__title">{title}</h1>
          {description ? <p className="page-header__description">{description}</p> : null}
        </div>
        {actions ? <div>{actions}</div> : null}
      </div>
    </header>
  )
}
