import { useCallback, useEffect, useId, useState, type FormEvent } from 'react'
import { importVideoByUrl } from '@/shared/api'
import { ApiError } from '@/shared/api/errors'
import { Button } from '@/shared/ui'

export type AddVideoByUrlDialogProps = {
  open: boolean
  onClose: () => void
  /** 등록 성공 시 userVideoId(문자열) — 목록 갱신·상세 이동에 사용 */
  onSuccess: (userVideoId: string) => void
}

/**
 * 학습 자산에 YouTube URL·영상 ID로 추가 — POST /api/v1/videos/import-url
 */
export function AddVideoByUrlDialog({ open, onClose, onSuccess }: AddVideoByUrlDialogProps) {
  const titleId = useId()
  const [value, setValue] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return
    setError(null)
  }, [open])

  useEffect(() => {
    if (!open) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open, onClose])

  const handleSubmit = useCallback(
    async (e: FormEvent) => {
      e.preventDefault()
      setError(null)
      setBusy(true)
      try {
        const res = await importVideoByUrl(value)
        const id = res.userVideoId != null ? String(res.userVideoId) : ''
        if (!id) {
          setError('응답에 userVideoId가 없습니다.')
          return
        }
        setValue('')
        onSuccess(id)
        onClose()
      } catch (err) {
        if (err instanceof ApiError) {
          if (err.status === 409) {
            setError('이미 학습 자산에 등록된 영상입니다.')
          } else {
            setError(err.message)
          }
        } else if (err instanceof Error) {
          setError(err.message)
        } else {
          setError('추가에 실패했습니다.')
        }
      } finally {
        setBusy(false)
      }
    },
    [value, onClose, onSuccess],
  )

  if (!open) return null

  return (
    <div className="vlib-dialog-backdrop" role="presentation" onClick={onClose}>
      <div
        className="vlib-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id={titleId} className="vlib-dialog__title">
          URL로 영상 추가
        </h2>
        <p className="vlib-dialog__hint">
          YouTube 시청·공유 주소를 붙여넣거나, 11자 영상 ID만 입력할 수 있습니다.
        </p>
        <form onSubmit={handleSubmit} className="vlib-dialog__form">
          <label htmlFor="vlib-add-url" className="vlib-dialog__label">
            주소 또는 영상 ID
          </label>
          <input
            id="vlib-add-url"
            type="text"
            className="vlib-dialog__input"
            value={value}
            onChange={(e) => setValue(e.target.value)}
            placeholder="https://www.youtube.com/watch?v=…"
            autoComplete="off"
            disabled={busy}
            autoFocus
          />
          {error ? (
            <p className="vlib-dialog__error" role="alert">
              {error}
            </p>
          ) : null}
          <div className="vlib-dialog__actions">
            <Button type="button" variant="ghost" onClick={onClose} disabled={busy}>
              취소
            </Button>
            <Button type="submit" variant="primary" disabled={busy || !value.trim()}>
              {busy ? '추가 중…' : '학습 자산에 추가'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
