import type { SettingsNotificationPrefs } from '@/shared/types/settings'

export type NotificationsSectionProps = {
  prefs: SettingsNotificationPrefs
  onChange: (patch: Partial<SettingsNotificationPrefs>) => void
}

/** 알림 — 실제 발송은 미구현, UI만 API 스키마에 맞춤 */
export function NotificationsSection({ prefs, onChange }: NotificationsSectionProps) {
  return (
    <section className="settings-section" aria-labelledby="settings-notify-title">
      <h2 id="settings-notify-title" className="settings-section__title">
        알림
      </h2>
      <p className="settings-section__desc">나중에 푸시·이메일 채널이 붙으면 이 설정이 그대로 쓰일 수 있게 잡아 두었습니다.</p>

      <label className="settings-toggle">
        <input
          type="checkbox"
          checked={prefs.emailDigest}
          onChange={(e) => onChange({ emailDigest: e.target.checked })}
        />
        <span className="settings-toggle__text">
          <strong>이메일 요약</strong>
          <span>주 1회 학습 요약을 메일로 받습니다.</span>
        </span>
      </label>

      <label className="settings-toggle">
        <input
          type="checkbox"
          checked={prefs.studyReminder}
          onChange={(e) => onChange({ studyReminder: e.target.checked })}
        />
        <span className="settings-toggle__text">
          <strong>학습 리마인더</strong>
          <span>나중에 보기·복습이 쌓였을 때 알림(추후).</span>
        </span>
      </label>

      <label className="settings-toggle">
        <input
          type="checkbox"
          checked={prefs.weeklySummary}
          onChange={(e) => onChange({ weeklySummary: e.target.checked })}
        />
        <span className="settings-toggle__text">
          <strong>주간 리포트</strong>
          <span>통계 페이지와 맞춘 주간 피드백 알림(추후).</span>
        </span>
      </label>
    </section>
  )
}
