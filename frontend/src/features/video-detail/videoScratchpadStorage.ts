/** 영상별 필기 — 브라우저 localStorage (추후 API로 대체 가능) */
const PREFIX = 'ylh.video-scratch.'

export function readVideoScratchpad(videoId: string): string {
  try {
    return localStorage.getItem(PREFIX + videoId) ?? ''
  } catch {
    return ''
  }
}

export function writeVideoScratchpad(videoId: string, text: string): void {
  try {
    const key = PREFIX + videoId
    if (text.trim() === '') localStorage.removeItem(key)
    else localStorage.setItem(key, text)
  } catch {
    /* private mode 등 */
  }
}
