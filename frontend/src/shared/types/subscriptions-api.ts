/** GET /api/v1/subscriptions — `SubscriptionResponse` unwrap 후 */

export type ChannelSummaryDto = {
  channelId?: number
  youtubeChannelId?: string
  title?: string
  thumbnailUrl?: string
  customUrl?: string
  channelLastSyncedAt?: string
}

export type SubscriptionResponseDto = {
  subscriptionId: number
  channel?: ChannelSummaryDto
  category?: string | null
  isFavorite?: boolean
  isLearningChannel?: boolean
  note?: string | null
  lastSyncedAt?: string
  lastChannelVideosSyncedAt?: string
  unreadNewVideoCount?: number
  createdAt?: string
  updatedAt?: string
}

export type SubscriptionRecentVideoDto = {
  subscriptionId?: number
  channelId?: number
  channelTitle?: string
  videoId?: number
  youtubeVideoId?: string
  title?: string
  thumbnailUrl?: string
  publishedAt?: string
  isNew?: boolean
  syncedAt?: string
}

/** PATCH /api/v1/subscriptions/{id} — 전달한 필드만 갱신 */
export type PatchSubscriptionRequestDto = {
  category?: string | null
  isFavorite?: boolean
  isLearningChannel?: boolean
  note?: string | null
}

export type SubscriptionSyncResponseDto = {
  syncedCount?: number
  createdCount?: number
  updatedCount?: number
  failedCount?: number
}
