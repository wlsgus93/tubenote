import { useCallback, useEffect, useState } from 'react'
import { ApiError, fetchVideos } from '@/shared/api'
import type { VideoLibraryEntry } from '@/shared/types/video-library'

export function useVideoLibrary() {
  const [items, setItems] = useState<VideoLibraryEntry[]>([])
  const [status, setStatus] = useState<'loading' | 'error' | 'success'>('loading')
  const [error, setError] = useState<ApiError | null>(null)

  const reload = useCallback(() => {
    setStatus('loading')
    fetchVideos()
      .then((next) => {
        setItems(next)
        setStatus('success')
        setError(null)
      })
      .catch((e) => {
        const err =
          e instanceof ApiError
            ? e
            : new ApiError({
                status: 0,
                message:
                  e instanceof Error && e.message
                    ? e.message
                    : '영상 목록을 불러오지 못했습니다. 네트워크·로그인 상태를 확인해 주세요.',
              })
        setError(err)
        setStatus('error')
      })
  }, [])

  useEffect(() => {
    reload()
  }, [reload])

  return { items, setItems, status, error, reload }
}
