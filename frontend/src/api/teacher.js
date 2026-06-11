import request from '@/utils/request'

export function getTeachers() {
  return request.get('/teachers')
}

export function getTeacherById(id) {
  return request.get(`/teachers/${id}`)
}

export function getTeachersBySpecialty(keyword) {
  return request.get('/teachers/by-specialty', { params: { keyword } })
}

export function addTeacher(data) {
  return request.post('/teachers', data)
}

export function updateTeacher(data) {
  return request.put('/teachers', data)
}

export function deleteTeacher(id) {
  return request.delete(`/teachers/${id}`)
}

// 教师薪酬汇总（管理员端）
export function getTeacherSalaries() {
  return request.get('/teachers/salaries')
}