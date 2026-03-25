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

  /**
   * OAuth는 최상위 창에서 열려야 하고, 상대 경로는 반드시 "앱이 떠 있는 프레임" 기준으로 절대 URL로 바꿔야 함.
   * 그렇지 않으면 top의 base가 chrome-error:// 이거나 IDE 미리보기 부모일 때 상대 경로 해석·도메인 불일치 오류가 난다.
   */
  function handleClick() {
    const raw = authorizeUrl
    const absolute =
      raw.startsWith('http://') || raw.startsWith('https://')
        ? raw
        : new URL(raw, window.location.origin).href
    try {
      const topWin = window.top ?? window
      topWin.location.assign(absolute)
    } catch {
      window.open(absolute, '_blank', 'noopener,noreferrer')
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
