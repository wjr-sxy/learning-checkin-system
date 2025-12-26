<template>
  <div class="teacher-tasks">
    <div class="mb-4 font-bold text-lg">教师任务</div>
    
    <el-row :gutter="20">
      <el-col :span="8" v-for="item in sortedTasks" :key="item.task.id">
        <el-card class="mb-4 task-card" :class="{ 'urgent': isUrgent(item.task.deadline) }">
          <template #header>
            <div class="flex justify-between items-center">
              <span class="truncate font-bold" style="max-width: 150px;" :title="item.task.title">{{ item.task.title }}</span>
              <el-tag :type="getStatusType(item)">{{ getStatusText(item) }}</el-tag>
            </div>
          </template>
          
          <div v-if="item.task.isRecurring" class="flex flex-col items-center mb-4">
             <el-progress type="dashboard" :percentage="getProgress(item)" :color="getProgressColor(item)">
                <template #default="{}">
                  <span class="text-xl font-bold">{{ item.checkinCount }}/{{ item.totalDays }}</span>
                  <div class="text-xs text-gray-500">已打卡</div>
                </template>
             </el-progress>
             <div v-if="item.todayChecked" class="text-success font-bold mt-2">今日已打卡</div>
             <div v-else class="text-warning font-bold mt-2">今日未打卡</div>
          </div>

          <div v-else class="text-gray-600 mb-2 line-clamp-2" style="height: 40px; overflow: hidden;">{{ item.task.content }}</div>
          
          <div class="flex justify-between text-sm text-gray-500 mb-2">
            <span>{{ item.task.isRecurring ? '结束: ' + formatDate(item.task.endDate) : '截止: ' + formatDate(item.task.deadline) }}</span>
            <span>积分: {{ item.task.rewardPoints }}</span>
          </div>
          
          <div v-if="!item.submission && !item.task.isRecurring" class="text-orange-500 text-sm mb-2">
            倒计时: {{ getCountdown(item.task.deadline) }}
          </div>

          <div class="flex justify-between items-center mt-4">
             <el-button 
               type="warning" 
               plain 
               size="small" 
               @click="viewLeaderboard(item.task)" 
               v-if="!item.task.isRecurring"
               class="leaderboard-btn"
             >
                 <el-icon class="mr-1"><Trophy /></el-icon>速度榜
             </el-button>
             <div v-else></div> <!-- Spacer -->

             <el-button type="primary" size="small" @click="openSubmitDialog(item)" 
               :disabled="isSubmitDisabled(item)">
               {{ getButtonText(item) }}
             </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Submit Dialog -->
    <el-dialog v-model="showSubmitDialog" :title="currentTask?.title" width="600px">
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="内容">
           <el-input 
             v-model="submitForm.content" 
             type="textarea" 
             rows="6" 
             maxlength="5000"
             show-word-limit
             placeholder="请输入作业内容（最多5000字）"
           />
        </el-form-item>
        <el-form-item label="附件/图片">
            <el-upload
                v-model:file-list="fileList"
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :on-remove="handleRemove"
                list-type="picture-card"
                :limit="3"
            >
                <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="text-gray-400 text-xs mt-1">支持图片(自动压缩WebP)和文档，单个最大5MB</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubmitDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <!-- Leaderboard Dialog -->
    <el-dialog v-model="showLeaderboardDialog" title="速度排行榜 (前10名)" width="600px">
        <el-alert title="仅展示截止前50%时间内完成的提交" type="info" show-icon :closable="false" class="mb-4" />
        <el-table :data="leaderboardData" stripe>
            <el-table-column type="index" label="排名" width="60">
                <template #default="scope">
                    <div class="flex justify-center">
                        <span v-if="scope.$index < 3" class="text-lg">
                            {{ scope.$index === 0 ? '🥇' : scope.$index === 1 ? '🥈' : '🥉' }}
                        </span>
                        <span v-else>{{ scope.$index + 1 }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="学生" width="150">
                <template #default="scope">
                    <div class="flex items-center">
                        <el-avatar :size="24" :src="scope.row.avatar" class="mr-2" />
                        <span class="truncate">{{ scope.row.studentName }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="duration" label="耗时" />
            <el-table-column prop="submitTime" label="提交时间">
                <template #default="scope">
                    {{ formatDate(scope.row.submitTime) }}
                </template>
            </el-table-column>
        </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getStudentTasks, submitTask, getTaskLeaderboard } from '../../api/task'
import { ElMessage } from 'element-plus'
import { Trophy, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

interface Task {
  id: number
  title: string
  content: string
  deadline: string
  endDate: string
  isRecurring: boolean
  rewardPoints: number
  contentTemplate?: string
}

interface Submission {
  id: number
  status: number // 0: submitted, 1: graded, 2: returned
  content: string
  fileUrls: string
}

interface StudentTaskItem {
  task: Task
  submission: Submission | null
  checkinCount: number
  totalDays: number
  todayChecked: boolean
}

interface LeaderboardItem {
    studentName: string
    avatar: string
    duration: string
    submitTime: string
}

const tasks = ref<StudentTaskItem[]>([])
const showSubmitDialog = ref(false)
const showLeaderboardDialog = ref(false)
const currentTask = ref<Task | null>(null)
const leaderboardData = ref<LeaderboardItem[]>([])
const submitting = ref(false)
const fileList = ref<any[]>([])
const submitForm = ref({
  content: '',
  fileUrls: ''
})

// File handling
const handleFileChange = (uploadFile: any, uploadFiles: any) => {
    // Validate file size/type
    const isLt5M = uploadFile.size / 1024 / 1024 < 5
    if (!isLt5M) {
        ElMessage.error('文件大小不能超过 5MB!')
        // Remove
        if (fileList.value && Array.isArray(fileList.value)) {
            const index = fileList.value.indexOf(uploadFile)
            if (index !== -1) fileList.value.splice(index, 1)
        }
        return
    }
    fileList.value = uploadFiles || []
}

const handleRemove = (_file: any, uploadFiles: any) => {
    fileList.value = uploadFiles || []
}

const uploadFileToServer = async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    
    // Use fetch or axios to upload
    // Here assume we use the global request or fetch
    const token = sessionStorage.getItem('active_token')
    // Fix: Use /api prefix to go through proxy
    const response = await fetch('/api/tasks/upload', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${token}`
        },
        body: formData
    })
    
    const res = await response.json()
    if (res.code === 200) {
        return res.data.url
    } else {
        throw new Error(res.message || 'Upload failed')
    }
}

const compressImage = (file: File): Promise<Blob> => {
    return new Promise((resolve, reject) => {
        if (!file.type.startsWith('image/')) {
            resolve(file)
            return
        }

        const reader = new FileReader()
        reader.readAsDataURL(file)
        reader.onload = (e: any) => {
            const img = new Image()
            img.src = e.target.result
            img.onload = () => {
                const canvas = document.createElement('canvas')
                canvas.width = img.width
                canvas.height = img.height
                const ctx = canvas.getContext('2d')
                ctx?.drawImage(img, 0, 0)
                // Compress to WebP
                canvas.toBlob((blob) => {
                    if (blob) resolve(blob)
                    else reject(new Error('Compression failed'))
                }, 'image/webp', 0.8)
            }
        }
        reader.onerror = reject
    })
}


const loadTasks = async () => {
  try {
    const res = await getStudentTasks()
    tasks.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const sortedTasks = computed(() => {
  return tasks.value.slice().sort((a, b) => {
    // Urgent (<24h) first
    const aUrgent = isUrgent(a.task.deadline)
    const bUrgent = isUrgent(b.task.deadline)
    if (aUrgent && !bUrgent) return -1
    if (!aUrgent && bUrgent) return 1
    return 0
  })
})

const isUrgent = (deadline: string) => {
  if (!deadline) return false
  const diff = dayjs(deadline).diff(dayjs(), 'hour')
  return diff >= 0 && diff < 24
}

const getProgress = (item: any) => {
    if (!item.totalDays || item.totalDays === 0) return 0
    return Math.min(100, Math.floor((item.checkinCount / item.totalDays) * 100))
}

const getProgressColor = (item: any) => {
    const p = getProgress(item)
    if (p >= 100) return '#67C23A'
    if (p >= 60) return '#409EFF'
    return '#E6A23C'
}

const isSubmitDisabled = (item: any) => {
    if (item.task.isRecurring) {
        return item.todayChecked
    }
    return item.submission && item.submission.status !== 2
}

const getStatusType = (item: any) => {
  if (item.task.isRecurring) {
      if (item.todayChecked) return 'success'
      return 'warning'
  }
  if (item.submission) {
     if (item.submission.status === 1) return 'success' // Graded
     if (item.submission.status === 2) return 'danger' // Returned
     return 'success' // Submitted
  }
  return 'warning' // Ongoing
}

const getStatusText = (item: any) => {
  if (item.task.isRecurring) {
      if (item.todayChecked) return '今日已打卡'
      return '今日未打卡'
  }
  if (item.submission) {
     if (item.submission.status === 1) return '已完成'
     if (item.submission.status === 2) return '已退回'
     return '已提交'
  }
  return '进行中'
}

const getButtonText = (item: any) => {
  if (item.task.isRecurring) {
      if (item.todayChecked) return '已打卡'
      return '打卡'
  }
  if (item.submission) {
    if (item.submission.status === 2) return '重新提交'
    return '已提交'
  }
  return '去提交'
}

const formatDate = (date: string) => {
    return date ? dayjs(date).format('MM-DD') : '-' // Short format for cards
}

const getCountdown = (deadline: string) => {
    if (!deadline) return '-'
    const now = dayjs()
    const end = dayjs(deadline)
    const diff = end.diff(now)
    if (diff <= 0) return '已截止'
    
    const d = Math.floor(diff / (1000 * 60 * 60 * 24))
    const h = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    
    if (d > 0) return `${d}天 ${h}小时`
    return `${h}小时 ${m}分`
}

const openSubmitDialog = (item: any) => {
  currentTask.value = item.task
  submitForm.value.content = ''
  submitForm.value.fileUrls = ''
  fileList.value = []
  
  // Restore draft if needed (not explicitly required for student, but good UX)
  // Requirement says "Submitted -> Returned -> To be submitted (Keep history)"
  if (item.submission && item.submission.status === 2) { // Returned
      submitForm.value.content = item.submission.content
      // parse files?
      try {
         JSON.parse(item.submission.fileUrls)
         // We can't easily restore fileList objects from URLs for re-upload,
         // but we can show them or just let user re-upload.
         // For simplicity, we just put content back.
      } catch(e) {}
  }
  
  showSubmitDialog.value = true
}

const handleSubmit = async () => {
    if (!currentTask.value) return
    
    if (!submitForm.value.content && fileList.value.length === 0) {
        ElMessage.warning('请输入内容或上传附件')
        return
    }
    
    submitting.value = true
    try {
        const urls = []
        for (const fileItem of fileList.value) {
            const file = fileItem.raw
            if (file) {
                // Compress if image
                let fileToUpload = file
                if (file.type.startsWith('image/')) {
                    try {
                        const blob = await compressImage(file)
                        // Create a new file from blob, preserving name but changing extension
                        fileToUpload = new File([blob], file.name.replace(/\.[^/.]+$/, "") + ".webp", { type: 'image/webp' })
                    } catch(e) {
                        console.warn('Compression failed, using original', e)
                    }
                }
                
                const url = await uploadFileToServer(fileToUpload)
                urls.push(url)
            } else if (fileItem.url) {
                urls.push(fileItem.url)
            }
        }
        
        const payload = {
            content: submitForm.value.content,
            fileUrls: JSON.stringify(urls)
        }
        
        const isRecurring = currentTask.value.isRecurring
        await submitTask(currentTask.value.id, payload)
        
        if (isRecurring) {
             ElMessage.success('打卡成功！坚持就是胜利！')
        } else {
             ElMessage.success('提交成功')
        }
        
        showSubmitDialog.value = false
        loadTasks()
    } catch (e: any) {
        ElMessage.error(e.message || '提交失败')
    } finally {
        submitting.value = false
    }
}

const viewLeaderboard = async (task: any) => {
    try {
        const res = await getTaskLeaderboard(task.id)
        leaderboardData.value = res.data
        showLeaderboardDialog.value = true
    } catch (e) {
        ElMessage.error('获取排行榜失败')
    }
}

let timer: any
onMounted(() => {
    loadTasks()
    timer = setInterval(() => {
        // Force refresh for countdown
        tasks.value = [...tasks.value] 
    }, 60000)
})

onUnmounted(() => {
    if (timer) clearInterval(timer)
})
</script>

<style scoped>
.urgent {
  border-left: 4px solid #ff4949;
}
.task-card {
  /* height: 220px; */
  margin-bottom: 20px;
}
</style>