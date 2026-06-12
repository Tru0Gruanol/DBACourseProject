<template>
  <div>
    <div class="page-header">
      <el-icon><EditPen /></el-icon>
      <h2>学生报名</h2>
    </div>

    <!-- ======== 管理端 ======== -->
    <template v-if="auth.isAdmin">
      <!-- 步骤1：未查询学生 → 只显示学号输入 -->
      <el-card v-if="!adminReady" shadow="hover" style="min-width:460px">
        <template #header>
          <span style="font-weight:600">查询学生</span>
        </template>
        <el-form :inline="true">
          <el-form-item label="学生ID">
            <el-input v-model="adminStudentId" placeholder="请输入学生ID" @keyup.enter="adminLookup" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="adminLookingUp" @click="adminLookup">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 步骤2：已查询到学生 → 全新页面，隐藏查询卡片 -->
      <template v-if="adminReady">
        <!-- 学生信息头部 -->
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:24px">
          <el-icon size="22" color="#5b6abf"><UserFilled /></el-icon>
          <span style="font-size:17px;font-weight:600;color:#1a1a1a">
            {{ adminStudentInfo.studentName || '学生' + adminStudentId }}
          </span>
          <el-tag type="info" size="small">学号 {{ adminStudentId }}</el-tag>
          <el-button size="small" text type="primary" @click="adminReset" style="margin-left:8px">
            <el-icon style="margin-right:2px"><Switch /></el-icon>更换学生
          </el-button>
        </div>

        <!-- 报名表单 -->
        <el-card shadow="hover" style="margin-bottom:20px">
          <template #header>
            <span style="font-weight:600">报名信息</span>
          </template>
          <el-form :model="form" label-width="120px">
            <el-form-item label="选择科目">
              <el-select v-model="selectedSubjectId" placeholder="请选择科目" @change="onSubjectChange" clearable style="width:100%">
                <el-option v-for="sub in subjects" :key="sub.subjectId"
                  :label="sub.subjectName + (enrolledSubjectIds.includes(sub.subjectId) ? '（已报）' : '')"
                  :value="sub.subjectId"
                  :disabled="enrolledSubjectIds.includes(sub.subjectId)" />
              </el-select>
            </el-form-item>
            <el-form-item label="选择班级">
              <el-select v-model="form.classCode" placeholder="请先选择科目" clearable style="width:100%">
                <el-option v-for="cls in classList" :key="cls.classCode"
                  :label="`${cls.classCode} | ${getTeacherName(cls.teacherId)} | ${cls.period} | 费用:¥${cls.fee} | 剩余:${cls.capacity - cls.enrolledCount}人`"
                  :value="cls.classCode" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">提交报名</el-button>
              <span style="color:#909399;font-size:12px;margin-left:12px">管理员代为报名，缴费由学生在「已选课程」自行完成</span>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 已选课程（含退课操作） -->
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight:600">已选课程</span>
          </template>
          <el-table v-if="adminEnrollments.length" :data="adminEnrollments" stripe border size="small" v-loading="adminEnrollLoading">
            <el-table-column label="科目" width="100">
              <template #default="{ row }">{{ row.subjectName }}</template>
            </el-table-column>
            <el-table-column prop="classCode" label="班级代号" width="140" />
            <el-table-column label="老师" width="100">
              <template #default="{ row }">{{ row.teacherName }}</template>
            </el-table-column>
            <el-table-column label="教师级别" width="100">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.teacherLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="学费" width="100">
              <template #default="{ row }">¥{{ (row.fee || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="已缴" width="100">
              <template #default="{ row }">¥{{ (row.totalPaid || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="欠费" width="100">
              <template #default="{ row }">
                <span v-if="row.status !== 'active'" style="color:#909399">—</span>
                <span v-else-if="(row.fee || 0) > (row.totalPaid || 0)" style="color:#F56C6C">
                  ¥{{ ((row.fee || 0) - (row.totalPaid || 0)).toFixed(2) }}
                </span>
                <span v-else style="color:#67C23A">已结清</span>
              </template>
            </el-table-column>
            <el-table-column label="报名状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '在读' : '已退课' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right" align="center">
              <template #default="{ row }">
                <el-button v-if="row.status === 'active'" size="small" type="danger" @click="adminCancelEnrollment(row)">退课</el-button>
                <span v-else style="color:#909399;font-size:12px">已退款</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!adminEnrollLoading && !adminEnrollments.length" description="该学生暂无选课记录" />
        </el-card>
      </template>
    </template>

    <!-- ======== 学生端 ======== -->
    <template v-else>
      <el-card shadow="hover">
        <template #header>
          <span style="font-weight:600">报名信息</span>
        </template>
        <el-form :model="form" label-width="120px">
          <el-form-item label="选择科目">
            <el-select v-model="selectedSubjectId" placeholder="请选择科目" @change="onSubjectChange" clearable style="width:100%">
              <el-option v-for="sub in subjects" :key="sub.subjectId"
                :label="sub.subjectName + (enrolledSubjectIds.includes(sub.subjectId) ? '（已报）' : '')"
                :value="sub.subjectId"
                :disabled="enrolledSubjectIds.includes(sub.subjectId)" />
            </el-select>
          </el-form-item>
          <el-form-item label="选择班级">
            <el-select v-model="form.classCode" placeholder="请先选择科目" clearable style="width:100%">
              <el-option v-for="cls in classList" :key="cls.classCode"
                :label="`${cls.classCode} | ${getTeacherName(cls.teacherId)} | ${cls.period} | 费用:¥${cls.fee} | 剩余:${cls.capacity - cls.enrolledCount}人`"
                :value="cls.classCode" />
            </el-select>
          </el-form-item>
          <el-form-item label="缴费金额">
            <el-input-number v-model="form.payment" :min="0" :precision="2" style="width:100%" />
            <span style="color:#909399;font-size:12px;margin-left:8px">选填，可先报名后到「已选课程」补缴</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交报名</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getSubjects } from '@/api/subject'
