import { apiPost } from '@/shared/api/client'
import { clearAccessToken, setAccessToken } from '@/shared/constants/authStorage'
import type { LoginRequestDto, LoginResponsePayloadDto } from '@/shared/types/api'

/**
 * 로그인 — unwrap 후 accessToken 저장까지 한 번에
 * 백엔드 경로가 다르면 이 파일의 path 만 수정하면 된다.
 */
export async function loginAndStoreToken(body: LoginRequestDto): Promise<LoginResponsePayloadDto> {
  const payload = await apiPost<LoginResponsePayloadDto>('/api/auth/login', body, { skipAuth: true })
  if (!payload?.accessToken) {
    throw new Error('로그인 응답에 accessToken 이 없습니다.')
  }
  setAccessToken(payload.accessToken)
  return payload
}

export function logoutClient(): void {
  clearAccessToken()
}
