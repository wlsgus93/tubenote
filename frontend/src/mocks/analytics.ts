import type { AnalyticsBundle } from '@/shared/types/analytics'

/** 분 → "N시간 M분" 표시 */
export function formatLearningMinutes(totalMinutes: number): string {
  if (totalMinutes < 60) return `${totalMinutes}분`
  const h = Math.floor(totalMinutes / 60)
  const m = totalMinutes % 60
  return m > 0 ? `${h}시간 ${m}분` : `${h}시간`
}

/**
 * 학습 통계 mock — 대시보드·라이브러리 톤과 맞춘 가상 수치
 */
export const ANALYTICS_MOCK: AnalyticsBundle = {
  periodLabel: '이번 주 (3/17 – 3/23)',
  totalLearningMinutes: 384,
  completedVideoCount: 6,
  memoWrittenCount: 28,
  reviewSessionCount: 14,
  categoryShares: [
    { collectionId: 'col-fe', name: '프론트엔드 심화', percent: 32, minutes: 122 },
    { collectionId: 'col-be', name: '백엔드·인프라', percent: 28, minutes: 108 },
    { collectionId: 'col-inbox', name: '인박스', percent: 18, minutes: 68 },
    { collectionId: 'col-career', name: '커리어·생산성', percent: 14, minutes: 55 },
    { collectionId: 'col-lang', name: '언어·커뮤니케이션', percent: 8, minutes: 31 },
  ],
  weekActivity: [
    { label: '월', learningMinutes: 45, sessionCount: 2 },
    { label: '화', learningMinutes: 72, sessionCount: 3 },
    { label: '수', learningMinutes: 38, sessionCount: 2 },
    { label: '목', learningMinutes: 91, sessionCount: 4 },
    { label: '금', learningMinutes: 55, sessionCount: 2 },
    { label: '토', learningMinutes: 48, sessionCount: 2 },
    { label: '일', learningMinutes: 35, sessionCount: 1 },
  ],
  channelPreferences: [
    { rank: 1, channelName: 'FE 아카이브', completedOrTouchedCount: 8, learningMinutes: 95 },
    { rank: 2, channelName: 'DevOps 스낵', completedOrTouchedCount: 5, learningMinutes: 62 },
    { rank: 3, channelName: '백엔드 실무 노트', completedOrTouchedCount: 4, learningMinutes: 58 },
    { rank: 4, channelName: 'CS 한 입', completedOrTouchedCount: 3, learningMinutes: 44 },
    { rank: 5, channelName: '커리어 로그', completedOrTouchedCount: 3, learningMinutes: 38 },
  ],
  lengthPreferences: [
    { id: 'short', label: '짧음 (~15분)', percent: 35, videoCount: 12 },
    { id: 'medium', label: '중간 (15~45분)', percent: 45, videoCount: 15 },
    { id: 'long', label: '김 (45분~)', percent: 20, videoCount: 7 },
  ],
  feedbackHeadline: '이번 주도 꾸준히 쌓였어요',
  feedbackBody:
    '완료한 영상과 메모·복습 기록이 함께 늘어나고 있어요. 짧은 영상 비중이 높은 날은 “틈새 학습”이 잘 먹혔다는 신호로 보면 됩니다. 다음 주는 한 번씩 중간 길이 영상으로 개념을 묶어 보는 것도 좋아요.',
}
