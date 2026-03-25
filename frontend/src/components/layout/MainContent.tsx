import type { ReactNode } from 'react'
import { useLocation } from 'react-router-dom'

export type MainContentProps = {
  children: ReactNode
  /** 읽기 폭 제한 여부 — 미지정 시 `/videos/:id` 만 전폭 */
  constrained?: boolean
}

/** 스크롤 가능한 본문 영역 — 영상 상세는 2열 레이아웃을 위해 폭 제한 해제 */
export function MainContent({ children, constrained: constrainedProp }: MainContentProps) {
  const { pathname } = useLocation()
  const isVideoDetail = /^\/videos\/.+/.test(pathname)
  const constrained = constrainedProp !== undefined ? constrainedProp : !isVideoDetail

  return (
    <main
      className={`main-content${constrained ? ' main-content--constrained' : ''}`}
      role="main"
    >
      <div className="main-content__inner">{children}</div>
    </main>
  )
}
