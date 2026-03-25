import { createBrowserRouter } from 'react-router-dom'
import { AppShell } from '@/components/layout/AppShell'
import { RequireAuth } from '@/app/router/RequireAuth'
import { RootLanding } from '@/app/router/RootLanding'
import { LoginPage } from '@/pages/auth/LoginPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { VideosPage } from '@/pages/videos/VideosPage'
import { VideoDetailPage } from '@/pages/video-detail/VideoDetailPage'
import { WatchLaterPage } from '@/pages/watch-later/WatchLaterPage'
import { NotesPage } from '@/pages/notes/NotesPage'
import { SubscriptionsPage } from '@/pages/subscriptions/SubscriptionsPage'
import { AnalyticsPage } from '@/pages/analytics/AnalyticsPage'
import { SettingsPage } from '@/pages/settings/SettingsPage'
import { OnboardingPage } from '@/pages/onboarding/OnboardingPage'
import { NotFoundPage } from '@/pages/not-found/NotFoundPage'

/** docs/routes.md 와 동일한 경로 골격 */
export const router = createBrowserRouter([
  { path: '/', element: <RootLanding /> },
  { path: '/login', element: <LoginPage /> },
  { path: '/onboarding', element: <OnboardingPage /> },
  {
    element: <RequireAuth />,
    children: [
      {
        element: <AppShell />,
        children: [
          { path: 'dashboard', element: <DashboardPage /> },
          { path: 'videos', element: <VideosPage /> },
          { path: 'videos/:videoId', element: <VideoDetailPage /> },
          { path: 'watch-later', element: <WatchLaterPage /> },
          { path: 'notes', element: <NotesPage /> },
          { path: 'subscriptions', element: <SubscriptionsPage /> },
          { path: 'analytics', element: <AnalyticsPage /> },
          { path: 'settings', element: <SettingsPage /> },
          { path: '*', element: <NotFoundPage /> },
        ],
      },
    ],
  },
])
