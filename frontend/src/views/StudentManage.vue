<template>
  <div>
    <h2>学生管理</h2>
    <el-button type="primary" @click="openAddDialog">新增学生</el-button>
    <el-table :data="students" border style="margin-top:20px" v-loading="loading">
      <el-table-column prop="studentId" label="学生ID" width="100" />
      <el-table-column prop="studentName" label="姓名" width="150" />
      <el-table-column label="注册时间" width="200">
        <template #default="{ row }">
          {{ formatDate(row.registrationTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.studentId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学生ID">
          <el-input v-model="form.studentId" :disabled="isEdit" placeholder="如：1006" />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model="form.studentName" placeholder="如：张小明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStudent">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStudents, addStudent, updateStudent, deleteStudent } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'

const students = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增学生')

const form = reactive({
  studentId: '',
  studentName: '',
})

onMounted(async () => {
  await loadStudents()
})

async function loadStudents() {
  loading.value = true
  try {
    students.value = await getStudents()
  } catch (e) {}
  loading.value = false
}

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '新增学生'
  Object.assign(form, { studentId: '', studentName: '' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑学生: ' + row.studentName
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function saveStudent() {
  if (!form.studentId || !form.studentName) {
    ElMessage.warning('请填写学生ID和姓名')
    return
  }
  try {
    if (isEdit.value) {
      await updateStudent({ ...form, studentId: Number(form.studentId) })
      ElMessage.success('修改成功')
    } else {
      await addStudent({ ...form, studentId: Number(form.studentId) })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadStudents()
  } catch (e) {}
}

async function handleDelete(studentId) {
  try {
    await ElMessageBox.confirm('确定要删除学生 ' + studentId + ' 吗？', '警告', { type: 'warning' })
    await deleteStudent(studentId)
    ElMessage.success('删除成功')
    await loadStudents()
  } catch (error) {}
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}`
}
</script>
