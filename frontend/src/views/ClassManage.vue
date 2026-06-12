<template>
  <div>
    <div class="page-header">
      <el-icon><School /></el-icon>
      <h2>班级管理</h2>
    </div>
    <el-card shadow="hover">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:10px">
        <div style="display:flex;align-items:center;gap:10px">
          <el-button type="primary" @click="openAddDialog">新增班级</el-button>
          <span style="font-size:13px;color:#909399">共 {{ filteredClasses.length }} 个班级</span>
        </div>
        <div style="display:flex;align-items:center;gap:8px">
          <el-select v-model="filterSubjectId" placeholder="按科目筛选" clearable style="width:150px">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
          <el-select v-model="filterTerm" placeholder="按期次筛选" clearable style="width:150px">
            <el-option v-for="t in termList" :key="t.name" :label="t.name" :value="t.name" />
          </el-select>
        </div>
      </div>

    <el-table :data="filteredClasses" stripe border v-loading="loading">
      <el-table-column prop="classCode" label="班级代号" width="140" />
      <el-table-column label="科目" width="100">
        <template #default="{ row }">
          {{ getSubjectName(row.subjectId) }}
        </template>
      </el-table-column>
      <el-table-column label="教师" width="100">
        <template #default="{ row }">
          {{ getTeacherName(row.teacherId) }}
        </template>
      </el-table-column>
      <el-table-column prop="term" label="期次" width="120" />
      <el-table-column prop="period" label="上课时间" width="180" />
      <el-table-column label="学费" width="100">
        <template #default="{ row }">¥{{ (row.fee || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="location" label="教室" width="120" />
      <el-table-column label="名额" width="120">
        <template #default="{ row }">
          <span :style="{ color: row.enrolledCount >= row.capacity ? '#F56C6C' : '' }">
            {{ row.enrolledCount }} / {{ row.capacity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="教师报酬" width="100">
        <template #default="{ row }">¥{{ (row.teacherRemuneration || 0).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.classCode)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="45%">
      <el-form :model="form" label-width="110px">
        <el-form-item label="班级代号">
          <el-input v-model="form.classCode" :disabled="isEdit" placeholder="如：MATH-2026-04" />
        </el-form-item>
        <el-form-item label="所属科目">
          <el-select v-model="form.subjectId" placeholder="请选择科目" style="width:100%" @change="onSubjectChangeForTeacher">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="任课教师">
          <el-select v-model="form.teacherId" placeholder="请选择教师" style="width:100%">
            <el-option v-for="t in filteredTeachers" :key="t.teacherId" :label="`${t.teacherName}（${t.teacherLevel}）`" :value="t.teacherId" />
          </el-select>
        </el-form-item>
        <el-form-item label="期次">
          <el-select v-model="form.term" placeholder="请选择学期" style="width:100%" allow-create filterable>
            <el-option v-for="t in termList" :key="t.name" :label="t.name" :value="t.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input v-model="form.period" placeholder="如：每周六 09:00-11:00" />
        </el-form-item>
        <el-form-item label="学费">
          <el-input-number v-model="form.fee" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="教室">
          <el-select v-model="form.location" placeholder="请选择教室" style="width:100%" allow-create filterable>
            <el-option v-for="c in classroomList" :key="c.name" :label="c.name" :value="c.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="招收人数">
          <el-input-number v-model="form.capacity" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="已报名人数">
          <el-input-number v-model="form.enrolledCount" :min="0" :disabled="isEdit" style="width:100%" />
          <span v-if="isEdit" style="color:#909399;font-size:12px;margin-left:8px">（由系统自动维护，不可手动修改）</span>
        </el-form-item>
        <el-form-item label="教师报酬">
          <el-input-number v-model="form.teacherRemuneration" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveClass">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getClasses, addClass, updateClass, deleteClass } from '@/api/classes'
import { getSubjects } from '@/api/subject'
import { getTeachers, getTeachersBySubject } from '@/api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'
import { School } from '@element-plus/icons-vue'

const classes = ref([])
const subjects = ref([])
const teachers = ref([])
const filteredTeachers = ref([])
const filterSubjectId = ref(null)
const filterTerm = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('新增班级')

const form = reactive({
  classCode: '',
  subjectId: null,
  teacherId: null,
  term: '',
  period: '',
  fee: 0,
  location: '',
  capacity: 30,
  enrolledCount: 0,
  teacherRemuneration: 0,
})

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  try {
    const [clsList, subList, teaList] = await Promise.all([
      getClasses(), getSubjects(), getTeachers(),
    ])
    classes.value = clsList
    subjects.value = subList
    teachers.value = teaList
    filteredTeachers.value = teaList
  } catch (e) {}
  loading.value = false
}

// 从现有班级数据提取 distinct 值，无需额外表
const classroomList = computed(() => {
  const names = [...new Set(classes.value.map(c => c.location).filter(Boolean))]
  return names.map(n => ({ name: n }))
})
const termList = computed(() => {
  const names = [...new Set(classes.value.map(c => c.term).filter(Boolean))]
  return names.map(n => ({ name: n }))
})

const filteredClasses = computed(() => {
  let list = classes.value
  if (filterSubjectId.value) list = list.filter(c => c.subjectId === filterSubjectId.value)
  if (filterTerm.value) list = list.filter(c => c.term === filterTerm.value)
  return list
})

async function onSubjectChangeForTeacher(subjectId) {
  if (!subjectId) {
    filteredTeachers.value = teachers.value
    form.teacherId = null
    return
  }
  try {
    filteredTeachers.value = await getTeachersBySubject(subjectId)
  } catch (e) {
    filteredTeachers.value = teachers.value
  }
  form.teacherId = null
}

function getSubjectName(subjectId) {
  const s = subjects.value.find(s => s.subjectId === subjectId)
  return s ? s.subjectName : subjectId
}

function getTeacherName(teacherId) {
  const t = teachers.value.find(t => t.teacherId === teacherId)
  return t ? t.teacherName : teacherId
}

function openAddDialog() {
  isEdit.value = false
  dialogTitle.value = '新增班级'
  Object.assign(form, {
    classCode: '',
    subjectId: null,
    teacherId: null,
    term: '',
    period: '',
    fee: 0,
    location: '',
    capacity: 30,
    enrolledCount: 0,
    teacherRemuneration: 0,
  })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  dialogTitle.value = '编辑班级: ' + row.classCode
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

async function saveClass() {
  if (!form.classCode || !form.subjectId || !form.teacherId || !form.term) {
    ElMessage.warning('请填写班级代号、科目、教师和期次')
    return
  }
  try {
    if (isEdit.value) {
      await updateClass({ ...form })
      ElMessage.success('更新成功')
    } else {
      await addClass({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {}
}

async function handleDelete(classCode) {
  try {
    await ElMessageBox.confirm('确定要删除班级 ' + classCode + ' 吗？', '警告', { type: 'warning' })
    await deleteClass(classCode)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {}
}
</script>
