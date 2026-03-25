import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ChannelDetailPanel,
  ChannelsToolbar,
  filterAndSortChannels,
  SubscriptionBulkActionBar,
  SubscriptionBulkListHeader,
  SubscriptionBulkListRow,
  SubscriptionChannelCard,
} from '@/features/subscription-management'
import '@/features/subscription-management/channel-library.css'
import { CHANNEL_CATEGORIES, CHANNEL_SUBSCRIPTIONS_MOCK } from '@/mocks/channels'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, EmptyState, FilterBar } from '@/shared/ui'
import type { ChannelFocus, ChannelListFilterState, ChannelSubscription } from '@/shared/types/channel-library'

const DEFAULT_FILTERS: ChannelListFilterState = {
  search: '',
  categoryId: 'all',
  focus: 'all',
  favoritesOnly: false,
  sortId: 'upload_desc',
}

const FOCUS_OPTIONS: { id: string; label: string }[] = [
  { id: 'all', label: '유형 전체' },
  { id: 'learning', label: '학습용만' },
  { id: 'general', label: '일반만' },
]

const FAVORITE_OPTIONS: { id: string; label: string }[] = [
  { id: 'all', label: '전체 채널' },
  { id: 'favorites', label: '즐겨찾기만' },
]

const VIEW_OPTIONS = [
  { id: 'cards', label: '카드·상세' },
  { id: 'list', label: '리스트·일괄' },
] as const

function categoryLabel(categories: typeof CHANNEL_CATEGORIES, id: string) {
  return categories.find((c) => c.id === id)?.name ?? '미분류'
}

