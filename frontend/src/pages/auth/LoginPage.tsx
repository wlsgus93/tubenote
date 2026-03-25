import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError, loginAndStoreToken } from '@/shared/api'
import { getAccessToken } from '@/shared/constants/authStorage'
import { Button } from '@/shared/ui'

/** 로그인 성공 시 accessToken 저장 후 대시보드로 이동 */
export function LoginPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  useEffect(() => {
    if (getAccessToken()) {
      navigate('/dashboard', { replace: true })
    }
  }, [navigate])

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setMessage(null)
    setSubmitting(true)
    try {
      await loginAndStoreToken({ email: email.trim(), password })
      navigate('/dashboard', { replace: true })
    } catch (err) {
      const text =
        err instanceof ApiError ? err.message : err instanceof Error ? err.message : '로그인에 실패했습니다.'
      setMessage(text)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>로그인</h1>
        <p>백엔드 `POST /api/auth/login` 으로 인증합니다. 성공 시 토큰이 저장되고 대시보드 API가 동작합니다.</p>

        <form className="login-form" onSubmit={handleSubmit}>
          <label className="login-field">
            <span className="login-field__label">이메일</span>
            <input
              className="login-field__input"
              type="email"
              name="email"
              autoComplete="username"
              value={email}
              onChange={(ev) => setEmail(ev.target.value)}
              required
            />
          </label>
          <label className="login-field">
            <span className="login-field__label">비밀번호</span>
            <input
              className="login-field__input"
              type="password"
              name="password"
              autoComplete="current-password"
              value={password}
              onChange={(ev) => setPassword(ev.target.value)}
              required
            />
          </label>
          {message ? (
            <p className="login-error" role="alert">
              {message}
            </p>
          ) : null}
          <Button variant="primary" size="md" type="submit" disabled={submitting}>
            {submitting ? '로그인 중…' : '로그인'}
          </Button>
        </form>

        <p className="login-card__note">
          <Link to="/onboarding">처음이라면 온보딩 시작</Link>
          {' · '}
          <Link to="/dashboard">대시보드로 이동</Link>
        </p>
      </div>
    </div>
  )
}
