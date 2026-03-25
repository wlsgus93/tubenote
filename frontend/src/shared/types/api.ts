/**
 * 백엔드 공통 응답·대시보드 페이로드 DTO
 * 실제 필드명이 다르면 `dashboard.ts` 매퍼에서 보정한다.
 */

/** Spring 등에서 흔한 success + data 래퍼 */
export type ApiSuccessEnvelope<T> = {
  success: true
  data: T
}

export type ApiFailureEnvelope = {
  success: false
  message?: string
  code?: string
}

export type ApiEnvelope<T> = ApiSuccessEnvelope<T> | ApiFailureEnvelope

/** data 만 오는 래퍼 (success 생략 백엔드 대비) */
export type ApiDataWrapper<T> = {
  data: T
}

/** 로그인 요청 — 백엔드 계약에 맞게 필드 추가 가능 */
export type LoginRequestDto = {
  email: string
  password: string
}

/** Google ID 토큰 로그인 요청 — 백엔드가 검증 후 내부 JWT 발급 */
export type GoogleIdTokenLoginRequestDto = {
  idToken: string
}

/** 로그인 응답에 실릴 사용자 요약(백엔드 필드명 차이 흡수) */
export type AuthUserPayloadDto = {
  id?: string | number
  userId?: string | number
  email?: string
  displayName?: string
  name?: string
  role?: string
}

/** 로그인 응답 페이로드(unwrap 후) — 이메일/구글 공통 */
export type LoginResponsePayloadDto = {
  accessToken: string
  refreshToken?: string
  tokenType?: string
  expiresIn?: number
  user?: AuthUserPayloadDto
}

/** 대시보드에 실리는 영상 한 줄 — userVideoId 권장(라우트 `/videos/:userVideoId`) */
export type VideoCardResponseDto = {
  userVideoId?: string
  id?: string
  title: string
  channelName: string
  thumbnailUrl?: string
  progressPercent?: number
  durationLabel?: string
  learningStatus?: string
  priority?: string
  reviewNeeded?: boolean
  contextHint?: string
  uploadedAtLabel?: string
}

export type NoteCardResponseDto = {
  id: string
  videoTitle: string
  timecode: string
  excerpt: string
  createdAtLabel?: string
  reviewSuggested?: boolean
}

export type QuickActionResponseDto = {
  id: string
  label: string
  to: string
  variant?: 'primary' | 'secondary' | 'ghost'
}

export type WeeklySummaryResponseDto = {
  completedCount: number
  minutesTotal: number
  streakDays: number
  reviewDueCount: number
}

/** GET /api/dashboard unwrap 직후 페이로드 */
export type DashboardPayloadDto = {
  nextUp: VideoCardResponseDto | null
  todayQueue: VideoCardResponseDto[]
  continueWatching: VideoCardResponseDto[]
  recentNotes: NoteCardResponseDto[]
  incompleteVideos: VideoCardResponseDto[]
  newFromFavorites: VideoCardResponseDto[]
  weekly: WeeklySummaryResponseDto
  quickActions?: QuickActionResponseDto[]
}

// --- 영상 라이브러리·상세 (GET/PATCH) — 필드명 불일치 시 `videos.ts` 매퍼에서 보정

/** GET /api/videos 항목 */
export type VideoLibraryItemDto = {
  userVideoId?: string
  id?: string
  title?: string
  channelName?: string
  thumbnailUrl?: string
  durationLabel?: string
  durationSec?: number
  durationMinutes?: number
  progressPercent?: number
  learningStatus?: string
  priority?: string
  reviewNeeded?: boolean
  tags?: string[]
  isStarred?: boolean
  collectionId?: string
  collectionName?: string
  updatedAt?: string
}

export type ScriptCueDto = {
  id?: string
  startSec?: number
  endSec?: number
  text?: string
}

export type TimelineNoteDto = {
  id?: string
  timeSec?: number
  body?: string
  createdLabel?: string
}

export type VideoHighlightDto = {
  id?: string
  timeSec?: number
  quote?: string
}

export type RelatedVideoBriefDto = {
  userVideoId?: string
  id?: string
  title?: string
  durationLabel?: string
  learningStatus?: string
  progressPercent?: number
}

export type ReviewPointDto = {
  id?: string
  title?: string
  detail?: string
}

/** GET /api/videos/{userVideoId} unwrap 직후 */
export type VideoDetailResponseDto = {
  userVideoId?: string
  id?: string
  title?: string
  channelName?: string
  durationLabel?: string
  durationSec?: number
  durationMinutes?: number
  progressPercent?: number
  learningStatus?: string
  priority?: string
  reviewNeeded?: boolean
  tags?: string[]
  isStarred?: boolean
  collectionId?: string
  collectionName?: string
  scriptCues?: ScriptCueDto[]
  timelineNotes?: TimelineNoteDto[]
  highlights?: VideoHighlightDto[]
  relatedInCollection?: RelatedVideoBriefDto[]
  reviewPoints?: ReviewPointDto[]
}

export type PatchLearningStateRequestDto = {
  learningStatus: string
}

export type PatchProgressRequestDto = {
  progressPercent: number
}
