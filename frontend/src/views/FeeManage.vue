<template>
  <div>
    <div class="page-header">
      <el-icon><Money /></el-icon>
      <h2>收费管理</h2>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- ==================== 缴费查询 ==================== -->
      <el-tab-pane label="缴费查询" name="query">
        <div class="query-bar">
          <el-form :inline="true">
            <el-form-item label="学生ID">
              <el-input v-model="queryStudentId" placeholder="请输入学生ID" @keyup.enter="handleQuery" style="width:200px" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleQuery" :loading="queryLoading">
                {{ summary ? '刷新' : '查询' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 统计卡片 -->
        <div v-if="summary" class="stat-cards">
          <div class="stat-card blue">
            <span class="stat-label">应缴总额</span>
            <span class="stat-value">¥{{ summary.totalFee }}</span>
          </div>
          <div class="stat-card green">
            <span class="stat-label">已缴总额</span>
            <span class="stat-value">¥{{ summary.totalPaid }}</span>
          </div>
          <div class="stat-card" :class="summary.totalFee > summary.totalPaid ? 'red' : 'green'">
            <span class="stat-label">{{ summary.totalFee > summary.totalPaid ? '尚欠金额' : '缴费状态' }}</span>
            <span class="stat-value" v-if="summary.totalFee > summary.totalPaid">
              ¥{{ (summary.totalFee - summary.totalPaid).toFixed(2) }}
            </span>
            <span class="stat-value" v-else style="font-size:20px">已结清</span>
          </div>
        </div>

        <!-- 报名明细 -->
        <el-table
          v-if="summary && summary.enrollments && summary.enrollments.length"
          :data="summary.enrollments"
          stripe border
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
          <el-table-column label="欠费" width="120">
            <template #default="{ row }">
              <template v-if="row.status === 'active'">
                <span v-if="row.fee > row.totalPaid" style="color:#F56C6C;font-weight:500">
                  欠 ¥{{ (row.fee - row.totalPaid).toFixed(2) }}
                </span>
                <span v-else style="color:#67C23A">已结清</span>
              </template>
              <span v-else style="color:#909399">—</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'active'" type="success" size="small">在读</el-tag>
              <el-tag v-else type="info" size="small">已退课</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'active'"
                size="small"
                type="danger"
                @click="handleCancelEnrollment(row)"
              >
                退课
              </el-button>
              <span v-else style="color:#909399;font-size:12px">—</span>
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="queried && !summary" description="未找到该学生的报名及缴费记录" />
      </el-tab-pane>

      <!-- ==================== 流水记录 ==================== -->
      <el-tab-pane label="流水记录" name="records">
        <div style="margin-bottom:12px">
          <el-button @click="refreshAccounts" :loading="accountsLoading">刷新数据</el-button>
        </div>
        <el-table :data="accounts" stripe border v-loading="accountsLoading" empty-text="暂无流水记录">
          <el-table-column prop="accountId" label="流水号" width="80" />
          <el-table-column label="日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.accountDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="studentId" label="学生ID" width="100" />
          <el-table-column prop="classCode" label="班级" width="140" />
          <el-table-column prop="subjectId" label="科目ID" width="80" />
          <el-table-column label="金额" min-width="140">
            <template #default="{ row }">
              <span v-if="row.amountPaid >= 0">¥{{ row.amountPaid.toFixed(2) }}</span>
              <span v-else style="color:#F56C6C;font-weight:500">
                退款 ¥{{ Math.abs(row.amountPaid).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- ==================== 催费列表 ==================== -->
      <el-tab-pane label="催费列表" name="debtors">
        <div style="margin-bottom:12px">
          <el-button type="warning" @click="handleQueryDebtors" :loading="debtorLoading">
            查询欠费学生
          </el-button>
        </div>
        <el-table
          v-if="debtors && debtors.length"
          :data="debtors" stripe border
          v-loading="debtorLoading"
        >
          <el-table-column prop="studentId" label="学生ID" width="100" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="classCode" label="班级" width="140" />
          <el-table-column label="应缴" width="100">
            <template #default="{ row }">¥{{ row.totalFee }}</template>
          </el-table-column>
          <el-table-column label="已缴" width="100">
            <template #default="{ row }">¥{{ row.totalPaid }}</template>
          </el-table-column>
          <el-table-column label="欠费" width="120">
            <template #default="{ row }">
              <span style="color:#F56C6C;font-weight:bold">
                ¥{{ (row.totalFee - row.totalPaid).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="debtorQueried" description="暂无欠费学生" />
      </el-tab-pane>

      <!-- ==================== 教师薪酬 ==================== -->
      <el-tab-pane label="教师薪酬" name="salaries">
        <div style="margin-bottom:12px">
          <el-button type="primary" @click="loadTeacherSalaries" :loading="salaryLoading">
            查询教师薪酬
          </el-button>
        </div>
        <el-table
          v-if="salaries && salaries.length"
          :data="salaries" stripe border
          v-loading="salaryLoading"
        >
          <el-table-column prop="teacherId" label="教师号" width="90" />
          <el-table-column prop="teacherName" label="姓名" width="100" />
          <el-table-column prop="teacherLevel" label="等级" width="90" />
          <el-table-column prop="specialty" label="特长" min-width="140" />
          <el-table-column prop="classCount" label="授课班级" width="90" />
          <el-table-column label="课时报酬合计" width="140">
            <template #default="{ row }">
              <span style="color:#5b6abf;font-weight:600">
                ¥{{ (row.totalRemuneration || 0).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="salaryQueried" description="暂无教师数据" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllAccounts, getStudentSummary, getDebtors } from '@/api/account'
import { cancelEnrollment as cancelEnrollmentApi } from '@/api/enrollment'
import { getSubjects } from '@/api/subject'
import { getTeacherSalaries } from '@/api/teacher'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Money } from '@element-plus/icons-vue'

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

// ======== 退课操作 ========
async function handleCancelEnrollment(row) {
  try {
    await ElMessageBox.confirm(
      `确定要让学生退出班级 ${row.classCode} 吗？已缴费用将全额退款。`,
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

// ======== 教师薪酬 ========
const salaries = ref([])
const salaryLoading = ref(false)
const salaryQueried = ref(false)

async function loadTeacherSalaries() {
  salaryLoading.value = true
  salaryQueried.value = true
  try {
    salaries.value = await getTeacherSalaries()
  } catch (e) {}
  salaryLoading.value = false
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

<style scoped>
/* ======== 统计卡片 ======== */
.stat-cards {
  display: flex;
  gap: 16px;
  margin-top: 16px;
}
.stat-card {
  flex: 1;
  border-radius: 8px;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.stat-card.blue  { background: #ecf5ff; }
.stat-card.green { background: #f0f9eb; }
.stat-card.red   { background: #fef0f0; }

.stat-label {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}
.stat-card.blue  .stat-value { color: #409eff; }
.stat-card.green .stat-value { color: #67c23a; }
.stat-card.red   .stat-value { color: #f56c6c; }
</style>
