import { useEffect, useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { GoogleSignInButton } from '@/features/auth'
import { getAccessToken } from '@/shared/constants/authStorage'
import { getGoogleOAuthAuthorizeUrl } from '@/shared/constants/googleOAuth'

/** Google OAuth — 백엔드 `/oauth2/authorization/google` 로 이동 */
export function LoginPage() {
  const navigate = useNavigate()

  const oauthTargetPreview = useMemo(() => {
    if (!import.meta.env.DEV) return null
    const url = getGoogleOAuthAuthorizeUrl()
    const backend = import.meta.env.VITE_API_BASE_URL?.trim()
    const usesProxy = !backend && !import.meta.env.VITE_OAUTH_GOOGLE_URL?.trim()
    return { url, usesProxy }
  }, [])

  useEffect(() => {
    if (getAccessToken()) {
      navigate('/dashboard', { replace: true })
    }
  }, [navigate])

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>로그인</h1>

        <div className="login-oauth">
          <GoogleSignInButton />
        </div>

        {oauthTargetPreview && (
          <p className="login-card__dev-hint" role="status">
            <span className="login-card__dev-hint-label">[dev] Google 로그인이 열 주소</span>
            <code className="login-card__dev-hint-url">{oauthTargetPreview.url}</code>
            {oauthTargetPreview.usesProxy && (
              <span className="login-card__dev-hint-note">
                백엔드 주소가 비어 있어 로컬 프록시(8080)로만 갑니다. 원격이면{' '}
                <code>frontend/.env</code>에 <code>VITE_API_BASE_URL=백엔드주소</code> 한 줄만 넣고 dev 재시작.
              </span>
            )}
          </p>
        )}

        <p className="login-card__note">
          <Link to="/onboarding">온보딩</Link>
        </p>
      </div>
    </div>
  )
}
