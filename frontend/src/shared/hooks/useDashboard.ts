import { useCallback, useEffect, useState } from 'react'
import { ApiError, fetchDashboard } from '@/shared/api'
import type { DashboardBundle } from '@/shared/types/dashboard'

export type UseDashboardState =
  | { status: 'loading' }
  | { status: 'success'; data: DashboardBundle }
  | { status: 'error'; error: ApiError }

/** 대시보드 GET /api/v1/dashboard — 로딩·에러·재시도 */
export function useDashboard() {
  const [state, setState] = useState<UseDashboardState>({ status: 'loading' })

  const load = useCallback(() => {
    setState({ status: 'loading' })
    fetchDashboard()
      .then((data) => setState({ status: 'success', data }))
      .catch((e) => {
        if (e instanceof ApiError) {
          setState({ status: 'error', error: e })
          return
        }
        const message =
          e instanceof Error && e.message ? e.message : '대시보드를 불러오지 못했습니다. 네트워크·서버 주소를 확인해 주세요.'
        setState({ status: 'error', error: new ApiError({ status: 0, message }) })
      })
  }, [])

  useEffect(() => {
    load()
  }, [load])

  return { state, reload: load }
}
