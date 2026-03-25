import { RouterProvider } from 'react-router-dom'
import { router } from '@/app/router'

export function AppProviders() {
  return (
    <RouterProvider
      router={router}
      future={{
        /** React Router v7 전환 준비 — state 업데이트를 startTransition으로 감쌈 */
        v7_startTransition: true,
      }}
    />
  )
}
