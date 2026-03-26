import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

/** 백엔드 `LearningStatus` enum JSON 값 ↔ 프론트 UI 상태 */
export function learningStatusFromBackend(value: unknown): LearningStatus {
  if (typeof value !== 'string') return 'not_started'
  const u = value.toUpperCase().replace(/-/g, '_')
  switch (u) {
    case 'NOT_STARTED':
      return 'not_started'
    case 'IN_PROGRESS':
      return 'in_progress'
    case 'COMPLETED':
      return 'completed'
    case 'DROPPED':
      return 'on_hold'
    default:
      if (value === 'not_started' || value === 'in_progress' || value === 'completed' || value === 'on_hold') {
        return value
      }
      return 'not_started'
  }
}

export function learningStatusToBackend(status: LearningStatus): string {
  const m: Record<LearningStatus, string> = {
    not_started: 'NOT_STARTED',
    in_progress: 'IN_PROGRESS',
    completed: 'COMPLETED',
    on_hold: 'DROPPED',
  }
  return m[status]
}

export function priorityFromBackend(value: unknown): LearningPriority | undefined {
  if (typeof value !== 'string') return undefined
  const u = value.toUpperCase()
  if (u === 'LOW') return 'low'
  if (u === 'NORMAL') return 'normal'
  if (u === 'HIGH' || u === 'URGENT') return 'high'
  if (value === 'low' || value === 'normal' || value === 'high') return value
  return undefined
}
