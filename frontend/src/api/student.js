import request from '@/utils/request'

export function getStudents() {
  return request.get('/students')
}

export function getStudentById(id) {
  return request.get(`/students/${id}`)
}

export function addStudent(data) {
  return request.post('/students', data)
}

export function updateStudent(data) {
  return request.put('/students', data)
}

export function deleteStudent(id) {
  return request.delete(`/students/${id}`)
}