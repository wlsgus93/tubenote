import type { SettingsProfile } from '@/shared/types/settings'

export type ProfileSectionProps = {
  profile: SettingsProfile
  onDisplayNameChange: (v: string) => void
}

/** 프로필 — 표시 이름 편집, 이메일은 API 연동 전까지 읽기 전용 안내 */
export function ProfileSection({ profile, onDisplayNameChange }: ProfileSectionProps) {
  return (
    <section className="settings-section" aria-labelledby="settings-profile-title">
      <h2 id="settings-profile-title" className="settings-section__title">
        프로필
      </h2>
      <p className="settings-section__desc">학습 허브에 표시되는 이름입니다. 이메일은 로그인 연동 후 수정할 수 있어요.</p>
      <div className="settings-field">
        <label className="settings-field__label" htmlFor="settings-display-name">
          표시 이름
        </label>
        <input
          id="settings-display-name"
          className="settings-input"
          type="text"
          autoComplete="nickname"
          value={profile.displayName}
          onChange={(e) => onDisplayNameChange(e.target.value)}
        />
      </div>
      <div className="settings-field">
        <label className="settings-field__label" htmlFor="settings-email">
          이메일
        </label>
        <input
          id="settings-email"
          className="settings-input"
          type="email"
          value={profile.email}
          disabled
          readOnly
          aria-describedby="settings-email-hint"
        />
        <p id="settings-email-hint" className="settings-field__hint">
          mock 고정값입니다. 추후 OAuth·계정 API와 연결됩니다.
        </p>
      </div>
    </section>
  )
}
