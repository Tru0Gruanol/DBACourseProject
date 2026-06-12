<template>
  <div>
    <div class="page-header">
      <el-icon><Avatar /></el-icon>
      <h2>教师管理</h2>
    </div>
    <el-card shadow="hover">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
        <div style="display:flex;align-items:center;gap:12px">
          <el-button type="primary" @click="openAddDialog">新增教师</el-button>
          <span style="font-size:13px;color:#909399">共 {{ filteredTeachers.length }} 位教师</span>
        </div>
        <el-input v-model="search" placeholder="输入教师工号或任教科目..." clearable style="width:240px" :prefix-icon="Search" />
      </div>
      <el-table :data="filteredTeachers" stripe border v-loading="loading">
        <el-table-column prop="teacherId" label="教师工号" width="100" />
        <el-table-column prop="teacherName" label="姓名" width="120" />
        <el-table-column prop="teacherLevel" label="等级" width="120" />
        <el-table-column label="任教科目" min-width="180">
          <template #default="{ row }">
            <template v-if="row.subjectIds && row.subjectIds.length">
              <el-tag v-for="sid in row.subjectIds" :key="sid" size="small" style="margin-right:4px;margin-bottom:2px">
                {{ getSubjectName(sid) }}
              </el-tag>
            </template>
            <span v-else style="color:#c0c4cc">—</span>
          </template>
        </el-table-column>
        <el-table-column label="特长" min-width="200">
          <template #default="{ row }">
            {{ row.specialty || '全能老师' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.teacherId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !filteredTeachers.length" description="暂无教师数据" />
    </el-card>

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
        <el-form-item label="任教科目">
          <el-select v-model="form.subjectIds" multiple placeholder="请选择任教科目（可多选）" style="width:100%">
            <el-option v-for="s in allSubjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="特长">
          <el-input v-model="form.specialty" placeholder="留空则自动设为「全能老师」" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { getTeachers, addTeacher, updateTeacher, deleteTeacher } from '@/api/teacher'
import { getSubjects } from '@/api/subject'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, Search } from '@element-plus/icons-vue'

const teachers = ref([])
const allSubjects = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增教师')
const search = ref('')

const form = reactive({
  teacherId: '',
  teacherName: '',
  teacherLevel: '',
  specialty: '',
  subjectIds: [],
})

onMounted(async () => {
  await Promise.all([loadTeachers(), loadAllSubjects()])
})

async function loadTeachers() {
  loading.value = true
  try { teachers.value = await getTeachers() } catch (e) {}
  loading.value = false
}

async function loadAllSubjects() {
  try { allSubjects.value = await getSubjects() } catch (e) {}
}

const filteredTeachers = computed(() => {
  if (!search.value) return teachers.value
  const kw = search.value.toLowerCase()
  return teachers.value.filter(t => {
    if (String(t.teacherId).includes(kw)) return true
    if (t.subjectIds && t.subjectIds.length) {
      return t.subjectIds.some(sid => {
        const s = allSubjects.value.find(s => s.subjectId === sid)
        return s && s.subjectName.toLowerCase().includes(kw)
      })
    }
    return false
  })
})

function getSubjectName(id) {
  const s = allSubjects.value.find(s => s.subjectId === id)
  return s ? s.subjectName : String(id)
}

function getSubjectNames(subjectIds) {
  if (!subjectIds || subjectIds.length === 0) return '—'
  return subjectIds
    .map(id => {
      const s = allSubjects.value.find(s => s.subjectId === id)
      return s ? s.subjectName : id
    })
    .join('、')
}

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '新增教师'
  Object.assign(form, { teacherId: '', teacherName: '', teacherLevel: '', specialty: '', subjectIds: [] })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑教师'
  Object.assign(form, {
    teacherId: row.teacherId,
    teacherName: row.teacherName,
    teacherLevel: row.teacherLevel,
    specialty: row.specialty || '',
    subjectIds: row.subjectIds ? [...row.subjectIds] : [],
  })
  dialogVisible.value = true
}

async function saveTeacher() {
  if (!form.teacherId || !form.teacherName || !form.teacherLevel) {
    ElMessage.warning('教师工号、姓名和等级不能为空')
    return
  }
  if (!form.subjectIds || form.subjectIds.length === 0) {
    ElMessage.warning('请至少选择一个任教科目')
    return
  }
  try {
    const payload = {
      teacherId: Number(form.teacherId),
      teacherName: form.teacherName,
      teacherLevel: form.teacherLevel,
      specialty: form.specialty || '全能老师',
      subjectIds: form.subjectIds,
    }
    if (isEdit.value) {
      await updateTeacher(payload)
      ElMessage.success('修改成功')
    } else {
      await addTeacher(payload)
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
