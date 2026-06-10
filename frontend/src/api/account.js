import request from '@/utils/request'

export function getAllAccounts() {
  return request.get('/accounts')
}

export function payFee(data) {
  return request.post('/accounts/pay', data)
}

export function getInvoice(studentId, classCode) {
  return request.get('/accounts/invoice', { params: { studentId, classCode } })
}

export function getDebtors() {
  return request.get('/accounts/debtors')
}

export function refund(data) {
  return request.post('/accounts/refund', data)
}

export function getTotalPaid(studentId, classCode) {
  return request.get('/accounts/totalPaid', { params: { studentId, classCode } })
}

export function getStudentSummary(studentId) {
  return request.get('/accounts/student-summary', { params: { studentId } })
}