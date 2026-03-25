import { useMemo } from 'react'
import { getGoogleOAuthAuthorizeUrl } from '@/shared/constants/googleOAuth'
import { Button } from '@/shared/ui'

type GoogleSignInButtonProps = {
  disabled?: boolean
}

/**
 * Spring OAuth2 — 백엔드 `/oauth2/authorization/google` 로 브라우저 전체 이동
 * (GIS·프론트 client_id 불필요)
 */
export function GoogleSignInButton({ disabled = false }: GoogleSignInButtonProps) {
  const authorizeUrl = useMemo(() => getGoogleOAuthAuthorizeUrl(), [])

  /** OAuth는 최상위 창에서만 허용 — iframe(IDE 미리보기 등)에서는 location.assign 만으로 오류·차단이 날 수 있음 */
  function handleClick() {
    const url = authorizeUrl
    try {
      const topWin = window.top ?? window
      topWin.location.assign(url)
    } catch {
      window.open(url, '_top', 'noopener,noreferrer')
    }
  }

  return (
    <Button
      type="button"
      variant="primary"
      size="md"
      className="google-signin-action"
      disabled={disabled}
      onClick={handleClick}
    >
      Google 계정으로 로그인
    </Button>
  )
}
