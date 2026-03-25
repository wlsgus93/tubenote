import { useNavigate } from 'react-router-dom'
import { Button } from '@/shared/ui'
import type { ChannelCategory, ChannelFocus, ChannelSubscription } from '@/shared/types/channel-library'

export type ChannelDetailPanelProps = {
  channel: ChannelSubscription | null
  categories: ChannelCategory[]
  categoryName: (id: string) => string
  onMemoChange: (id: string, memo: string) => void
  onFocusChange: (id: string, focus: ChannelFocus) => void
  onCategoryChange: (id: string, categoryId: string) => void
  onToggleFavorite: (id: string) => void
}

function formatUploadDetail(iso: string) {
  const d = new Date(iso)
  return d.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** 선택 채널 상세 — 메모·유형·카테고리·학습 자산 이동 */
export function ChannelDetailPanel({
  channel,
  categories,
  categoryName,
  onMemoChange,
  onFocusChange,
  onCategoryChange,
  onToggleFavorite,
}: ChannelDetailPanelProps) {
  const navigate = useNavigate()

  if (!channel) {
    return (
      <aside className="chlib-panel chlib-panel--empty" aria-label="채널 상세">
        <p className="chlib-panel__placeholder">왼쪽에서 채널을 선택하면 구독 목적·통계를 여기서 정리할 수 있어요.</p>
      </aside>
    )
  }

  const catLabel = categoryName(channel.categoryId)

  return (
    <aside className="chlib-panel" aria-label={`${channel.name} 상세`}>
      <header className="chlib-panel__head">
        <div>
          <p className="chlib-panel__eyebrow">{catLabel}</p>
          <h2 className="chlib-panel__title">{channel.name}</h2>
        </div>
        <button
          type="button"
          className={`chlib-panel__fav${channel.isFavorite ? ' chlib-panel__fav--on' : ''}`}
          aria-pressed={channel.isFavorite}
          onClick={() => onToggleFavorite(channel.id)}
        >
          {channel.isFavorite ? '즐겨찾기됨' : '즐겨찾기'}
        </button>
      </header>

      <div className="chlib-panel__stats">
        <div className="chlib-panel__stat">
          <span className="chlib-panel__stat-label">신규 영상(최근 구간)</span>
          <span className="chlib-panel__stat-value">{channel.newVideoCount}개</span>
        </div>
        <div className="chlib-panel__stat">
          <span className="chlib-panel__stat-label">학습 자산에 저장</span>
          <span className="chlib-panel__stat-value">{channel.savedVideoCount}개</span>
        </div>
        <div className="chlib-panel__stat chlib-panel__stat--full">
          <span className="chlib-panel__stat-label">마지막 업로드 시각</span>
          <span className="chlib-panel__stat-value">{formatUploadDetail(channel.lastUploadAt)}</span>
        </div>
      </div>

      <div className="chlib-panel__field">
        <span className="chlib-panel__label">채널 유형</span>
        <div className="chlib-panel__segment" role="group" aria-label="학습용 또는 일반">
          <button
            type="button"
            className={channel.focus === 'learning' ? 'chlib-panel__seg is-active' : 'chlib-panel__seg'}
            onClick={() => onFocusChange(channel.id, 'learning')}
          >
            학습용
          </button>
          <button
            type="button"
            className={channel.focus === 'general' ? 'chlib-panel__seg is-active' : 'chlib-panel__seg'}
            onClick={() => onFocusChange(channel.id, 'general')}
          >
            일반
          </button>
        </div>
      </div>

      <div className="chlib-panel__field">
        <label className="chlib-panel__label" htmlFor={`ch-cat-${channel.id}`}>
          카테고리
        </label>
        <select
          id={`ch-cat-${channel.id}`}
          className="chlib-panel__select"
          value={channel.categoryId}
          onChange={(e) => onCategoryChange(channel.id, e.target.value)}
        >
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
      </div>

      <div className="chlib-panel__field chlib-panel__field--grow">
        <label className="chlib-panel__label" htmlFor={`ch-memo-${channel.id}`}>
          구독 메모 (왜 보는지)
        </label>
        <textarea
          id={`ch-memo-${channel.id}`}
          className="chlib-panel__textarea"
          rows={5}
          value={channel.memo}
          onChange={(e) => onMemoChange(channel.id, e.target.value)}
          placeholder="이 채널을 어떤 학습 목적으로 구독했는지 적어 두면 나중에 정리하기 쉽습니다."
        />
      </div>

      <div className="chlib-panel__actions">
        <Button variant="primary" onClick={() => navigate(`/videos?q=${encodeURIComponent(channel.name)}`)}>
          이 채널 영상 — 학습 자산에서 보기
        </Button>
      </div>
    </aside>
  )
}
