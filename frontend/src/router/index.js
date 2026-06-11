import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import StudentEnrollment from '@/views/StudentEnrollment.vue'
import SubjectManage from '@/views/SubjectManage.vue'
import ClassManage from '@/views/ClassManage.vue'
import TeacherManage from '@/views/TeacherManage.vue'
import FeeManage from '@/views/FeeManage.vue'
import ScheduleQuery from '@/views/ScheduleQuery.vue'
import StudentManage from '@/views/StudentManage.vue'
import LoginView from '@/views/LoginView.vue'

const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    component: LoginView,
    meta: { guest: true },
  },
  {
    path: '/enrollment',
    component: StudentEnrollment,
    meta: { roles: ['student', 'admin'] },
  },
  {
    path: '/subjects',
    component: SubjectManage,
    meta: { roles: ['admin'] },
  },
  {
    path: '/classes',
    component: ClassManage,
    meta: { roles: ['admin'] },
  },
  {
    path: '/teachers',
    component: TeacherManage,
    meta: { roles: ['admin'] },
  },
  {
    path: '/fee',
    component: FeeManage,
    meta: { roles: ['admin'] },
  },
  {
    path: '/schedule',
    component: ScheduleQuery,
    meta: { roles: ['student', 'teacher', 'admin'] },
  },
  {
    path: '/students',
    component: StudentManage,
    meta: { roles: ['admin'] },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()

  // 允许未登录访问的页面（登录页）
  if (to.meta.guest) {
    if (auth.isLoggedIn) {
      // 已登录用户访问登录页 → 重定向到对应首页
      if (auth.isAdmin) return next('/students')
      if (auth.isTeacher) return next('/schedule')
      return next('/enrollment')
    }
    return next()
  }

  // 需要登录的页面
  if (!auth.isLoggedIn) {
    return next('/login')
  }

  // 检查角色权限
  const allowedRoles = to.meta.roles
  if (allowedRoles && !allowedRoles.includes(auth.role)) {
    // 无权访问 → 重定向到角色对应的首页
    if (auth.isAdmin) return next('/students')
    if (auth.isTeacher) return next('/schedule')
    return next('/enrollment')
  }

  next()
})

export default router
