import request from '@/utils/request'

export function getStudents() {
  return request.get('/students')
}

export function addStudent(data) {
  return request.post('/students', data)
}