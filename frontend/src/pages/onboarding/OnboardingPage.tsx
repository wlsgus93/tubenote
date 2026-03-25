import { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import '@/features/settings-hub/settings-hub.css'
import '@/features/onboarding-flow/onboarding.css'
import {
  DEFAULT_INTEREST_IDS,
  DEFAULT_NOTIFICATION_PREFS,
  LEARNING_INTEREST_OPTIONS,
} from '@/mocks/settingsPreferences'
import { Button } from '@/shared/ui'
import { clearOnboardingDone, writeOnboardingDone } from '@/shared/constants/storage'
import type { SettingsNotificationPrefs } from '@/shared/types/settings'

const STEP_COUNT = 4

/** 신규 사용자 온보딩 — 완료 시 localStorage 플래그만 설정(mock) */
export function OnboardingPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [interests, setInterests] = useState<Set<string>>(() => new Set(DEFAULT_INTEREST_IDS))
  const [notify, setNotify] = useState<SettingsNotificationPrefs>(DEFAULT_NOTIFICATION_PREFS)

  const toggleInterest = useCallback((id: string) => {
    setInterests((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }, [])

  const patchNotify = useCallback((patch: Partial<SettingsNotificationPrefs>) => {
    setNotify((p) => ({ ...p, ...patch }))
  }, [])

  const finish = useCallback(() => {
    writeOnboardingDone()
    navigate('/dashboard', { replace: true })
  }, [navigate])

  const skipToDashboard = useCallback(() => {
    writeOnboardingDone()
    navigate('/dashboard', { replace: true })
  }, [navigate])

  const restartFlow = useCallback(() => {
    clearOnboardingDone()
    setStep(0)
  }, [])

  return (
    <div className="ob-page">
      <div className="ob-card">
        <div className="ob-progress" aria-hidden>
          {Array.from({ length: STEP_COUNT }, (_, i) => (
            <span key={i} className={i <= step ? 'ob-progress__dot ob-progress__dot--on' : 'ob-progress__dot'} />
          ))}
        </div>

        {step === 0 ? (
          <>
            <h1 className="ob-title">유튜브 학습을 한곳에서 정리해요</h1>
            <p className="ob-desc">
              영상·메모·큐·통계가 학습 흐름으로 이어지도록 설계했습니다. 잠깐만 취향을 알려 주시면 나중에 추천과
              통계에 반영할 수 있어요(현재는 mock).
            </p>
            <div className="ob-actions">
              <Button variant="primary" type="button" onClick={() => setStep(1)}>
                다음
              </Button>
            </div>
          </>
        ) : null}

        {step === 1 ? (
          <>
            <h1 className="ob-title">어떤 주제를 주로 공부하나요?</h1>
            <p className="ob-desc">복수 선택 가능합니다. 설정에서 언제든 바꿀 수 있어요.</p>
            <div className="ob-chips" role="group" aria-label="관심 주제">
              {LEARNING_INTEREST_OPTIONS.map((o) => {
                const on = interests.has(o.id)
                return (
                  <button
                    key={o.id}
                    type="button"
                    className={on ? 'ob-chip ob-chip--on' : 'ob-chip'}
                    aria-pressed={on}
                    onClick={() => toggleInterest(o.id)}
                  >
                    {o.label}
                  </button>
                )
              })}
            </div>
            <div className="ob-actions ob-actions--spread">
              <Button variant="ghost" type="button" onClick={() => setStep(0)}>
                이전
              </Button>
              <Button variant="primary" type="button" onClick={() => setStep(2)}>
                다음
              </Button>
            </div>
          </>
        ) : null}

        {step === 2 ? (
          <>
            <h1 className="ob-title">알림은 어떻게 받을까요?</h1>
            <p className="ob-desc">실제 발송은 아직 없고, 나중에 붙일 API와 같은 스위치입니다.</p>
            <label className="settings-toggle">
              <input
                type="checkbox"
                checked={notify.emailDigest}
                onChange={(e) => patchNotify({ emailDigest: e.target.checked })}
              />
              <span className="settings-toggle__text">
                <strong>이메일 요약</strong>
                <span>주간 학습 요약 메일</span>
              </span>
            </label>
            <label className="settings-toggle">
              <input
                type="checkbox"
                checked={notify.studyReminder}
                onChange={(e) => patchNotify({ studyReminder: e.target.checked })}
              />
              <span className="settings-toggle__text">
                <strong>학습 리마인더</strong>
                <span>큐·복습 알림(예정)</span>
              </span>
            </label>
            <label className="settings-toggle">
              <input
                type="checkbox"
                checked={notify.weeklySummary}
                onChange={(e) => patchNotify({ weeklySummary: e.target.checked })}
              />
              <span className="settings-toggle__text">
                <strong>주간 리포트</strong>
                <span>통계와 맞춘 피드백(예정)</span>
              </span>
            </label>
            <div className="ob-actions ob-actions--spread">
              <Button variant="ghost" type="button" onClick={() => setStep(1)}>
                이전
              </Button>
              <Button variant="primary" type="button" onClick={() => setStep(3)}>
                다음
              </Button>
            </div>
          </>
        ) : null}

        {step === 3 ? (
          <>
            <h1 className="ob-title">준비됐어요</h1>
            <p className="ob-desc">
              대시보드에서 오늘의 큐와 이어보기를 확인해 보세요. 구독 채널·나중에 보기·메모 아카이브는 사이드 메뉴에
              모여 있습니다.
            </p>
            <div className="ob-actions ob-actions--spread">
              <Button variant="ghost" type="button" onClick={restartFlow}>
                처음부터 다시
              </Button>
              <Button variant="primary" type="button" onClick={finish}>
                대시보드로 시작하기
              </Button>
            </div>
          </>
        ) : null}

        <p className="ob-skip">
          <button type="button" className="ob-skip-btn" onClick={skipToDashboard}>
            건너뛰고 대시보드로
          </button>
        </p>
      </div>
    </div>
  )
}
