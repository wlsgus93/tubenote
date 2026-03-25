import { useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { setAccessToken } from '@/shared/constants/authStorage'

function parseHashParams(hash: string): Record<string, string> {
  const raw = String(hash ?? '').replace(/^#/, '')
  const params = new URLSearchParams(raw)
  const out: Record<string, string> = {}
  for (const [k, v] of params.entries()) out[k] = v
  return out
}

export function AuthCallbackPage() {
  const navigate = useNavigate()

  const params = useMemo(() => parseHashParams(window.location.hash), [])
  const accessToken = params.accessToken?.trim()
  const oauthError = params.oauthError?.trim()

  useEffect(() => {
    if (oauthError) {
      navigate(`/login?oauthError=${encodeURIComponent(oauthError)}`, { replace: true })
      return
    }
    if (!accessToken) {
      navigate('/login?oauthError=NO_TOKEN', { replace: true })
      return
    }
    setAccessToken(accessToken)
    navigate('/dashboard', { replace: true })
  }, [accessToken, navigate, oauthError])

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>로그인 처리 중…</h1>
        <p className="login-card__note">잠시만 기다려 주세요.</p>
      </div>
    </div>
  )
}

