/** API 호출 실패 시 throw — status·메시지·백엔드 code 보존 */
export type ApiErrorInit = {
  status: number
  message: string
  code?: string
}

export class ApiError extends Error {
  readonly status: number
  readonly code?: string

  constructor(init: ApiErrorInit) {
    super(init.message)
    this.name = 'ApiError'
    this.status = init.status
    this.code = init.code
  }
}
