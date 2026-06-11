<template>
  <div>
    <div class="page-header">
      <el-icon><Calendar /></el-icon>
      <h2>课表查询</h2>
    </div>

    <!-- 学生端：只显示自己的课表 -->
    <template v-if="auth.isStudent">
      <el-table v-if="studentSchedule.length" :data="studentSchedule" stripe border v-loading="studentLoading">
        <el-table-column prop="classCode" label="班级代号" width="150" />
        <el-table-column label="科目" width="100">
          <template #default="{ row }">{{ getSubjectName(row.subjectId) }}</template>
        </el-table-column>
        <el-table-column label="任课教师" width="100">
          <template #default="{ row }">{{ getTeacherName(row.teacherId) }}</template>
        </el-table-column>
        <el-table-column prop="term" label="期次" width="140" />
        <el-table-column prop="period" label="上课时间" min-width="200" />
        <el-table-column prop="location" label="教室" width="140" />
      </el-table>
      <el-empty v-else-if="studentQueried && !studentLoading" description="暂无选课记录" />
    </template>

    <!-- 教师端：只显示自己的课表 + 薪酬 -->
    <template v-else-if="auth.isTeacher">
      <el-table v-if="teacherSchedule.length" :data="teacherSchedule" stripe border v-loading="teacherLoading">
        <el-table-column prop="classCode" label="班级代号" width="140" />
        <el-table-column label="科目" width="90">
          <template #default="{ row }">{{ getSubjectName(row.subjectId) }}</template>
        </el-table-column>
        <el-table-column prop="term" label="期次" width="130" />
        <el-table-column prop="period" label="上课时间" min-width="180" />
        <el-table-column prop="location" label="教室" width="110" />
        <el-table-column label="学生" width="80">
          <template #default="{ row }">{{ row.enrolledCount || 0 }}/{{ row.capacity || 0 }}</template>
        </el-table-column>
        <el-table-column label="课时报酬" width="110">
          <template #default="{ row }">¥{{ (row.teacherRemuneration || 0).toFixed(2) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="teacherSchedule.length" class="salary-summary">
        <div class="salary-row">
          <span>授课班级数</span>
          <b>{{ teacherSchedule.length }} 个</b>
        </div>
        <div class="salary-row">
          <span>课时报酬合计</span>
          <b class="salary-total">¥{{ totalRemuneration.toFixed(2) }}</b>
        </div>
      </div>

      <el-empty v-else-if="teacherQueried && !teacherLoading" description="暂无排课记录" />
    </template>

    <!-- 管理员端：双标签页 -->
    <el-tabs v-else type="border-card">
      <el-tab-pane label="学生课表" name="student">
        <el-form :inline="true">
          <el-form-item label="学生ID">
            <el-input v-model="studentId" placeholder="请输入学生ID" @keyup.enter="queryStudentSchedule" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="studentLoading" @click="queryStudentSchedule">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table v-if="studentSchedule.length" :data="studentSchedule" stripe border style="margin-top:12px">
          <el-table-column prop="classCode" label="班级代号" width="150" />
          <el-table-column label="科目" width="100">
            <template #default="{ row }">{{ getSubjectName(row.subjectId) }}</template>
          </el-table-column>
          <el-table-column label="任课教师" width="100">
            <template #default="{ row }">{{ getTeacherName(row.teacherId) }}</template>
          </el-table-column>
          <el-table-column prop="term" label="期次" width="140" />
          <el-table-column prop="period" label="上课时间" min-width="200" />
          <el-table-column prop="location" label="教室" width="140" />
        </el-table>
        <el-empty v-if="studentQueried && !studentSchedule.length" description="该学生暂无选课记录" />
      </el-tab-pane>

      <el-tab-pane label="教师课表" name="teacher">
        <el-form :inline="true">
          <el-form-item label="教师ID">
            <el-input v-model="teacherId" placeholder="请输入教师ID" @keyup.enter="queryTeacherSchedule" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="teacherLoading" @click="queryTeacherSchedule">查询</el-button>
          </el-form-item>
        </el-form>
        <el-table v-if="teacherSchedule.length" :data="teacherSchedule" stripe border style="margin-top:12px">
          <el-table-column prop="classCode" label="班级代号" width="150" />
          <el-table-column label="科目" width="100">
            <template #default="{ row }">{{ getSubjectName(row.subjectId) }}</template>
          </el-table-column>
          <el-table-column prop="term" label="期次" width="140" />
          <el-table-column prop="period" label="上课时间" min-width="200" />
          <el-table-column prop="location" label="教室" width="140" />
        </el-table>
        <el-empty v-if="teacherQueried && !teacherSchedule.length" description="该教师暂无排课记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getStudentSchedule, getTeacherSchedule } from '@/api/schedule'
import { getSubjects } from '@/api/subject'
import { getTeachers } from '@/api/teacher'
import { ElMessage } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'

const auth = useAuthStore()

const studentId = ref('')
const studentSchedule = ref([])
const studentLoading = ref(false)
const studentQueried = ref(false)

async function queryStudentSchedule() {
  if (auth.isAdmin && !studentId.value) {
    ElMessage.warning('请输入学生ID')
    return
  }
  studentLoading.value = true
  studentQueried.value = true
  try {
    studentSchedule.value = await getStudentSchedule(studentId.value)
  } catch (e) {}
  studentLoading.value = false
}

const teacherId = ref('')
const teacherSchedule = ref([])
const teacherLoading = ref(false)
const teacherQueried = ref(false)

async function queryTeacherSchedule() {
  if (auth.isAdmin && !teacherId.value) {
    ElMessage.warning('请输入教师ID')
    return
  }
  teacherLoading.value = true
  teacherQueried.value = true
  try {
    teacherSchedule.value = await getTeacherSchedule(teacherId.value)
  } catch (e) {}
  teacherLoading.value = false
}

const totalRemuneration = computed(() => {
  return teacherSchedule.value.reduce((sum, row) => sum + (row.teacherRemuneration || 0), 0)
})

const subjects = ref([])
const teachers = ref([])

onMounted(async () => {
  try {
    const [subList, teaList] = await Promise.all([getSubjects(), getTeachers()])
    subjects.value = subList
    teachers.value = teaList
  } catch (e) {}

  if (auth.isStudent && auth.userId) {
    studentId.value = auth.userId
    queryStudentSchedule()
  }
  if (auth.isTeacher && auth.userId) {
    teacherId.value = auth.userId
    queryTeacherSchedule()
  }
})

function getSubjectName(subjectId) {
  const s = subjects.value.find(s => s.subjectId === subjectId)
  return s ? s.subjectName : subjectId
}

function getTeacherName(teacherId) {
  const t = teachers.value.find(t => t.teacherId === teacherId)
  return t ? t.teacherName : '教师ID:' + teacherId
}
</script>

<style scoped>
.salary-summary {
  margin-top: 20px;
  background: #fafafc;
  border: 1px solid #e8e8ed;
  border-radius: 4px;
  padding: 16px 24px;
  display: flex;
  gap: 32px;
}
.salary-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}
.salary-row b {
  font-size: 18px;
  color: #303133;
}
.salary-total {
  color: #5b6abf !important;
}
</style>
