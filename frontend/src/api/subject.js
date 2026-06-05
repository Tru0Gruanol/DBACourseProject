import request from '@/utils/request'

export function getSubjects() {
  return request.get('/subjects')
}

export function getSubjectById(id) {
  return request.get(`/subjects/${id}`)
}

export function addSubject(data) {
  return request.post('/subjects', data)
}

export function updateSubject(data) {
  return request.put('/subjects', data)
}

export function deleteSubject(id) {
  return request.delete(`/subjects/${id}`)
}