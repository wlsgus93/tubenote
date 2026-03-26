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
   * IDE 단순 브라우저·미리보기 등에서 부모 프레임이 chrome-error:// 이거나 교차 출처면
   * {@code top.location.assign} 이 "Domains, protocols and ports must match" 로 막힌다.
   * 같은 origin 의 top 만 갱신하고, 그 외에는 현재 창 또는 새 탭으로 연다.
   */
  function handleClick() {
    const raw = authorizeUrl
    const absolute =
      raw.startsWith('http://') || raw.startsWith('https://')
        ? raw
        : new URL(raw, window.location.origin).href

    if (import.meta.env.DEV) {
      console.log('[auth] Google OAuth 시작 — 백엔드로 이동', { authorizeUrl: raw, absolute })
    }

    const topWin = window.top
    if (topWin == null || topWin === window.self) {
      window.location.assign(absolute)
      return
    }

    try {
      if (topWin.location.origin === window.location.origin) {
        topWin.location.assign(absolute)
        return
      }
    } catch {
      // 교차 출처 top (chrome-error, vscode-webview 등) — top 읽기 자체가 막힘
    }

    const opened = window.open(absolute, '_blank', 'noopener,noreferrer')
    if (!opened) {
      window.location.assign(absolute)
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
