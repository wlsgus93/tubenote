import { Link } from 'react-router-dom'
import { PageHeader } from '@/components/layout/PageHeader'

export function NotFoundPage() {
  return (
    <>
      <PageHeader
        title="페이지를 찾을 수 없습니다"
        description="주소가 바뀌었거나 존재하지 않는 경로입니다. 대시보드에서 다시 시작할 수 있습니다."
        actions={
          <Link to="/dashboard" style={{ fontWeight: 600, color: 'var(--color-primary)' }}>
            대시보드로
          </Link>
        }
      />
      <div className="placeholder-body">
        <p className="page-header__description" style={{ margin: 0 }}>
          학습 흐름을 끊지 않도록 자주 쓰는 메뉴는 왼쪽 사이드바에서 선택하세요.
        </p>
      </div>
    </>
  )
}
