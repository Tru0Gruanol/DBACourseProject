<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="login-header">
        <el-icon size="36" color="#5b6abf"><School /></el-icon>
        <h1>托管培训中心信息管理系统</h1>
      </div>

      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入学号 / 工号 / 管理员名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password
            @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width:100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
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

const form = reactive({ username: '', password: '' })
const loading = ref(false)

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const result = await loginApi({
      username: form.username,
      password: form.password,
    })
    if (result.success) {
      auth.login(result.role, result.userId, result.userName)
      const greetings = {
        student: '同学',
        teacher: '老师',
        admin: '',
      }
      const label = greetings[result.role] || ''
      ElMessage.success(`欢迎回来，${result.userName}${label}！`)

      // 按角色跳转首页
      const homeMap = {
        admin: '/students',
        teacher: '/schedule',
        student: '/enrollment',
      }
      router.push(homeMap[result.role] || '/enrollment')
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (e) {
    ElMessage.error('登录失败，请检查网络连接')
  }
  loading.value = false
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #eef0f8 0%, #f4f5f9 40%, #f0f2f7 100%);
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 44px 40px;
  box-shadow: 0 4px 32px rgba(0, 0, 0, 0.08), 0 1px 4px rgba(0, 0, 0, 0.04);
}
.login-header {
  text-align: center;
  margin-bottom: 28px;
}
.login-header h1 {
  font-size: 20px;
  margin: 14px 0 4px;
  color: #1b1c22;
  font-weight: 600;
}
.login-header p {
  font-size: 13px;
  color: #8c8d96;
  margin: 0;
}
</style>
