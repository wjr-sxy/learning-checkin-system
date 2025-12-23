<template>
  <div class="study-plan-container">
    <el-header class="main-header">
      <div class="header-content container">
        <div class="logo-section">
            <h1 @click="router.push('/')" style="cursor: pointer;">学习打卡系统</h1>
        </div>
        <div class="header-actions">
             <el-button @click="router.push('/')">返回仪表盘</el-button>
        </div>
      </div>
    </el-header>

    <el-main class="container">
      <div class="page-header mb-4">
        <div class="flex justify-between items-center">
          <h2>我的学习计划</h2>
          <div class="flex items-center">
            <div class="mr-4" style="width: 200px;">
              <div class="text-xs text-gray-500 mb-1">今日自建计划积分 ({{ dailyPoints }}/10)</div>
              <el-progress :percentage="dailyPoints * 10" :format="() => dailyPoints" status="success" />
            </div>
            <el-button type="primary" @click="dialogVisible = true">
                <el-icon class="el-icon--left"><Plus /></el-icon>创建新计划
            </el-button>
          </div>
        </div>
      </div>

      <el-card shadow="hover" v-if="plans.length === 0">
          <el-empty description="暂无计划，快去创建一个吧！" />
      </el-card>

      <draggable 
        v-model="plans" 
        item-key="id" 
        handle=".drag-handle"
        animation="300"
        @end="handleDragEnd"
      >
        <template #item="{element}">
            <el-card shadow="hover" class="plan-item mb-2">
                <div class="plan-content">
                    <div class="drag-handle mr-4">
                        <el-icon><Rank /></el-icon>
                    </div>
                    <div class="plan-info">
                        <div class="d-flex justify-content-between align-items-center">
                            <h3>{{ element.title }}</h3>
                            <span class="text-muted small">
                                <span v-if="element.totalTasks > 0" class="mr-2">{{ element.completedTasks }}/{{ element.totalTasks }} 任务</span>
                                <strong>{{ element.progressPercentage || 0 }}%</strong>
                            </span>
                        </div>
                        <p class="text-secondary">{{ element.description }}</p>
                        
                        <div class="mb-2" v-if="element.totalTasks > 0">
                            <el-progress 
                                :percentage="element.progressPercentage || 0" 
                                :format="(p: number) => p + '%'"
                                :status="element.status === 1 ? 'success' : ''"
                            />
                        </div>

                        <div class="plan-meta">
                            <el-tag size="small" type="info" class="mr-2">
                                <el-icon><Timer /></el-icon> {{ element.targetHours }}h (剩 {{ getRemainingHours(element) }}h)
                            </el-tag>
                            <el-tag size="small" type="info" class="mr-2">
                                <el-icon><Calendar /></el-icon> {{ element.startDate }} ~ {{ element.endDate }}
                            </el-tag>
                            <el-tag :type="getStatusType(element.status)">
                                {{ getStatusText(element.status) }}
                            </el-tag>
                        </div>
                    </div>
                    <div class="plan-actions">
                 <el-button 
                  v-if="element.status === 0" 
                  size="small" 
                  type="primary" 
                  plain
                  :icon="Refresh"
                  @click="handleOpenProgress(element)"
                >
                  进度
                </el-button>
                <el-button
                  v-if="element.status === 0"
                  size="small"
                  type="warning"
                  plain
                  :icon="List"
                  @click="handleManageTasks(element)"
                >
                  任务
                </el-button>
                 <el-button 
                  size="small" 
                  type="info" 
                  plain
                  :icon="List"
                  @click="handleShowHistory(element.id)"
                >
                  历史
                </el-button>
                 <el-button 
                  v-if="element.status === 0" 
                  size="small" 
                  type="success" 
                  @click="handleComplete(element.id)"
                >
                  完成
                </el-button>
                <el-button 
                  size="small" 
                  type="primary" 
                  :icon="Edit"
                  @click="handleEdit(element)"
                >
                  编辑
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  :icon="Delete"
                  @click="handleDelete(element.id)"
                >
                  删除
                </el-button>
            </div>
        </div>
    </el-card>
</template>
</draggable>

