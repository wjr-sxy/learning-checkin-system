<!--
  TeachingPlan.vue
  教师端 - 教学计划模块
  用于管理课程的教学计划，支持新建计划模板和一键下发给所有学生
-->
<template>
  <div class="teaching-plan">
    <div class="filter-section mb-4 flex-between">
      <div class="flex-start">
        <span class="mr-2">选择课程:</span>
        <el-select v-model="internalPlanSelectedCourseId" placeholder="请选择课程" @change="handlePlanCourseChange" style="width: 200px">
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
      </div>
      <el-button type="primary" @click="openCreatePlanDialog" :disabled="!internalPlanSelectedCourseId">
        <el-icon class="mr-2"><Plus /></el-icon>新建计划模板
      </el-button>
    </div>

    <div v-if="internalPlanSelectedCourseId">
      <el-table :data="plans" stripe style="width: 100%" v-loading="loadingPlans">
        <el-table-column prop="title" label="计划标题" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="时间范围" width="220">
          <template #default="scope">
            {{ scope.row.startDate }} ~ {{ scope.row.endDate }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="success" size="small" @click="handleDistributePlan(scope.row)">
              <el-icon class="mr-1"><Promotion /></el-icon>一键下发
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-empty v-else description="请先选择一个课程进行计划管理" />

    <!-- Create Plan Dialog -->
    <el-dialog v-model="createPlanDialogVisible" title="新建教学计划模板" width="600px">
      <el-form :model="newPlan" label-width="100px">
        <el-form-item label="计划标题">
          <el-input v-model="newPlan.title" placeholder="如：Vue.js 基础学习计划" />
        </el-form-item>
        <el-form-item label="计划描述">
          <el-input v-model="newPlan.description" type="textarea" placeholder="详细描述该计划的目标..." />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="planDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="任务列表">
          <div class="task-input flex-start mb-2">
            <el-input v-model="newTaskContent" placeholder="输入任务描述" @keyup.enter="addPlanTask" />
            <el-button type="primary" class="ml-2" @click="addPlanTask">添加</el-button>
          </div>
          <el-table :data="newPlan.tasks" size="small" style="width: 100%">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="任务描述">
              <template #default="scope">
                {{ scope.row }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="60">
              <template #default="scope">
                <el-button type="danger" link @click="newPlan.tasks.splice(scope.$index, 1)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createPlanDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreatePlan">创建模板</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Plus, Promotion, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCoursePlans, createPlan, distributePlan, addTask } from '@/api/plan'
import { useUserStore } from '@/stores/user'

const props = defineProps<{
  courses: any[]
  planSelectedCourseId: number | null
}>()

const emit = defineEmits(['update:planSelectedCourseId'])

const userStore = useUserStore()
const internalPlanSelectedCourseId = ref(props.planSelectedCourseId)
const plans = ref<any[]>([])
const loadingPlans = ref(false)
const createPlanDialogVisible = ref(false)
const planDateRange = ref<[string, string] | null>(null)
const newTaskContent = ref('')

const newPlan = ref({
  courseId: null as number | null,
  title: '',
  description: '',
  startDate: '',
  endDate: '',
  tasks: [] as string[]
})

watch(() => props.planSelectedCourseId, (newVal) => {
  internalPlanSelectedCourseId.value = newVal
  if (newVal) handlePlanCourseChange(newVal)
})

const handlePlanCourseChange = async (courseId: number) => {
  if (!courseId) return
  emit('update:planSelectedCourseId', courseId)
  loadingPlans.value = true
  try {
    const res: any = await getCoursePlans(courseId, userStore.user?.id)
    plans.value = res.data || []
  } catch (error) {
    ElMessage.error('获取计划列表失败')
  } finally {
    loadingPlans.value = false
  }
}

const openCreatePlanDialog = () => {
  newPlan.value = {
    courseId: internalPlanSelectedCourseId.value,
    title: '',
    description: '',
    startDate: '',
    endDate: '',
    tasks: []
  }
  planDateRange.value = null
  createPlanDialogVisible.value = true
}

const addPlanTask = () => {
  if (!newTaskContent.value.trim()) return
  newPlan.value.tasks.push(newTaskContent.value.trim())
  newTaskContent.value = ''
}

const handleCreatePlan = async () => {
  if (!newPlan.value.title || !newPlan.value.courseId || !planDateRange.value) {
    ElMessage.warning('请填写完整计划信息')
    return
  }
  
  newPlan.value.startDate = planDateRange.value[0]
  newPlan.value.endDate = planDateRange.value[1]
  
  try {
    const planRes: any = await createPlan({
      courseId: newPlan.value.courseId,
      userId: userStore.user?.id,
      title: newPlan.value.title,
      description: newPlan.value.description,
      startDate: newPlan.value.startDate,
      endDate: newPlan.value.endDate
    })
    const planId = planRes.data.id
    
    for (const taskContent of newPlan.value.tasks) {
      await addTask(planId, taskContent)
    }
    
    ElMessage.success('计划模板创建成功')
    createPlanDialogVisible.value = false
    handlePlanCourseChange(newPlan.value.courseId)
  } catch (error: any) {
    ElMessage.error(error.message || '创建失败')
  }
}

const handleDistributePlan = async (plan: any) => {
  if (!internalPlanSelectedCourseId.value) return
  try {
    await distributePlan(plan.id, internalPlanSelectedCourseId.value)
    ElMessage.success('计划已成功下发给所有课程学生')
  } catch (error: any) {
    ElMessage.error(error.message || '下发失败')
  }
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.mb-2 { margin-bottom: 0.5rem; }
.ml-2 { margin-left: 0.5rem; }
.mr-2 { margin-right: 0.5rem; }
.mr-1 { margin-right: 0.25rem; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-start { display: flex; align-items: center; }
</style>
