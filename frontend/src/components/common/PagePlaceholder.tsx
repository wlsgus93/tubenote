import { PageHeader } from '@/components/layout/PageHeader'

export type PagePlaceholderProps = {
  title: string
  description: string
  bullets?: string[]
}

/** 기능 구현 전 — 목적·향후 범위를 한 화면에서 파악 */
export function PagePlaceholder({ title, description, bullets }: PagePlaceholderProps) {
  return (
    <>
      <PageHeader title={title} description={description} />
      <div className="placeholder-body">
        {bullets && bullets.length > 0 ? (
          <ul className="placeholder-list">
            {bullets.map((line) => (
              <li key={line}>{line}</li>
            ))}
          </ul>
        ) : null}
      </div>
    </>
  )
}
