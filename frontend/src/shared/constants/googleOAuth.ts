/**
 * 백엔드 주소(`VITE_API_BASE_URL`) 뒤에 OAuth 경로만 붙이면 됨.
 * `VITE_OAUTH_GOOGLE_URL` 은 OAuth 주소만 따로 써야 할 때만(거의 안 씀).
 * 둘 다 없으면 상대 경로 → Vite가 localhost:8080 으로만 프록시.
 */
export function getGoogleOAuthAuthorizeUrl(): string {
  const explicit = import.meta.env.VITE_OAUTH_GOOGLE_URL?.trim()
  if (explicit) return explicit

  const base = import.meta.env.VITE_API_BASE_URL?.trim().replace(/\/$/, '')
  if (base) return `${base}/oauth2/authorization/google`

  if (import.meta.env.DEV) {
    console.warn(
      '[tubenote] 백엔드 주소(VITE_API_BASE_URL)가 비어 있어 /oauth2/... 가 로컬 프록시(8080)로만 갑니다. ' +
        'ngrok 등 원격 백엔드면 frontend/.env 에 백엔드 전체 URL 을 넣고 dev 서버를 재시작하세요.'
    )
  }
  return '/oauth2/authorization/google'
}
