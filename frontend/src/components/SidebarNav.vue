<template>
  <el-aside width="220px" style="background:#fff;border-right:1px solid #eeeef2;overflow:hidden;display:flex;flex-direction:column">
    <div style="padding:20px 20px 16px;display:flex;align-items:center;gap:10px">
      <el-icon size="22" color="#5b6abf"><School /></el-icon>
      <span style="font-size:15px;font-weight:600;color:#1a1a1a">培训中心管理</span>
    </div>

    <el-menu
      router
      :default-active="route.path"
      background-color="#fff"
      text-color="#4a4a5a"
      active-text-color="#5b6abf"
      style="border-right:none;padding:0 12px;flex:1"
    >
      <el-menu-item v-if="visibleMenu('enrollment')" index="/enrollment">
        <el-icon><EditPen /></el-icon>
        <span>学生报名</span>
      </el-menu-item>
      <el-menu-item v-if="visibleMenu('myCourses')" index="/my-courses">
        <el-icon><Notebook /></el-icon>
        <span>已选课程</span>
      </el-menu-item>
      <el-menu-item v-if="visibleMenu('subjects')" index="/subjects">
        <el-icon><Notebook /></el-icon>
        <span>科目管理</span>
      </el-menu-item>
      <el-menu-item v-if="visibleMenu('classes')" index="/classes">
        <el-icon><School /></el-icon>
        <span>班级管理</span>
      </el-menu-item>
      <el-menu-item v-if="visibleMenu('teachers')" index="/teachers">
        <el-icon><Avatar /></el-icon>
        <span>教师管理</span>
      </el-menu-item>
      <el-menu-item v-if="visibleMenu('students')" index="/students">
        <el-icon><UserFilled /></el-icon>
        <span>学生管理</span>
      </el-menu-item>
      <el-menu-item v-if="visibleMenu('fee')" index="/fee">
        <el-icon><Money /></el-icon>
        <span>收费管理</span>
      </el-menu-item>
      <el-menu-item v-if="visibleMenu('schedule')" index="/schedule">
        <el-icon><Calendar /></el-icon>
        <span>课表查询</span>
      </el-menu-item>
    </el-menu>

    <!-- 底部用户信息区 -->
    <div style="padding:12px 16px;border-top:1px solid #eeeef2">
      <!-- 学生/教师：通知铃铛 -->
      <div v-if="auth.isStudent || auth.isTeacher" style="margin-bottom:8px;display:flex;align-items:center;gap:8px">
        <el-popover placement="right-start" :width="320" trigger="click" @show="loadNotifications">
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount===0" :max="99">
              <el-button size="small" text style="padding:2px">
                <el-icon size="18"><Bell /></el-icon>
              </el-button>
            </el-badge>
          </template>
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
            <b style="font-size:14px">通知</b>
            <el-button size="small" text type="primary" @click.stop="readAll" v-if="unreadCount>0">全部已读</el-button>
          </div>
          <div v-if="notifications.length===0" style="color:#909399;font-size:13px;text-align:center;padding:20px 0">暂无通知</div>
          <div v-for="n in notifications" :key="n.id"
            style="padding:8px 0;border-bottom:1px solid #f2f2f6;cursor:pointer"
            :style="{ opacity: n.isRead ? 0.5 : 1 }"
            @click="readOne(n)">
            <div style="font-size:13px;font-weight:500">{{ n.title }}</div>
            <div style="font-size:12px;color:#909399;margin-top:2px">{{ n.content }}</div>
            <div style="font-size:11px;color:#c0c4cc;margin-top:2px">{{ formatTime(n.createdAt) }}</div>
          </div>
        </el-popover>
        <div>
          <div style="font-size:12px;color:#bbb">{{ auth.isStudent ? '学号' : '教师号' }} {{ auth.userId }}</div>
          <div style="font-size:12px;color:#606266">{{ auth.userName }}</div>
        </div>
      </div>

      <!-- 管理员：铃铛 + 用户名 -->
      <div v-if="auth.isAdmin" style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px">
        <div style="display:flex;align-items:center;gap:8px">
          <el-popover placement="right-start" :width="320" trigger="click" @show="loadNotifications">
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount===0" :max="99">
                <el-button size="small" text style="padding:2px"><el-icon size="18"><Bell /></el-icon></el-button>
              </el-badge>
            </template>
            <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:10px">
              <b style="font-size:14px">通知</b>
              <el-button size="small" text type="primary" @click.stop="readAll" v-if="unreadCount>0">全部已读</el-button>
            </div>
            <div v-if="notifications.length===0" style="color:#909399;font-size:13px;text-align:center;padding:20px 0">暂无通知</div>
            <div v-for="n in notifications" :key="n.id"
              style="padding:8px 0;border-bottom:1px solid #f2f2f6;cursor:pointer"
              :style="{ opacity: n.isRead ? 0.5 : 1 }"
              @click="readOne(n)">
              <div style="font-size:13px;font-weight:500">{{ n.title }}</div>
              <div style="font-size:12px;color:#909399;margin-top:2px">{{ n.content }}</div>
              <div style="font-size:11px;color:#c0c4cc;margin-top:2px">{{ formatTime(n.createdAt) }}</div>
            </div>
          </el-popover>
          <span style="font-size:13px;color:#606266">🔧 管理员</span>
        </div>
        <el-button size="small" text type="danger" @click="handleLogout" style="padding:0 4px">退出</el-button>
      </div>

      <!-- 学生/教师：退出 + 改密 -->
      <div v-if="auth.isStudent || auth.isTeacher" style="display:flex;align-items:center;gap:8px;margin-top:4px">
        <el-button size="small" text type="danger" @click="handleLogout" style="padding:0 4px">退出</el-button>
        <el-button size="small" text @click="openChangePasswordDialog" style="font-size:12px;color:#909399;padding:0">
          <el-icon style="margin-right:2px"><Lock /></el-icon>
          修改密码
        </el-button>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="cpDialogVisible" title="修改密码" width="380px" :close-on-click-modal="false">
      <el-form :model="cpForm" label-width="80px">
        <el-form-item label="当前密码">
          <el-input v-model="cpForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="cpForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="cpForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"
            @keyup.enter="submitChangePassword" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cpDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="cpLoading" @click="submitChangePassword">确认修改</el-button>
      </template>
    </el-dialog>
  </el-aside>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { changePassword as changePwdApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { School, EditPen, Notebook, Avatar, UserFilled, Money, Calendar, Lock, Bell } from '@element-plus/icons-vue'
import { getNotifications, getUnreadCount, markRead, markAllRead } from '@/api/notification'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const adminMenu = ['enrollment', 'subjects', 'classes', 'teachers', 'students', 'fee', 'schedule']
const studentMenu = ['enrollment', 'myCourses', 'schedule']
const teacherMenu = ['schedule']

const menuMap = { admin: adminMenu, student: studentMenu, teacher: teacherMenu }

function visibleMenu(key) {
  const allowed = menuMap[auth.role]
  return allowed && allowed.includes(key)
}

const roleLabel = computed(() => {
  const map = { admin: '🔧 管理员', student: '🧑 学生', teacher: '👨‍🏫 教师' }
  return map[auth.role] || ''
})

function handleLogout() {
  auth.logout()
  router.push('/login')
}

// ======== 通知 ========
const notifications = ref([])
const unreadCount = ref(0)

function _uid() { return auth.isAdmin ? 0 : Number(auth.userId) }

async function loadNotifications() {
  try {
    notifications.value = await getNotifications(_uid(), auth.role)
    unreadCount.value = notifications.value.filter(n => !n.isRead).length
  } catch (e) {}
}

async function readOne(n) {
  if (!n.isRead) {
    try { await markRead(n.id); n.isRead = 1; unreadCount.value-- } catch (e) {}
  }
}

async function readAll() {
  const uid = _uid()
  if (uid == null || isNaN(uid)) return
  try {
    await markAllRead(uid, auth.role)
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
  } catch (e) {}
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

// 定时刷新未读数
import { onMounted, onUnmounted } from 'vue'
let unreadTimer = null
onMounted(async () => {
  const uid = auth.isAdmin ? 0 : Number(auth.userId)
  try { const r = await getUnreadCount(uid, auth.role); unreadCount.value = r.count || 0 } catch (e) {}
  unreadTimer = setInterval(async () => {
    try { const r = await getUnreadCount(uid, auth.role); unreadCount.value = r.count || 0 } catch (e) {}
  }, 30000)
})
onUnmounted(() => { if (unreadTimer) clearInterval(unreadTimer) })

// ======== 修改密码 ========
const cpDialogVisible = ref(false)
const cpLoading = ref(false)
const cpForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

function openChangePasswordDialog() {
  cpForm.oldPassword = ''
  cpForm.newPassword = ''
  cpForm.confirmPassword = ''
  cpDialogVisible.value = true
}

async function submitChangePassword() {
  if (!cpForm.oldPassword || !cpForm.newPassword) {
    ElMessage.warning('请填写当前密码和新密码')
    return
  }
  if (cpForm.newPassword !== cpForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  cpLoading.value = true
  try {
    const result = await changePwdApi({
      role: auth.role,
      id: Number(auth.userId),
      oldPassword: cpForm.oldPassword,
      newPassword: cpForm.newPassword,
    })
    ElMessage.success(result)
    cpDialogVisible.value = false
  } catch (e) {}
  cpLoading.value = false
}
</script>

<style scoped>
.el-menu-item {
  height: 40px;
  line-height: 40px;
  margin: 1px 0;
  border-radius: 4px;
  font-size: 14px;
}
.el-menu-item:hover {
  background: #f5f5fa !important;
}
.el-menu-item.is-active {
  background: #edf0fc !important;
  font-weight: 500;
}
</style>
