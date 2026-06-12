import request from '@/utils/request'

export function getTeachers() {
  return request.get('/teachers')
}

export function getTeacherById(id) {
  return request.get(`/teachers/${id}`)
}

// 根据科目ID精确查询能教该科目的所有教师
export function getTeachersBySubject(subjectId) {
  return request.get(`/teachers/by-subject/${subjectId}`)
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