import { Button } from '@/shared/ui'
import type { WatchLaterEntry } from '@/shared/types/watch-later'

export type TodayQueueStripProps = {
  items: WatchLaterEntry[]
  onMoveUp: (id: string) => void
  onMoveDown: (id: string) => void
  onRemoveFromQueue: (id: string) => void
  onOpenVideo?: (libraryVideoId: string) => void
}

/** 오늘 볼 영상 — 순서만 정하면 되는 작은 계획 영역 */
export function TodayQueueStrip({
  items,
  onMoveUp,
  onMoveDown,
  onRemoveFromQueue,
  onOpenVideo,
}: TodayQueueStripProps) {
  if (items.length === 0) {
    return (
      <section className="wl-today wl-today--empty" aria-labelledby="wl-today-heading">
        <h2 id="wl-today-heading" className="wl-today__title">
          오늘 볼 영상
        </h2>
        <p className="wl-today__calm">
          오늘은 <strong>1~3개</strong>만 골라도 충분해요. 아래 목록에서 「오늘 큐」로 넣어 보세요.
        </p>
        <a href="#watch-later-list" className="wl-today__link">
          대기 목록으로 이동
        </a>
      </section>
    )
  }

  return (
    <section className="wl-today" aria-labelledby="wl-today-heading">
      <div className="wl-today__head">
        <h2 id="wl-today-heading" className="wl-today__title">
          오늘 볼 영상
        </h2>
        <p className="wl-today__hint">위에서 아래로 시청 순서입니다. 화살표로 바꿀 수 있어요.</p>
      </div>
      <ol className="wl-today__list">
        {items.map((e, index) => (
          <li key={e.id} className="wl-today__item">
            <span className="wl-today__index" aria-hidden>
              {index + 1}
            </span>
            <div className="wl-today__body">
              <p className="wl-today__item-title">{e.title}</p>
              <p className="wl-today__item-meta">
                {e.channelName} · {e.durationLabel}
              </p>
            </div>
            <div className="wl-today__actions">
              {e.libraryVideoId ? (
                <Button variant="ghost" size="sm" type="button" onClick={() => onOpenVideo?.(e.libraryVideoId!)}>
                  학습 화면
                </Button>
              ) : null}
              <Button
                variant="ghost"
                size="sm"
                type="button"
                disabled={index === 0}
                onClick={() => onMoveUp(e.id)}
                aria-label="한 칸 위로"
              >
                ↑
              </Button>
              <Button
                variant="ghost"
                size="sm"
                type="button"
                disabled={index >= items.length - 1}
                onClick={() => onMoveDown(e.id)}
                aria-label="한 칸 아래로"
              >
                ↓
              </Button>
              <Button variant="ghost" size="sm" type="button" onClick={() => onRemoveFromQueue(e.id)}>
                큐에서 빼기
              </Button>
            </div>
          </li>
        ))}
      </ol>
    </section>
  )
}
