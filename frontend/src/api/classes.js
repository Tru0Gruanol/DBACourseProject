import request from '@/utils/request'

export function getClasses() {
  return request.get('/classes')
}

export function getClassByCode(code) {
  return request.get(`/classes/${code}`)
}

export function getClassesBySubject(subjectId) {
  return request.get('/classes/by-subject', { params: { subjectId } })
}

export function addClass(data) {
  return request.post('/classes', data)
}