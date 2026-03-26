import { useCallback, useEffect, useState } from 'react'
import { fetchAllSubscriptions, mapSubscriptionResponseToChannel } from '@/shared/api/subscriptions'
import { ApiError } from '@/shared/api/errors'
import type { ChannelCategory, ChannelSubscription } from '@/shared/types/channel-library'

export function useSubscriptions(categories: ChannelCategory[]) {
  const [channels, setChannels] = useState<ChannelSubscription[]>([])
  const [status, setStatus] = useState<'loading' | 'error' | 'success'>('loading')
  const [error, setError] = useState<ApiError | null>(null)

  const reload = useCallback(() => {
    setStatus('loading')
    fetchAllSubscriptions()
      .then((rows) => {
        setChannels(rows.map((r) => mapSubscriptionResponseToChannel(r, categories)))
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
                    : '구독 목록을 불러오지 못했습니다. 네트워크·로그인 상태를 확인해 주세요.',
              })
        setError(err)
        setChannels([])
        setStatus('error')
      })
  }, [categories])

  useEffect(() => {
    reload()
  }, [reload])

  return { channels, setChannels, status, error, reload }
}
