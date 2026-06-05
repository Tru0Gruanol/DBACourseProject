import request from '@/utils/request'

export function getTeachers() {
  return request.get('/teachers')
}