<!-- Create/Edit Plan Dialog -->
<el-dialog 
  v-model="dialogVisible" 
  :title="isEdit ? '编辑学习计划' : '创建学习计划'" 
  width="500px" 
  destroy-on-close
  @close="resetForm"
>
<el-form :model="form" label-width="100px">
  <el-form-item label="标题">
    <el-input v-model="form.title" placeholder="例如：学习 Vue 3" />
  </el-form-item>
  <el-form-item label="描述">
    <el-input v-model="form.description" type="textarea" placeholder="详细目标..." />
  </el-form-item>
  <el-form-item label="目标工时">
    <el-input-number v-model="form.targetHours" :min="1" />
  </el-form-item>
  <el-form-item label="起止日期">
     <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        style="width: 100%"
      />
  </el-form-item>
</el-form>
<template #footer>
  <span class="dialog-footer">
    <el-button @click="dialogVisible = false">取消</el-button>
    <el-button type="primary" @click="handleSave">确认</el-button>
  </span>
</template>
</el-dialog>

<!-- Update Progress Dialog -->
<el-dialog
  v-model="progressDialogVisible"
  title="更新学习进度"
  width="400px"
  @close="resetProgressForm"
>
  <el-form :model="progressForm" label-width="80px">
    <el-form-item label="更新方式">
      <el-radio-group v-model="progressMode">
        <el-radio label="task">按任务数</el-radio>
        <el-radio label="percentage">直接输入百分比</el-radio>
      </el-radio-group>
    </el-form-item>
    
    <template v-if="progressMode === 'task'">
      <el-form-item label="总任务数">
        <el-input-number v-model="progressForm.totalTasks" :min="1" />
      </el-form-item>
      <el-form-item label="已完成">
        <el-input-number v-model="progressForm.completedTasks" :min="0" :max="progressForm.totalTasks" />
      </el-form-item>
    </template>
    
    <template v-else>
      <el-form-item label="当前进度">
        <el-input-number v-model="progressForm.completedTasks" :min="0" :max="100" />
        <span class="ml-2">%</span>
      </el-form-item>
    </template>

    <el-form-item label="备注">
      <el-input v-model="progressForm.note" placeholder="例如：完成了第一章" />
    </el-form-item>
  </el-form>
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="progressDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveProgress">保存</el-button>
    </span>
  </template>
</el-dialog>

<!-- Task Management Dialog -->
<el-dialog
  v-model="tasksDialogVisible"
  title="子任务管理"
  width="600px"
  @close="resetTaskForm"
>
  <div class="mb-3">
      <el-form :inline="true" :model="newTaskForm" class="demo-form-inline">
        <el-form-item label="任务名称">
          <el-input v-model="newTaskForm.name" placeholder="输入任务名称" />
        </el-form-item>
        <el-form-item label="截止日期">
            <el-date-picker
                v-model="newTaskForm.deadline"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 150px"
            />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleAddTask">添加</el-button>
        </el-form-item>
      </el-form>
  </div>

  <el-table :data="taskList" style="width: 100%" max-height="400">
    <el-table-column width="40">
        <template #default="scope">
             <el-checkbox 
                v-model="scope.row.isCompleted" 
                @change="(val: any) => handleTaskStatusChange(scope.row, val)"
             />
        </template>
    </el-table-column>
    <el-table-column prop="name" label="任务名称" />
    <el-table-column prop="deadline" label="截止日期" width="120" />
    <el-table-column label="操作" width="80">
        <template #default="scope">
            <el-button 
                type="danger" 
                size="small" 
                link
                @click="handleDeleteTask(scope.row.id)"
            >
                删除
            </el-button>
        </template>
    </el-table-column>
  </el-table>
</el-dialog>

<!-- History Drawer -->
<el-drawer
  v-model="historyDrawerVisible"
  title="进度历史记录"
  direction="rtl"
  size="40%"
