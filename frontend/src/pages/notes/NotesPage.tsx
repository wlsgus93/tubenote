import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  filterAndSortNoteArchive,
  NoteArchiveCard,
  NoteArchiveListRow,
  NoteArchiveToolbar,
} from '@/features/note-archive-management'
import '@/features/note-archive-management/note-archive.css'
import { NOTE_ARCHIVE_MOCK, NOTE_ARCHIVE_TAGS } from '@/mocks/noteArchive'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, EmptyState, FilterBar } from '@/shared/ui'
import type { NoteArchiveFilterState } from '@/shared/types/note-archive'

const DEFAULT_FILTERS: NoteArchiveFilterState = {
  search: '',
  kind: 'all',
  tagId: 'all',
  reviewOnly: false,
  sortId: 'recent_desc',
}

const KIND_OPTIONS = [
  { id: 'all', label: '전체' },
  { id: 'memo', label: '메모만' },
  { id: 'highlight', label: '하이라이트만' },
] as const

const REVIEW_OPTIONS = [
  { id: 'all', label: '복습 필터 없음' },
  { id: 'review', label: '복습 필요만' },
]

const TAG_FILTER_OPTIONS = [{ id: 'all', label: '태그 전체' }, ...NOTE_ARCHIVE_TAGS.map((t) => ({ id: t, label: t }))]

/** 메모·하이라이트 아카이브 — 학습 흔적 단위로 모아보기 */
export function NotesPage() {
  const navigate = useNavigate()
  const [filters, setFilters] = useState<NoteArchiveFilterState>(DEFAULT_FILTERS)
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid')

  const filtered = useMemo(() => filterAndSortNoteArchive(NOTE_ARCHIVE_MOCK, filters), [filters])
  const reviewFilterId = filters.reviewOnly ? 'review' : 'all'
  const noResults = NOTE_ARCHIVE_MOCK.length > 0 && filtered.length === 0

  const resetFilters = () => setFilters(DEFAULT_FILTERS)

  return (
    <div className="na-page">
      <PageHeader
        title="메모 · 하이라이트"
        description="영상마다 남긴 기록을 한곳에 모았습니다. 복습이 필요한 흔적만 골라 보거나, 태그로 주제를 이어 가세요."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/videos')}>
            학습 자산
          </Button>
        }
      />

      <NoteArchiveToolbar
        search={filters.search}
        onSearchChange={(search) => setFilters((f) => ({ ...f, search }))}
        sortId={filters.sortId}
        onSortChange={(sortId) => setFilters((f) => ({ ...f, sortId }))}
        viewMode={viewMode}
        onViewModeChange={setViewMode}
      />

      <div>
        <p className="na-filter-label">기록 종류</p>
        <FilterBar
          filters={[...KIND_OPTIONS]}
          activeId={filters.kind}
          onChange={(kind) => setFilters((f) => ({ ...f, kind: kind as NoteArchiveFilterState['kind'] }))}
        />
      </div>

      <div>
        <p className="na-filter-label">복습</p>
        <FilterBar
          filters={[...REVIEW_OPTIONS]}
          activeId={reviewFilterId}
          onChange={(id) => setFilters((f) => ({ ...f, reviewOnly: id === 'review' }))}
        />
      </div>

      <div>
        <p className="na-filter-label">태그별 보기</p>
        <FilterBar
          filters={TAG_FILTER_OPTIONS}
          activeId={filters.tagId}
          onChange={(tagId) => setFilters((f) => ({ ...f, tagId }))}
        />
      </div>

      <p className="na-result-count" role="status">
        {noResults
          ? '조건에 맞는 학습 흔적이 없습니다.'
          : `표시 중 ${filtered.length}개 / 전체 ${NOTE_ARCHIVE_MOCK.length}개`}
      </p>

      {NOTE_ARCHIVE_MOCK.length === 0 ? (
        <EmptyState
          title="아직 기록이 없어요"
          description="영상 상세에서 메모와 하이라이트를 남기면 여기에 쌓입니다."
          action={
            <Button variant="primary" onClick={() => navigate('/videos')}>
              학습 자산으로 이동
            </Button>
          }
        />
      ) : noResults ? (
        <EmptyState
          title="조건에 맞는 기록이 없습니다"
          description="태그·복습·종류 필터를 바꿔 보세요."
          action={
            <Button variant="secondary" onClick={resetFilters}>
              필터 초기화
            </Button>
          }
        />
      ) : viewMode === 'grid' ? (
        <div className="na-grid">
          {filtered.map((e) => (
            <NoteArchiveCard key={e.id} entry={e} />
          ))}
        </div>
      ) : (
        <div className="na-list">
          {filtered.map((e) => (
            <NoteArchiveListRow key={e.id} entry={e} />
          ))}
        </div>
      )}
    </div>
  )
}
