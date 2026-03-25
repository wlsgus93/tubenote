/** 액세스 토큰 — 로그인 응답 저장, 보호 API Authorization 헤더에 사용 */
export const ACCESS_TOKEN_STORAGE_KEY = 'ylh.accessToken.v1'

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
    /* private mode 등 */
  }
}

export function clearAccessToken(): void {
  try {
    localStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY)
  } catch {
    /* ignore */
  }
}
