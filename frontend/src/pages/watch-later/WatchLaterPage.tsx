import { useCallback, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  applyTodayQueueOrder,
  BulkActionBar,
  filterAndSortWatchLater,
  getTodayQueueSorted,
  normalizeWatchLaterQueue,
  reorderQueueIds,
  TodayQueueStrip,
  WatchLaterListRow,
  WatchLaterToolbar,
} from '@/features/watch-later-management'
import '@/features/watch-later-management/watch-later.css'
import { isWatchLaterStale, WATCH_LATER_MOCK } from '@/mocks/watchLater'
import { VIDEO_COLLECTIONS } from '@/shared/constants/videoCollections'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, EmptyState, FilterBar } from '@/shared/ui'
import type { LearningPriority } from '@/shared/types/learning'
import type { WatchLaterEntry, WatchLaterFilterState, WatchLaterIntent } from '@/shared/types/watch-later'

const DEFAULT_FILTERS: WatchLaterFilterState = {
  search: '',
  intent: 'all',
  sortId: 'plan_order',
}

const INTENT_OPTIONS: { id: string; label: string }[] = [
  { id: 'all', label: '의도 전체' },
  { id: 'learning', label: '학습용만' },
  { id: 'casual', label: '비학습용만' },
]

function collectionName(id: string) {
  return VIDEO_COLLECTIONS.find((c) => c.id === id)?.name ?? '미분류'
}

