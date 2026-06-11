<template>
  <div>
    <div class="page-header">
      <el-icon><EditPen /></el-icon>
      <h2>学生报名</h2>
    </div>

    <!-- 学生端：我的缴费状态（含催费提示） -->
    <el-card v-if="auth.isStudent" shadow="hover" style="margin-bottom:20px">
      <template #header>
        <span style="font-weight:600">我的缴费状态</span>
      </template>
      <div v-if="paymentSummary && paymentSummary.enrollments && paymentSummary.enrollments.length">
        <el-table :data="paymentSummary.enrollments" stripe border size="small">
          <el-table-column label="科目" width="100">
            <template #default="{ row }">
              {{ getSubjectNameByEnrollment(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="classCode" label="班级代号" width="130" />
          <el-table-column label="学费" width="100">
            <template #default="{ row }">
              ¥{{ row.fee || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="已缴" width="100">
            <template #default="{ row }">
              ¥{{ row.totalPaid || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="180">
            <template #default="{ row }">
              <span v-if="row.arrears > 0" style="color:#F56C6C;font-weight:500">
                ⚠ 欠费 ¥{{ row.arrears }}
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
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'active' && row.arrears > 0"
                size="small"
                type="primary"
                @click="openPayDialog(row)"
              >
                补缴
              </el-button>
              <span v-else style="color:#909399;font-size:12px">—</span>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:12px;display:flex;gap:24px">
          <span>应缴总额：<b style="color:#5b6abf">¥{{ paymentSummary.totalFee || 0 }}</b></span>
          <span>已缴总额：<b style="color:#67C23A">¥{{ paymentSummary.totalPaid || 0 }}</b></span>
          <span v-if="(paymentSummary.totalFee || 0) > (paymentSummary.totalPaid || 0)">
            尚欠：<b style="color:#F56C6C">¥{{ ((paymentSummary.totalFee || 0) - (paymentSummary.totalPaid || 0)).toFixed(2) }}</b>
          </span>
          <span v-else style="color:#67C23A;font-weight:500">✅ 全部已缴清</span>
        </div>
        <el-alert v-if="hasArrears" type="warning" title="催费通知" :closable="false" show-icon style="margin-top:12px">
          您有课程尚未缴清费用，请在下方操作列点击「补缴」按钮完成缴费。
        </el-alert>
      </div>
      <el-empty v-else description="暂无报名记录" />
    </el-card>

    <!-- 报名表单 -->
    <el-card shadow="hover" style="margin-bottom:20px">
      <template #header>
        <span style="font-weight:600">报名信息</span>
      </template>
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择科目">
          <el-select v-model="selectedSubjectId" placeholder="请选择科目" @change="onSubjectChange" clearable style="width:100%">
            <el-option v-for="sub in subjects" :key="sub.subjectId" :label="sub.subjectName" :value="sub.subjectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择班级">
          <el-select v-model="form.classCode" placeholder="请先选择科目" clearable style="width:100%">
            <el-option v-for="cls in classList" :key="cls.classCode"
              :label="`${cls.classCode} | ${getTeacherName(cls.teacherId)} | ${cls.period} | 费用:¥${cls.fee} | 剩余:${cls.capacity - cls.enrolledCount}人`"
              :value="cls.classCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="学生ID">
          <el-input v-model="form.studentId" :disabled="auth.isStudent" />
        </el-form-item>
        <el-form-item label="缴费金额">
          <el-input-number v-model="form.payment" :min="0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">提交报名</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 退课操作 -->
    <el-card shadow="hover" style="max-width:600px">
      <template #header>
        <span style="font-weight:600;color:#F56C6C">退课操作</span>
      </template>
      <el-form :inline="true">
        <el-form-item label="学生ID">
          <el-input v-model="cancelForm.studentId" placeholder="学生ID" :disabled="auth.isStudent" />
        </el-form-item>
        <el-form-item label="班级代号">
          <el-input v-model="cancelForm.classCode" placeholder="班级代号" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" :loading="cancelling" @click="handleCancel">退课</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 补缴弹窗 -->
    <el-dialog v-model="payDialogVisible" title="补缴学费" width="400px" :close-on-click-modal="false">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="班级">
          <el-input :model-value="payTargetRow ? payTargetRow.classCode + '（学费 ¥' + payTargetRow.fee + '）' : ''" disabled />
        </el-form-item>
        <el-form-item label="已缴">
          <el-input :model-value="payTargetRow ? '¥' + (payTargetRow.totalPaid || 0) : ''" disabled />
        </el-form-item>
        <el-form-item label="补缴金额">
          <el-input-number v-model="payForm.amount" :min="0.01" :max="payTargetRow ? payTargetRow.arrears : 0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="payTargetRow">
          <span style="color:#909399;font-size:12px">
            尚欠 ¥{{ payTargetRow.arrears || 0 }}，本次最多可缴 ¥{{ payTargetRow.arrears || 0 }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="handlePay">确认补缴</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getSubjects } from '@/api/subject'
import { getTeachers } from '@/api/teacher'
import { getClassesBySubject } from '@/api/classes'
import { submitEnrollment, cancelEnrollment, checkEnrollment } from '@/api/enrollment'
import { getStudentSummary, payFee } from '@/api/account'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen } from '@element-plus/icons-vue'

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

const cancelForm = reactive({
  studentId: '',
  classCode: '',
})

const submitting = ref(false)
const cancelling = ref(false)

// ======== 补缴 ========
const payDialogVisible = ref(false)
const paying = ref(false)
const payTargetRow = ref(null)
const payForm = reactive({ amount: 0 })

function openPayDialog(row) {
  payTargetRow.value = row
  payForm.amount = row.arrears || 0
  payDialogVisible.value = true
}

async function handlePay() {
  if (!payForm.amount || payForm.amount <= 0) {
    ElMessage.warning('补缴金额必须大于0')
    return
  }
  paying.value = true
  try {
    const result = await payFee({
      studentId: Number(auth.userId),
      classCode: payTargetRow.value.classCode,
      amount: payForm.amount,
    })
    ElMessage.success(result)
    payDialogVisible.value = false
    loadPaymentSummary()
  } catch (e) {}
  paying.value = false
}

// ======== 缴费状态（学生端） ========
const paymentSummary = ref(null)

const hasArrears = computed(() => {
  if (!paymentSummary.value) return false
  return (paymentSummary.value.totalFee || 0) > (paymentSummary.value.totalPaid || 0)
})

function getSubjectNameByEnrollment(enrollment) {
  // enrollment 中可能没有 subjectName，需要从 subjects 中查找
  // classCode 可用于查找班级，再通过班级的 subjectId 找科目名
  // 简化处理：enrollment 如果有 subjectId 字段就使用，否则通过 classCode 推测
  if (enrollment.subjectName) return enrollment.subjectName
  // 尝试从 subjects 中通过 subjectId 查找（后端链路不同，字段可能不同）
  const sub = subjects.value.find(s => s.subjectId === enrollment.subjectId)
  return sub ? sub.subjectName : (enrollment.subjectId || '')
}

async function loadPaymentSummary() {
  if (!auth.isStudent || !auth.userId) return
  try {
    const data = await getStudentSummary(Number(auth.userId))
    // 后端不返回 arrears，前端计算
    if (data && data.enrollments) {
      data.enrollments = data.enrollments.map(e => ({
        ...e,
        arrears: Math.max(0, (e.fee || 0) - (e.totalPaid || 0)),
      }))
    }
    paymentSummary.value = data
  } catch (e) {
    // 无数据时静默处理
  }
}

// ======== 初始化 ========
onMounted(async () => {
  // 学生登录后自动填入学生ID
  if (auth.isStudent && auth.userId) {
    form.studentId = auth.userId
    cancelForm.studentId = auth.userId
    loadPaymentSummary()
  }
  try {
    const [subList, teaList] = await Promise.all([
      getSubjects(),
      getTeachers(),
    ])
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
    // 检查是否为重新报名（之前退过该课程）
    const checkResult = await checkEnrollment(Number(form.studentId), form.classCode)
    if (checkResult.exists && checkResult.status === 'cancelled') {
      const date = new Date(checkResult.enrollmentTime).toLocaleDateString('zh-CN')
      await ElMessageBox.confirm(
        `该学生已于 ${date} 退过此课程（班级：${form.classCode}），是否重新报名？`,
        '重新报名确认',
        { type: 'warning', confirmButtonText: '确认重新报名', cancelButtonText: '取消' }
      )
    }

    const payload = {
      studentId: Number(form.studentId),
      classCode: form.classCode,
      payment: form.payment,
    }
    const result = await submitEnrollment(payload)
    ElMessage.success(result)
    // 只清空学生相关字段，保留科目选择和班级列表以方便连续报名
    form.studentId = auth.isStudent ? auth.userId : ''
    form.classCode = ''
    form.payment = 0
    // 刷新班级列表 + 缴费状态
    if (selectedSubjectId.value) {
      classList.value = await getClassesBySubject(selectedSubjectId.value)
    }
    if (auth.isStudent) {
      loadPaymentSummary()
    }
  } catch (error) {}
  submitting.value = false
}

const handleCancel = async () => {
  if (!cancelForm.studentId || !cancelForm.classCode) {
    ElMessage.warning('请填写学生ID和班级代号')
    return
  }
  cancelling.value = true
  try {
    await ElMessageBox.confirm(
      `确定要让学生 ${cancelForm.studentId} 退出班级 ${cancelForm.classCode} 吗？`,
      '确认退课',
      { type: 'warning' }
    )
    const result = await cancelEnrollment(Number(cancelForm.studentId), cancelForm.classCode)
    ElMessage.success(result)
    cancelForm.studentId = auth.isStudent ? auth.userId : ''
    cancelForm.classCode = ''
    // 退课后刷新缴费状态
    if (auth.isStudent) {
      loadPaymentSummary()
    }
  } catch (error) {}
  cancelling.value = false
}
</script>