/** 구독 채널 — 카드 정리 뷰 + 대량 정리용 리스트 뷰 */
export function SubscriptionsPage() {
  const navigate = useNavigate()
  const [channels, setChannels] = useState<ChannelSubscription[]>(() => [...CHANNEL_SUBSCRIPTIONS_MOCK])
  const [filters, setFilters] = useState<ChannelListFilterState>(DEFAULT_FILTERS)
  const [viewMode, setViewMode] = useState<(typeof VIEW_OPTIONS)[number]['id']>('cards')
  const [selectedId, setSelectedId] = useState<string | null>(() => CHANNEL_SUBSCRIPTIONS_MOCK[0]?.id ?? null)
  const [bulkSelected, setBulkSelected] = useState<Set<string>>(() => new Set())

  const categoryOptions = useMemo(
    () => [{ id: 'all', label: '카테고리 전체' }, ...CHANNEL_CATEGORIES.map((c) => ({ id: c.id, label: c.name }))],
    [],
  )

  const filtered = useMemo(() => filterAndSortChannels(channels, filters), [channels, filters])

  const favoriteFilterId = filters.favoritesOnly ? 'favorites' : 'all'

  const selected = useMemo(
    () => (selectedId ? channels.find((c) => c.id === selectedId) ?? null : null),
    [channels, selectedId],
  )

  const bulkCount = bulkSelected.size

  useEffect(() => {
    if (filtered.length === 0) return
    if (!selectedId || !filtered.some((c) => c.id === selectedId)) {
      setSelectedId(filtered[0]!.id)
    }
  }, [filtered, selectedId])

  useEffect(() => {
    setBulkSelected(new Set())
  }, [viewMode])

  useEffect(() => {
    setBulkSelected((prev) => {
      const next = new Set<string>()
      for (const id of prev) {
        if (channels.some((c) => c.id === id)) next.add(id)
      }
      return next
    })
  }, [channels])

  const updateChannel = useCallback((id: string, patch: Partial<ChannelSubscription>) => {
    setChannels((prev) => prev.map((c) => (c.id === id ? { ...c, ...patch } : c)))
  }, [])

  const handleToggleFavorite = useCallback(
    (id: string) => {
      setChannels((prev) =>
        prev.map((c) => (c.id === id ? { ...c, isFavorite: !c.isFavorite } : c)),
      )
    },
    [],
  )

  const handleMemoChange = useCallback(
    (id: string, memo: string) => {
      updateChannel(id, { memo })
    },
    [updateChannel],
  )

  const handleFocusChange = useCallback(
    (id: string, focus: ChannelFocus) => {
      updateChannel(id, { focus })
    },
    [updateChannel],
  )

  const handleCategoryChange = useCallback(
    (id: string, categoryId: string) => {
      updateChannel(id, { categoryId })
    },
    [updateChannel],
  )

  const resetFilters = useCallback(() => {
    setFilters(DEFAULT_FILTERS)
  }, [])

  const reloadMockChannels = useCallback(() => {
    setChannels([...CHANNEL_SUBSCRIPTIONS_MOCK])
    setBulkSelected(new Set())
    setSelectedId(CHANNEL_SUBSCRIPTIONS_MOCK[0]?.id ?? null)
  }, [])

  const unsubscribeOne = useCallback((id: string) => {
    setChannels((prev) => prev.filter((c) => c.id !== id))
    setBulkSelected((prev) => {
      const next = new Set(prev)
      next.delete(id)
      return next
    })
  }, [])

  const toggleBulkSelect = useCallback((id: string) => {
    setBulkSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }, [])

  const selectAllFiltered = useCallback(() => {
    setBulkSelected(new Set(filtered.map((c) => c.id)))
  }, [filtered])

  const clearBulkSelection = useCallback(() => setBulkSelected(new Set()), [])

  const bulkUnsubscribe = useCallback(() => {
    setChannels((prev) => prev.filter((c) => !bulkSelected.has(c.id)))
    setBulkSelected(new Set())
  }, [bulkSelected])

  const noResults = channels.length > 0 && filtered.length === 0
  const listEmpty = channels.length === 0

  const pageClass =
    viewMode === 'list'
      ? `chlib-page chlib-page--bulk${bulkCount > 0 ? ' chlib-page--bulk-active' : ''}`
      : 'chlib-page'

  return (
    <div className={pageClass}>
      <PageHeader
        title="구독 채널"
        description={
          viewMode === 'list'
            ? '필터로 좁힌 뒤 체크하고 일괄 구독 취소할 수 있어요. 수백 개일 때 정리용으로 쓰기 좋습니다(mock).'
            : '채널을 학습 자산처럼 분류하고, 왜 구독했는지 메모해 두면 흐름이 끊기지 않습니다. 신규 업로드와 저장 영상 수를 한눈에 봅니다.'
        }
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/videos')}>
            학습 자산
          </Button>
        }
      />

      <div className="chlib-view-toggle">
        <p className="chlib-view-toggle__label">보기</p>
        <FilterBar
          filters={[...VIEW_OPTIONS]}
          activeId={viewMode}
          onChange={(id) => setViewMode(id as (typeof VIEW_OPTIONS)[number]['id'])}
        />
      </div>

      <ChannelsToolbar
        search={filters.search}
        onSearchChange={(search) => setFilters((f) => ({ ...f, search }))}
        sortId={filters.sortId}
        onSortChange={(sortId) => setFilters((f) => ({ ...f, sortId }))}
      />

      <div className="chlib-filter-block">
        <p className="chlib-filter-block__label">카테고리</p>
        <FilterBar
          filters={categoryOptions}
          activeId={filters.categoryId}
          onChange={(categoryId) => setFilters((f) => ({ ...f, categoryId }))}
        />
      </div>

      <div className="chlib-filter-block">
        <p className="chlib-filter-block__label">채널 유형</p>
        <FilterBar
          filters={FOCUS_OPTIONS}
          activeId={filters.focus}
          onChange={(focus) =>
            setFilters((f) => ({ ...f, focus: focus as ChannelListFilterState['focus'] }))
          }
        />
      </div>

      <div className="chlib-filter-block">
        <p className="chlib-filter-block__label">즐겨찾기</p>
        <FilterBar
          filters={FAVORITE_OPTIONS}
          activeId={favoriteFilterId}
          onChange={(id) => setFilters((f) => ({ ...f, favoritesOnly: id === 'favorites' }))}
        />
      </div>

      <p className="chlib-result-count" role="status">
        {listEmpty
          ? '구독 채널이 없습니다.'
          : noResults
            ? '조건에 맞는 채널이 없습니다.'
            : `표시 중 ${filtered.length}개 / 전체 ${channels.length}개`}
      </p>

      {listEmpty ? (
        <EmptyState
          title="구독 목록이 비어 있어요"
          description="구독 취소로 모두 제거했거나 mock을 비운 상태입니다. 다시 불러오거나 필터를 초기화해 보세요."
          action={
            <Button variant="primary" onClick={reloadMockChannels}>
              mock 채널 다시 불러오기
            </Button>
          }
        />
      ) : viewMode === 'list' ? (
        <>
          {noResults ? (
            <EmptyState
              title="조건에 맞는 채널이 없습니다"
              description="카테고리·유형·즐겨찾기 또는 검색어를 바꿔 보세요."
              action={
                <Button variant="secondary" onClick={resetFilters}>
                  필터 초기화
                </Button>
              }
            />
          ) : (
            <div className="chlib-bulk-wrap">
              <div className="chlib-bulk-scroll">
                <SubscriptionBulkListHeader
                  filtered={filtered}
                  selectedIds={bulkSelected}
                  onSelectAllFiltered={selectAllFiltered}
                  onClearSelection={clearBulkSelection}
                />
                {filtered.map((ch) => (
                  <SubscriptionBulkListRow
                    key={ch.id}
                    channel={ch}
                    categoryName={categoryLabel(CHANNEL_CATEGORIES, ch.categoryId)}
                    selected={bulkSelected.has(ch.id)}
                    onToggleSelect={toggleBulkSelect}
                    onUnsubscribe={unsubscribeOne}
                  />
                ))}
              </div>
            </div>
          )}
          <SubscriptionBulkActionBar
            selectedCount={bulkCount}
            onBulkUnsubscribe={bulkUnsubscribe}
            onClearSelection={clearBulkSelection}
          />
        </>
      ) : (
        <div className="chlib-layout">
          <div>
            {noResults ? (
              <EmptyState
                title="조건에 맞는 채널이 없습니다"
                description="카테고리·유형·즐겨찾기 또는 검색어를 바꿔 보세요."
                action={
                  <Button variant="secondary" onClick={resetFilters}>
                    필터 초기화
                  </Button>
                }
              />
            ) : (
              <div className="chlib-grid">
                {filtered.map((ch) => (
                  <SubscriptionChannelCard
                    key={ch.id}
                    channel={ch}
                    categoryName={categoryLabel(CHANNEL_CATEGORIES, ch.categoryId)}
                    selected={ch.id === selectedId}
                    onSelect={setSelectedId}
                    onToggleFavorite={handleToggleFavorite}
                  />
                ))}
              </div>
            )}
          </div>

          <ChannelDetailPanel
            channel={noResults ? null : selected}
            categories={CHANNEL_CATEGORIES}
            categoryName={(id) => categoryLabel(CHANNEL_CATEGORIES, id)}
            onMemoChange={handleMemoChange}
            onFocusChange={handleFocusChange}
            onCategoryChange={handleCategoryChange}
            onToggleFavorite={handleToggleFavorite}
          />
        </div>
      )}
    </div>
  )
}
