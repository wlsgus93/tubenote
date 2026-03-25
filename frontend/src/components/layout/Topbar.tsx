export type TopbarProps = {
  /** 모바일에서 사이드바 토글 */
  onMenuClick?: () => void
}

/** 상단 바 — 브랜딩·검색·계정 자리 */
export function Topbar({ onMenuClick }: TopbarProps) {
  return (
    <header className="topbar">
      <div className="topbar__start">
        <button
          type="button"
          className="topbar__menu-btn"
          aria-label="메뉴 열기"
          onClick={onMenuClick}
        >
          <span className="topbar__menu-icon" aria-hidden />
        </button>
        <div className="topbar__brand">
          <span className="topbar__title">학습 허브</span>
          <span className="topbar__tagline">유튜브 영상을 학습 목표 중심으로 정리하는 공간</span>
        </div>
      </div>
      <div className="topbar__search">
        <input type="search" placeholder="영상·메모 검색 (준비 중)" disabled aria-disabled="true" />
      </div>
      <div className="topbar__user" aria-hidden="true">
        계정 연동 예정
      </div>
    </header>
  )
}
