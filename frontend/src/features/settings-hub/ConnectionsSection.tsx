import { Button } from '@/shared/ui'
import type { LinkedAccountRow, LinkedAccountStatus } from '@/shared/types/settings'

export type ConnectionsSectionProps = {
  accounts: LinkedAccountRow[]
}

function statusClass(s: LinkedAccountStatus) {
  if (s === 'connected') return 'settings-status settings-status--ok'
  if (s === 'error') return 'settings-status settings-status--err'
  return 'settings-status settings-status--off'
}

function statusLabel(s: LinkedAccountStatus) {
  if (s === 'connected') return '연결됨'
  if (s === 'error') return '오류'
  return '미연결'
}

/** 외부 계정 — OAuth·동기화 API 연결 지점 안내 */
export function ConnectionsSection({ accounts }: ConnectionsSectionProps) {
  return (
    <section className="settings-section" aria-labelledby="settings-conn-title">
      <h2 id="settings-conn-title" className="settings-section__title">
        연동 계정
      </h2>
      <p className="settings-section__desc">유튜브 구독·시청 데이터를 가져오려면 여기서 계정을 연결하게 됩니다. 지금은 상태만 mock입니다.</p>
      <div className="settings-conn">
        {accounts.map((a) => (
          <div key={a.id} className="settings-conn-card">
            <div className="settings-conn-card__body">
              <h4>{a.providerLabel}</h4>
              <p>{a.description}</p>
              {a.connectedDetail ? <p className="settings-field__hint">{a.connectedDetail}</p> : null}
            </div>
            <div className="settings-conn-card__actions">
              <span className={statusClass(a.status)}>{statusLabel(a.status)}</span>
              <Button variant="secondary" size="sm" type="button" disabled title="API 연동 후 사용 가능">
                {a.status === 'connected' ? '관리' : '연결'}
              </Button>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
