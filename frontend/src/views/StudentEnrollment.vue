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
            :label="`${cls.classCode} (${cls.period}) 费用:${cls.fee} 剩余:${cls.capacity - cls.enrolledCount}`"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSubjects } from '@/api/subject'
import { getClassesBySubject } from '@/api/classes'
import { submitEnrollment } from '@/api/enrollment'
import { ElMessage } from 'element-plus'

const subjects = ref([])
const selectedSubjectId = ref('')
const classList = ref([])

const form = reactive({
  studentId: '',
  classCode: '',
  payment: 0,
})

onMounted(async () => {
  subjects.value = await getSubjects()
})

const onSubjectChange = async (subjectId) => {
  if (!subjectId) {
    classList.value = []
    return
  }
  classList.value = await getClassesBySubject(subjectId)
  form.classCode = ''
}

const handleSubmit = async () => {
  if (!form.studentId || !form.classCode) {
    ElMessage.warning('请完整填写信息')
    return
  }
  try {
    await submitEnrollment(form)
    ElMessage.success('报名成功')
    form.studentId = ''
    form.classCode = ''
    form.payment = 0
    selectedSubjectId.value = ''
    classList.value = []
  } catch (error) {}
}
</script>