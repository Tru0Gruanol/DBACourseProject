<template>
  <div>
    <h2>教师管理</h2>
    <el-button type="primary" @click="openAddDialog">新增教师</el-button>
    <el-table :data="teachers" border style="margin-top:20px" v-loading="loading">
      <el-table-column prop="teacherId" label="教师工号" width="100" />
      <el-table-column prop="teacherName" label="姓名" width="120" />
      <el-table-column prop="teacherLevel" label="等级" width="120" />
      <el-table-column prop="specialty" label="特长" min-width="200" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.teacherId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="35%">
      <el-form :model="form" label-width="100px">
        <el-form-item label="教师工号">
          <el-input v-model="form.teacherId" :disabled="isEdit" placeholder="如：105" />
        </el-form-item>
        <el-form-item label="教师姓名">
          <el-input v-model="form.teacherName" placeholder="如：张明" />
        </el-form-item>
        <el-form-item label="教师等级">
          <el-select v-model="form.teacherLevel" placeholder="请选择等级" style="width:100%">
            <el-option label="金牌教师" value="金牌教师" />
            <el-option label="特级教师" value="特级教师" />
            <el-option label="高级教师" value="高级教师" />
            <el-option label="中级教师" value="中级教师" />
            <el-option label="初级教师" value="初级教师" />
          </el-select>
        </el-form-item>
        <el-form-item label="特长">
          <el-input v-model="form.specialty" placeholder="如：奥数,围棋（多个特长用逗号分隔）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTeacher">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTeachers, addTeacher, updateTeacher, deleteTeacher } from '@/api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'

const teachers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增教师')

const form = reactive({
  teacherId: '',
  teacherName: '',
  teacherLevel: '',
  specialty: '',
})

onMounted(async () => {
  await loadTeachers()
})

async function loadTeachers() {
  loading.value = true
  try {
    teachers.value = await getTeachers()
  } catch (e) {}
  loading.value = false
}

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '新增教师'
  Object.assign(form, { teacherId: '', teacherName: '', teacherLevel: '', specialty: '' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑教师'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function saveTeacher() {
  if (!form.teacherId || !form.teacherName) {
    ElMessage.warning('教师工号和姓名不能为空')
    return
  }
  try {
    if (isEdit.value) {
      await updateTeacher({ ...form, teacherId: Number(form.teacherId) })
      ElMessage.success('修改成功')
    } else {
      await addTeacher({ ...form, teacherId: Number(form.teacherId) })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadTeachers()
  } catch (e) {}
}

async function handleDelete(teacherId) {
  try {
    await ElMessageBox.confirm('删除教师前请确认该教师名下已无关联班级，确定删除吗？', '警告', { type: 'warning' })
    await deleteTeacher(teacherId)
    ElMessage.success('删除成功')
    await loadTeachers()
  } catch (error) {}
}
</script>
