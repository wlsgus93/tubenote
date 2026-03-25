import { useCallback, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ConnectionsSection,
  InterestsSection,
  NotificationsSection,
  ProfileSection,
} from '@/features/settings-hub'
import '@/features/settings-hub/settings-hub.css'
import {
  DEFAULT_INTEREST_IDS,
  DEFAULT_NOTIFICATION_PREFS,
  DEFAULT_SETTINGS_PROFILE,
  LEARNING_INTEREST_OPTIONS,
  LINKED_ACCOUNTS_MOCK,
} from '@/mocks/settingsPreferences'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button } from '@/shared/ui'
import { clearOnboardingDone } from '@/shared/constants/storage'
import type { SettingsNotificationPrefs, SettingsProfile } from '@/shared/types/settings'

/** 프로필·관심사·알림·연동 — 로컬 상태 mock, 저장은 UI 피드백만 */
export function SettingsPage() {
  const navigate = useNavigate()
  const [profile, setProfile] = useState<SettingsProfile>(DEFAULT_SETTINGS_PROFILE)
  const [interests, setInterests] = useState<Set<string>>(() => new Set(DEFAULT_INTEREST_IDS))
  const [notifications, setNotifications] = useState<SettingsNotificationPrefs>(DEFAULT_NOTIFICATION_PREFS)
  const [savedMsg, setSavedMsg] = useState<string | null>(null)

  const toggleInterest = useCallback((id: string) => {
    setInterests((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
    setSavedMsg(null)
  }, [])

  const handleSave = useCallback(() => {
    setSavedMsg('저장했습니다. (로컬 mock — 추후 API로 대체)')
  }, [])

  const openOnboardingAgain = useCallback(() => {
    clearOnboardingDone()
    navigate('/onboarding')
  }, [navigate])

  return (
    <div className="settings-page">
      <PageHeader
        title="설정"
        description="학습 경험과 알림, 외부 연동 상태를 한곳에서 봅니다. 민감한 동작은 API 연동 후 활성화됩니다."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/dashboard')}>
            대시보드
          </Button>
        }
      />

      <ProfileSection
        profile={profile}
        onDisplayNameChange={(displayName) => {
          setProfile((p) => ({ ...p, displayName }))
          setSavedMsg(null)
        }}
      />

      <InterestsSection options={LEARNING_INTEREST_OPTIONS} selectedIds={interests} onToggle={toggleInterest} />

      <NotificationsSection
        prefs={notifications}
        onChange={(patch) => {
          setNotifications((p) => ({ ...p, ...patch }))
          setSavedMsg(null)
        }}
      />

      <ConnectionsSection accounts={LINKED_ACCOUNTS_MOCK} />

      <div className="settings-save-row">
        <Button variant="primary" type="button" onClick={handleSave}>
          변경 사항 저장
        </Button>
        {savedMsg ? (
          <p className="settings-save-msg" role="status">
            {savedMsg}
          </p>
        ) : null}
      </div>

      <footer className="settings-footer-links">
        <p>처음 설정을 다시 진행하려면 온보딩으로 이동하세요. 완료 플래그가 초기화됩니다.</p>
        <Button variant="secondary" size="sm" type="button" onClick={openOnboardingAgain}>
          온보딩 다시 보기
        </Button>
        <p className="settings-footer-links__sub">
          <Link to="/login">로그인 페이지</Link> ·{' '}
          <Link to="/onboarding">온보딩 직접 열기</Link>
        </p>
      </footer>
    </div>
  )
}
