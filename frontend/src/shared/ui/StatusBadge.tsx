import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

export type StatusBadgeProps =
  | { type: 'learning'; status: LearningStatus }
  | { type: 'priority'; level: LearningPriority }
  | { type: 'review'; needed: boolean }

const learningLabel: Record<LearningStatus, string> = {
  not_started: '미시청',
  in_progress: '진행',
  completed: '완료',
  on_hold: '보류',
}

const priorityLabel: Record<LearningPriority, string> = {
  low: '낮은 우선순위',
  normal: '보통',
  high: '높은 우선순위',
}

/** 학습 상태 · 우선순위 · 복습 필요 — 카드·목록 메타 */
export function StatusBadge(props: StatusBadgeProps) {
  if (props.type === 'learning') {
    return (
      <span className={`ui-badge ui-badge--learning-${props.status}`}>
        {learningLabel[props.status]}
      </span>
    )
  }
  if (props.type === 'priority') {
    return (
      <span className={`ui-badge ui-badge--priority-${props.level}`}>
        {priorityLabel[props.level]}
      </span>
    )
  }
  return (
    <span className={props.needed ? 'ui-badge ui-badge--review-needed' : 'ui-badge ui-badge--review-ok'}>
      {props.needed ? '복습 권장' : '복습 양호'}
    </span>
  )
}
