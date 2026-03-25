/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  /**
   * Google OAuth 진입 전체 URL (ngrok 등). 예:
   * https://xxxx.ngrok-free.app/oauth2/authorization/google
   * 비우면 VITE_API_BASE_URL 기준으로 경로 조합, 그것도 없으면 /oauth2/authorization/google
   */
  readonly VITE_OAUTH_GOOGLE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