>
  <el-timeline>
    <el-timeline-item
      v-for="(activity, index) in historyList"
      :key="index"
      :timestamp="activity.createTime"
      placement="top"
    >
      <el-card>
        <h4>进度更新: {{ activity.previousProgress }}% -> {{ activity.newProgress }}%</h4>
        <p>完成任务: {{ activity.completedTasks }} / {{ activity.totalTasks }}</p>
        <p v-if="activity.note">备注: {{ activity.note }}</p>
      </el-card>
    </el-timeline-item>
  </el-timeline>
</el-drawer>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { 
    getUserPlans, createPlan, updatePlan, deletePlan, completePlan, updateProgress, getProgressHistory,
    getPlanTasks, addTask, deleteTask, updateTaskStatus, getDailyPoints
} from '../api/studyPlan'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Rank, Timer, Calendar, Refresh, List } from '@element-plus/icons-vue'
import draggable from 'vuedraggable'

const router = useRouter()
const userStore = useUserStore()
const plans = ref<any[]>([])
const dailyPoints = ref(0)
const dialogVisible = ref(false)
const progressDialogVisible = ref(false)
const historyDrawerVisible = ref(false)
const tasksDialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const currentPlanId = ref<number | null>(null)
const dateRange = ref<any[]>([])
const historyList = ref<any[]>([])
const taskList = ref<any[]>([])

const form = ref<any>({
  title: '',
  description: '',
  targetHours: 10,
  startDate: '',
  endDate: ''
})

const progressForm = ref({
  completedTasks: 0,
  totalTasks: 0,
  note: ''
})

const newTaskForm = ref({
    name: '',
    deadline: ''
})

const resetForm = () => {
  form.value = {
    title: '',
    description: '',
    targetHours: 10,
    startDate: '',
    endDate: ''
  }
  dateRange.value = []
  isEdit.value = false
  editingId.value = null
}

const progressMode = ref('task') // 'task' or 'percentage'

const resetProgressForm = () => {
  progressForm.value = {
    completedTasks: 0,
    totalTasks: 0,
    note: ''
  }
  progressMode.value = 'task'
  currentPlanId.value = null
}

const resetTaskForm = () => {
    newTaskForm.value = { name: '', deadline: '' }
    taskList.value = []
    currentPlanId.value = null
}

const loadPlans = async () => {
  console.log('Loading plans for user:', userStore.user)
  if (userStore.user) {
    try {
      const res: any = await getUserPlans(userStore.user.id)
      console.log('Plans loaded:', res)
      plans.value = res.data
      
      const pointsRes: any = await getDailyPoints(userStore.user.id)
      dailyPoints.value = pointsRes.data
    } catch (error) {
      console.error('Error loading plans:', error)
    }
  } else {
    console.warn('User not found in store')
  }
}

onMounted(() => {
    if (!userStore.token) {
        router.push('/login')
    } else {
        loadPlans()
    }
})

const handleDragEnd = () => {
  // Logic to save the new order if needed
  console.log('Drag ended, new order:', plans.value)
}

const getStatusText = (status: number) => {
  switch (status) {
    case 0: return '进行中'
    case 1: return '已完成'
    case 2: return '已过期'
    default: return '未知'
  }
}

const getStatusType = (status: number) => {
    switch (status) {
    case 0: return 'primary'
    case 1: return 'success'
    case 2: return 'info'
    default: return 'info'
  }
}

const getRemainingHours = (plan: any) => {
  if (!plan.targetHours) return 0
  if (!plan.progressPercentage) return plan.targetHours
  const remaining = plan.targetHours * (1 - plan.progressPercentage / 100)
  return Math.round(remaining * 10) / 10 // Keep 1 decimal
}

