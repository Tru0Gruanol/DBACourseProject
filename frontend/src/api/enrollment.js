import request from '@/utils/request'

export function submitEnrollment(data) {
  return request.post('/enrollments/submit', data)
}