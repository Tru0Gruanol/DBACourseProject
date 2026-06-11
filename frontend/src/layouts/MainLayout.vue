<template>
  <!-- 未登录：只渲染登录页，无侧边栏无顶栏 -->
  <template v-if="!auth.isLoggedIn">
    <router-view />
  </template>

  <!-- 已登录：完整布局 -->
  <el-container v-else style="height: 100vh">
    <SidebarNav />

    <el-container>
      <el-header style="background:#fff;display:flex;align-items:center;justify-content:space-between;padding:0 32px;border-bottom:1px solid #eeeef2;height:48px">
        <span style="font-size:13px;color:#8c8c9a">托管培训中心信息管理系统</span>
        <span style="font-size:12px;color:#5b6abf">
          <el-icon style="margin-right:2px;vertical-align:middle"><User /></el-icon>
          欢迎，{{ auth.userName || auth.userId }}
        </span>
      </el-header>

      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
import SidebarNav from '@/components/SidebarNav.vue'
import { User } from '@element-plus/icons-vue'

const auth = useAuthStore()
</script>

<style scoped>
.el-header {
  line-height: 48px;
}
</style>
