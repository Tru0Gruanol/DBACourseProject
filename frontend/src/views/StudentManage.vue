<template>
  <div>
    <h2>学生管理</h2>
    <el-form :inline="true">
      <el-form-item label="学生ID">
        <el-input v-model="newStudent.studentId" />
      </el-form-item>
      <el-form-item label="学生姓名">
        <el-input v-model="newStudent.studentName" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleAddStudent">新增学生</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="students" border>
      <el-table-column prop="studentId" label="学生ID" />
      <el-table-column prop="studentName" label="姓名" />
      <el-table-column prop="registrationTime" label="注册时间" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudents, addStudent } from '@/api/student'
import { ElMessage } from 'element-plus'

const students = ref([])
const newStudent = ref({ studentId: '', studentName: '' })

onMounted(async () => {
  await loadStudents()
})

async function loadStudents() {
  students.value = await getStudents()
}

async function handleAddStudent() {
  if (!newStudent.value.studentId || !newStudent.value.studentName) {
    ElMessage.warning('请填写完整')
    return
  }
  await addStudent(newStudent.value)
  ElMessage.success('新增成功')
  newStudent.value = { studentId: '', studentName: '' }
  await loadStudents()
}
</script>