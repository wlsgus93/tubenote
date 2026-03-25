import { apiPost } from '@/shared/api/client'
import {
  clearAuthSession,
  clearRefreshToken,
  clearStoredUser,
  setAccessToken,
  setRefreshToken,
  setStoredUser,
  type StoredAuthUser,
} from '@/shared/constants/authStorage'
import type {
  AuthUserPayloadDto,
  GoogleIdTokenLoginRequestDto,
  LoginRequestDto,
  LoginResponsePayloadDto,
} from '@/shared/types/api'

function normalizeStoredUser(dto: AuthUserPayloadDto): StoredAuthUser {
  const rawId = dto.id ?? dto.userId
  if (rawId === undefined || rawId === null || String(rawId).trim() === '') {
    throw new Error('로그인 응답에 사용자 식별자(id/userId)가 없습니다.')
  }
  const displayName = dto.displayName ?? dto.name
  return {
    id: String(rawId),
    email: dto.email,
    displayName,
    role: dto.role,
  }
}

/**
 * 로그인 페이로드를 로컬 세션에 반영 — Google credential은 여기 넣지 않음(백엔드 JWT만 저장)
 */
export function applyLoginResponsePayload(payload: LoginResponsePayloadDto): void {
  if (!payload?.accessToken) {
    throw new Error('로그인 응답에 accessToken 이 없습니다.')
  }
  setAccessToken(payload.accessToken)
  if (payload.refreshToken) {
    setRefreshToken(payload.refreshToken)
  } else {
    clearRefreshToken()
  }
  if (payload.user) {
    setStoredUser(normalizeStoredUser(payload.user))
  } else {
    clearStoredUser()
  }
}

/**
 * 로그인 — unwrap 후 accessToken·refreshToken·user 저장까지 한 번에
 */
export async function loginAndStoreToken(body: LoginRequestDto): Promise<LoginResponsePayloadDto> {
  const payload = await apiPost<LoginResponsePayloadDto>('/api/auth/login', body, { skipAuth: true })
  applyLoginResponsePayload(payload)
  return payload
}

/**
 * Google ID 토큰으로 로그인 — POST /api/auth/google/login
 */
export async function loginWithGoogleIdTokenAndStore(idToken: string): Promise<LoginResponsePayloadDto> {
  const body: GoogleIdTokenLoginRequestDto = { idToken }
  const payload = await apiPost<LoginResponsePayloadDto>('/api/auth/google/login', body, { skipAuth: true })
  applyLoginResponsePayload(payload)
  return payload
}

export function logoutClient(): void {
  clearAuthSession()
}
