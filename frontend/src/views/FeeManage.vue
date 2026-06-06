<template>
  <div>
    <h2>收费管理</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 流水查询 -->
      <el-tab-pane label="流水记录" name="records">
        <el-table :data="accounts" border v-loading="loading">
          <el-table-column prop="accountId" label="流水号" width="80" />
          <el-table-column prop="accountDate" label="日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.accountDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="classCode" label="班级" width="140" />
          <el-table-column prop="studentId" label="学生ID" width="100" />
          <el-table-column prop="subjectId" label="科目ID" width="100" />
          <el-table-column prop="amountPaid" label="交款金额" width="120">
            <template #default="{ row }">
              ¥{{ row.amountPaid }}
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" style="margin-top:12px" @click="refreshAccounts">刷新流水</el-button>
      </el-tab-pane>

      <!-- 单独缴费 -->
      <el-tab-pane label="单独缴费" name="pay">
        <el-form :model="payForm" label-width="120px" style="max-width:450px">
          <el-form-item label="学生ID">
            <el-input v-model="payForm.studentId" placeholder="请输入学生ID" />
          </el-form-item>
          <el-form-item label="班级代号">
            <el-input v-model="payForm.classCode" placeholder="请输入班级代号" />
          </el-form-item>
          <el-form-item label="缴费金额">
            <el-input-number v-model="payForm.amount" :min="0" :precision="2" style="width:100%" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handlePay">确认缴费</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 收费清单 -->
      <el-tab-pane label="收费清单" name="invoice">
        <el-form :inline="true">
          <el-form-item label="学生ID">
            <el-input v-model="invoiceQuery.studentId" placeholder="学生ID" />
          </el-form-item>
          <el-form-item label="班级代号">
            <el-input v-model="invoiceQuery.classCode" placeholder="班级代号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQueryInvoice">查询清单</el-button>
          </el-form-item>
        </el-form>

        <el-descriptions v-if="invoice" :column="2" border style="margin-top:16px">
          <el-descriptions-item label="学生ID">{{ invoice.studentId }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ invoice.className }}</el-descriptions-item>
          <el-descriptions-item label="应缴总额">¥{{ invoice.totalFee }}</el-descriptions-item>
          <el-descriptions-item label="已缴总额">¥{{ invoice.totalPaid }}</el-descriptions-item>
          <el-descriptions-item label="欠费金额">
            <span style="color:red;font-weight:bold">¥{{ invoice.debt }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <h4 v-if="invoice && invoice.payments && invoice.payments.length" style="margin-top:16px">缴费明细</h4>
        <el-table v-if="invoice && invoice.payments && invoice.payments.length" :data="invoice.payments" border style="margin-top:8px">
          <el-table-column prop="accountId" label="流水号" width="80" />
          <el-table-column prop="accountDate" label="缴费日期" width="140">
            <template #default="{ row }">
              {{ formatDate(row.accountDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="amountPaid" label="金额">
            <template #default="{ row }">
              ¥{{ row.amountPaid }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 催费列表 -->
      <el-tab-pane label="催费列表" name="debtors">
        <el-button type="warning" @click="handleQueryDebtors" style="margin-bottom:12px">查询欠费学生</el-button>
        <el-table :data="debtors" border v-loading="debtorLoading" v-if="debtors && debtors.length">
          <el-table-column prop="studentId" label="学生ID" width="100" />
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="classCode" label="班级" width="140" />
          <el-table-column prop="totalFee" label="应缴总额" width="120">
            <template #default="{ row }">
              ¥{{ row.totalFee }}
            </template>
          </el-table-column>
          <el-table-column prop="totalPaid" label="已缴" width="120">
            <template #default="{ row }">
              ¥{{ row.totalPaid }}
            </template>
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
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getAllAccounts, payFee, getInvoice, getDebtors } from '@/api/account'
import { ElMessage } from 'element-plus'

const activeTab = ref('records')

// ======== 流水记录 ========
const accounts = ref([])
const loading = ref(false)

async function refreshAccounts() {
  loading.value = true
  try {
    accounts.value = await getAllAccounts()
    ElMessage.success('流水加载完成')
  } catch (e) {}
  loading.value = false
}

// 初始化加载
refreshAccounts()

// ======== 单独缴费 ========
const payForm = reactive({
  studentId: '',
  classCode: '',
  amount: 0,
})

async function handlePay() {
  if (!payForm.studentId || !payForm.classCode || payForm.amount <= 0) {
    ElMessage.warning('请完整填写缴费信息')
    return
  }
  try {
    const result = await payFee({ ...payForm })
    ElMessage.success(result)
    payForm.studentId = ''
    payForm.classCode = ''
    payForm.amount = 0
    refreshAccounts()
  } catch (e) {}
}

// ======== 收费清单 ========
const invoiceQuery = reactive({ studentId: '', classCode: '' })
const invoice = ref(null)

async function handleQueryInvoice() {
  if (!invoiceQuery.studentId || !invoiceQuery.classCode) {
    ElMessage.warning('请输入学生ID和班级代号')
    return
  }
  try {
    invoice.value = await getInvoice(invoiceQuery.studentId, invoiceQuery.classCode)
    if (invoice.value.error) {
      ElMessage.error(invoice.value.error)
      invoice.value = null
    }
  } catch (e) {}
}

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