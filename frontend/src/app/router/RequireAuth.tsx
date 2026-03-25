import { Navigate, Outlet } from 'react-router-dom'
import { getAccessToken } from '@/shared/constants/authStorage'

/** 액세스 토큰 없으면 로그인 랜딩으로 */
export function RequireAuth() {
  if (!getAccessToken()) {
    return <Navigate to="/login" replace />
  }
  return <Outlet />
}
