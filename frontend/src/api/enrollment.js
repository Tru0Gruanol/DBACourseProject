import request from '@/utils/request'

export function submitEnrollment(data) {
  return request.post('/enrollments/submit', data)
}

export function cancelEnrollment(studentId, classCode) {
  return request.delete('/enrollments/cancel', { params: { studentId, classCode } })
}

export function checkEnrollment(studentId, classCode) {
  return request.get('/enrollments/check', { params: { studentId, classCode } })
}