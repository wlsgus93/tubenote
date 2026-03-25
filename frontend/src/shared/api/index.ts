export { apiGet, apiPatch, apiPost, apiRequest } from '@/shared/api/client'
export type { ApiRequestOptions } from '@/shared/api/client'
export { ApiError } from '@/shared/api/errors'
export { configureApiClient } from '@/shared/api/interceptors'
export type { ApiClientHandlers } from '@/shared/api/interceptors'
export {
  applyLoginResponsePayload,
  loginAndStoreToken,
  loginWithGoogleIdTokenAndStore,
  logoutClient,
} from '@/shared/api/auth'
export { fetchDashboard, mapDashboardPayloadToBundle } from '@/shared/api/dashboard'
export {
  fetchVideoDetail,
  fetchVideos,
  mapVideoDetailDto,
  mapVideoLibraryItemDto,
  patchVideoLearningState,
  patchVideoProgress,
  unwrapVideoListPayload,
} from '@/shared/api/videos'
