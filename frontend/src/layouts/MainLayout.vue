<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #304156">
      <el-menu
        router
        :default-active="$route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/enrollment">
          <el-icon><User /></el-icon>
          <span>学生报名</span>
        </el-menu-item>
        <el-menu-item index="/subjects">
          <el-icon><Notebook /></el-icon>
          <span>科目管理</span>
        </el-menu-item>
        <el-menu-item index="/classes">
          <el-icon><School /></el-icon>
          <span>班级管理</span>
        </el-menu-item>
        <el-menu-item index="/fee">
          <el-icon><Money /></el-icon>
          <span>收费管理</span>
        </el-menu-item>
        <el-menu-item index="/schedule">
          <el-icon><Calendar /></el-icon>
          <span>课表查询</span>
        </el-menu-item>
        <el-menu-item index="/students">
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="text-align: right; font-size: 12px">
        <span>托管培训中心管理系统</span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { User, Notebook, School, Money, Calendar } from '@element-plus/icons-vue'
</script>

<style scoped>
.el-header {
  background-color: #f5f7fa;
  line-height: 60px;
  border-bottom: 1px solid #e6e6e6;
}
</style>