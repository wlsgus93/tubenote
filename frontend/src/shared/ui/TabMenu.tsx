export type TabItem = {
  id: string
  label: string
}

export type TabMenuProps = {
  tabs: TabItem[]
  activeId: string
  onChange: (id: string) => void
  /** a11y용 패널 id 접두어 */
  ariaLabel?: string
}

/** 본문 내 보조 전환 — 이어보기/복습 등 세그먼트 */
export function TabMenu({ tabs, activeId, onChange, ariaLabel = '보조 탭' }: TabMenuProps) {
  return (
    <div className="ui-tab-menu" role="tablist" aria-label={ariaLabel}>
      {tabs.map((tab) => {
        const selected = tab.id === activeId
        return (
          <button
            key={tab.id}
            type="button"
            role="tab"
            id={`tab-${tab.id}`}
            aria-selected={selected}
            tabIndex={selected ? 0 : -1}
            className={`ui-tab-menu__tab${selected ? ' ui-tab-menu__tab--active' : ''}`}
            onClick={() => onChange(tab.id)}
          >
            {tab.label}
          </button>
        )
      })}
    </div>
  )
}