import { getTeachers } from '@/api/teacher'
import { getClassesBySubject } from '@/api/classes'
import { submitEnrollment, cancelEnrollment, checkEnrollment } from '@/api/enrollment'
import { getStudentSummary } from '@/api/account'
import { getStudentById } from '@/api/student'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, UserFilled, Switch } from '@element-plus/icons-vue'

const auth = useAuthStore()

const subjects = ref([])
const teachers = ref([])
const selectedSubjectId = ref('')
const classList = ref([])

const form = reactive({
  studentId: '',
  classCode: '',
  payment: 0,
})

const submitting = ref(false)

// ======== 管理端 ========
const adminStudentId = ref('')
const adminStudentInfo = ref({ studentName: '' })
const adminLookingUp = ref(false)
const adminReady = ref(false)
const adminEnrollments = ref([])
const adminEnrollLoading = ref(false)
const myEnrollments = ref([])  // 学生端自己的报名记录

const enrolledSubjectIds = computed(() => {
  const list = auth.isAdmin ? adminEnrollments.value : myEnrollments.value
  return list.filter(e => e.status === 'active').map(e => e.subjectId).filter(Boolean)
})

async function adminLookup() {
  if (!adminStudentId.value) {
    ElMessage.warning('请输入学生ID')
    return
  }
  adminLookingUp.value = true
  try {
    // 先查学生信息获取姓名
    const student = await getStudentById(Number(adminStudentId.value))
    if (!student || !student.studentName) {
      // 学生不存在，阻止进入下一步
      ElMessage.error('未找到学号为 ' + adminStudentId.value + ' 的学生，请先到「学生管理」中新增该学生')
      adminReady.value = false
    } else {
      adminStudentInfo.value = student
      adminReady.value = true
      form.studentId = String(adminStudentId.value)
      loadAdminEnrollments()
    }
  } catch (e) {
    ElMessage.error('查询失败，请检查网络或稍后重试')
    adminReady.value = false
  }
  adminLookingUp.value = false
}

