import { useCallback, useEffect, useState } from 'react'
import { Button } from '@/shared/ui'
import { readVideoScratchpad, writeVideoScratchpad } from '@/features/video-detail/videoScratchpadStorage'

export type VideoScratchpadPanelProps = {
  videoId: string
}

/** 영상별 자유 필기 노트 — 자동 저장(localStorage) + 수동 저장·비우기 */
export function VideoScratchpadPanel({ videoId }: VideoScratchpadPanelProps) {
  const [text, setText] = useState(() => readVideoScratchpad(videoId))

  useEffect(() => {
    setText(readVideoScratchpad(videoId))
  }, [videoId])

  useEffect(() => {
    const t = window.setTimeout(() => writeVideoScratchpad(videoId, text), 700)
    return () => window.clearTimeout(t)
  }, [text, videoId])

  const [manualSavedAt, setManualSavedAt] = useState<string | null>(null)

  const saveNow = useCallback(() => {
    writeVideoScratchpad(videoId, text)
    setManualSavedAt(
      new Date().toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    )
  }, [videoId, text])

  useEffect(() => {
    setManualSavedAt(null)
  }, [videoId])

  const handleClear = useCallback(() => {
    if (!text.trim()) return
    if (!window.confirm('필기 내용을 모두 지울까요?')) return
    setText('')
    writeVideoScratchpad(videoId, '')
    setManualSavedAt(null)
  }, [text, videoId])

  return (
    <div className="vd-scratchpad">
      <p className="vd-scratchpad__hint">
        강의 요점·질문·복습할 문장을 자유롭게 적습니다. 이 영상에만 연결되며 브라우저에 자동 저장됩니다.
      </p>
      <textarea
        id={`vd-scratch-${videoId}`}
        className="vd-scratchpad__textarea"
        aria-label="이 영상 필기 내용"
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="이 영상을 보며 필기할 내용을 입력하세요…"
        rows={10}
        spellCheck
      />
      <div className="vd-scratchpad__footer">
        <div className="vd-scratchpad__meta">
          {manualSavedAt ? (
            <span className="vd-scratchpad__saved" role="status">
              수동 저장 {manualSavedAt}
            </span>
          ) : (
            <span className="vd-scratchpad__saved vd-scratchpad__saved--muted">입력 후 약 0.7초 뒤 자동 저장</span>
          )}
        </div>
        <div className="vd-scratchpad__actions">
          <Button variant="secondary" size="sm" type="button" onClick={saveNow}>
            지금 저장
          </Button>
          <Button variant="ghost" size="sm" type="button" onClick={handleClear} disabled={!text.trim()}>
            비우기
          </Button>
        </div>
      </div>
    </div>
  )
}
