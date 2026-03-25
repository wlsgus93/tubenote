import { Navigate } from 'react-router-dom'
import { getAccessToken } from '@/shared/constants/authStorage'

/** 사이트 진입 — 로그인 여부에 따라 대시보드 또는 로그인 */
export function RootLanding() {
  return <Navigate to={getAccessToken() ? '/dashboard' : '/login'} replace />
}
