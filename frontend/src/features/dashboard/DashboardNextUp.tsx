import { Button } from '@/shared/ui/Button'
import { StatusBadge } from '@/shared/ui/StatusBadge'
import type { VideoCardModel } from '@/shared/types/cards'

export type DashboardNextUpProps = {
  video: VideoCardModel | null
  onContinue: () => void
  onAddNote?: () => void
  /** nextUp 없을 때 — 큐·영상 목록으로 보내기 */
  onPickVideo?: () => void
}

/** 지금 이어서 할 학습 — 화면 최상단 CTA */
export function DashboardNextUp({ video, onContinue, onAddNote, onPickVideo }: DashboardNextUpProps) {
  if (!video) {
    return (
      <div className="dash-next dash-next--empty">
        <div>
          <p className="dash-next__eyebrow">지금 이어서</p>
          <h2 className="dash-next__title">이어서 볼 영상을 골라 주세요</h2>
          <p className="dash-next__meta">오늘 큐나 영상 목록에서 다음 학습을 시작할 수 있습니다.</p>
        </div>
        <div className="dash-next__actions">
          {onPickVideo ? (
            <Button variant="primary" size="md" onClick={onPickVideo}>
              영상 고르기
            </Button>
          ) : null}
          {onAddNote ? (
            <Button variant="secondary" size="md" onClick={onAddNote}>
              메모 허브
            </Button>
          ) : null}
        </div>
      </div>
    )
  }

  const progress =
    typeof video.progressPercent === 'number' ? Math.min(100, Math.max(0, video.progressPercent)) : null

  return (
    <div className="dash-next">
      <div>
        <p className="dash-next__eyebrow">지금 이어서</p>
        <h2 className="dash-next__title">{video.title}</h2>
        <p className="dash-next__meta">
          {video.channelName}
          {progress !== null ? ` · 진행 ${progress}%` : null}
          {video.durationLabel ? ` · ${video.durationLabel}` : null}
        </p>
        <div className="dash-next__badges">
          <StatusBadge type="learning" status={video.learningStatus} />
          {video.priority ? <StatusBadge type="priority" level={video.priority} /> : null}
          {video.reviewNeeded ? <StatusBadge type="review" needed /> : null}
        </div>
      </div>
      <div className="dash-next__actions">
        <Button variant="primary" size="md" onClick={onContinue}>
          이어서 학습
        </Button>
        {onAddNote ? (
          <Button variant="secondary" size="md" onClick={onAddNote}>
            메모 허브
          </Button>
        ) : null}
      </div>
    </div>
  )
}
