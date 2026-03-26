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
import { CHANNEL_CATEGORIES } from '@/mocks/channels'
import { PageHeader } from '@/components/layout/PageHeader'
import { importVideoByUrl } from '@/shared/api'
import { ApiError } from '@/shared/api/errors'
import {
  mapSubscriptionResponseToChannel,
  patchSubscription,
  postSubscriptionChannelUpdatesSync,
  postSubscriptionsYoutubeSync,
} from '@/shared/api/subscriptions'
import { useSubscriptionRecentFeed } from '@/shared/hooks/useSubscriptionRecentFeed'
import { useSubscriptions } from '@/shared/hooks/useSubscriptions'
import type { SubscriptionResponseDto } from '@/shared/types/subscriptions-api'
import { Button, EmptyState, FilterBar } from '@/shared/ui'
import { youtubeWatchUrlFromVideoId } from '@/shared/utils/youtubeUrl'
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
  const found = categories.find((c) => c.id === id)
  if (found) return found.name
  return id.trim() ? id : '미분류'
}

/** 구독 채널 — GET/PATCH `/api/v1/subscriptions`, 피드는 `…/recent-videos` */
export function SubscriptionsPage() {
  const navigate = useNavigate()
  const { channels, setChannels, status, error, reload } = useSubscriptions(CHANNEL_CATEGORIES)
  const [filters, setFilters] = useState<ChannelListFilterState>(DEFAULT_FILTERS)
  const [viewMode, setViewMode] = useState<(typeof VIEW_OPTIONS)[number]['id']>('cards')
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [bulkSelected, setBulkSelected] = useState<Set<string>>(() => new Set())
  const [feedBusyYoutubeId, setFeedBusyYoutubeId] = useState<string | null>(null)
  const [feedImportError, setFeedImportError] = useState<string | null>(null)
  const [feedImportedIds, setFeedImportedIds] = useState<Set<string>>(() => new Set())
  const [syncBusy, setSyncBusy] = useState(false)
  const [actionMessage, setActionMessage] = useState<string | null>(null)

  const {
    items: recentFeed,
    status: feedStatus,
    error: feedLoadErr,
  } = useSubscriptionRecentFeed(selectedId)

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
    if (status !== 'success') return
    if (channels.length === 0) {
      setSelectedId(null)
      return
    }
    if (!selectedId || !channels.some((c) => c.id === selectedId)) {
      setSelectedId(channels[0]!.id)
    }
  }, [status, channels, selectedId])

  useEffect(() => {
    setBulkSelected(new Set())
  }, [viewMode])

  useEffect(() => {
    setFeedImportError(null)
  }, [selectedId])

  useEffect(() => {
    setBulkSelected((prev) => {
      const next = new Set<string>()
      for (const id of prev) {
        if (channels.some((c) => c.id === id)) next.add(id)
      }
      return next
    })
  }, [channels])

  const mergeFromDto = useCallback(
    (dto: SubscriptionResponseDto) => {
      const row = mapSubscriptionResponseToChannel(dto, CHANNEL_CATEGORIES)
      setChannels((prev) => prev.map((c) => (c.id === String(dto.subscriptionId) ? row : c)))
    },
    [setChannels],
  )

  /** PATCH 본문이 비어 있거나 id가 없으면 목록을 다시 받아 상태를 맞춤 */
  const finishPatch = useCallback(
    (dto: SubscriptionResponseDto | undefined | null) => {
      if (dto != null && dto.subscriptionId != null) {
        mergeFromDto(dto)
      } else {
        void reload()
      }
    },
    [mergeFromDto, reload],
  )

  const patchFailureMessage = useCallback((e: unknown, fallback: string) => {
    if (e instanceof ApiError) return e.message
    if (e instanceof Error && e.message) return e.message
    return fallback
  }, [])

  const updateChannel = useCallback((id: string, patch: Partial<ChannelSubscription>) => {
    setChannels((prev) => prev.map((c) => (c.id === id ? { ...c, ...patch } : c)))
  }, [setChannels])

  const handleToggleFavorite = useCallback(
    async (id: string) => {
      const ch = channels.find((c) => c.id === id)
      if (!ch) return
      const next = !ch.isFavorite
      updateChannel(id, { isFavorite: next })
      try {
        const dto = await patchSubscription(id, { isFavorite: next })
        finishPatch(dto)
        setActionMessage(null)
      } catch (e) {
        updateChannel(id, { isFavorite: ch.isFavorite })
        setActionMessage(patchFailureMessage(e, '즐겨찾기 저장에 실패했습니다.'))
      }
    },
    [channels, finishPatch, patchFailureMessage, updateChannel],
  )

  const handleMemoChange = useCallback(
    (id: string, memo: string) => {
      updateChannel(id, { memo })
    },
    [updateChannel],
  )

  const handleMemoBlur = useCallback(
    async (id: string, memo: string) => {
      const prevMemo = channels.find((c) => c.id === id)?.memo
      try {
        const dto = await patchSubscription(id, { note: memo })
        finishPatch(dto)
        setActionMessage(null)
      } catch (e) {
        if (prevMemo !== undefined) updateChannel(id, { memo: prevMemo })
        setActionMessage(patchFailureMessage(e, '메모 저장에 실패했습니다.'))
      }
    },
    [channels, finishPatch, patchFailureMessage, updateChannel],
  )

  const handleFocusChange = useCallback(
    async (id: string, focus: ChannelFocus) => {
      const ch = channels.find((c) => c.id === id)
      if (!ch) return
      const prev = ch.focus
      updateChannel(id, { focus })
      try {
        const dto = await patchSubscription(id, { isLearningChannel: focus === 'learning' })
        finishPatch(dto)
        setActionMessage(null)
      } catch (e) {
        updateChannel(id, { focus: prev })
        setActionMessage(patchFailureMessage(e, '채널 유형 저장에 실패했습니다.'))
      }
    },
    [channels, finishPatch, patchFailureMessage, updateChannel],
  )

  const handleCategoryChange = useCallback(
    async (id: string, categoryId: string) => {
      const ch = channels.find((c) => c.id === id)
      if (!ch) return
      const prev = ch.categoryId
      updateChannel(id, { categoryId })
      try {
        const dto = await patchSubscription(id, { category: categoryId })
        finishPatch(dto)
        setActionMessage(null)
      } catch (e) {
        updateChannel(id, { categoryId: prev })
        setActionMessage(patchFailureMessage(e, '카테고리 저장에 실패했습니다.'))
      }
    },
    [channels, finishPatch, patchFailureMessage, updateChannel],
  )

  const resetFilters = useCallback(() => {
    setFilters(DEFAULT_FILTERS)
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

  const handleYoutubeSync = useCallback(async () => {
    setActionMessage(null)
    setSyncBusy(true)
    try {
      await postSubscriptionsYoutubeSync()
      setActionMessage('유튜브 구독 목록을 반영했습니다.')
      reload()
    } catch (e) {
      const msg =
        e instanceof ApiError
          ? e.message
          : e instanceof Error
            ? e.message
            : '동기화에 실패했습니다.'
      setActionMessage(msg)
    } finally {
      setSyncBusy(false)
    }
  }, [reload])

  const handleChannelFeedSync = useCallback(async () => {
    setActionMessage(null)
    setSyncBusy(true)
    try {
      await postSubscriptionChannelUpdatesSync()
      setActionMessage('채널별 최근 업로드 피드를 갱신했습니다.')
      reload()
    } catch (e) {
      const msg =
        e instanceof ApiError
          ? e.message
          : e instanceof Error
            ? e.message
            : '피드 동기화에 실패했습니다.'
      setActionMessage(msg)
    } finally {
      setSyncBusy(false)
    }
  }, [reload])

  const handleAddFeedVideoToLibrary = useCallback(async (youtubeVideoId: string) => {
    setFeedBusyYoutubeId(youtubeVideoId)
    setFeedImportError(null)
    try {
      const url = youtubeWatchUrlFromVideoId(youtubeVideoId)
      await importVideoByUrl(url)
      setFeedImportedIds((prev) => new Set(prev).add(youtubeVideoId))
    } catch (e) {
      const msg =
        e instanceof ApiError
          ? e.status === 409
            ? '이미 학습 자산에 있는 영상입니다.'
            : e.message
          : e instanceof Error
            ? e.message
            : '추가에 실패했습니다.'
      setFeedImportError(msg)
    } finally {
      setFeedBusyYoutubeId(null)
    }
  }, [])

  const noResults = channels.length > 0 && filtered.length === 0
  const listEmpty = status === 'success' && channels.length === 0

  const pageClass =
    viewMode === 'list'
      ? `chlib-page chlib-page--bulk${bulkCount > 0 ? ' chlib-page--bulk-active' : ''}`
      : 'chlib-page'

  if (status === 'loading') {
    return (
      <div className="chlib-page">
        <PageHeader title="구독 채널" description="YouTube 구독 목록을 불러오는 중입니다." />
        <p className="chlib-result-count" role="status">
          구독 목록을 불러오는 중…
        </p>
      </div>
    )
  }

  if (status === 'error' && error) {
    const isUnauthorized = error.status === 401
    return (
      <div className="chlib-page">
        <PageHeader title="구독 채널" description="목록을 불러오지 못했습니다." />
        <EmptyState
          title={isUnauthorized ? '로그인이 필요합니다' : '구독 목록을 불러올 수 없습니다'}
          description={isUnauthorized ? '로그인한 뒤 다시 시도해 주세요.' : error.message}
          action={
            <Button variant="primary" onClick={() => (isUnauthorized ? navigate('/login') : reload())}>
              {isUnauthorized ? '로그인' : '다시 시도'}
            </Button>
          }
        />
      </div>
    )
  }

  return (
    <div className={pageClass}>
      <PageHeader
        title="구독 채널"
        description={
          viewMode === 'list'
            ? '필터로 좁힌 뒤 체크해 선택할 수 있습니다. 카테고리는 행의 선택 상자에서 바꿀 수 있어요. 유튜브 동기화만으로는 자동 분류되지 않습니다.'
            : '서버에 동기화된 YouTube 구독입니다. 카테고리는 직접 지정하면 상단 필터에 반영됩니다. 유형·메모도 저장됩니다. 최근 업로드는 피드 동기화 후에 채워집니다.'
        }
        actions={
          <div className="chlib-header-actions">
            <Button variant="ghost" size="sm" type="button" onClick={() => reload()} disabled={syncBusy}>
              새로고침
            </Button>
            <Button variant="secondary" size="sm" type="button" onClick={handleYoutubeSync} disabled={syncBusy}>
              {syncBusy ? '동기화 중…' : '유튜브 구독 동기화'}
            </Button>
            <Button variant="secondary" size="sm" type="button" onClick={handleChannelFeedSync} disabled={syncBusy}>
              채널 업로드 동기화
            </Button>
            <Button variant="ghost" size="sm" type="button" onClick={() => navigate('/videos')}>
              학습 자산
            </Button>
          </div>
        }
      />

      {actionMessage ? (
        <p className="chlib-action-message" role="status">
          {actionMessage}
        </p>
      ) : null}

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
          title="구독 채널이 아직 없어요"
          description="「유튜브 구독 동기화」로 계정의 구독 목록을 가져옵니다. 이후 「채널 업로드 동기화」로 최근 영상 피드를 채울 수 있어요."
          action={
            <div className="chlib-empty-actions">
              <Button variant="primary" type="button" onClick={handleYoutubeSync} disabled={syncBusy}>
                유튜브 구독 동기화
              </Button>
              <Button variant="secondary" type="button" onClick={() => navigate('/videos')}>
                학습 자산으로 이동
              </Button>
            </div>
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
                    categories={CHANNEL_CATEGORIES}
                    categoryNameById={(id) => categoryLabel(CHANNEL_CATEGORIES, id)}
                    onCategoryChange={handleCategoryChange}
                    selected={bulkSelected.has(ch.id)}
                    onToggleSelect={toggleBulkSelect}
                  />
                ))}
              </div>
            </div>
          )}
          <SubscriptionBulkActionBar selectedCount={bulkCount} onClearSelection={clearBulkSelection} />
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
            onMemoBlur={handleMemoBlur}
            onFocusChange={handleFocusChange}
            onCategoryChange={handleCategoryChange}
            onToggleFavorite={handleToggleFavorite}
            recentVideos={recentFeed}
            recentFeedLoading={feedStatus === 'loading'}
            recentFeedLoadError={feedStatus === 'error' ? feedLoadErr : null}
            feedSource="api"
            pendingImportYoutubeId={feedBusyYoutubeId}
            importedYoutubeIds={feedImportedIds}
            onAddFeedVideoToLibrary={handleAddFeedVideoToLibrary}
            feedImportError={feedImportError}
          />
        </div>
      )}
    </div>
  )
}
