<template>
  <div>
    <h2>学生报名</h2>
    <el-form :model="form" label-width="120px">
      <el-form-item label="选择科目">
        <el-select v-model="selectedSubjectId" placeholder="请选择科目" @change="onSubjectChange" clearable>
          <el-option
            v-for="sub in subjects"
            :key="sub.subjectId"
            :label="sub.subjectName"
            :value="sub.subjectId"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="选择班级">
        <el-select v-model="form.classCode" placeholder="请先选择科目" clearable>
          <el-option
            v-for="cls in classList"
            :key="cls.classCode"
            :label="`${cls.classCode} | ${getTeacherName(cls.teacherId)} | ${cls.period} | 费用:¥${cls.fee} | 剩余:${cls.capacity - cls.enrolledCount}人`"
            :value="cls.classCode"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="学生ID">
        <el-input v-model="form.studentId" />
      </el-form-item>

      <el-form-item label="缴费金额">
        <el-input-number v-model="form.payment" :min="0" :precision="2" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交报名</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <div style="max-width:450px">
      <h3 style="margin-bottom:12px">退课操作</h3>
      <el-form :inline="true">
        <el-form-item label="学生ID">
          <el-input v-model="cancelForm.studentId" placeholder="学生ID" />
        </el-form-item>
        <el-form-item label="班级代号">
          <el-input v-model="cancelForm.classCode" placeholder="班级代号" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="handleCancel">退课</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSubjects } from '@/api/subject'
import { getTeachers } from '@/api/teacher'
import { getClassesBySubject } from '@/api/classes'
import { submitEnrollment, cancelEnrollment } from '@/api/enrollment'
import { ElMessage, ElMessageBox } from 'element-plus'

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

onMounted(async () => {
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
  try {
    const payload = {
      studentId: Number(form.studentId),
      classCode: form.classCode,
      payment: form.payment,
    }
    const result = await submitEnrollment(payload)
    ElMessage.success(result)
    form.studentId = ''
    form.classCode = ''
    form.payment = 0
    selectedSubjectId.value = ''
    classList.value = []
  } catch (error) {}
}

const handleCancel = async () => {
  if (!cancelForm.studentId || !cancelForm.classCode) {
    ElMessage.warning('请填写学生ID和班级代号')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定要让学生 ${cancelForm.studentId} 退出班级 ${cancelForm.classCode} 吗？`,
      '确认退课',
      { type: 'warning' }
    )
    const result = await cancelEnrollment(Number(cancelForm.studentId), cancelForm.classCode)
    ElMessage.success(result)
    cancelForm.studentId = ''
    cancelForm.classCode = ''
  } catch (error) {}
}
</script>
