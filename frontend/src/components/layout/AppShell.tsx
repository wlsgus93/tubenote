import { useCallback, useEffect, useState } from 'react'
import { Outlet } from 'react-router-dom'
import { MainContent } from '@/components/layout/MainContent'
import { Sidebar } from '@/components/layout/Sidebar'
import { Topbar } from '@/components/layout/Topbar'

const NAV_BREAKPOINT_PX = 900

/** 앱 쉘 — Topbar + Sidebar + 메인(Outlet), 좁은 화면에서 드로어 */
export function AppShell() {
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false)

  const closeMobileSidebar = useCallback(() => setMobileSidebarOpen(false), [])
  const toggleMobileSidebar = useCallback(() => setMobileSidebarOpen((v) => !v), [])

  useEffect(() => {
    const mq = window.matchMedia(`(max-width: ${NAV_BREAKPOINT_PX}px)`)
    const onChange = () => {
      if (!mq.matches) setMobileSidebarOpen(false)
    }
    mq.addEventListener('change', onChange)
    return () => mq.removeEventListener('change', onChange)
  }, [])

  return (
    <div className="app-shell">
      <Topbar onMenuClick={toggleMobileSidebar} />
      <button
        type="button"
        className={`sidebar-backdrop${mobileSidebarOpen ? ' sidebar-backdrop--visible' : ''}`}
        aria-label="메뉴 닫기"
        onClick={closeMobileSidebar}
      />
      <div className="app-shell__body">
        <Sidebar mobileOpen={mobileSidebarOpen} onMobileClose={closeMobileSidebar} />
        <MainContent>
          <Outlet />
        </MainContent>
      </div>
    </div>
  )
}
