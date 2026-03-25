/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  /** 거의 불필요. OAuth 시작 URL만 API 베이스와 다를 때만. */
  readonly VITE_OAUTH_GOOGLE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
