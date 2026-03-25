/** 설정 화면용 프로필(mock / 추후 GET /me) */
export type SettingsProfile = {
  displayName: string
  email: string
}

/** 알림 — 추후 PATCH /settings/notifications */
export type SettingsNotificationPrefs = {
  emailDigest: boolean
  studyReminder: boolean
  weeklySummary: boolean
}

export type LinkedAccountStatus = 'connected' | 'disconnected' | 'error'

/** 외부 계정 연동 한 행 — 추후 OAuth 상태 API */
export type LinkedAccountRow = {
  id: string
  providerLabel: string
  description: string
  status: LinkedAccountStatus
  /** 연결됨일 때 부가 문구 */
  connectedDetail?: string
}
