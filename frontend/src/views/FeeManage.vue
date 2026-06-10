<template>
  <div>
    <h2>收费管理</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ==================== 缴费查询 ==================== -->
      <el-tab-pane label="缴费查询" name="query">
        <el-form :inline="true">
          <el-form-item label="学生ID">
            <el-input v-model="queryStudentId" placeholder="请输入学生ID" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
        </el-form>

        <!-- 学生概览：只显示欠费或已结清，不存在多缴 -->
        <el-descriptions v-if="summary" :column="3" border style="margin-top:16px">
          <el-descriptions-item label="应缴总额">
            <span style="font-weight:bold">¥{{ summary.totalFee }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已缴总额">
            <span style="font-weight:bold">¥{{ summary.totalPaid }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="欠费金额">
            <span v-if="summary.totalFee > summary.totalPaid" style="color:red;font-weight:bold">
              欠费 ¥{{ (summary.totalFee - summary.totalPaid).toFixed(2) }}
            </span>
            <span v-else style="color:green;font-weight:bold">已结清</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 报名明细：每行自带操作按钮，无需复选框 -->
        <el-table
          v-if="summary && summary.enrollments && summary.enrollments.length"
          :data="summary.enrollments"
          border
          style="margin-top:16px"
          v-loading="queryLoading"
        >
          <el-table-column label="科目" width="100">
            <template #default="{ row }">
              {{ getSubjectName(row.subjectId) }}
            </template>
          </el-table-column>
          <el-table-column prop="classCode" label="班级" width="140" />
          <el-table-column prop="term" label="期次" width="140" />
          <el-table-column label="学费" width="100">
            <template #default="{ row }">¥{{ row.fee }}</template>
          </el-table-column>
          <el-table-column label="已缴" width="100">
            <template #default="{ row }">¥{{ row.totalPaid }}</template>
          </el-table-column>
          <el-table-column label="欠费" width="100">
            <template #default="{ row }">
              <span v-if="row.status === 'active' && row.fee > row.totalPaid" style="color:red">
                ¥{{ (row.fee - row.totalPaid).toFixed(2) }}
              </span>
              <span v-else-if="row.status === 'active'" style="color:green">已结清</span>
              <span v-else style="color:#909399">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'active'" type="success">在读</el-tag>
              <el-tag v-else type="info">已退课（已退款）</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <template v-if="row.status === 'active'">
                <el-button size="small" type="primary" @click="showPayDialog(row)">缴费</el-button>
                <el-button size="small" type="danger" @click="handleCancelEnrollment(row)">退课</el-button>
              </template>
              <span v-else style="color:#909399;font-size:12px">—</span>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="queried && !summary" description="未找到该学生的报名及缴费记录" />
      </el-tab-pane>

      <!-- ==================== 流水记录 ==================== -->
      <el-tab-pane label="流水记录" name="records">
        <el-table :data="accounts" border v-loading="accountsLoading">
          <el-table-column prop="accountId" label="流水号" width="80" />
          <el-table-column label="日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.accountDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="studentId" label="学生ID" width="100" />
          <el-table-column prop="classCode" label="班级" width="140" />
          <el-table-column prop="subjectId" label="科目ID" width="80" />
          <el-table-column label="金额" width="140">
            <template #default="{ row }">
              <span v-if="row.amountPaid >= 0">¥{{ row.amountPaid }}</span>
              <span v-else style="color:red;font-weight:bold">-¥{{ Math.abs(row.amountPaid) }}（退款）</span>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" style="margin-top:12px" @click="refreshAccounts">刷新流水</el-button>
      </el-tab-pane>

      <!-- ==================== 催费列表 ==================== -->
      <el-tab-pane label="催费列表" name="debtors">
        <el-button type="warning" @click="handleQueryDebtors" style="margin-bottom:12px">查询欠费学生</el-button>
        <el-table :data="debtors" border v-loading="debtorLoading" v-if="debtors && debtors.length">
          <el-table-column prop="studentId" label="学生ID" width="100" />
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="classCode" label="班级" width="140" />
          <el-table-column prop="totalFee" label="应缴总额" width="120">
            <template #default="{ row }">¥{{ row.totalFee }}</template>
          </el-table-column>
          <el-table-column prop="totalPaid" label="已缴" width="120">
            <template #default="{ row }">¥{{ row.totalPaid }}</template>
          </el-table-column>
          <el-table-column label="欠费" width="120">
            <template #default="{ row }">
              <span style="color:red;font-weight:bold">¥{{ (row.totalFee - row.totalPaid).toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="debtorQueried" description="暂无欠费记录" />
      </el-tab-pane>
    </el-tabs>

    <!-- 缴费弹窗 -->
    <el-dialog v-model="payDialogVisible" title="缴纳学费" width="420px">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="学生ID">
          <el-input :model-value="queryStudentId" disabled />
        </el-form-item>
        <el-form-item label="缴费班级">
          <el-input :model-value="payTargetRow ? payTargetRow.classCode + '（学费 ¥' + payTargetRow.fee + '）' : ''" disabled />
        </el-form-item>
        <el-form-item label="缴费金额">
          <el-input-number v-model="payForm.amount" :min="0.01" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="summary">
          <span style="color:#909399;font-size:12px">
            应缴总额: ¥{{ summary.totalFee }} | 已缴总额: ¥{{ summary.totalPaid }} | 本次可缴上限: ¥{{ maxPayable }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePay">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAllAccounts, getStudentSummary, payFee, getDebtors } from '@/api/account'
import { cancelEnrollment as cancelEnrollmentApi } from '@/api/enrollment'
import { getSubjects } from '@/api/subject'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('query')

// ======== 缴费查询 ========
const queryStudentId = ref('')
const queryLoading = ref(false)
const queried = ref(false)
const summary = ref(null)

const subjectMap = ref({})
onMounted(async () => {
  try {
    const subs = await getSubjects()
    subs.forEach(s => { subjectMap.value[s.subjectId] = s.subjectName })
  } catch (e) {}
})

function getSubjectName(subjectId) {
  return subjectMap.value[subjectId] || '科目' + subjectId
}

const maxPayable = computed(() => {
  if (!summary.value) return 0
  return Math.max(0, (summary.value.totalFee || 0) - (summary.value.totalPaid || 0))
})

async function handleQuery() {
  if (!queryStudentId.value) {
    ElMessage.warning('请输入学生ID')
    return
  }
  queryLoading.value = true
  queried.value = true
  try {
    summary.value = await getStudentSummary(queryStudentId.value)
  } catch (e) {
    summary.value = null
  }
  queryLoading.value = false
}

// ======== 缴费弹窗 ========
const payDialogVisible = ref(false)
const payForm = reactive({ amount: 0 })
const payTargetRow = ref(null)

function showPayDialog(row) {
  payTargetRow.value = row
  payForm.amount = 0
  payDialogVisible.value = true
}

async function handlePay() {
  if (!payForm.amount || payForm.amount <= 0) {
    ElMessage.warning('缴费金额必须大于0')
    return
  }
  if (payForm.amount > maxPayable.value) {
    ElMessage.warning(`缴费金额超过可缴上限 ¥${maxPayable.value}`)
    return
  }
  try {
    const result = await payFee({
      studentId: Number(queryStudentId.value),
      classCode: payTargetRow.value.classCode,
      amount: payForm.amount,
    })
    ElMessage.success(result)
    payDialogVisible.value = false
    await handleQuery()
  } catch (e) {}
}

// ======== 退课操作 ========
async function handleCancelEnrollment(row) {
  try {
    await ElMessageBox.confirm(
      `确定要让学生退出班级 ${row.classCode} 吗？如有超额缴费，系统将自动退款。`,
      '确认退课',
      { type: 'warning' }
    )
    const result = await cancelEnrollmentApi(Number(queryStudentId.value), row.classCode)
    ElMessage.success(result)
    await handleQuery()
  } catch (e) {}
}

// ======== 流水记录 ========
const accounts = ref([])
const accountsLoading = ref(false)

async function refreshAccounts() {
  accountsLoading.value = true
  try {
    accounts.value = await getAllAccounts()
  } catch (e) {}
  accountsLoading.value = false
}
refreshAccounts()

// ======== 催费列表 ========
const debtors = ref([])
const debtorLoading = ref(false)
const debtorQueried = ref(false)

async function handleQueryDebtors() {
  debtorLoading.value = true
  debtorQueried.value = true
  try {
    debtors.value = await getDebtors()
  } catch (e) {}
  debtorLoading.value = false
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
</script>
