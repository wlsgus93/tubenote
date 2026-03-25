import { formatLearningMinutes } from '@/mocks/analytics'
import type { AnalyticsChannelRank, AnalyticsLengthBucket } from '@/shared/types/analytics'

export type PreferenceColumnsProps = {
  channels: AnalyticsChannelRank[]
  lengths: AnalyticsLengthBucket[]
}

/** 선호 채널 순위 + 길이 분포 */
export function PreferenceColumns({ channels, lengths }: PreferenceColumnsProps) {
  return (
    <div className="an-pref-grid">
      <section className="an-panel" aria-labelledby="an-ch-title">
        <h3 id="an-ch-title" className="an-panel__title">
          선호 채널
        </h3>
        <p className="an-panel__desc">시청·완료가 많이 겹친 채널 순입니다.</p>
        <ol className="an-ch-list">
          {channels.map((c) => (
            <li key={c.channelName} className="an-ch-item">
              <span className="an-ch-item__rank">{c.rank}</span>
              <div className="an-ch-item__body">
                <span className="an-ch-item__name">{c.channelName}</span>
                <span className="an-ch-item__meta">
                  영상 {c.completedOrTouchedCount}편 · 누적 {formatLearningMinutes(c.learningMinutes)}
                </span>
              </div>
            </li>
          ))}
        </ol>
      </section>

      <section className="an-panel" aria-labelledby="an-len-title">
        <h3 id="an-len-title" className="an-panel__title">
          선호 영상 길이
        </h3>
        <p className="an-panel__desc">짧은 틈새 vs 몰입 시간의 균형을 봅니다.</p>
        <ul className="an-len-list">
          {lengths.map((b, i) => (
            <li key={b.id} className="an-len-item">
              <div className="an-len-item__row">
                <span className="an-len-item__label">{b.label}</span>
                <span className="an-len-item__meta">
                  {b.percent}% · {b.videoCount}편
                </span>
              </div>
              <div
                className="an-len-item__track"
                role="img"
                aria-label={`${b.label} ${b.percent}퍼센트`}
              >
                <div
                  className={`an-len-item__fill an-len-item__fill--${(i % 3) + 1}`}
                  style={{ width: `${Math.min(100, b.percent)}%` }}
                />
              </div>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
