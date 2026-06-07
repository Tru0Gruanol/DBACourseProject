import { createRouter, createWebHistory } from 'vue-router'
import StudentEnrollment from '@/views/StudentEnrollment.vue'
import SubjectManage from '@/views/SubjectManage.vue'
import ClassManage from '@/views/ClassManage.vue'
import TeacherManage from '@/views/TeacherManage.vue'
import FeeManage from '@/views/FeeManage.vue'
import ScheduleQuery from '@/views/ScheduleQuery.vue'
import StudentManage from '@/views/StudentManage.vue'

const routes = [
  { path: '/', redirect: '/enrollment' },
  { path: '/enrollment', component: StudentEnrollment },
  { path: '/subjects', component: SubjectManage },
  { path: '/classes', component: ClassManage },
  { path: '/teachers', component: TeacherManage },
  { path: '/fee', component: FeeManage },
  { path: '/schedule', component: ScheduleQuery },
  { path: '/students', component: StudentManage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router