const handleEdit = (plan: any) => {
  isEdit.value = true
  editingId.value = plan.id
  form.value = {
    title: plan.title,
    description: plan.description,
    targetHours: plan.targetHours,
    startDate: plan.startDate,
    endDate: plan.endDate
  }
  dateRange.value = [plan.startDate, plan.endDate]
  dialogVisible.value = true
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个学习计划吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePlan(id)
    ElMessage.success('删除成功')
    loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSave = async () => {
  if (!userStore.user) return
  if (!dateRange.value || dateRange.value.length !== 2) {
      ElMessage.warning('请选择起止日期')
      return
  }
  
  form.value.startDate = dateRange.value[0]
  form.value.endDate = dateRange.value[1]
  
  try {
    if (isEdit.value && editingId.value) {
      await updatePlan({ 
        ...form.value, 
        id: editingId.value,
        userId: userStore.user.id 
      })
      ElMessage.success('计划更新成功')
    } else {
      await createPlan({ ...form.value, userId: userStore.user.id })
      ElMessage.success('计划创建成功')
    }
    dialogVisible.value = false
    loadPlans()
    resetForm()
  } catch (error: any) {
    ElMessage.error(error.message || (isEdit.value ? '更新失败' : '创建失败'))
  }
}

const handleComplete = async (id: number) => {
  try {
    await completePlan(id)
    ElMessage.success('计划完成！')
    // Update user points locally
    if(userStore.user) {
        // userStore.user.points += 50 // No longer static 50
        // We reload plans to get updated points and daily stats
        loadPlans()
        // Reload user info to get latest total points
        userStore.initUser()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleOpenProgress = (plan: any) => {
  currentPlanId.value = plan.id
  
  if (plan.totalTasks === 100 || plan.totalTasks === 0) {
      progressMode.value = 'percentage'
      progressForm.value = {
        completedTasks: Math.round(plan.progressPercentage || 0),
        totalTasks: 100,
        note: ''
      }
  } else {
      progressMode.value = 'task'
      progressForm.value = {
        completedTasks: plan.completedTasks || 0,
        totalTasks: plan.totalTasks || 0,
        note: ''
      }
  }
  progressDialogVisible.value = true
}

const saveProgress = async () => {
  if (!currentPlanId.value) return
  try {
    if (progressMode.value === 'percentage') {
        progressForm.value.totalTasks = 100
    }
    
    await updateProgress(currentPlanId.value, progressForm.value)
    ElMessage.success('进度更新成功')
    progressDialogVisible.value = false
    loadPlans()
    resetProgressForm()
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
  }
}

const handleShowHistory = async (planId: number) => {
  try {
    const res: any = await getProgressHistory(planId)
    historyList.value = res.data
    historyDrawerVisible.value = true
  } catch (error) {
    console.error(error)
    ElMessage.error('获取历史记录失败')
  }
}

// Task Management Functions
const handleManageTasks = async (plan: any) => {
    currentPlanId.value = plan.id
    try {
        const res: any = await getPlanTasks(plan.id)
        taskList.value = res.data.map((t: any) => ({
            ...t,
            isCompleted: t.status === 1
        }))
        tasksDialogVisible.value = true
    } catch (error) {
        ElMessage.error('加载任务失败')
    }
}

const handleAddTask = async () => {
    if (!currentPlanId.value) return
    if (!newTaskForm.value.name) {
        ElMessage.warning('请输入任务名称')
        return
    }

    try {
        await addTask(currentPlanId.value, newTaskForm.value)
        ElMessage.success('添加成功')
        newTaskForm.value.name = '' // Reset name only, keep date maybe? No, reset both better
        newTaskForm.value.deadline = ''
        
        // Reload tasks
        const res: any = await getPlanTasks(currentPlanId.value)
        taskList.value = res.data.map((t: any) => ({
            ...t,
            isCompleted: t.status === 1
        }))
        
        // Reload plans to update progress
        loadPlans()
    } catch (error: any) {
        ElMessage.error(error.message || '添加失败')
    }
}

const handleDeleteTask = async (taskId: number) => {
    try {
        await deleteTask(taskId)
        ElMessage.success('删除成功')
        if (currentPlanId.value) {
            const res: any = await getPlanTasks(currentPlanId.value)
            taskList.value = res.data.map((t: any) => ({
                ...t,
                isCompleted: t.status === 1
            }))
            loadPlans()
        }
    } catch (error) {
        ElMessage.error('删除失败')
    }
}

const handleTaskStatusChange = async (task: any, val: boolean) => {
    try {
        await updateTaskStatus(task.id, val ? 1 : 0)
        // Reload plans to update progress
        loadPlans()
    } catch (error) {
        ElMessage.error('状态更新失败')
        task.isCompleted = !val // Revert on error
    }
}
</script>

<style scoped>
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
