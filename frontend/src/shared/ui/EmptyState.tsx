import type { ReactNode } from 'react'

export type EmptyStateProps = {
  title: string
  description?: string
  action?: ReactNode
}

/** 빈 큐·빈 검색 결과 — 다음 행동 제시 */
export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="ui-empty" role="status">
      <h3 className="ui-empty__title">{title}</h3>
      {description ? <p className="ui-empty__description">{description}</p> : null}
      {action}
    </div>
  )
}
