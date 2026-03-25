import { useCallback, useEffect, useState } from 'react'
import { ApiError, fetchVideoDetail } from '@/shared/api'
import type { VideoDetailDocument } from '@/shared/types/video-detail'

export type VideoDetailLoadStatus = 'idle' | 'loading' | 'error' | 'success' | 'notFound'

export function useVideoDetail(userVideoId: string | undefined) {
  const [detail, setDetail] = useState<VideoDetailDocument | null>(null)
  const [status, setStatus] = useState<VideoDetailLoadStatus>('idle')
  const [error, setError] = useState<ApiError | null>(null)

  const reload = useCallback(() => {
    if (!userVideoId) {
      setDetail(null)
      setStatus('idle')
      setError(null)
      return
    }
    setStatus('loading')
    fetchVideoDetail(userVideoId)
      .then((d) => {
        setDetail(d)
        setStatus('success')
        setError(null)
      })
      .catch((e) => {
        if (e instanceof ApiError && e.status === 404) {
          setDetail(null)
          setStatus('notFound')
          setError(null)
          return
        }
        const err =
          e instanceof ApiError
            ? e
            : new ApiError({
                status: 0,
                message:
                  e instanceof Error && e.message
                    ? e.message
                    : '영상 정보를 불러오지 못했습니다. 네트워크·로그인 상태를 확인해 주세요.',
              })
        setError(err)
        setDetail(null)
        setStatus('error')
      })
  }, [userVideoId])

  useEffect(() => {
    reload()
  }, [reload])

  return { detail, setDetail, status, error, reload }
}
