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
          <span v-if="row.status !== 'active'" style="color:#909399">—</span>
          <span v-else-if="row.arrears > 0" style="color:#F56C6C;font-weight:500">
            ⚠ 欠费 ¥{{ row.arrears.toFixed(2) }}
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
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'active'">
            <el-button
              v-if="row.arrears > 0"
              size="small"
              type="primary"
              @click="openPayDialog(row)"
            >
              补缴
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleCancel(row)"
            >
              退课
            </el-button>
          </template>
          <span v-else style="color:#909399;font-size:12px">已退课（已退款）</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getStudentSummary, payFee } from '@/api/account'
import { cancelEnrollment } from '@/api/enrollment'
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

// ======== 退课 ========
async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(
      `确定要退出班级「${row.classCode}」(${row.subjectName}) 吗？已缴金额 ¥${(row.totalPaid || 0).toFixed(2)} 将全额退还。`,
      '确认退课',
      { type: 'warning', confirmButtonText: '确认退课', cancelButtonText: '取消' }
    )
    const result = await cancelEnrollment(Number(auth.userId), row.classCode)
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
</script>

<style scoped>
.summary-card {
  margin-top: 20px;
  background: #fafafc;
  border: 1px solid #e8e8ed;
  border-radius: 4px;
  padding: 16px 24px;
  display: flex;
  gap: 32px;
}
.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}
.summary-item b {
  font-size: 18px;
  color: #303133;
}
</style>
