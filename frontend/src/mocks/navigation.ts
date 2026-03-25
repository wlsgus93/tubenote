/**
 * 사이드바 내비 mock — 배지 등 메타는 UI 검증용
 * 실서비스 시 API/스토어 값으로 치환
 */
export type SidebarNavItem = {
  to: string
  label: string
  end?: boolean
  /** 대기 건수·알림 등 시각 검증용 */
  badgeCount?: number
}

export type SidebarNavGroup = {
  label: string
  items: SidebarNavItem[]
}

export const SIDEBAR_NAV_MOCK: SidebarNavGroup[] = [
  {
    label: '오늘',
    items: [{ to: '/dashboard', label: '대시보드', end: true, badgeCount: 3 }],
  },
  {
    label: '학습',
    items: [
      { to: '/videos', label: '영상' },
      { to: '/watch-later', label: '나중에 보기', badgeCount: 5 },
      { to: '/notes', label: '메모 · 하이라이트', badgeCount: 2 },
    ],
  },
  {
    label: '채널',
    items: [{ to: '/subscriptions', label: '구독 채널', end: true }],
  },
  {
    label: '인사이트',
    items: [{ to: '/analytics', label: '학습 통계', end: true }],
  },
  {
    label: '계정',
    items: [{ to: '/settings', label: '설정', end: true }],
  },
]
