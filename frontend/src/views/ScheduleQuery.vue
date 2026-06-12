<template>
  <div>
    <div class="page-header">
      <el-icon><Calendar /></el-icon>
      <h2>课表查询</h2>
    </div>

    <!-- 学生端：自己的课表 -->
    <template v-if="auth.isStudent">
      <el-card shadow="hover" v-loading="studentLoading">
        <el-table v-if="studentSchedule.length" :data="studentSchedule" stripe border>
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
        <el-empty v-else-if="studentQueried" description="暂无选课记录" />
      </el-card>
    </template>

    <!-- 教师端：个人信息 + 课表 + 薪酬 -->
    <template v-else-if="auth.isTeacher">
      <!-- 个人信息卡片 -->
      <div class="teacher-info-card" v-if="teacherInfo.name">
        <div class="teacher-info-left">
          <el-icon size="28" color="#5b6abf"><Avatar /></el-icon>
          <div>
            <div style="font-size:17px;font-weight:600;color:#1a1a1a">{{ teacherInfo.name }}</div>
            <div style="font-size:12px;color:#909399;margin-top:2px">工号 {{ auth.userId }} ｜ {{ teacherInfo.level }}</div>
          </div>
        </div>
        <div class="teacher-info-right">
          <span style="font-size:12px;color:#909399">任教科目：</span>
          <el-tag v-for="sn in teacherInfo.subjects" :key="sn" size="small" effect="plain" style="margin-left:4px">{{ sn }}</el-tag>
          <span v-if="!teacherInfo.subjects.length" style="color:#c0c4cc">—</span>
        </div>
      </div>

      <el-card shadow="hover" v-loading="teacherLoading">
        <el-table v-if="teacherSchedule.length" :data="teacherSchedule" stripe border>
          <el-table-column prop="classCode" label="班级代号" width="140" />
          <el-table-column label="科目" width="90">
            <template #default="{ row }">{{ getSubjectName(row.subjectId) }}</template>
          </el-table-column>
          <el-table-column prop="term" label="期次" width="130" />
          <el-table-column prop="period" label="上课时间" min-width="180" />
          <el-table-column prop="location" label="教室" width="110" />
          <el-table-column label="学生" width="100">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="showClassStudents(row)">
                {{ row.enrolledCount || 0 }} / {{ row.capacity || 0 }}
              </el-button>
            </template>
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

        <el-empty v-else-if="teacherQueried" description="暂无排课记录" />
      </el-card>

      <!-- 学生名单弹窗 -->
      <el-dialog v-model="studentDialogVisible" :title="'班级 ' + selectedClassCode + ' 在读学生'" width="400px">
        <el-table :data="classStudents" stripe border size="small" v-loading="studentListLoading" max-height="360">
          <el-table-column prop="studentId" label="学号" width="90" />
          <el-table-column prop="studentName" label="姓名" />
        </el-table>
        <el-empty v-if="!studentListLoading && !classStudents.length" description="暂无在读学生" />
        <template #footer>
          <el-button @click="studentDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </template>

    <!-- 管理端 -->
    <template v-else>
      <!-- 步骤1：未查询 → 只显示 ID 输入 -->
      <el-card v-if="!scheduleQueried" shadow="hover" style="min-width:460px">
        <template #header>
          <span style="font-weight:600">课表查询</span>
        </template>
        <el-form :inline="true">
          <el-form-item label="学号/工号">
            <el-input v-model="scheduleId" placeholder="请输入学生ID或教师ID" @keyup.enter="querySchedule" style="width:200px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="scheduleLoading" @click="querySchedule">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 步骤2：已查询 → 全新结果页，隐藏输入卡片 -->
      <template v-if="scheduleQueried">
        <!-- 没有匹配结果 -->
        <template v-if="!studentSchedule.length && !teacherSchedule.length">
          <el-card shadow="hover" style="max-width:460px;margin-bottom:20px">
            <template #header>
              <span style="font-weight:600">课表查询</span>
            </template>
            <el-form :inline="true">
              <el-form-item label="学号/工号">
                <el-input v-model="scheduleId" placeholder="请输入学生ID或教师ID" @keyup.enter="querySchedule" style="width:200px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="scheduleLoading" @click="querySchedule">查询</el-button>
              </el-form-item>
            </el-form>
            <div style="color:#F56C6C;font-size:13px">未找到该ID对应的学生或教师记录</div>
          </el-card>
        </template>

        <!-- 学生课表结果 -->
        <template v-if="studentSchedule.length">
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:20px">
            <el-icon size="22" color="#5b6abf"><UserFilled /></el-icon>
            <span style="font-size:17px;font-weight:600;color:#1a1a1a">
              {{ studentInfo.name || '学生' + scheduleId }}
            </span>
            <el-tag type="info" size="small">学号 {{ scheduleId }}</el-tag>
            <el-button size="small" text type="primary" @click="resetSchedule" style="margin-left:8px">
              <el-icon style="margin-right:2px"><Switch /></el-icon>更换查询
            </el-button>
          </div>
          <el-table :data="studentSchedule" stripe border v-loading="scheduleLoading">
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
        </template>

        <!-- 教师课表结果 -->
        <template v-if="teacherSchedule.length">
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:20px">
            <el-icon size="22" color="#5b6abf"><Avatar /></el-icon>
            <span style="font-size:17px;font-weight:600;color:#1a1a1a">
              {{ teacherInfo.name || '教师' + scheduleId }}
            </span>
            <el-tag type="info" size="small">工号 {{ scheduleId }}</el-tag>
            <el-tag v-if="teacherInfo.level" size="small" effect="plain">{{ teacherInfo.level }}</el-tag>
            <el-button size="small" text type="primary" @click="resetSchedule" style="margin-left:8px">
              <el-icon style="margin-right:2px"><Switch /></el-icon>更换查询
            </el-button>
          </div>
          <el-table :data="teacherSchedule" stripe border v-loading="scheduleLoading">
            <el-table-column prop="classCode" label="班级代号" width="150" />
            <el-table-column label="科目" width="100">
              <template #default="{ row }">{{ getSubjectName(row.subjectId) }}</template>
            </el-table-column>
            <el-table-column prop="term" label="期次" width="140" />
            <el-table-column prop="period" label="上课时间" min-width="200" />
            <el-table-column prop="location" label="教室" width="140" />
          </el-table>
        </template>
      </template>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getStudentSchedule, getTeacherSchedule } from '@/api/schedule'
