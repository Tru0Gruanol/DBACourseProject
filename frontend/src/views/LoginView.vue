<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-header">
        <el-icon size="36" color="#5b6abf"><School /></el-icon>
        <h1>托管培训中心信息管理系统</h1>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 账号登录：学生 / 教师通用，系统自动识别 -->
        <el-tab-pane label="账号登录" name="account">
          <el-form :model="accountForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="accountForm.id" placeholder="请输入学号 / 工号" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="accountForm.password" type="password" placeholder="请输入密码" show-password
                @keyup.enter="loginAsAccount" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="accountLoading" @click="loginAsAccount" style="width:100%">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 管理员登录 -->
        <el-tab-pane label="管理员登录" name="admin">
          <el-form :model="adminForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input v-model="adminForm.username" placeholder="请输入管理员用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="adminForm.password" type="password" placeholder="请输入管理员密码" show-password
                @keyup.enter="loginAsAdmin" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="adminLoading" @click="loginAsAdmin" style="width:100%">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { login as loginApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { School } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()

const activeTab = ref('account')

// ======== 账号登录（学生/教师自动识别） ========
const accountForm = reactive({ id: '', password: '' })
const accountLoading = ref(false)

async function loginAsAccount() {
  if (!accountForm.id || !accountForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  accountLoading.value = true
  try {
    const result = await loginApi({
      id: Number(accountForm.id),
      password: accountForm.password,
    })
    if (result.success) {
      auth.login(result.role, result.userId, result.userName)
      const label = result.role === 'student' ? '同学' : '老师'
      ElMessage.success(`欢迎回来，${result.userName}${label}！`)
      router.push(result.role === 'teacher' ? '/schedule' : '/enrollment')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error('登录失败，请检查网络连接')
  }
  accountLoading.value = false
}

// ======== 管理员登录 ========
const adminForm = reactive({ username: '', password: '' })
const adminLoading = ref(false)

function loginAsAdmin() {
  if (!adminForm.username || !adminForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (adminForm.username !== 'admin' || adminForm.password !== 'admin123') {
    ElMessage.error('用户名或密码错误')
    return
  }
  adminLoading.value = true
  auth.login('admin', 'admin', '系统管理员')
  ElMessage.success('欢迎回来，管理员！')
  router.push('/students')
  adminLoading.value = false
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8edf5 0%, #f0f2f5 50%, #eef1f8 100%);
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.login-header h1 {
  font-size: 20px;
  margin: 12px 0 4px;
  color: #1a1a1a;
}
.login-header p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}
.login-tabs {
  margin-bottom: 8px;
}
</style>
