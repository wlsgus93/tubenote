import { StatCard } from '@/shared/ui'

export type AnalyticsKpiRowProps = {
  totalTimeLabel: string
  completedCount: number
  memoCount: number
  reviewCount: number
}

/** 핵심 수치 — hint로 피드백 톤 보강 */
export function AnalyticsKpiRow({ totalTimeLabel, completedCount, memoCount, reviewCount }: AnalyticsKpiRowProps) {
  return (
    <div className="an-kpi-grid" role="list">
      <div role="listitem">
        <StatCard
          label="총 학습 시간"
          value={totalTimeLabel}
          hint="재생·복습에 쓴 시간을 합산한 값이에요."
          tone="primary"
        />
      </div>
      <div role="listitem">
        <StatCard
          label="완료한 영상"
          value={completedCount}
          hint="이번 주 학습 자산에서 완료 처리한 개수입니다."
          tone="success"
        />
      </div>
      <div role="listitem">
        <StatCard
          label="메모 작성"
          value={memoCount}
          hint="영상별 메모·하이라이트를 남긴 횟수예요."
        />
      </div>
      <div role="listitem">
        <StatCard
          label="복습 기록"
          value={reviewCount}
          hint="복습 포인트를 다시 연 횟수로 가정한 mock입니다."
          tone="warning"
        />
      </div>
    </div>
  )
}
