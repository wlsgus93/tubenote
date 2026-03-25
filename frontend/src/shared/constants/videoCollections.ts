import type { VideoCollection } from '@/shared/types/video-library'

/** 컬렉션 선택 UI용 정적 목록 — 추후 GET /api/collections 로 대체 가능 */
export const VIDEO_COLLECTIONS: VideoCollection[] = [
  { id: 'col-inbox', name: '인박스' },
  { id: 'col-fe', name: '프론트엔드 심화' },
  { id: 'col-be', name: '백엔드·인프라' },
  { id: 'col-lang', name: '언어·커뮤니케이션' },
  { id: 'col-career', name: '커리어·생산성' },
]
