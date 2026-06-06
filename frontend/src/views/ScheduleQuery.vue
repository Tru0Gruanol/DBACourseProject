<template>
  <div>
    <h2>课表查询</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 学生课表 -->
      <el-tab-pane label="学生课表" name="student">
        <el-form :inline="true">
          <el-form-item label="学生ID">
            <el-input v-model="studentId" placeholder="请输入学生ID" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="queryStudentSchedule">查询</el-button>
          </el-form-item>
        </el-form>

        <el-table v-if="studentSchedule.length" :data="studentSchedule" border v-loading="studentLoading">
          <el-table-column prop="classCode" label="班级代号" width="150" />
          <el-table-column label="科目" width="100">
            <template #default="{ row }">
              {{ getSubjectName(row.subjectId) }}
            </template>
          </el-table-column>
          <el-table-column label="教师ID" width="100" prop="teacherId" />
          <el-table-column prop="term" label="期次" width="140" />
          <el-table-column prop="period" label="上课时间" min-width="200" />
          <el-table-column prop="location" label="教室" width="140" />
        </el-table>
        <el-empty v-if="studentQueried && !studentSchedule.length && !studentLoading" description="该学生暂无选课记录" />
      </el-tab-pane>

      <!-- 教师课表 -->
      <el-tab-pane label="教师课表" name="teacher">
        <el-form :inline="true">
          <el-form-item label="教师ID">
            <el-input v-model="teacherId" placeholder="请输入教师ID" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="queryTeacherSchedule">查询</el-button>
          </el-form-item>
        </el-form>

        <el-table v-if="teacherSchedule.length" :data="teacherSchedule" border v-loading="teacherLoading">
          <el-table-column prop="classCode" label="班级代号" width="150" />
          <el-table-column label="科目" width="100">
            <template #default="{ row }">
              {{ getSubjectName(row.subjectId) }}
            </template>
          </el-table-column>
          <el-table-column prop="term" label="期次" width="140" />
          <el-table-column prop="period" label="上课时间" min-width="200" />
          <el-table-column prop="location" label="教室" width="140" />
        </el-table>
        <el-empty v-if="teacherQueried && !teacherSchedule.length && !teacherLoading" description="该教师暂无排课记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStudentSchedule, getTeacherSchedule } from '@/api/schedule'
import { getSubjects } from '@/api/subject'
import { ElMessage } from 'element-plus'

const activeTab = ref('student')

// ======== 学生课表 ========
const studentId = ref('')
const studentSchedule = ref([])
const studentLoading = ref(false)
const studentQueried = ref(false)

async function queryStudentSchedule() {
  if (!studentId.value) {
    ElMessage.warning('请输入学生ID')
    return
  }
  studentLoading.value = true
  studentQueried.value = true
  try {
    studentSchedule.value = await getStudentSchedule(studentId.value)
    if (!studentSchedule.value.length) {
      ElMessage.info('该学生暂无选课记录')
    }
  } catch (e) {}
  studentLoading.value = false
}

// ======== 教师课表 ========
const teacherId = ref('')
const teacherSchedule = ref([])
const teacherLoading = ref(false)
const teacherQueried = ref(false)

async function queryTeacherSchedule() {
  if (!teacherId.value) {
    ElMessage.warning('请输入教师ID')
    return
  }
  teacherLoading.value = true
  teacherQueried.value = true
  try {
    teacherSchedule.value = await getTeacherSchedule(teacherId.value)
    if (!teacherSchedule.value.length) {
      ElMessage.info('该教师暂无排课记录')
    }
  } catch (e) {}
  teacherLoading.value = false
}

// ======== 科目名称映射 ========
const subjects = ref([])
onMounted(async () => {
  try {
    subjects.value = await getSubjects()
  } catch (e) {}
})

function getSubjectName(subjectId) {
  const s = subjects.value.find(s => s.subjectId === subjectId)
  return s ? s.subjectName : subjectId
}
</script>