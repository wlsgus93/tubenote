/** 재생 위치 표시 — 1시간 미만 m:ss, 이상 h:mm:ss */
export function formatTimecode(totalSec: number): string {
  const safe = Math.max(0, Math.floor(totalSec))
  const h = Math.floor(safe / 3600)
  const m = Math.floor((safe % 3600) / 60)
  const s = safe % 60
  const ss = String(s).padStart(2, '0')
  if (h > 0) {
    return `${h}:${String(m).padStart(2, '0')}:${ss}`
  }
  return `${m}:${ss}`
}
