<!--
  ReminderCenter.vue
  教师端 - 催学中心模块
  用于查看长期未打卡的学生并发送批量提醒
-->
<template>
  <div class="reminder-center">
    <div class="filter-section mb-4 flex-between">
      <div class="flex-start">
        <span class="mr-2">选择课程:</span>
        <el-select v-model="internalRemindCourseId" placeholder="请选择课程" @change="handleRemindCourseChange" style="width: 200px">
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
      </div>
      <el-button type="warning" :disabled="selectedRemindStudents.length === 0" @click="handleBatchRemind">
        <el-icon class="mr-2"><Bell /></el-icon>一键提醒 ({{ selectedRemindStudents.length }}人)
      </el-button>
    </div>

    <div v-if="internalRemindCourseId">
      <el-alert title="以下学生已超过3天未打卡" type="warning" show-icon class="mb-4" :closable="false" />
      <el-table 
        :data="lazyStudents" 
        stripe 
        style="width: 100%" 
        v-loading="loadingRemind"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="lastCheckinDate" label="上次打卡" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.lastCheckinDate) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default>
            <el-tag type="danger">待提醒</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-empty v-else description="请先选择一个课程进行催学管理" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Bell } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkStudentsToRemind, sendBatchReminders } from '@/api/course'

const props = defineProps<{
  courses: any[]
  remindCourseId: number | null
}>()

const emit = defineEmits(['update:remindCourseId'])

const internalRemindCourseId = ref(props.remindCourseId)
const lazyStudents = ref<any[]>([])
const loadingRemind = ref(false)
const selectedRemindStudents = ref<any[]>([])

watch(() => props.remindCourseId, (newVal) => {
  internalRemindCourseId.value = newVal
  if (newVal) handleRemindCourseChange(newVal)
})

const handleRemindCourseChange = async (courseId: number) => {
  if (!courseId) return
  emit('update:remindCourseId', courseId)
  loadingRemind.value = true
  try {
    const res: any = await checkStudentsToRemind(courseId)
    lazyStudents.value = res.data || []
  } catch (error) {
    ElMessage.error('获取催学列表失败')
  } finally {
    loadingRemind.value = false
  }
}

const handleSelectionChange = (val: any[]) => {
  selectedRemindStudents.value = val
}

const handleBatchRemind = async () => {
  if (selectedRemindStudents.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定要向选中的 ${selectedRemindStudents.value.length} 名学生发送提醒吗？`, '批量提醒', {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    const userIds = selectedRemindStudents.value.map(s => s.id)
    await sendBatchReminders(
      internalRemindCourseId.value!, 
      '学习打卡提醒', 
      '亲爱的同学，你已经有几天没有打卡了，请记得及时完成学习任务哦！', 
      userIds
    )
    ElMessage.success('提醒已发送')
    handleRemindCourseChange(internalRemindCourseId.value!)
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '发送失败')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '从未打卡'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.mr-2 { margin-right: 0.5rem; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-start { display: flex; align-items: center; }
</style>
