import { useNavigate } from 'react-router-dom'
import { Button } from '@/shared/ui/Button'
import type { QuickActionItem } from '@/shared/types/dashboard'

export type DashboardQuickActionsProps = {
  actions: QuickActionItem[]
}

/** 자주 쓰는 이동 — 학습 재개·메모·탐색 유도 */
export function DashboardQuickActions({ actions }: DashboardQuickActionsProps) {
  const navigate = useNavigate()

  return (
    <nav className="dash-quick" aria-label="빠른 이동">
      {actions.map((a) => (
        <Button
          key={a.id}
          variant={a.variant ?? 'ghost'}
          size="sm"
          onClick={() => navigate(a.to)}
        >
          {a.label}
        </Button>
      ))}
    </nav>
  )
}
