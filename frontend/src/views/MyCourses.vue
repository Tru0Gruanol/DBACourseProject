<template>
  <div>
    <div class="page-header">
      <el-icon><Notebook /></el-icon>
      <h2>已选课程</h2>
    </div>

    <el-table v-if="enrollments.length" :data="enrollments" stripe border v-loading="loading">
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
      <el-table-column label="缴费状态" width="110">
        <template #default="{ row }">
          <span v-if="row.status === 'pending_cancel'" style="color:#E6A23C">审批中</span>
          <span v-else-if="row.status !== 'active'" style="color:#909399">—</span>
          <span v-else-if="row.arrears > 0" style="color:#F56C6C;font-weight:500">
            ⚠ 欠费 ¥{{ row.arrears.toFixed(2) }}
          </span>
          <span v-else style="color:#67C23A">已结清</span>
        </template>
      </el-table-column>
      <el-table-column label="报名状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : row.status === 'pending_cancel' ? 'warning' : 'info'" size="small">
            {{ row.status === 'active' ? '在读' : row.status === 'pending_cancel' ? '待审批' : '已退课' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right" align="center">
        <template #default="{ row }">
          <template v-if="row.status === 'active'">
            <div style="display:flex;align-items:center;justify-content:center;gap:4px">
              <el-button v-if="row.arrears > 0" size="small" type="primary" @click="openPayDialog(row)">补缴</el-button>
              <el-button size="small" type="danger" @click="handleCancel(row)">申请退课</el-button>
            </div>
          </template>
          <span v-else-if="row.status === 'pending_cancel'" style="color:#E6A23C;font-size:12px">等待审批</span>
          <span v-else style="color:#909399;font-size:12px">已退课</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 汇总卡片 -->
    <div v-if="enrollments.length" class="summary-card">
      <div class="summary-item">
        <span>应缴总额（在读）</span>
        <b style="color:#5b6abf">¥{{ summary.totalFee.toFixed(2) }}</b>
      </div>
      <div class="summary-item">
        <span>已缴总额</span>
        <b style="color:#67C23A">¥{{ summary.totalPaid.toFixed(2) }}</b>
      </div>
      <div class="summary-item">
        <span>尚欠</span>
        <b :style="{ color: summary.totalFee > summary.totalPaid ? '#F56C6C' : '#67C23A' }">
          {{ summary.totalFee > summary.totalPaid ? '¥' + (summary.totalFee - summary.totalPaid).toFixed(2) : '已结清' }}
        </b>
      </div>
      <div class="summary-item">
        <el-button type="primary" size="small" @click="showAllInvoice">全部收据</el-button>
      </div>
    </div>

    <!-- 催费提示 -->
    <el-alert
      v-if="hasArrears"
      type="warning"
      title="催费通知"
      :closable="false"
      show-icon
      style="margin-top:16px"
    >
      您有课程尚未缴清费用，请在操作列点击「补缴」按钮完成缴费。
    </el-alert>

    <el-empty v-if="!loading && !enrollments.length" description="暂无选课记录" />

    <!-- 补缴弹窗 -->
    <el-dialog v-model="payDialogVisible" title="补缴学费" width="400px" :close-on-click-modal="false">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="班级">
          <el-input :model-value="payTargetRow ? payTargetRow.classCode + '（' + payTargetRow.subjectName + ' | ¥' + payTargetRow.fee + '）' : ''" disabled />
        </el-form-item>
        <el-form-item label="已缴">
          <el-input :model-value="payTargetRow ? '¥' + (payTargetRow.totalPaid || 0).toFixed(2) : ''" disabled />
        </el-form-item>
        <el-form-item label="补缴金额">
          <el-input-number v-model="payForm.amount" :min="0.01" :max="payTargetRow ? payTargetRow.arrears : 0" :precision="2" style="width:100%" />
        </el-form-item>
        <el-form-item v-if="payTargetRow && payTargetRow.arrears > 0">
          <span style="color:#909399;font-size:12px">
            尚欠 ¥{{ payTargetRow.arrears.toFixed(2) }}，本次最多可缴 ¥{{ payTargetRow.arrears.toFixed(2) }}
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="handlePay">确认补缴</el-button>
      </template>
    </el-dialog>

    <!-- 全部收据弹窗 -->
    <el-dialog v-model="invoiceVisible" title="全部收据" width="580px" :close-on-click-modal="false">
      <div v-if="allPayments.length" style="font-size:14px">
        <div style="margin-bottom:4px"><b>学生：</b>{{ auth.userName || auth.userId }}（{{ auth.userId }}）</div>
        <el-table :data="allPayments" stripe border size="small" max-height="360">
          <el-table-column label="班级" prop="classCode" />
          <el-table-column label="缴费日期" width="120">
            <template #default="{ row }">{{ formatDate(row.accountDate) }}</template>
          </el-table-column>
          <el-table-column label="金额（元）" width="110" align="right">
            <template #default="{ row }">
              <span :style="{ color: row.amountPaid < 0 ? '#F56C6C' : '#1a1a1a' }">¥{{ row.amountPaid.toFixed(2) }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div style="display:flex;justify-content:space-between;margin-top:14px;padding-top:10px;border-top:1px solid #eee">
          <span>应缴总计 <b>¥{{ summary.totalFee.toFixed(2) }}</b></span>
          <span>已缴总计 <b style="color:#67C23A">¥{{ summary.totalPaid.toFixed(2) }}</b></span>
          <span>尚欠 <b :style="{ color: summary.totalFee > summary.totalPaid ? '#F56C6C' : '#67C23A' }">¥{{ (summary.totalFee - summary.totalPaid).toFixed(2) }}</b></span>
        </div>
      </div>
      <el-empty v-else description="暂无缴费记录" />
      <template #footer>
        <el-button type="primary" @click="doPrintReceipt">打印</el-button>
        <el-button @click="invoiceVisible = false">取消</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getStudentSummary, payFee, getInvoice } from '@/api/account'
import { requestCancelEnrollment } from '@/api/enrollment'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Notebook } from '@element-plus/icons-vue'

const auth = useAuthStore()

const loading = ref(false)
const enrollments = ref([])
const summary = reactive({ totalFee: 0, totalPaid: 0 })

const hasArrears = computed(() => summary.totalFee > summary.totalPaid)

async function loadData() {
  if (!auth.userId) return
  loading.value = true
  try {
    const data = await getStudentSummary(Number(auth.userId))
    if (data && data.enrollments) {
      enrollments.value = data.enrollments.map(e => ({
        ...e,
        arrears: Math.max(0, (e.fee || 0) - (e.totalPaid || 0)),
      }))
    } else {
      enrollments.value = []
    }
    summary.totalFee = data.totalFee || 0
    summary.totalPaid = data.totalPaid || 0
  } catch (e) {
    enrollments.value = []
  }
  loading.value = false
}

// ======== 全部收据 ========
const invoiceVisible = ref(false)
const allPayments = ref([])

async function showAllInvoice() {
  invoiceVisible.value = true
  allPayments.value = []
  for (const e of enrollments.value) {
    if (e.status !== 'active') continue
    try {
      const inv = await getInvoice(Number(auth.userId), e.classCode)
      if (inv && inv.payments) {
        for (const p of inv.payments) allPayments.value.push({ ...p, classCode: e.classCode })
      }
    } catch (ex) {}
  }
}

function doPrintReceipt() {
  if (!allPayments.value.length) return
  const rows = allPayments.value.map(p =>
    `<tr><td>${p.classCode}</td><td>${formatDate(p.accountDate)}</td><td style="text-align:right">¥${p.amountPaid.toFixed(2)}</td></tr>`
  ).join('')
  const tf = summary.value.totalFee.toFixed(2)
  const tp = summary.value.totalPaid.toFixed(2)
  const debt = (summary.value.totalFee - summary.value.totalPaid).toFixed(2)
  const html = `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>缴费收据</title><style>
    *{margin:0;padding:0;box-sizing:border-box}
    body{font-family:"PingFang SC","Microsoft YaHei",sans-serif;padding:32px;color:#1a1a1a}
    h2{text-align:center;border-bottom:2px solid #1a1a1a;padding-bottom:12px;margin-bottom:8px}
    .sub{text-align:center;color:#666;font-size:13px;margin-bottom:24px}
    table{width:100%;border-collapse:collapse;margin-bottom:20px}
    th,td{border:1px solid #555;padding:10px 14px;font-size:14px}
    th{background:#e8e8e8;text-align:center;font-weight:600}
    td{text-align:center}
    .sum{display:flex;justify-content:space-between;border-top:3px double #1a1a1a;padding-top:16px;font-size:15px;font-weight:600}
    @media print{body{padding:20px}}
  </style></head><body>
    <h2>缴费收据</h2>
    <div class="sub">学生：${auth.userName||auth.userId}（学号 ${auth.userId}）｜ 开票日期：${new Date().toLocaleDateString('zh-CN')}</div>
    <table><thead><tr><th>班级</th><th>缴费日期</th><th>金额（元）</th></tr></thead><tbody>${rows}</tbody></table>
    <div class="sum"><span>应缴合计 ¥${tf}</span><span>已缴合计 ¥${tp}</span><span>尚欠 ¥${debt}</span></div>
  </body></html>`
  const w = window.open('', '_blank', 'width=640,height=520')
  if (w) { w.document.write(html); w.document.close() }
  else { ElMessage.warning('弹窗被拦截，请允许此网站弹出窗口后重试') }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(
      `确定申请退出班级「${row.classCode}」(${row.subjectName}) 吗？申请提交后将由管理员审批，审批通过后自动退款。`,
      '申请退课',
      { type: 'warning', confirmButtonText: '确认申请', cancelButtonText: '取消' }
    )
    const result = await requestCancelEnrollment(Number(auth.userId), row.classCode)
    ElMessage.success(result)
    loadData()
  } catch (e) {}
}

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
    loadData()
  } catch (e) {}
  paying.value = false
}

onMounted(() => {
  loadData()
})

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
.summary-card {
  margin-top: 20px;
  background: #f9fafb;
  border: none;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  padding: 20px 24px;
  display: flex;
  gap: 32px;
}
.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #8b8c95;
}
.summary-item b {
  font-size: 18px;
  color: #1b1c22;
}
</style>
