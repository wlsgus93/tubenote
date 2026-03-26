/**
 * 개발 모드에서만 API·인증 관련 콘솔 로깅 — 프로덕션 번들에서는 호출부가 없도록 분리
 */

const SENSITIVE_BODY_KEYS = new Set([
  'password',
  'currentPassword',
  'newPassword',
  'idToken',
  'refreshToken',
  'accessToken',
  'clientSecret',
  'secret',
])

function maskToken(value: string): string {
  if (value.length <= 24) return `${value.slice(0, 6)}…(len=${value.length})`
  return `${value.slice(0, 16)}…${value.slice(-8)} (len=${value.length})`
}

/** JSON 요청 본문을 로그용으로 복사(민감 필드만 마스킹) */
export function redactJsonBodyForLog(parsed: unknown): unknown {
  if (parsed === null || typeof parsed !== 'object') return parsed
  if (Array.isArray(parsed)) {
    return parsed.map((item) => redactJsonBodyForLog(item))
  }
  const o = { ...(parsed as Record<string, unknown>) }
  for (const key of Object.keys(o)) {
    const v = o[key]
    if (SENSITIVE_BODY_KEYS.has(key) && typeof v === 'string') {
      o[key] = maskToken(v)
    } else if (v !== null && typeof v === 'object') {
      o[key] = redactJsonBodyForLog(v) as unknown
    }
  }
  return o
}

export function parseBodyPreview(body: BodyInit | null | undefined): unknown {
  if (body === undefined || body === null) return undefined
  if (typeof body === 'string') {
    try {
      return redactJsonBodyForLog(JSON.parse(body) as unknown)
    } catch {
      const s = body.length > 800 ? `${body.slice(0, 800)}…` : body
      return s
    }
  }
  if (typeof FormData !== 'undefined' && body instanceof FormData) {
    return `[FormData: ${[...body.keys()].join(', ') || '(empty)'}]`
  }
  return `[${typeof body}]`
}

export function authHeaderForLog(path: string, authValue: string | null): string {
  if (!authValue) return '(없음, skipAuth 또는 미로그인)'
  // 로그인·토큰 교환 경로는 디버깅용으로 JWT 전체 출력
  const isAuthPath =
    path.includes('/api/v1/auth') ||
    path.includes('/api/auth/') ||
    path.includes('/oauth2/') ||
    path.includes('/login')
  if (isAuthPath) return authValue
  return maskToken(authValue.replace(/^Bearer\s+/i, ''))
}

export function tryParseResponseText(text: string): unknown {
  const t = text.trim()
  if (!t) return '(빈 본문)'
  try {
    return JSON.parse(t) as unknown
  } catch {
    return text.length > 2000 ? `${text.slice(0, 2000)}…` : text
  }
}
