import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import {
  filterAndSortVideos,
  type VideoLibraryFilterState,
  VideoLibraryGridCard,
  VideoLibraryListRow,
  VideosToolbar,
  VideoTagFilterBar,
} from '@/features/video-management'
import '@/features/video-management/video-library.css'
import { PageHeader } from '@/components/layout/PageHeader'
import { patchVideoLearningState } from '@/shared/api'
import { VIDEO_COLLECTIONS } from '@/shared/constants/videoCollections'
import { useVideoLibrary } from '@/shared/hooks/useVideoLibrary'
import { Button, EmptyState, FilterBar } from '@/shared/ui'
import type { LearningStatus } from '@/shared/types/learning'
import type { VideoLengthFilterId, VideoLibraryEntry, VideoLibrarySortId } from '@/shared/types/video-library'

const STATUS_FILTER_OPTIONS: { id: LearningStatus | 'all'; label: string }[] = [
  { id: 'all', label: '상태 전체' },
  { id: 'not_started', label: '미시청' },
  { id: 'in_progress', label: '진행' },
  { id: 'completed', label: '완료' },
  { id: 'on_hold', label: '보류' },
]

const LENGTH_FILTER_OPTIONS: { id: VideoLengthFilterId; label: string }[] = [
  { id: 'all', label: '길이 전체' },
  { id: 'short', label: '짧음 (~15분)' },
  { id: 'medium', label: '중간 (15~45분)' },
  { id: 'long', label: '김 (45분~)' },
]

const DEFAULT_FILTERS: VideoLibraryFilterState = {
  search: '',
  statusId: 'all',
  selectedTags: [],
  lengthId: 'all',
  sortId: 'updated_desc',
}

function collectionLabel(collections: typeof VIDEO_COLLECTIONS, id: string) {
  return collections.find((c) => c.id === id)?.name ?? '미분류'
}

