/**
 * Spring Security OAuth2 Client 기본 진입점
 * 전체 URL(ngrok 등)을 쓰려면 VITE_OAUTH_GOOGLE_URL 우선.
 * 없으면 VITE_API_BASE_URL + /oauth2/authorization/google
 * 둘 다 없으면 상대 경로 /oauth2/authorization/google (Vite proxy 필요)
 */
export function getGoogleOAuthAuthorizeUrl(): string {
  const explicit = import.meta.env.VITE_OAUTH_GOOGLE_URL?.trim()
  if (explicit) return explicit

  const base = import.meta.env.VITE_API_BASE_URL?.trim().replace(/\/$/, '')
  if (base) return `${base}/oauth2/authorization/google`

  return '/oauth2/authorization/google'
}
