import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const role = ref(localStorage.getItem('role') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const userName = ref(localStorage.getItem('userName') || '')

  const isLoggedIn = computed(() => !!role.value)
  const isAdmin = computed(() => role.value === 'admin')
  const isStudent = computed(() => role.value === 'student')
  const isTeacher = computed(() => role.value === 'teacher')

  function persist() {
    localStorage.setItem('role', role.value)
    localStorage.setItem('userId', userId.value)
    localStorage.setItem('userName', userName.value)
  }

  function login(r, id, name) {
    role.value = r
    userId.value = String(id || '')
    userName.value = name || ''
    persist()
  }

  function logout() {
    role.value = ''
    userId.value = ''
    userName.value = ''
    localStorage.removeItem('role')
    localStorage.removeItem('userId')
    localStorage.removeItem('userName')
  }

  return { role, userId, userName, isLoggedIn, isAdmin, isStudent, isTeacher, login, logout }
})
