<template>
  <div>
    <h2>班级管理</h2>
    <el-button type="primary" @click="openAddDialog">新增班级</el-button>

    <el-table :data="classes" border style="margin-top:20px" v-loading="loading">
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
      <el-table-column prop="fee" label="学费" width="100" />
      <el-table-column prop="location" label="教室" width="120" />
      <el-table-column label="名额" width="120">
        <template #default="{ row }">
          {{ row.enrolledCount }} / {{ row.capacity }}
        </template>
      </el-table-column>
      <el-table-column prop="teacherRemuneration" label="教师报酬" width="100" />
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增班级" width="45%">
      <el-form :model="form" label-width="110px">
        <el-form-item label="班级代号">
          <el-input v-model="form.classCode" placeholder="如：MATH-2026-04" />
        </el-form-item>
        <el-form-item label="所属科目">
          <el-select v-model="form.subjectId" placeholder="请选择科目" style="width:100%">
            <el-option v-for="s in subjects" :key="s.subjectId" :label="s.subjectName" :value="s.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="任课教师">
          <el-select v-model="form.teacherId" placeholder="请选择教师" style="width:100%">
            <el-option v-for="t in teachers" :key="t.teacherId" :label="`${t.teacherName}（${t.teacherLevel}）`" :value="t.teacherId" />
          </el-select>
        </el-form-item>
        <el-form-item label="期次">
          <el-input v-model="form.term" placeholder="如：2026暑假二期" />
        </el-form-item>
        <el-form-item label="上课时间">
          <el-input v-model="form.period" placeholder="如：每周六 09:00-11:00" />
        </el-form-item>
        <el-form-item label="学费">
          <el-input-number v-model="form.fee" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item label="教室">
          <el-input v-model="form.location" placeholder="如：A栋101" />
        </el-form-item>
        <el-form-item label="招收人数">
          <el-input-number v-model="form.capacity" :min="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="已报名人数">
          <el-input-number v-model="form.enrolledCount" :min="0" style="width:100%" />
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
import { ref, reactive, onMounted } from 'vue'
import { getClasses, addClass } from '@/api/classes'
import { getSubjects } from '@/api/subject'
import { getTeachers } from '@/api/teacher'
import { ElMessage } from 'element-plus'

const classes = ref([])
const subjects = ref([])
const teachers = ref([])
const loading = ref(false)
const dialogVisible = ref(false)

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
      getClasses(),
      getSubjects(),
      getTeachers(),
    ])
    classes.value = clsList
    subjects.value = subList
    teachers.value = teaList
  } catch (e) {}
  loading.value = false
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

async function saveClass() {
  if (!form.classCode || !form.subjectId || !form.teacherId || !form.term) {
    ElMessage.warning('请填写班级代号、科目、教师和期次')
    return
  }
  try {
    const result = await addClass({ ...form })
    ElMessage.success(result)
    dialogVisible.value = false
    await loadData()
  } catch (e) {}
}
</script>