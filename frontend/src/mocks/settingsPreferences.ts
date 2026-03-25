import type { LinkedAccountRow, SettingsNotificationPrefs, SettingsProfile } from '@/shared/types/settings'

export type LearningInterestOption = {
  id: string
  label: string
}

/** 학습 관심사 칩 옵션 — 추후 서버에서 주제 트리로 확장 가능 */
export const LEARNING_INTEREST_OPTIONS: LearningInterestOption[] = [
  { id: 'fe', label: '프론트엔드' },
  { id: 'be', label: '백엔드' },
  { id: 'devops', label: 'DevOps' },
  { id: 'cs', label: 'CS·알고리즘' },
  { id: 'product', label: '프로덕트·UX' },
  { id: 'career', label: '커리어' },
  { id: 'lang', label: '언어' },
  { id: 'data', label: '데이터·ML' },
]

export const DEFAULT_SETTINGS_PROFILE: SettingsProfile = {
  displayName: '학습자',
  email: 'you@example.com',
}

export const DEFAULT_INTEREST_IDS: string[] = ['fe', 'cs', 'product']

export const DEFAULT_NOTIFICATION_PREFS: SettingsNotificationPrefs = {
  emailDigest: true,
  studyReminder: true,
  weeklySummary: false,
}

export const LINKED_ACCOUNTS_MOCK: LinkedAccountRow[] = [
  {
    id: 'yt',
    providerLabel: 'YouTube',
    description: '구독 채널·나중에 보기와 동기화할 계정입니다.',
    status: 'connected',
    connectedDetail: '연결됨 · mock',
  },
  {
    id: 'google',
    providerLabel: 'Google',
    description: '로그인 및 캘린더 연동(예정)에 사용합니다.',
    status: 'disconnected',
  },
]
