<template>
  <div>
    <h2>科目管理</h2>
    <el-button type="primary" @click="openAddDialog">新增科目</el-button>
    <el-table :data="subjects" border style="margin-top:20px">
      <el-table-column prop="subjectId" label="科目编号" />
      <el-table-column prop="subjectName" label="科目名称" />
      <el-table-column prop="hours" label="课时数" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.subjectId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form :model="currentSubject" label-width="100px">
        <el-form-item label="科目编号">
          <el-input v-model="currentSubject.subjectId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="科目名称">
          <el-input v-model="currentSubject.subjectName" />
        </el-form-item>
        <el-form-item label="课时数">
          <el-input-number v-model="currentSubject.hours" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSubject">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSubjects, addSubject, updateSubject, deleteSubject } from '@/api/subject'
import { ElMessage, ElMessageBox } from 'element-plus'

const subjects = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentSubject = ref({ subjectId: '', subjectName: '', hours: 0 })

onMounted(async () => {
  loadSubjects()
})

async function loadSubjects() {
  subjects.value = await getSubjects()
}

function openAddDialog() {
  isEdit.value = false
  currentSubject.value = { subjectId: '', subjectName: '', hours: 0 }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  currentSubject.value = { ...row }
  dialogVisible.value = true
}

async function saveSubject() {
  if (!currentSubject.value.subjectId || !currentSubject.value.subjectName) {
    ElMessage.warning('科目编号和名称不能为空')
    return
  }
  try {
    if (isEdit.value) {
      await updateSubject(currentSubject.value)
      ElMessage.success('修改成功')
    } else {
      await addSubject(currentSubject.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadSubjects()
  } catch (e) {
    // 错误消息已由 request.js 拦截器统一展示，此处仅阻止后续代码执行
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('删除科目会导致关联班级无法使用，确定删除吗？', '警告', { type: 'warning' })
    await deleteSubject(id)
    ElMessage.success('删除成功')
    loadSubjects()
  } catch (error) {}
}
</script>