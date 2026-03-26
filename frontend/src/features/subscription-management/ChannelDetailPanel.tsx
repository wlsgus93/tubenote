import { useNavigate } from 'react-router-dom'
import { Button } from '@/shared/ui'
import type {
  ChannelCategory,
  ChannelFocus,
  ChannelRecentFeedItem,
  ChannelSubscription,
} from '@/shared/types/channel-library'

export type ChannelDetailPanelProps = {
  channel: ChannelSubscription | null
  categories: ChannelCategory[]
  categoryName: (id: string) => string
  onMemoChange: (id: string, memo: string) => void
  onFocusChange: (id: string, focus: ChannelFocus) => void
  onCategoryChange: (id: string, categoryId: string) => void
  onToggleFavorite: (id: string) => void
  /** 최근 피드에서 학습 자산으로 추가 — mock 또는 API 피드 */
  recentVideos?: ChannelRecentFeedItem[]
  pendingImportYoutubeId?: string | null
  importedYoutubeIds?: ReadonlySet<string>
  onAddFeedVideoToLibrary?: (youtubeVideoId: string) => void
  feedImportError?: string | null
  recentFeedLoading?: boolean
  recentFeedLoadError?: string | null
  /** api: 서버 피드 / mock: 로컬 데모 */
  feedSource?: 'api' | 'mock'
  /** 메모 입력은 onChange로 로컬만 갱신 후, blur 시 서버 반영(API) */
  onMemoBlur?: (id: string, memo: string) => void
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

/** 선택 채널 상세 — 최근 업로드·메모·유형·카테고리·학습 자산 이동 */
export function ChannelDetailPanel({
  channel,
  categories,
  categoryName,
  onMemoChange,
  onFocusChange,
  onCategoryChange,
  onToggleFavorite,
  recentVideos,
  pendingImportYoutubeId,
  importedYoutubeIds,
  onAddFeedVideoToLibrary,
  feedImportError,
  recentFeedLoading,
  recentFeedLoadError,
  feedSource = 'api',
  onMemoBlur,
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

      {onAddFeedVideoToLibrary ? (
        <section className="chlib-panel__feed" aria-label="최근 업로드">
          <h3 className="chlib-panel__feed-title">최근 업로드</h3>
          {feedSource === 'api' ? (
            <p className="chlib-panel__feed-note">
              서버 DB에 쌓인 피드입니다. 비어 있으면 상단의 「채널 업로드 동기화」를 실행해 보세요.
            </p>
          ) : (
            <p className="chlib-panel__feed-note">로컬 mock 피드입니다.</p>
          )}
          {recentFeedLoading ? (
            <p className="chlib-panel__feed-status" role="status">
              피드를 불러오는 중…
            </p>
          ) : recentFeedLoadError ? (
            <p className="chlib-panel__feed-error" role="alert">
              {recentFeedLoadError}
            </p>
          ) : recentVideos && recentVideos.length > 0 ? (
            <ul className="chlib-panel__feed-list">
              {recentVideos.map((v) => {
                const done = importedYoutubeIds?.has(v.youtubeVideoId)
                const busy = pendingImportYoutubeId === v.youtubeVideoId
                return (
                  <li key={v.youtubeVideoId} className="chlib-panel__feed-row">
                    <div className="chlib-panel__feed-main">
                      <p className="chlib-panel__feed-titleline">{v.title}</p>
                      <p className="chlib-panel__feed-meta">
                        {v.publishedAtLabel ? `${v.publishedAtLabel} · ` : null}
                        <code className="chlib-panel__feed-id">{v.youtubeVideoId}</code>
                      </p>
                    </div>
                    <Button
                      type="button"
                      variant={done ? 'ghost' : 'secondary'}
                      size="sm"
                      disabled={Boolean(done) || busy}
                      onClick={() => onAddFeedVideoToLibrary(v.youtubeVideoId)}
                    >
                      {done ? '추가됨' : busy ? '추가 중…' : '라이브러리에 추가'}
                    </Button>
                  </li>
                )
              })}
            </ul>
          ) : (
            <p className="chlib-panel__feed-empty">표시할 최근 영상이 없습니다.</p>
          )}
          {feedImportError ? (
            <p className="chlib-panel__feed-error" role="alert">
              {feedImportError}
            </p>
          ) : null}
        </section>
      ) : null}

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
          {!categories.some((c) => c.id === channel.categoryId) && channel.categoryId ? (
            <option value={channel.categoryId}>{categoryName(channel.categoryId)}</option>
          ) : null}
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
          onBlur={onMemoBlur ? (e) => onMemoBlur(channel.id, e.target.value) : undefined}
          placeholder="이 채널을 어떤 학습 목적으로 구독했는지 적어 두면 나중에 정리하기 쉽습니다."
        />
      </div>

      <div className="chlib-panel__actions chlib-panel__actions--split">
        <Button variant="primary" onClick={() => navigate(`/videos?q=${encodeURIComponent(channel.name)}`)}>
          이 채널 영상 — 학습 자산에서 보기
        </Button>
        <Button variant="secondary" onClick={() => navigate('/videos')}>
          URL로 영상 추가
        </Button>
      </div>
    </aside>
  )
}
