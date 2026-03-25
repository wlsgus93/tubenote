/** 컬렉션(카테고리)별 학습 비중 */
export type AnalyticsCategoryShare = {
  collectionId: string
  name: string
  /** 0–100 */
  percent: number
  minutes: number
}

/** 주간 하루 활동 */
export type AnalyticsWeekDay = {
  /** 짧은 요일 라벨 */
  label: string
  learningMinutes: number
  sessionCount: number
}

/** 선호 채널 순위 */
export type AnalyticsChannelRank = {
  rank: number
  channelName: string
  completedOrTouchedCount: number
  learningMinutes: number
}

/** 길이 선호 버킷 */
export type AnalyticsLengthBucket = {
  id: 'short' | 'medium' | 'long'
  label: string
  /** 0–100 */
  percent: number
  videoCount: number
}

/** 통계 페이지 단일 묶음 — mock/API 공통 */
export type AnalyticsBundle = {
  /** 예: "이번 주 (3/17 – 3/23)" */
  periodLabel: string
  totalLearningMinutes: number
  completedVideoCount: number
  memoWrittenCount: number
  reviewSessionCount: number
  categoryShares: AnalyticsCategoryShare[]
  weekActivity: AnalyticsWeekDay[]
  channelPreferences: AnalyticsChannelRank[]
  lengthPreferences: AnalyticsLengthBucket[]
  /** 동기·피드백 상단 카피 */
  feedbackHeadline: string
  feedbackBody: string
}
