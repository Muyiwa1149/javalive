import { createRouter, createWebHistory } from 'vue-router'
import { useAuthUserStore } from '@/stores/authUser'
import { useAuthAdminStore } from '@/stores/authAdmin'

const PublicLayout = () => import('@/layouts/PublicLayout.vue')
const UserLayout = () => import('@/layouts/UserLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')

const routes = [
  {
    path: '/',
    component: PublicLayout,
    children: [
      { path: '', name: 'home', component: () => import('@/pages/public/Home.vue') },
      // Remaining public pages (terms, privacy, about, faq, ...) added in Phase 3.
    ],
  },
  {
    path: '/dashboard',
    component: UserLayout,
    meta: { requiresUserAuth: true },
    children: [
      { path: '', name: 'user.dashboard', component: () => import('@/pages/user/Dashboard.vue') },
      // Remaining user-dashboard pages added in Phase 4.
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdminAuth: true },
    children: [
      { path: 'dashboard', name: 'admin.dashboard', component: () => import('@/pages/admin/Dashboard.vue') },
      // Remaining admin pages added in Phase 5.
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/pages/NotFound.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresUserAuth) {
    const authUser = useAuthUserStore()
    if (!authUser.isAuthenticated) {
      return { name: 'home' }
    }
  }
  if (to.meta.requiresAdminAuth) {
    const authAdmin = useAuthAdminStore()
    if (!authAdmin.isAuthenticated) {
      return { name: 'home' }
    }
  }
  return true
})

export default router
