import type { ChannelCategory } from '@/shared/types/channel-library'

/** 구독 채널 카테고리 — PATCH `category` 에 id 문자열로 저장해 서버와 맞춤 */
export const CHANNEL_CATEGORIES: ChannelCategory[] = [
  { id: 'cat-frontend', name: '프론트엔드' },
  { id: 'cat-backend', name: '백엔드' },
  { id: 'cat-product', name: '프로덕트' },
  { id: 'cat-devops', name: 'DevOps' },
  { id: 'cat-career', name: '커리어' },
  { id: 'cat-cs', name: 'CS' },
  { id: 'cat-productivity', name: '생산성' },
  { id: 'cat-ml', name: 'ML·수학' },
  { id: 'cat-general', name: '일반·엔터' },
]