function adminReset() {
  adminStudentId.value = ''
  adminStudentInfo.value = { studentName: '' }
  adminReady.value = false
  adminEnrollments.value = []
  form.studentId = ''
  form.classCode = ''
  form.payment = 0
  selectedSubjectId.value = ''
  classList.value = []
}

async function loadAdminEnrollments() {
  adminEnrollLoading.value = true
  try {
    const data = await getStudentSummary(Number(adminStudentId.value))
    adminEnrollments.value = data && data.enrollments ? data.enrollments : []
  } catch (e) {
    adminEnrollments.value = []
  }
  adminEnrollLoading.value = false
}

async function adminCancelEnrollment(row) {
  try {
    await ElMessageBox.confirm(
      `确定要让学生 ${adminStudentId.value} 退出班级「${row.classCode}」吗？已缴 ¥${(row.totalPaid || 0).toFixed(2)} 将全额退还。`,
      '确认退课',
      { type: 'warning', confirmButtonText: '确认退课', cancelButtonText: '取消' }
    )
    const result = await cancelEnrollment(Number(adminStudentId.value), row.classCode)
    ElMessage.success(result)
    loadAdminEnrollments()
    if (selectedSubjectId.value) {
      classList.value = await getClassesBySubject(selectedSubjectId.value)
    }
  } catch (e) {}
}

// ======== 初始化 ========
onMounted(async () => {
  if (auth.isStudent && auth.userId) {
    form.studentId = auth.userId
    // 加载学生已有报名（标记已选科目用）
    try {
      const data = await getStudentSummary(Number(auth.userId))
      myEnrollments.value = data && data.enrollments ? data.enrollments : []
    } catch (e) {}
  }
  try {
    const [subList, teaList] = await Promise.all([getSubjects(), getTeachers()])
    subjects.value = subList
    teachers.value = teaList
  } catch (e) {}
})

function getTeacherName(teacherId) {
  const t = teachers.value.find(t => t.teacherId === teacherId)
  return t ? t.teacherName : '教师ID:' + teacherId
}

const onSubjectChange = async (subjectId) => {
  if (!subjectId) {
    classList.value = []
    return
  }
  try {
    classList.value = await getClassesBySubject(subjectId)
  } catch (e) {}
  form.classCode = ''
}

const handleSubmit = async () => {
  if (!form.studentId || !form.classCode) {
    ElMessage.warning('请完整填写信息')
    return
  }
  submitting.value = true
  try {
    const payload = {
      studentId: Number(form.studentId),
      classCode: form.classCode,
      payment: form.payment,
    }

    try {
      const checkResult = await checkEnrollment(Number(form.studentId), form.classCode)
      if (checkResult.exists && checkResult.status === 'cancelled') {
        const date = new Date(checkResult.enrollmentTime).toLocaleDateString('zh-CN')
        await ElMessageBox.confirm(
          `该学生已于 ${date} 退过此课程（班级：${form.classCode}），是否重新报名？`,
          '重新报名确认',
          { type: 'warning', confirmButtonText: '确认重新报名', cancelButtonText: '取消' }
        )
      }
    } catch (e) {}

    const result = await submitEnrollment(payload)
    ElMessage.success(result)
    form.classCode = ''
    form.payment = 0
    if (selectedSubjectId.value) {
      classList.value = await getClassesBySubject(selectedSubjectId.value)
    }
    if (auth.isAdmin && adminReady.value) {
      loadAdminEnrollments()
    }
    if (auth.isStudent) {
      try {
        const data = await getStudentSummary(Number(auth.userId))
        myEnrollments.value = data && data.enrollments ? data.enrollments : []
      } catch (e) {}
    }
  } catch (error) {}
  submitting.value = false
}
</script>
