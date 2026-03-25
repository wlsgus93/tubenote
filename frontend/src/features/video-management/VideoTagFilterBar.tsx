export type VideoTagFilterBarProps = {
  tagOptions: string[]
  selectedTags: string[]
  onToggleTag: (tag: string) => void
  onClearTags: () => void
}

/** 태그 다중 선택 — OR 매칭, 비어 있으면 전체 */
export function VideoTagFilterBar({ tagOptions, selectedTags, onToggleTag, onClearTags }: VideoTagFilterBarProps) {
  return (
    <div className="vlib-filter-block">
      <p className="vlib-filter-block__label">태그</p>
      <div className="vlib-tag-row">
        <button
          type="button"
          className="vlib-tag-row__clear"
          aria-pressed={selectedTags.length === 0}
          onClick={onClearTags}
        >
          전체
        </button>
        {tagOptions.map((tag) => {
          const on = selectedTags.includes(tag)
          return (
            <button key={tag} type="button" aria-pressed={on} onClick={() => onToggleTag(tag)}>
              {tag}
            </button>
          )
        })}
      </div>
    </div>
  )
}
