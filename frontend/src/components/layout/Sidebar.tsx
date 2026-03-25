import { NavLink } from 'react-router-dom'
import type { SidebarNavGroup } from '@/mocks/navigation'
import { SIDEBAR_NAV_MOCK } from '@/mocks/navigation'

export type SidebarProps = {
  groups?: SidebarNavGroup[]
  /** 모바일 드로어 열림 */
  mobileOpen?: boolean
  onMobileClose?: () => void
}

/** 왼쪽 내비 — IA 그룹·배지(mock) */
export function Sidebar({ groups = SIDEBAR_NAV_MOCK, mobileOpen = false, onMobileClose }: SidebarProps) {
  return (
    <aside
      className={`sidebar${mobileOpen ? ' sidebar--open' : ''}`}
      aria-label="주요 메뉴"
    >
      {groups.map((group) => (
        <div key={group.label} className="sidebar__group">
          <p className="sidebar__group-label">{group.label}</p>
          <nav aria-label={group.label}>
            {group.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  `sidebar__link${isActive ? ' sidebar__link--active' : ''}`
                }
                onClick={() => onMobileClose?.()}
              >
                <span className="sidebar__link-text">{item.label}</span>
                {typeof item.badgeCount === 'number' && item.badgeCount > 0 ? (
                  <span className="sidebar__badge">{item.badgeCount > 99 ? '99+' : item.badgeCount}</span>
                ) : null}
              </NavLink>
            ))}
          </nav>
        </div>
      ))}
      <div className="sidebar__footer">
        <NavLink to="/login" className="sidebar__link" end onClick={() => onMobileClose?.()}>
          <span className="sidebar__link-text">로그인 (플레이스홀더)</span>
        </NavLink>
      </div>
    </aside>
  )
}
