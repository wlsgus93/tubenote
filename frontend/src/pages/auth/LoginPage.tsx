import { useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { GoogleSignInButton } from '@/features/auth'
import { getAccessToken } from '@/shared/constants/authStorage'

/** Google OAuth — 백엔드 `/oauth2/authorization/google` 로 이동 */
export function LoginPage() {
  const navigate = useNavigate()

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

        <p className="login-card__note">
          <Link to="/onboarding">온보딩</Link>
        </p>
      </div>
    </div>
  )
}
