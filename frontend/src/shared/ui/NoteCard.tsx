import type { NoteCardModel } from '@/shared/types/cards'
import { StatusBadge } from '@/shared/ui/StatusBadge'

export type NoteCardProps = {
  note: NoteCardModel
  onOpen?: (id: string) => void
}

/** 타임코드 메모 미리보기 */
export function NoteCard({ note, onOpen }: NoteCardProps) {
  const { id, videoTitle, timecode, excerpt, createdAtLabel, reviewSuggested } = note

  return (
    <button type="button" className="ui-note-card" onClick={() => onOpen?.(id)}>
      <div className="ui-note-card__top">
        <p className="ui-note-card__video">{videoTitle}</p>
        <span className="ui-note-card__time">{timecode}</span>
      </div>
      <p className="ui-note-card__excerpt">{excerpt}</p>
      <div className="ui-note-card__footer">
        {createdAtLabel ? <p className="ui-note-card__date">{createdAtLabel}</p> : null}
        {reviewSuggested ? <StatusBadge type="review" needed /> : null}
      </div>
    </button>
  )
}
