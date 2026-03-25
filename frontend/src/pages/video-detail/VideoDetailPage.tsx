import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import {
  HighlightSection,
  MemoTimelinePanel,
  RelatedInCollection,
  ReviewPointsSection,
  ScriptPanel,
  VideoDetailMetaBar,
  VideoInfoHeader,
  VideoPlayerPanel,
  VideoScratchpadPanel,
} from '@/features/video-detail'
import '@/features/video-detail/video-detail.css'
import { patchVideoLearningState, patchVideoProgress } from '@/shared/api'
import { VIDEO_COLLECTIONS } from '@/shared/constants/videoCollections'
import { useVideoDetail } from '@/shared/hooks/useVideoDetail'
import { Button, EmptyState, TabMenu } from '@/shared/ui'
import type { LearningPriority, LearningStatus } from '@/shared/types/learning'

const RIGHT_TABS = [
  { id: 'script', label: '스크립트' },
  { id: 'memo', label: '메모' },
] as const

/** 라우트 `:videoId` = 백엔드 `userVideoId` */
export function VideoDetailPage() {
  const { videoId: userVideoId } = useParams<{ videoId: string }>()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const { detail, setDetail, status, error, reload } = useVideoDetail(userVideoId)

  const [currentTimeSec, setCurrentTimeSec] = useState(0)
  const [rightTab, setRightTab] = useState<string>('script')
  const [learningStatus, setLearningStatus] = useState<LearningStatus>('not_started')
  const [priority, setPriority] = useState<LearningPriority | undefined>(undefined)
  const [collectionId, setCollectionId] = useState('')
  const [isStarred, setIsStarred] = useState(false)

  const progressDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    return () => {
      if (progressDebounceRef.current) clearTimeout(progressDebounceRef.current)
    }
  }, [])

  useEffect(() => {
    if (!detail) return
    setLearningStatus(detail.learningStatus)
    setPriority(detail.priority)
    setCollectionId(detail.collectionId)
    setIsStarred(detail.isStarred)
    setCurrentTimeSec(Math.floor((detail.progressPercent / 100) * detail.durationSec))
  }, [detail])

  useEffect(() => {
    if (!detail) return
    const t = searchParams.get('t')
    if (t == null || t === '') return
    const sec = Number.parseFloat(t)
    if (!Number.isFinite(sec)) return
    const clamped = Math.max(0, Math.min(detail.durationSec, sec))
    setCurrentTimeSec(clamped)
    setRightTab('memo')
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev)
        next.delete('t')
        return next
      },
      { replace: true },
    )
  }, [detail, searchParams, setSearchParams])

  const flushProgress = useCallback(
    (sec: number) => {
      if (!userVideoId || !detail) return
      const dur = Math.max(1, detail.durationSec)
      const pct = Math.min(100, Math.round((sec / dur) * 100))
      patchVideoProgress(userVideoId, pct)
        .then(() => setDetail((d) => (d ? { ...d, progressPercent: pct } : null)))
        .catch(() => {})
    },
    [userVideoId, detail, setDetail],
  )

  const seekTo = useCallback(
    (sec: number) => {
      if (!detail) return
      const t = Math.max(0, Math.min(detail.durationSec, sec))
      setCurrentTimeSec(t)
      if (progressDebounceRef.current) clearTimeout(progressDebounceRef.current)
      progressDebounceRef.current = setTimeout(() => flushProgress(t), 900)
    },
    [detail, flushProgress],
  )

  const handleStatusChange = useCallback(
    async (next: LearningStatus) => {
      if (!userVideoId) return
      const prev = learningStatus
      setLearningStatus(next)
      try {
        await patchVideoLearningState(userVideoId, next)
        setDetail((d) => (d ? { ...d, learningStatus: next } : null))
      } catch {
        setLearningStatus(prev)
      }
    },
    [userVideoId, learningStatus, setDetail],
  )

  if (!userVideoId) {
    return (
      <div className="vd-page">
        <EmptyState
          title="잘못된 주소입니다"
          description="영상 ID가 없습니다."
          action={
            <Button variant="primary" onClick={() => navigate('/videos')}>
              학습 자산 목록
            </Button>
          }
        />
      </div>
    )
  }

  if (status === 'loading') {
    return (
      <div className="vd-page">
        <p className="vd-page__status" role="status">
          영상 정보를 불러오는 중…
        </p>
      </div>
    )
  }

  if (status === 'error' && error) {
    const isUnauthorized = error.status === 401
    return (
      <div className="vd-page">
        <EmptyState
          title={isUnauthorized ? '로그인이 필요합니다' : '영상을 불러올 수 없습니다'}
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

  if (status === 'notFound' || !detail) {
    return (
      <div className="vd-page">
        <EmptyState
          title="이 학습 자산을 찾을 수 없습니다"
          description="라이브러리에 없는 영상이거나 주소가 잘못되었을 수 있습니다."
          action={
            <>
              <Button variant="primary" onClick={() => navigate('/videos')}>
                학습 자산 목록
              </Button>
              <Button variant="ghost" onClick={() => navigate('/dashboard')}>
                대시보드
              </Button>
            </>
          }
        />
      </div>
    )
  }

  return (
    <div className="vd-page">
      <div className="vd-main-grid">
        <div className="vd-left">
          <VideoInfoHeader detail={detail} />
          <VideoPlayerPanel
            title={detail.title}
            durationSec={detail.durationSec}
            currentTimeSec={currentTimeSec}
            onSeek={seekTo}
          />
          <VideoDetailMetaBar
            learningStatus={learningStatus}
            priority={priority}
            reviewNeeded={detail.reviewNeeded}
            isStarred={isStarred}
            collectionId={collectionId}
            collections={VIDEO_COLLECTIONS}
            onStatusChange={handleStatusChange}
            onPriorityChange={(p) => {
              setPriority(p)
              setDetail((d) => (d ? { ...d, priority: p } : null))
            }}
            onCollectionChange={(id) => {
              setCollectionId(id)
              setDetail((d) =>
                d
                  ? {
                      ...d,
                      collectionId: id,
                      collectionName: VIDEO_COLLECTIONS.find((c) => c.id === id)?.name ?? d.collectionName,
                    }
                  : null,
              )
            }}
            onStarToggle={() => {
              setIsStarred((v) => !v)
              setDetail((d) => (d ? { ...d, isStarred: !d.isStarred } : null))
            }}
          />
        </div>
        <aside className="vd-right" aria-label="스크립트와 메모">
          <TabMenu
            tabs={[...RIGHT_TABS]}
            activeId={rightTab}
            onChange={setRightTab}
            ariaLabel="스크립트 또는 메모"
          />
          <div className="vd-panel-body" role="tabpanel">
            {rightTab === 'script' ? (
              <ScriptPanel cues={detail.scriptCues} currentTimeSec={currentTimeSec} onSeekTo={seekTo} />
            ) : (
              <MemoTimelinePanel
                notes={detail.timelineNotes}
                currentTimeSec={currentTimeSec}
                onSeekTo={seekTo}
              />
            )}
          </div>
        </aside>
      </div>

      <section className="vd-section vd-scratchpad-section" aria-labelledby="vd-scratchpad-title">
        <h2 id="vd-scratchpad-title" className="vd-section__title">
          필기 노트
        </h2>
        <p className="vd-section__desc">
          타임라인 메모와 별개로, 이 영상 전체에 대한 자유 서술형 필기를 남길 수 있습니다. (로컬 저장 · 추후 서버 동기화 가능)
        </p>
        <VideoScratchpadPanel videoId={detail.id} />
      </section>

      <div className="vd-bottom">
        <section className="vd-section" aria-labelledby="vd-hl-title">
          <h2 id="vd-hl-title" className="vd-section__title">
            하이라이트
          </h2>
          <p className="vd-section__desc">인용한 문장을 다시 찾을 때 시점으로 바로 이동합니다.</p>
          <HighlightSection highlights={detail.highlights} onSeekTo={seekTo} />
        </section>

        <section className="vd-section" aria-labelledby="vd-rel-title">
          <h2 id="vd-rel-title" className="vd-section__title">
            같은 컬렉션의 영상
          </h2>
          <p className="vd-section__desc">학습 흐름을 이어갈 다음 자산을 고릅니다.</p>
          <RelatedInCollection items={detail.relatedInCollection} collectionName={detail.collectionName} />
        </section>

        <section className="vd-section" aria-labelledby="vd-rev-title">
          <h2 id="vd-rev-title" className="vd-section__title">
            복습 포인트 · 학습 정리
          </h2>
          <p className="vd-section__desc">영상을 “본” 것에서 “익힌” 것으로 넘기기 위한 체크입니다.</p>
          <ReviewPointsSection points={detail.reviewPoints} />
        </section>
      </div>
    </div>
  )
}
