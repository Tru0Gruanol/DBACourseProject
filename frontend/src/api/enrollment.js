import request from '@/utils/request'

export function submitEnrollment(data) {
  return request.post('/enrollments/submit', data)
}

export function cancelEnrollment(studentId, classCode) {
  return request.delete('/enrollments/cancel', { params: { studentId, classCode } })
}

// 学生申请退课（进入待审批）
export function requestCancelEnrollment(studentId, classCode) {
  return request.post('/enrollments/request-cancel', null, { params: { studentId, classCode } })
}

export function checkEnrollment(studentId, classCode) {
  return request.get('/enrollments/check', { params: { studentId, classCode } })
}

export function getStudentsByClassCode(classCode) {
  return request.get('/enrollments/students', { params: { classCode } })
}

export function getPendingCancels() {
  return request.get('/enrollments/pending-cancels')
}

export function approveCancel(studentId, classCode) {
  return request.put('/enrollments/approve-cancel', null, { params: { studentId, classCode } })
}

export function rejectCancel(studentId, classCode) {
  return request.put('/enrollments/reject-cancel', null, { params: { studentId, classCode } })
}