import { useNavigate } from 'react-router-dom'
import { PageHeader } from '@/components/layout/PageHeader'
import {
  DashboardNextUp,
  DashboardQuickActions,
  DashboardSection,
} from '@/features/dashboard'
import '@/features/dashboard/dashboard.css'
import { useDashboard } from '@/shared/hooks/useDashboard'
import { Button, EmptyState, NoteCard, StatCard, VideoCard } from '@/shared/ui'

/** 학습 중심 허브 — GET /api/dashboard */
export function DashboardPage() {
  const navigate = useNavigate()
  const { state, reload } = useDashboard()

  const openVideo = (id: string) => navigate(`/videos/${id}`)
  const openNote = (id: string) => navigate(`/notes?highlight=${id}`)

  if (state.status === 'loading') {
    return (
      <div className="dash-page">
        <PageHeader
          title="대시보드"
          description="오늘의 큐와 이어보기, 메모·복습을 한 화면에서 이어갑니다. 가장 위가 지금 할 일입니다."
        />
        <p className="dash-page__loading" role="status">
          대시보드를 불러오는 중…
        </p>
      </div>
    )
  }

  if (state.status === 'error') {
    const isUnauthorized = state.error.status === 401
    return (
      <div className="dash-page">
        <PageHeader title="대시보드" description="데이터를 불러오지 못했습니다." />
        <EmptyState
          title={isUnauthorized ? '로그인이 필요합니다' : '대시보드를 불러올 수 없습니다'}
          description={
            isUnauthorized ? '보호된 API입니다. 로그인 후 다시 시도해 주세요.' : state.error.message
          }
          action={
            <div className="dash-page__error-actions">
              {isUnauthorized ? (
                <Button variant="primary" onClick={() => navigate('/login')}>
                  로그인
                </Button>
              ) : (
                <Button variant="primary" onClick={() => reload()}>
                  다시 시도
                </Button>
              )}
            </div>
          }
        />
      </div>
    )
  }

  const data = state.data

  return (
    <div className="dash-page">
      <PageHeader
        title="대시보드"
        description="오늘의 큐와 이어보기, 메모·복습을 한 화면에서 이어갑니다. 가장 위가 지금 할 일입니다."
        actions={
          <>
            <Button variant="ghost" size="sm" onClick={() => navigate('/notes')}>
              메모 허브
            </Button>
            <Button variant="secondary" size="sm" onClick={() => navigate('/watch-later')}>
              큐 정리
            </Button>
          </>
        }
      />

      <DashboardNextUp
        video={data.nextUp}
        onContinue={() => data.nextUp && openVideo(data.nextUp.id)}
        onAddNote={() => navigate('/notes')}
        onPickVideo={() => navigate('/watch-later')}
      />

      <DashboardQuickActions actions={data.quickActions} />

      <DashboardSection
        level="focus"
        eyebrow="오늘"
        title="오늘의 학습 큐"
        description="정해진 순서대로 진행해 보세요. 카드를 누르면 영상 상세로 이동합니다."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/watch-later')}>
            큐 전체 관리
          </Button>
        }
      >
        {data.todayQueue.length === 0 ? (
          <EmptyState
            title="오늘 큐가 비어 있어요"
            description="나중에 보기에서 영상을 담으면 여기에 순서가 표시됩니다."
            action={
              <Button variant="primary" onClick={() => navigate('/watch-later')}>
                나중에 보기로 이동
              </Button>
            }
          />
        ) : (
          <div className="dash-scroll">
            {data.todayQueue.map((video) => (
              <div key={video.id} className="dash-scroll__item">
                <VideoCard video={video} onOpen={openVideo} />
              </div>
            ))}
          </div>
        )}
      </DashboardSection>

      <DashboardSection
        level="standard"
        eyebrow="진행"
        title="이어보기 영상"
        description="최근 재생한 영상 중 아직 끝내지 않은 항목입니다."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/videos')}>
            전체 영상
          </Button>
        }
      >
        {data.continueWatching.length === 0 ? (
          <EmptyState
            title="이어볼 영상이 없습니다"
            description="새 영상을 시작하거나 큐에서 골라보세요."
            action={
              <Button variant="secondary" onClick={() => navigate('/videos')}>
                영상 둘러보기
              </Button>
            }
          />
        ) : (
          <div className="dash-video-grid">
            {data.continueWatching.map((video) => (
              <VideoCard key={video.id} video={video} onOpen={openVideo} />
            ))}
          </div>
        )}
      </DashboardSection>

      <div className="dash-split">
        <DashboardSection
          level="standard"
          eyebrow="기록"
          title="최근 메모 · 하이라이트"
          description="타임코드와 연결된 기록입니다. 복습이 필요한 항목은 배지로 표시됩니다."
          actions={
            <Button variant="ghost" size="sm" onClick={() => navigate('/notes')}>
              전체 보기
            </Button>
          }
        >
          {data.recentNotes.length === 0 ? (
            <EmptyState
              title="최근 메모가 없어요"
              description="영상을 보며 메모하면 복습 흐름이 자연스럽게 이어집니다."
              action={
                <Button variant="primary" size="sm" onClick={() => navigate('/videos')}>
                  영상 학습 시작
                </Button>
              }
            />
          ) : (
            <div className="dash-note-stack">
              {data.recentNotes.map((note) => (
                <NoteCard key={note.id} note={note} onOpen={() => openNote(note.id)} />
              ))}
            </div>
          )}
        </DashboardSection>

        <DashboardSection
          level="standard"
          eyebrow="정리"
          title="미완료 학습 영상"
          description="미시청·진행 중·보류인 영상입니다. 끝내면 주간 통계에 반영됩니다."
          actions={
            <Button variant="ghost" size="sm" onClick={() => navigate('/videos')}>
              필터로 보기
            </Button>
          }
        >
          {data.incompleteVideos.length === 0 ? (
            <EmptyState title="미완료 영상이 없습니다" description="훌륭해요. 새 목표를 담아 보세요." />
          ) : (
            <div className="dash-note-stack">
              {data.incompleteVideos.map((video) => (
                <VideoCard key={video.id} video={video} onOpen={openVideo} />
              ))}
            </div>
          )}
        </DashboardSection>
      </div>

      <DashboardSection
        level="standard"
        eyebrow="소스"
        title="즐겨찾기 채널의 신규 업로드"
        description="자주 보는 채널의 새 영상입니다. 큐에 넣기 전에 여기서 훑어보세요."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/subscriptions')}>
            채널 관리
          </Button>
        }
      >
        {data.newFromFavorites.length === 0 ? (
          <EmptyState
            title="신규 업로드가 없습니다"
            description="즐겨찾기 채널을 추가하면 새 영상이 여기 모입니다."
            action={
              <Button variant="secondary" size="sm" onClick={() => navigate('/subscriptions')}>
                구독 채널
              </Button>
            }
          />
        ) : (
          <div className="dash-video-grid">
            {data.newFromFavorites.map((video) => (
              <VideoCard key={video.id} video={video} onOpen={openVideo} />
            ))}
          </div>
        )}
      </DashboardSection>

      <DashboardSection
        level="support"
        eyebrow="요약"
        title="이번 주 학습 통계"
        description="짧게 숫자로만 확인하고, 자세한 차트는 통계 페이지에서 볼 수 있습니다."
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/analytics')}>
            상세 통계
          </Button>
        }
      >
        <div className="dash-stat-row">
          <StatCard label="이번 주 완료" value={data.weekly.completedCount} hint="영상 기준" tone="success" />
          <StatCard label="학습 시간" value={`${data.weekly.minutesTotal}분`} hint="재생 기록 합산" tone="primary" />
          <StatCard label="연속 학습" value={`${data.weekly.streakDays}일`} hint="목표 달성일 연속" />
          <StatCard
            label="복습 대기"
            value={data.weekly.reviewDueCount}
            hint="메모·완료 영상 기준"
            tone="warning"
          />
        </div>
      </DashboardSection>
    </div>
  )
}