/** 저장된 학습 영상 라이브러리 — GET /api/videos, PATCH learning-state */
export function VideosPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const { items, setItems, status, error, reload } = useVideoLibrary()
  const [filters, setFilters] = useState<VideoLibraryFilterState>(DEFAULT_FILTERS)
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid')

  useEffect(() => {
    const q = searchParams.get('q')
    if (!q) return
    setFilters((f) => ({ ...f, search: q }))
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev)
        next.delete('q')
        return next
      },
      { replace: true },
    )
  }, [searchParams, setSearchParams])

  const tagOptions = useMemo(() => {
    const set = new Set<string>()
    items.forEach((v) => v.tags.forEach((t) => set.add(t)))
    return [...set].sort((a, b) => a.localeCompare(b, 'ko'))
  }, [items])

  const filtered = useMemo(() => filterAndSortVideos(items, filters), [items, filters])

  const updateItem = useCallback((id: string, patch: Partial<VideoLibraryEntry>) => {
    setItems((prev) => prev.map((v) => (v.id === id ? { ...v, ...patch, updatedAt: new Date().toISOString() } : v)))
  }, [setItems])

  const handleStatusChange = useCallback(
    async (id: string, nextStatus: LearningStatus) => {
      const prev = items.find((v) => v.id === id)?.learningStatus
      updateItem(id, { learningStatus: nextStatus })
      try {
        await patchVideoLearningState(id, nextStatus)
      } catch {
        if (prev) updateItem(id, { learningStatus: prev })
      }
    },
    [items, updateItem],
  )

  const handleStarToggle = useCallback(
    (id: string) => {
      setItems((prev) =>
        prev.map((v) =>
          v.id === id ? { ...v, isStarred: !v.isStarred, updatedAt: new Date().toISOString() } : v,
        ),
      )
    },
    [setItems],
  )

  const handleCollectionChange = useCallback(
    (id: string, collectionId: string) => {
      updateItem(id, { collectionId })
    },
    [updateItem],
  )

  const toggleTag = useCallback((tag: string) => {
    setFilters((f) => {
      const has = f.selectedTags.includes(tag)
      const selectedTags = has ? f.selectedTags.filter((t) => t !== tag) : [...f.selectedTags, tag]
      return { ...f, selectedTags }
    })
  }, [])

  const clearTags = useCallback(() => {
    setFilters((f) => ({ ...f, selectedTags: [] }))
  }, [])

  const resetAllFilters = useCallback(() => {
    setFilters(DEFAULT_FILTERS)
  }, [])

  const openVideo = useCallback((id: string) => navigate(`/videos/${id}`), [navigate])

  const libraryEmpty = status === 'success' && items.length === 0
  const noResults = status === 'success' && items.length > 0 && filtered.length === 0

  if (status === 'loading') {
    return (
      <div className="vlib-page">
        <PageHeader
          title="학습 자산"
          description="저장해 둔 영상을 상태·태그·길이로 나누어 관리합니다. 유튜브 구독 목록이 아니라 내 학습 큐입니다."
        />
        <p className="vlib-page__status" role="status">
          영상 목록을 불러오는 중…
        </p>
      </div>
    )
  }

  if (status === 'error' && error) {
    const isUnauthorized = error.status === 401
    return (
      <div className="vlib-page">
        <PageHeader title="학습 자산" description="목록을 불러오지 못했습니다." />
        <EmptyState
          title={isUnauthorized ? '로그인이 필요합니다' : '목록을 불러올 수 없습니다'}
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
    <div className="vlib-page">
      <PageHeader
        title="학습 자산"
        description="저장해 둔 영상을 상태·태그·길이로 나누어 관리합니다. 유튜브 구독 목록이 아니라 내 학습 큐입니다."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/watch-later')}>
            나중에 보기
          </Button>
        }
      />

      <VideosToolbar
        search={filters.search}
        onSearchChange={(search) => setFilters((f) => ({ ...f, search }))}
        sortId={filters.sortId}
        onSortChange={(sortId: VideoLibrarySortId) => setFilters((f) => ({ ...f, sortId }))}
        viewMode={viewMode}
        onViewModeChange={setViewMode}
      />

      <div className="vlib-filter-block">
        <p className="vlib-filter-block__label">학습 상태</p>
        <FilterBar
          filters={STATUS_FILTER_OPTIONS}
          activeId={filters.statusId}
          onChange={(statusId) =>
            setFilters((f) => ({ ...f, statusId: statusId as LearningStatus | 'all' }))
          }
        />
      </div>

      <VideoTagFilterBar
        tagOptions={tagOptions}
        selectedTags={filters.selectedTags}
        onToggleTag={toggleTag}
        onClearTags={clearTags}
      />

      <div className="vlib-filter-block">
        <p className="vlib-filter-block__label">길이</p>
        <FilterBar
          filters={LENGTH_FILTER_OPTIONS}
          activeId={filters.lengthId}
          onChange={(lengthId) => setFilters((f) => ({ ...f, lengthId: lengthId as VideoLengthFilterId }))}
        />
      </div>

      <p className="vlib-result-count" role="status">
        {libraryEmpty
          ? '저장된 학습 영상이 없습니다.'
          : `표시 중 ${filtered.length}개 / 전체 ${items.length}개`}
      </p>

      {libraryEmpty ? (
        <EmptyState
          title="아직 저장된 학습 영상이 없어요"
          description="나중에 보기나 구독 채널에서 영상을 담으면 이 목록에 쌓입니다."
          action={
            <Button variant="primary" onClick={() => navigate('/watch-later')}>
              나중에 보기로 이동
            </Button>
          }
        />
      ) : noResults ? (
        <EmptyState
          title="조건에 맞는 학습 자산이 없습니다"
          description="검색어나 필터를 바꾸면 다시 나타날 수 있어요."
          action={
            <Button variant="secondary" onClick={resetAllFilters}>
              필터·검색 초기화
            </Button>
          }
        />
      ) : viewMode === 'grid' ? (
        <div className="vlib-grid">
          {filtered.map((video) => (
            <VideoLibraryGridCard
              key={video.id}
              video={video}
              collections={VIDEO_COLLECTIONS}
              collectionName={collectionLabel(VIDEO_COLLECTIONS, video.collectionId)}
              onOpen={openVideo}
              onStatusChange={handleStatusChange}
              onStarToggle={handleStarToggle}
              onCollectionChange={handleCollectionChange}
            />
          ))}
        </div>
      ) : (
        <div className="vlib-list">
          {filtered.map((video) => (
            <VideoLibraryListRow
              key={video.id}
              video={video}
              collections={VIDEO_COLLECTIONS}
              collectionName={collectionLabel(VIDEO_COLLECTIONS, video.collectionId)}
              onOpen={openVideo}
              onStatusChange={handleStatusChange}
              onStarToggle={handleStarToggle}
              onCollectionChange={handleCollectionChange}
            />
          ))}
        </div>
      )}
    </div>
  )
}
