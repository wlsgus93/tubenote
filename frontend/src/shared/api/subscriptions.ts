import { apiGet, apiPatch, apiPost } from '@/shared/api/client'
import { API_V1_PREFIX } from '@/shared/constants/apiPaths'
import type { ChannelCategory, ChannelRecentFeedItem, ChannelSubscription } from '@/shared/types/channel-library'
import type {
  PatchSubscriptionRequestDto,
  SubscriptionRecentVideoDto,
  SubscriptionResponseDto,
  SubscriptionSyncResponseDto,
} from '@/shared/types/subscriptions-api'

function booleansFromSubscriptionDto(dto: SubscriptionResponseDto): { favorite: boolean; learning: boolean } {
  const raw = dto as unknown as Record<string, unknown>
  const fav = dto.isFavorite ?? raw.isFavorite ?? raw.favorite
  const learn = dto.isLearningChannel ?? raw.isLearningChannel ?? raw.learningChannel
  return {
    favorite: fav === true,
    learning: learn === true,
  }
}

function categoryIdFromApiCategory(
  apiCategory: string | null | undefined,
  categories: ChannelCategory[],
): string {
  if (apiCategory === undefined || apiCategory === null || apiCategory.trim() === '') {
    return 'cat-general'
  }
  const trimmed = apiCategory.trim()
  if (categories.some((c) => c.id === trimmed)) return trimmed
  const byName = categories.find((c) => c.name === trimmed)
  return byName?.id ?? trimmed
}

function formatPublishedLabel(iso: string | undefined): string | undefined {
  if (!iso) return undefined
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return undefined
  return d.toLocaleString('ko-KR', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** API 구독 한 건 → 목록 카드용 `ChannelSubscription` (recentFeed 는 별도 로드) */
export function mapSubscriptionResponseToChannel(
  dto: SubscriptionResponseDto,
  categories: ChannelCategory[],
): ChannelSubscription {
  const { favorite, learning } = booleansFromSubscriptionDto(dto)
  const ch = dto.channel
  const title = ch?.title?.trim() || '이름 없는 채널'
  const initial = title.slice(0, 1).toUpperCase()
  const lastUpload =
    dto.lastChannelVideosSyncedAt ||
    ch?.channelLastSyncedAt ||
    dto.updatedAt ||
    dto.lastSyncedAt ||
    new Date().toISOString()

  return {
    id: String(dto.subscriptionId),
    name: title,
    initial,
    categoryId: categoryIdFromApiCategory(dto.category, categories),
    focus: learning ? 'learning' : 'general',
    isFavorite: favorite,
    newVideoCount: dto.unreadNewVideoCount ?? 0,
    lastUploadAt: lastUpload,
    memo: dto.note ?? '',
    savedVideoCount: 0,
  }
}

export function mapRecentVideoToFeedItem(d: SubscriptionRecentVideoDto): ChannelRecentFeedItem | null {
  const yid = d.youtubeVideoId?.trim()
  if (!yid) return null
  return {
    youtubeVideoId: yid,
    title: d.title?.trim() || yid,
    publishedAtLabel: formatPublishedLabel(d.publishedAt),
  }
}

/** 백엔드 `SubscriptionController.list` — `size` 는 @Max(100) */
export const SUBSCRIPTIONS_PAGE_SIZE_MAX = 100

/** GET /api/v1/subscriptions (한 페이지) */
export async function fetchSubscriptions(
  page = 1,
  size = SUBSCRIPTIONS_PAGE_SIZE_MAX,
): Promise<SubscriptionResponseDto[]> {
  const safeSize = Math.min(Math.max(1, size), SUBSCRIPTIONS_PAGE_SIZE_MAX)
  const q = new URLSearchParams({ page: String(page), size: String(safeSize) })
  return apiGet<SubscriptionResponseDto[]>(`${API_V1_PREFIX}/subscriptions?${q.toString()}`)
}

/** 구독이 100개 넘을 때 페이지를 순회해 전부 수집 */
export async function fetchAllSubscriptions(): Promise<SubscriptionResponseDto[]> {
  const out: SubscriptionResponseDto[] = []
  let page = 1
  const size = SUBSCRIPTIONS_PAGE_SIZE_MAX
  const maxPages = 50
  while (page <= maxPages) {
    const chunk = await fetchSubscriptions(page, size)
    out.push(...chunk)
    if (chunk.length < size) break
    page += 1
  }
  return out
}

/** GET /api/v1/subscriptions/{subscriptionId}/recent-videos */
export async function fetchSubscriptionRecentVideos(
  subscriptionId: string | number,
  page = 1,
  size = 30,
): Promise<ChannelRecentFeedItem[]> {
  const q = new URLSearchParams({ page: String(page), size: String(size) })
  const raw = await apiGet<SubscriptionRecentVideoDto[]>(
    `${API_V1_PREFIX}/subscriptions/${encodeURIComponent(String(subscriptionId))}/recent-videos?${q.toString()}`,
  )
  return raw.map(mapRecentVideoToFeedItem).filter(Boolean) as ChannelRecentFeedItem[]
}

/** PATCH /api/v1/subscriptions/{subscriptionId} */
export async function patchSubscription(
  subscriptionId: string | number,
  body: PatchSubscriptionRequestDto,
): Promise<SubscriptionResponseDto> {
  return apiPatch<SubscriptionResponseDto>(
    `${API_V1_PREFIX}/subscriptions/${encodeURIComponent(String(subscriptionId))}`,
    body,
  )
}

/** POST /api/v1/subscriptions/sync — YouTube 구독 목록 반영 */
export async function postSubscriptionsYoutubeSync(): Promise<SubscriptionSyncResponseDto> {
  return apiPost<SubscriptionSyncResponseDto>(`${API_V1_PREFIX}/subscriptions/sync`, {})
}

/** POST /api/v1/subscriptions/channel-updates/sync — 채널별 최근 업로드 피드 갱신 */
export async function postSubscriptionChannelUpdatesSync(): Promise<unknown> {
  return apiPost<unknown>(`${API_V1_PREFIX}/subscriptions/channel-updates/sync`, {})
}
