/** 온보딩 완료 여부 — 클라이언트만, API 전환 시 사용자 메타로 대체 */
export const ONBOARDING_DONE_STORAGE_KEY = 'ylh.onboarding.v1'

export function readOnboardingDone(): boolean {
  try {
    return localStorage.getItem(ONBOARDING_DONE_STORAGE_KEY) === '1'
  } catch {
    return false
  }
}

export function writeOnboardingDone(): void {
  try {
    localStorage.setItem(ONBOARDING_DONE_STORAGE_KEY, '1')
  } catch {
    /* 저장소 불가 시 무시 */
  }
}

export function clearOnboardingDone(): void {
  try {
    localStorage.removeItem(ONBOARDING_DONE_STORAGE_KEY)
  } catch {
    /* ignore */
  }
}
