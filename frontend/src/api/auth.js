import request from '@/utils/request'

/**
 * 统一登录（学生/教师/管理员）
 * @param {Object} data - { username: string, password: string }
 * @returns {Promise} { success: boolean, role: string, userId: string, userName: string, message: string }
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 修改密码（学生/教师）
 * @param {Object} data - { role: 'student'|'teacher', id: number, oldPassword: string, newPassword: string }
 * @returns {Promise} string 提示信息
 */
export function changePassword(data) {
  return request.put('/auth/change-password', data)
}
