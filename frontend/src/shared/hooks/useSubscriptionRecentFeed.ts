import { useEffect, useState } from 'react'
import { fetchSubscriptionRecentVideos } from '@/shared/api/subscriptions'
import type { ChannelRecentFeedItem } from '@/shared/types/channel-library'

/** 선택된 구독의 최근 업로드 피드 — GET …/subscriptions/{id}/recent-videos */
export function useSubscriptionRecentFeed(subscriptionId: string | null) {
  const [items, setItems] = useState<ChannelRecentFeedItem[]>([])
  const [status, setStatus] = useState<'idle' | 'loading' | 'error' | 'success'>('idle')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!subscriptionId) {
      setItems([])
      setStatus('idle')
      setError(null)
      return
    }

    let cancelled = false
    setStatus('loading')
    setError(null)

    fetchSubscriptionRecentVideos(subscriptionId)
      .then((rows) => {
        if (!cancelled) {
          setItems(rows)
          setStatus('success')
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setItems([])
          setError(
            e instanceof Error && e.message
              ? e.message
              : '최근 업로드 피드를 불러오지 못했습니다.',
          )
          setStatus('error')
        }
      })

    return () => {
      cancelled = true
    }
  }, [subscriptionId])

  return { items, status, error }
}
