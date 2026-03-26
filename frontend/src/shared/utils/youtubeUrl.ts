/** YouTube 시청 URL — 백엔드 `import-url` 에 그대로 넘길 수 있음 */
export function youtubeWatchUrlFromVideoId(videoId: string): string {
  const id = videoId.trim()
  if (!id) return ''
  return `https://www.youtube.com/watch?v=${encodeURIComponent(id)}`
}

/**
 * 붙여넣기 값 정규화 — 전체 URL이면 그대로, 11자 video id만 오면 watch URL로 변환
 */
export function normalizeYouTubeImportInput(raw: string): string {
  const s = raw.trim()
  if (!s) return ''
  if (/^https?:\/\//i.test(s)) return s
  if (/^[a-zA-Z0-9_-]{11}$/.test(s)) return youtubeWatchUrlFromVideoId(s)
  return s
}
