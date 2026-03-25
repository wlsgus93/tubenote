import { clearAccessToken } from '@/shared/constants/authStorage'
import { ApiError } from '@/shared/api/errors'
import type { ApiEnvelope } from '@/shared/types/api'

/** 401/403 등 훅 — `configureApiClient`로 주입 */
export type ApiClientHandlers = {
  /** 기본: 토큰 삭제 후 `/login` 이동 */
  onUnauthorized?: () => void
  /** 기본: console 경고만 */
  onForbidden?: () => void
}

let handlers: ApiClientHandlers = {
  onUnauthorized: () => {
    clearAccessToken()
    if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/login')) {
      window.location.assign('/login')
    }
  },
  onForbidden: () => {
    console.warn('[api] 403 Forbidden')
  },
}

export function configureApiClient(next: Partial<ApiClientHandlers>): void {
  handlers = { ...handlers, ...next }
}

function extractMessage(body: unknown): string | undefined {
  if (body === null || typeof body !== 'object') return undefined
  const o = body as Record<string, unknown>
  const m = o.message ?? o.error ?? o.detail
  return typeof m === 'string' ? m : undefined
}

function extractCode(body: unknown): string | undefined {
  if (body === null || typeof body !== 'object') return undefined
  const c = (body as Record<string, unknown>).code
  return typeof c === 'string' ? c : undefined
}

/** HTTP 응답 본문을 JSON으로 읽고 공통 래퍼를 벗긴 뒤 T 반환 */
export async function parseResponseUnwrap<T>(res: Response): Promise<T> {
  const contentType = res.headers.get('content-type') ?? ''
  const text = await res.text()
  /** 204·빈 본문 PATCH 등 */
  if (res.ok && (!text || text.trim() === '')) {
    return undefined as T
  }

  let body: unknown = null
  if (text && contentType.includes('application/json')) {
    try {
      body = JSON.parse(text) as unknown
    } catch {
      body = null
    }
  }

  if (!res.ok) {
    if (res.status === 401) {
      handlers.onUnauthorized?.()
    }
    if (res.status === 403) {
      handlers.onForbidden?.()
    }
    const message = extractMessage(body) ?? res.statusText ?? '요청에 실패했습니다.'
    throw new ApiError({ status: res.status, message, code: extractCode(body) })
  }

  return unwrapEnvelope<T>(body)
}

function unwrapEnvelope<T>(body: unknown): T {
  if (body === null || body === undefined) {
    return body as T
  }

  if (typeof body !== 'object') {
    return body as T
  }

  const o = body as Record<string, unknown>

  // { success: false, message }
  if ('success' in o && o.success === false) {
    throw new ApiError({
      status: 200,
      message: typeof o.message === 'string' ? o.message : '요청에 실패했습니다.',
      code: typeof o.code === 'string' ? o.code : undefined,
    })
  }

  // { success: true, data: T }
  if ('success' in o && o.success === true && 'data' in o) {
    return o.data as T
  }

  // ApiEnvelope 형태지만 success 없이 data만
  if ('data' in o && typeof o.data !== 'undefined') {
    const onlyData = Object.keys(o).length === 1
    const looksLikeError = 'error' in o || 'errors' in o
    if (onlyData || !looksLikeError) {
      return o.data as T
    }
  }

  // 래퍼 없이 본문이 곧 T
  return body as T
}

/** 타입 가드 — 외부에서 envelope 여부 판별 시 */
export function isApiFailureEnvelope(v: unknown): v is ApiEnvelope<never> {
  return typeof v === 'object' && v !== null && 'success' in v && (v as { success: unknown }).success === false
}
