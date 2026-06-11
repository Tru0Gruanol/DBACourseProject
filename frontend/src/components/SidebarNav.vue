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
      <!-- 学生/教师显示自己的ID号 -->
      <div v-if="auth.userId && auth.userId !== 'admin'" style="margin-bottom:8px;display:flex;align-items:center;gap:6px">
        <span style="font-size:11px;color:#bbb">{{ auth.isStudent ? '学号' : '教师号' }}</span>
        <span style="font-size:14px;font-weight:600;color:#5b6abf">{{ auth.userId }}</span>
      </div>

      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px">
        <span style="font-size:13px;color:#606266">
          {{ roleLabel }}
          <template v-if="auth.userName && auth.userName !== '系统管理员'">
            &nbsp;{{ auth.userName }}
          </template>
        </span>
        <el-button size="small" text type="danger" @click="handleLogout" style="padding:0 4px">退出</el-button>
      </div>

      <!-- 学生/教师：修改密码入口 -->
      <el-button
        v-if="auth.isStudent || auth.isTeacher"
        size="small"
        text
        @click="openChangePasswordDialog"
        style="font-size:12px;color:#909399;padding:0"
      >
        <el-icon style="margin-right:2px"><Lock /></el-icon>
        修改密码
      </el-button>
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
import { School, EditPen, Notebook, Avatar, UserFilled, Money, Calendar, Lock } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const adminMenu = ['enrollment', 'subjects', 'classes', 'teachers', 'students', 'fee', 'schedule']
const studentMenu = ['enrollment', 'schedule']
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
