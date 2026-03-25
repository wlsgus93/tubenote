import { useNavigate } from 'react-router-dom'
import {
  AnalyticsKpiRow,
  CategoryShareSection,
  FeedbackIntro,
  PreferenceColumns,
  WeeklyActivityBars,
} from '@/features/learning-analytics'
import '@/features/learning-analytics/analytics.css'
import { ANALYTICS_MOCK, formatLearningMinutes } from '@/mocks/analytics'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button } from '@/shared/ui'

/** 학습 통계 — 피드백 톤의 요약·막대 시각화(mock) */
export function AnalyticsPage() {
  const navigate = useNavigate()
  const data = ANALYTICS_MOCK
  const timeLabel = formatLearningMinutes(data.totalLearningMinutes)

  return (
    <div className="an-page">
      <PageHeader
        title="학습 통계"
        description={`${data.periodLabel} 기준 요약입니다. 숫자만 보지 말고, 아래 피드백과 함께 이번 주 리듬을 점검해 보세요.`}
        actions={
          <>
            <Button variant="ghost" size="sm" onClick={() => navigate('/dashboard')}>
              대시보드
            </Button>
            <Button variant="secondary" size="sm" onClick={() => navigate('/videos')}>
              학습 자산
            </Button>
          </>
        }
      />

      <FeedbackIntro headline={data.feedbackHeadline} body={data.feedbackBody} />

      <AnalyticsKpiRow
        totalTimeLabel={timeLabel}
        completedCount={data.completedVideoCount}
        memoCount={data.memoWrittenCount}
        reviewCount={data.reviewSessionCount}
      />

      <div className="an-split">
        <CategoryShareSection items={data.categoryShares} />
        <WeeklyActivityBars days={data.weekActivity} />
      </div>

      <PreferenceColumns channels={data.channelPreferences} lengths={data.lengthPreferences} />

      <p className="an-footer-hint">
        꾸준함이 가장 큰 복리예요. 다음 주에도 한 번만 대시보드에서 &quot;지금 이어서&quot;를 열어도 충분합니다.
      </p>
    </div>
  )
}