import { getSubjects } from '@/api/subject'
import { getTeachers } from '@/api/teacher'
import { getStudentById } from '@/api/student'
import { getStudentsByClassCode } from '@/api/enrollment'
import { ElMessage } from 'element-plus'
import { Calendar, UserFilled, Avatar, Switch } from '@element-plus/icons-vue'

const auth = useAuthStore()

const studentLoading = ref(false)
const studentQueried = ref(false)
const teacherLoading = ref(false)
const teacherQueried = ref(false)

// ======== 管理员端 ========
const scheduleId = ref('')
const scheduleLoading = ref(false)
const scheduleQueried = ref(false)
const studentInfo = ref({ name: '' })
const teacherInfo = ref({ name: '', level: '', subjects: [] })

const studentSchedule = ref([])
const teacherSchedule = ref([])

async function querySchedule() {
  if (!scheduleId.value) {
    ElMessage.warning('请输入学生ID或教师ID')
    return
  }
  scheduleLoading.value = true
  const id = Number(scheduleId.value)

  // 并行查询：课表 + 学生信息 + 教师信息
  const [stuResult, teaResult, stuInfoResult] = await Promise.allSettled([
    getStudentSchedule(id),
    getTeacherSchedule(id),
    getStudentById(id),
  ])

  studentSchedule.value = stuResult.status === 'fulfilled' ? stuResult.value : []
  teacherSchedule.value = teaResult.status === 'fulfilled' ? teaResult.value : []

  // 从学生表获取姓名
  if (stuInfoResult.status === 'fulfilled' && stuInfoResult.value) {
    studentInfo.value = { name: stuInfoResult.value.studentName || '' }
  } else {
    studentInfo.value = { name: '' }
  }

  // 从教师列表获取教师姓名和等级
  if (teacherSchedule.value.length) {
    const t = teachers.value.find(t => t.teacherId === id)
    teacherInfo.value = { name: t ? t.teacherName : '', level: t ? t.teacherLevel : '', subjects: [] }
  } else {
    teacherInfo.value = { name: '', level: '', subjects: [] }
  }

  scheduleQueried.value = true
  scheduleLoading.value = false
}

function resetSchedule() {
  scheduleId.value = ''
  scheduleQueried.value = false
  studentSchedule.value = []
  teacherSchedule.value = []
  studentInfo.value = { name: '' }
  teacherInfo.value = { name: '', level: '', subjects: [] }
}

const totalRemuneration = computed(() => {
  return teacherSchedule.value.reduce((sum, row) => sum + (row.teacherRemuneration || 0), 0)
})

const subjects = ref([])
const teachers = ref([])

// 学生名单弹窗
const studentDialogVisible = ref(false)
const selectedClassCode = ref('')
const classStudents = ref([])
const studentListLoading = ref(false)

async function showClassStudents(row) {
  selectedClassCode.value = row.classCode
  studentDialogVisible.value = true
  studentListLoading.value = true
  try {
    classStudents.value = await getStudentsByClassCode(row.classCode)
  } catch (e) {
    classStudents.value = []
  }
  studentListLoading.value = false
}

onMounted(async () => {
  try {
    const [subList, teaList] = await Promise.all([getSubjects(), getTeachers()])
    subjects.value = subList
    teachers.value = teaList
  } catch (e) {}

  if (auth.isStudent && auth.userId) {
    studentLoading.value = true
    studentQueried.value = true
    const id = Number(auth.userId)
    try { studentSchedule.value = await getStudentSchedule(id) } catch (e) {}
    studentLoading.value = false
  }
  if (auth.isTeacher && auth.userId) {
    teacherLoading.value = true
    teacherQueried.value = true
    const id = Number(auth.userId)
    try { teacherSchedule.value = await getTeacherSchedule(id) } catch (e) {}
    teacherLoading.value = false
    // 填充教师个人信息
    const t = teachers.value.find(t => t.teacherId === id)
    if (t) {
      teacherInfo.value = {
        name: t.teacherName || '',
        level: t.teacherLevel || '',
        subjects: (t.subjectIds || []).map(sid => {
          const s = subjects.value.find(s => s.subjectId === sid)
          return s ? s.subjectName : String(sid)
        })
      }
    }
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
.teacher-info-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border: 1px solid #e8e8ed;
  border-radius: 4px;
  padding: 16px 24px;
  margin-bottom: 20px;
}
.teacher-info-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.teacher-info-right {
  display: flex;
  align-items: center;
}
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