/** 나중에 보기 — 학습 계획·오늘 큐·일괄 정리·컬렉션 이동 */
export function WatchLaterPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState<WatchLaterEntry[]>(() => [...WATCH_LATER_MOCK])
  const [filters, setFilters] = useState<WatchLaterFilterState>(DEFAULT_FILTERS)
  const [selected, setSelected] = useState<Set<string>>(() => new Set())
  const [bulkCollectionId, setBulkCollectionId] = useState(VIDEO_COLLECTIONS[0]?.id ?? 'col-inbox')

  const queueSorted = useMemo(() => getTodayQueueSorted(items), [items])
  const filtered = useMemo(() => filterAndSortWatchLater(items, filters), [items, filters])
  const selectionCount = selected.size
  const noResults = items.length > 0 && filtered.length === 0

  const toggleSelect = useCallback((id: string) => {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }, [])

  const moveQueue = useCallback((id: string, dir: -1 | 1) => {
    setItems((prev) => {
      const ids = getTodayQueueSorted(prev).map((e) => e.id)
      const newIds = reorderQueueIds(ids, id, dir)
      return normalizeWatchLaterQueue(applyTodayQueueOrder(prev, newIds))
    })
  }, [])

  const removeFromQueue = useCallback((id: string) => {
    setItems((prev) =>
      normalizeWatchLaterQueue(
        prev.map((e) => (e.id === id ? { ...e, inTodayQueue: false, todayQueueOrder: 99 } : e)),
      ),
    )
  }, [])

  const toggleTodayQueue = useCallback((id: string) => {
    setItems((prev) => {
      const target = prev.find((x) => x.id === id)
      if (!target) return prev
      if (target.inTodayQueue) {
        return normalizeWatchLaterQueue(
          prev.map((x) => (x.id === id ? { ...x, inTodayQueue: false, todayQueueOrder: 99 } : x)),
        )
      }
      const maxOrder = Math.max(-1, ...prev.filter((i) => i.inTodayQueue).map((i) => i.todayQueueOrder))
      return normalizeWatchLaterQueue(
        prev.map((x) => (x.id === id ? { ...x, inTodayQueue: true, todayQueueOrder: maxOrder + 1 } : x)),
      )
    })
  }, [])

  const updateOne = useCallback((id: string, patch: Partial<WatchLaterEntry>) => {
    setItems((prev) => prev.map((e) => (e.id === id ? { ...e, ...patch } : e)))
  }, [])

  const openLibrary = useCallback((libraryVideoId: string) => navigate(`/videos/${libraryVideoId}`), [navigate])

  const bulkAddToTodayQueue = useCallback(() => {
    setItems((prev) => {
      let nextOrder = Math.max(-1, ...prev.filter((i) => i.inTodayQueue).map((i) => i.todayQueueOrder)) + 1
      const next = prev.map((item) => {
        if (!selected.has(item.id) || item.inTodayQueue) return item
        const u = { ...item, inTodayQueue: true, todayQueueOrder: nextOrder }
        nextOrder += 1
        return u
      })
      return normalizeWatchLaterQueue(next)
    })
  }, [selected])

  const bulkRemoveFromTodayQueue = useCallback(() => {
    setItems((prev) =>
      normalizeWatchLaterQueue(
        prev.map((item) =>
          selected.has(item.id) ? { ...item, inTodayQueue: false, todayQueueOrder: 99 } : item,
        ),
      ),
    )
  }, [selected])

  const bulkSetIntent = useCallback(
    (intent: WatchLaterIntent) => {
      setItems((prev) => prev.map((item) => (selected.has(item.id) ? { ...item, intent } : item)))
    },
    [selected],
  )

  const bulkSetPriority = useCallback(
    (priority: LearningPriority) => {
      setItems((prev) => prev.map((item) => (selected.has(item.id) ? { ...item, priority } : item)))
    },
    [selected],
  )

  const bulkMoveCollection = useCallback(() => {
    setItems((prev) =>
      prev.map((item) => (selected.has(item.id) ? { ...item, collectionId: bulkCollectionId } : item)),
    )
  }, [selected, bulkCollectionId])

  const bulkRemoveFromList = useCallback(() => {
    setItems((prev) => prev.filter((item) => !selected.has(item.id)))
    setSelected(new Set())
  }, [selected])

  const clearSelection = useCallback(() => setSelected(new Set()), [])

  const resetFilters = useCallback(() => setFilters(DEFAULT_FILTERS), [])

  const listEmpty = items.length === 0

  return (
    <div className={`wl-page${selectionCount > 0 ? ' wl-page--has-bulk' : ''}`}>
      <PageHeader
        title="나중에 보기"
        description="보관함이 아니라 오늘·이후 학습 계획을 세우는 공간이에요. 전부 비울 필요 없이, 오늘 큐에만 옮겨도 충분합니다."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/videos')}>
            학습 자산
          </Button>
        }
      />

      <TodayQueueStrip
        items={queueSorted}
        onMoveUp={(id) => moveQueue(id, -1)}
        onMoveDown={(id) => moveQueue(id, 1)}
        onRemoveFromQueue={removeFromQueue}
        onOpenVideo={openLibrary}
      />

      <WatchLaterToolbar
        search={filters.search}
        onSearchChange={(search) => setFilters((f) => ({ ...f, search }))}
        sortId={filters.sortId}
        onSortChange={(sortId) => setFilters((f) => ({ ...f, sortId }))}
      />

      <div>
        <p className="wl-filter-label">시청 의도</p>
        <FilterBar
          filters={INTENT_OPTIONS}
          activeId={filters.intent}
          onChange={(intent) => setFilters((f) => ({ ...f, intent: intent as WatchLaterFilterState['intent'] }))}
        />
      </div>

      <p className="wl-result-count" role="status">
        {listEmpty
          ? '나중에 보기가 비어 있어요.'
          : noResults
            ? '조건에 맞는 영상이 없습니다.'
            : `표시 중 ${filtered.length}개 / 전체 ${items.length}개 · 오늘 큐 ${queueSorted.length}개`}
      </p>

      <section id="watch-later-list" aria-label="대기 목록">
        {listEmpty ? (
          <EmptyState
            title="담아 둔 영상이 없어요"
            description="구독·검색에서 담으면 여기에 쌓입니다. 그때 학습용인지 비학습용인지만 구분해 두면 계획 세우기 쉬워요."
            action={
              <Button variant="primary" onClick={() => navigate('/subscriptions')}>
                구독 채널 보기
              </Button>
            }
          />
        ) : noResults ? (
          <EmptyState
            title="조건에 맞는 영상이 없습니다"
            description="검색어나 의도 필터를 바꿔 보세요."
            action={
              <Button variant="secondary" onClick={resetFilters}>
                필터 초기화
              </Button>
            }
          />
        ) : (
          <div className="wl-list">
            {filtered.map((entry) => (
              <WatchLaterListRow
                key={entry.id}
                entry={entry}
                collectionName={collectionName(entry.collectionId)}
                collections={VIDEO_COLLECTIONS}
                stale={isWatchLaterStale(entry.addedAt)}
                selected={selected.has(entry.id)}
                onToggleSelect={toggleSelect}
                onToggleTodayQueue={toggleTodayQueue}
                onPriorityChange={(id, p) => updateOne(id, { priority: p })}
                onIntentChange={(id, intent) => updateOne(id, { intent })}
                onCollectionChange={(id, collectionId) => updateOne(id, { collectionId })}
                onOpenLibrary={openLibrary}
              />
            ))}
          </div>
        )}
      </section>

      <BulkActionBar
        count={selectionCount}
        collections={VIDEO_COLLECTIONS}
        bulkCollectionId={bulkCollectionId}
        onBulkCollectionIdChange={setBulkCollectionId}
        onAddToTodayQueue={bulkAddToTodayQueue}
        onRemoveFromTodayQueue={bulkRemoveFromTodayQueue}
        onSetIntent={bulkSetIntent}
        onSetPriority={bulkSetPriority}
        onMoveToCollection={bulkMoveCollection}
        onRemoveFromList={bulkRemoveFromList}
        onClearSelection={clearSelection}
      />
    </div>
  )
}
