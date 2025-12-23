<!--
  ClassManagement.vue
  教师端 - 班级管理模块
  用于管理课程下的学生，包括禁入、解除禁入、移除和查看学生画像
-->
<template>
  <div class="class-management">
    <div class="filter-section mb-4 flex-between">
      <div class="flex-start">
        <span class="mr-2">选择课程:</span>
        <el-select v-model="internalSelectedCourseId" placeholder="请选择课程" @change="handleCourseChange" style="width: 200px">
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
      </div>
      <div v-if="internalSelectedCourseId">
        <el-input v-model="studentSearch" placeholder="搜索姓名/邮箱" style="width: 250px" prefix-icon="Search" />
      </div>
    </div>

    <div v-if="internalSelectedCourseId">
      <el-row :gutter="20" class="mb-4">
        <el-col :span="8">
          <el-card shadow="hover" :body-style="{ padding: '15px' }">
            <div class="text-center">
              <div class="text-muted mb-1">总人数</div>
              <div style="font-size: 24px; font-weight: bold;">{{ classStats.total }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" :body-style="{ padding: '15px' }">
            <div class="text-center">
              <div class="text-muted mb-1">正常</div>
              <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ classStats.active }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" :body-style="{ padding: '15px' }">
            <div class="text-center">
              <div class="text-muted mb-1">已禁入</div>
              <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ classStats.banned }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="filteredStudents" stripe style="width: 100%" v-loading="loadingStudents">
        <el-table-column prop="username" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="points" label="积分" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'danger' : 'success'">
              {{ scope.row.status === 1 ? '已禁入' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="$emit('open-portrait', scope.row)">
              画像
            </el-button>
            <el-button v-if="scope.row.status === 0" type="danger" link size="small" @click="handleBan(scope.row)">
              禁入
            </el-button>
            <el-button v-else type="success" link size="small" @click="handleUnban(scope.row)">
              解除禁入
            </el-button>
            <el-button type="warning" link size="small" @click="handleRemove(scope.row)">
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-empty v-else description="请先选择一个课程来管理学生" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseStudentDetails, removeStudent, banStudent, unbanStudent } from '@/api/course'

const props = defineProps<{
  courses: any[]
  selectedCourseId: number | null
}>()

const emit = defineEmits(['update:selectedCourseId', 'open-portrait'])

const internalSelectedCourseId = ref(props.selectedCourseId)
const students = ref<any[]>([])
const loadingStudents = ref(false)
const studentSearch = ref('')

watch(() => props.selectedCourseId, (newVal) => {
  internalSelectedCourseId.value = newVal
  if (newVal) handleCourseChange(newVal)
})

const handleCourseChange = async (courseId: number) => {
  if (!courseId) return
  emit('update:selectedCourseId', courseId)
  loadingStudents.value = true
  try {
    const res: any = await getCourseStudentDetails(courseId)
    students.value = res.data || []
  } catch (error) {
    ElMessage.error('获取学生列表失败')
    students.value = []
  } finally {
    loadingStudents.value = false
  }
}

const filteredStudents = computed(() => {
  if (!studentSearch.value) return students.value
  const keyword = studentSearch.value.toLowerCase()
  return students.value.filter(s => 
    s.username.toLowerCase().includes(keyword) || 
    (s.email && s.email.toLowerCase().includes(keyword))
  )
})

const classStats = computed(() => {
  const total = students.value.length
  const active = students.value.filter(s => s.status === 0).length
  const banned = students.value.filter(s => s.status === 1).length
  return { total, active, banned }
})

const handleRemove = async (student: any) => {
  try {
    await ElMessageBox.confirm(`确定要将学生 "${student.username}" 移出班级吗？`, '确认移除', {
      confirmButtonText: '移除', cancelButtonText: '取消', type: 'warning'
    })
    await removeStudent(internalSelectedCourseId.value!, student.id)
    ElMessage.success('已移除')
    handleCourseChange(internalSelectedCourseId.value!)
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

const handleBan = async (student: any) => {
  try {
    await ElMessageBox.confirm(`确定要禁止学生 "${student.username}" 再次加入吗？`, '确认禁入', {
      confirmButtonText: '禁入', cancelButtonText: '取消', type: 'error'
    })
    await banStudent(internalSelectedCourseId.value!, student.id)
    ElMessage.success('已设置禁入')
    handleCourseChange(internalSelectedCourseId.value!)
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

const handleUnban = async (student: any) => {
  try {
    await ElMessageBox.confirm(`确定要解除学生 "${student.username}" 的禁入状态吗？`, '解除禁入', {
      confirmButtonText: '解除', cancelButtonText: '取消', type: 'warning'
    })
    await unbanStudent(internalSelectedCourseId.value!, student.id)
    ElMessage.success('已解除禁入')
    handleCourseChange(internalSelectedCourseId.value!)
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '操作失败')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.mr-2 { margin-right: 0.5rem; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-start { display: flex; align-items: center; }
.text-center { text-align: center; }
.text-muted { color: #909399; }
</style>
