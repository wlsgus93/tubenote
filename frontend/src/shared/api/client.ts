import {
  authHeaderForLog,
  parseBodyPreview,
  tryParseResponseText,
} from '@/shared/api/apiDevLog'
import { parseResponseUnwrap } from '@/shared/api/interceptors'
import { getAccessToken } from '@/shared/constants/authStorage'

export type ApiRequestOptions = RequestInit & {
  /** true면 Authorization 헤더를 붙이지 않음(로그인 등) */
  skipAuth?: boolean
}

function getApiBaseUrl(): string {
  const base = import.meta.env.VITE_API_BASE_URL ?? ''
  return String(base).replace(/\/$/, '')
}

function buildUrl(path: string): string {
  const p = path.startsWith('/') ? path : `/${path}`
  const base = getApiBaseUrl()
  return base ? `${base}${p}` : p
}

/**
 * 공통 fetch — baseURL·JSON·Authorization·unwrap 한 경로로 처리
 */
export async function apiRequest<T>(path: string, init: ApiRequestOptions = {}): Promise<T> {
  const { skipAuth, headers: initHeaders, body, ...rest } = init
  const headers = new Headers(initHeaders)

  const isFormData = typeof FormData !== 'undefined' && body instanceof FormData
  if (body !== undefined && body !== null && typeof body === 'string' && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (isFormData) {
    headers.delete('Content-Type')
  }

  if (!skipAuth) {
    const token = getAccessToken()
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }
  }

  const url = buildUrl(path)
  const method = (rest.method ?? 'GET').toUpperCase()

  if (import.meta.env.DEV) {
    const authLine = headers.get('Authorization')
    console.groupCollapsed(`[api] → ${method} ${path}`)
    console.log('URL', url)
    console.log('skipAuth', Boolean(skipAuth))
    console.log('Authorization(개발 로그)', authLine ? authHeaderForLog(path, authLine) : '(없음)')
    console.log('요청 본문', parseBodyPreview(body))
    console.groupEnd()
  }

  const res = await fetch(url, {
    ...rest,
    headers,
    body,
  })

  if (import.meta.env.DEV) {
    const rawText = await res.clone().text()
    console.groupCollapsed(`[api] ← ${res.status} ${method} ${path}`)
    console.log('URL', url)
    console.log('본문(raw 파싱)', tryParseResponseText(rawText))
    console.groupEnd()
  }

  return parseResponseUnwrap<T>(res)
}

export async function apiGet<T>(path: string, init?: Omit<ApiRequestOptions, 'method' | 'body'>): Promise<T> {
  return apiRequest<T>(path, { ...init, method: 'GET' })
}

export async function apiPost<T>(
  path: string,
  jsonBody: unknown,
  init?: Omit<ApiRequestOptions, 'method' | 'body'>,
): Promise<T> {
  return apiRequest<T>(path, {
    ...init,
    method: 'POST',
    body: JSON.stringify(jsonBody),
  })
}

export async function apiPatch<T>(
  path: string,
  jsonBody: unknown,
  init?: Omit<ApiRequestOptions, 'method' | 'body'>,
): Promise<T> {
  return apiRequest<T>(path, {
    ...init,
    method: 'PATCH',
    body: JSON.stringify(jsonBody),
  })
}
