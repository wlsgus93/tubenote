/** 액세스 토큰 — 로그인 응답 저장, 보호 API Authorization 헤더에 사용 */
export const ACCESS_TOKEN_STORAGE_KEY = 'ylh.accessToken.v1'
export const REFRESH_TOKEN_STORAGE_KEY = 'ylh.refreshToken.v1'
export const AUTH_USER_STORAGE_KEY = 'ylh.authUser.v1'

/** localStorage에 저장된 사용자 요약(JSON) */
export type StoredAuthUser = {
  id: string
  email?: string
  displayName?: string
  role?: string
}

export function getAccessToken(): string | null {
  try {
    return localStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)
  } catch {
    return null
  }
}

export function setAccessToken(token: string): void {
  try {
    localStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, token)
  } catch {
    throw new Error('액세스 토큰을 저장할 수 없습니다. 브라우저 저장소·사생활 보호 모드를 확인해 주세요.')
  }
}

export function clearAccessToken(): void {
  try {
    localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY)
  } catch {
    /* ignore */
  }
}

export function getRefreshToken(): string | null {
  try {
    return localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY)
  } catch {
    return null
  }
}

export function setRefreshToken(token: string): void {
  try {
    localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, token)
  } catch {
    throw new Error('리프레시 토큰을 저장할 수 없습니다. 브라우저 저장소·사생활 보호 모드를 확인해 주세요.')
  }
}

export function clearRefreshToken(): void {
  try {
    localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY)
  } catch {
    /* ignore */
  }
}

export function getStoredAuthUser(): StoredAuthUser | null {
  try {
    const raw = localStorage.getItem(AUTH_USER_STORAGE_KEY)
    if (!raw) return null
    const o = JSON.parse(raw) as unknown
    if (typeof o !== 'object' || o === null || typeof (o as { id?: unknown }).id !== 'string') return null
    return o as StoredAuthUser
  } catch {
    return null
  }
}

export function setStoredUser(user: StoredAuthUser): void {
  try {
    localStorage.setItem(AUTH_USER_STORAGE_KEY, JSON.stringify(user))
  } catch {
    throw new Error('사용자 정보를 저장할 수 없습니다. 브라우저 저장소·사생활 보호 모드를 확인해 주세요.')
  }
}

export function clearStoredUser(): void {
  try {
    localStorage.removeItem(AUTH_USER_STORAGE_KEY)
  } catch {
    /* ignore */
  }
}

/** 401·로그아웃 시 액세스·리프레시·캐시 사용자 일괄 삭제 */
export function clearAuthSession(): void {
  clearAccessToken()
  clearRefreshToken()
  clearStoredUser()
}
