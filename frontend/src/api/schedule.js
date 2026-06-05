import request from '@/utils/request'

export function getStudentSchedule(studentId) {
  return request.get(`/schedules/student/${studentId}`)
}

export function getTeacherSchedule(teacherId) {
  return request.get(`/schedules/teacher/${teacherId}`)
}