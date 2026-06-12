import request from '@/utils/request'

export function getNotifications(userId, role) {
  return request.get(`/notifications?userId=${userId}&role=${role}`)
}
export function getUnreadCount(userId, role) {
  return request.get(`/notifications/unread-count?userId=${userId}&role=${role}`)
}
export function markRead(id) {
  return request.put(`/notifications/${id}/read`)
}
export function markAllRead(userId, role) {
  return request.put(`/notifications/read-all?userId=${userId}&role=${role}`)
}
