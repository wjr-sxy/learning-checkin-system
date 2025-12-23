<template>
  <div class="grading-workbench">
    <!-- Header -->
    <div class="workbench-header">
      <div class="header-left">
        <el-button icon="Back" circle class="mr-4" @click="$router.back()" />
        <div>
           <h1 class="header-title">批改工作台</h1>
           <p class="header-subtitle" v-if="taskTitle">任务: {{ taskTitle }}</p>
        </div>
      </div>
      <div class="header-right">
         <el-progress 
            type="circle" 
            :percentage="progressPercentage" 
            :width="40" 
            :stroke-width="4"
            :show-text="false"
         />
         <div class="progress-info">
            <div class="label">批改进度</div>
            <div class="value">{{ gradedCount }} / {{ totalCount }}</div>
         </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="workbench-body">
      <!-- Left Sidebar: Student List -->
      <div class="student-list-sidebar">
        <!-- Filter -->
        <div class="list-filter">
           <el-input 
             v-model="searchQuery" 
             placeholder="搜索学生ID/姓名" 
             prefix-icon="Search"
             clearable
             class="search-input"
           />
           <el-radio-group v-model="filterStatus" size="small" class="status-filter">
             <el-radio-button label="ALL">全部</el-radio-button>
             <el-radio-button label="PENDING">未批</el-radio-button>
             <el-radio-button label="GRADED">已批</el-radio-button>
           </el-radio-group>
        </div>

        <!-- List -->
        <div class="list-content">
           <div 
             v-for="sub in filteredSubmissions" 
             :key="sub.id"
             class="list-item"
             :class="{'active': currentSubmission?.id === sub.id}"
             @click="selectSubmission(sub)"
           >
              <div class="item-header">
                 <span class="student-name">学生 {{ sub.studentId }}</span>
                 <el-tag size="small" :type="getStatusType(sub.status)">{{ getStatusText(sub.status) }}</el-tag>
              </div>
              <div class="item-time">
                 提交于: {{ formatDate(sub.submitTime) }}
              </div>
              <div class="item-preview" :title="sub.content">
                 {{ sub.content }}
              </div>
           </div>
           <el-empty v-if="filteredSubmissions.length === 0" description="暂无数据" :image-size="60" />
        </div>
      </div>

      <!-- Right Content: Grading Area -->
      <div class="grading-area">
        <template v-if="currentSubmission">
           <!-- Submission Content -->
           <div class="submission-content">
              <div class="content-wrapper">
                 <!-- Student Info Header -->
                 <div class="student-info-header">
                    <div class="student-basic">
                       <el-avatar :size="48" class="mr-4">{{ currentSubmission.studentId }}</el-avatar>
                       <div>
                          <h2 class="student-title">学生 {{ currentSubmission.studentId }} 的提交</h2>
                          <div class="submission-meta">
                             <el-icon class="mr-1"><Clock /></el-icon>
                             {{ formatDate(currentSubmission.submitTime) }}
                             <span class="separator">|</span>
                             <span class="similarity" v-if="currentSubmission.similarityScore">
                                <el-icon class="mr-1"><CopyDocument /></el-icon>
                                相似度: {{ currentSubmission.similarityScore }}%
                             </span>
                          </div>
                       </div>
                    </div>
                    <div>
                       <el-button type="primary" link @click="viewHistory">查看历史版本</el-button>
                    </div>
                 </div>

                 <!-- Content Body -->
                 <div class="content-body">
                    <h3 class="section-label">文本内容</h3>
                    <div class="text-content">
                       {{ currentSubmission.content }}
                    </div>
                 </div>

                 <!-- Attachments -->
                 <div v-if="currentSubmission.fileUrls" class="attachments-section">
                    <h3 class="section-label">附件</h3>
                    <div class="attachment-list">
                       <!-- Mock rendering for file urls -->
                       <a 
                         v-for="(url, index) in currentSubmission.fileUrls.split(',')" 
                         :key="index"
                         :href="url"
                         target="_blank"
                         class="attachment-item"
                       >
                          <el-icon class="mr-2"><Document /></el-icon>
                          <span>附件 {{ index + 1 }}</span>
                       </a>
                    </div>
                 </div>
              </div>
           </div>

           <!-- Grading Panel (Bottom Fixed) -->
           <div class="grading-panel">
              <div class="panel-inner">
                 <!-- Score -->
                 <div class="score-section">
                    <label class="form-label">评分 (满分 {{ taskPoints }})</label>
                    <el-input-number v-model="gradeForm.score" :min="0" :max="taskPoints" class="w-full" />
                    
                    <div class="rating-wrapper">
                       <label class="form-label">等级评定</label>
                       <el-rate v-model="gradeForm.rating" />
                    </div>
                 </div>

                 <!-- Comment -->
                 <div class="comment-section">
                    <div class="comment-header">
                       <label class="form-label">评语</label>
                       <el-button type="primary" link size="small" @click="showTemplates = true">
                          <el-icon class="mr-1"><Collection /></el-icon>
                          使用模板
                       </el-button>
                    </div>
                    <el-input 
                      v-model="gradeForm.comment" 
                      type="textarea" 
                      :rows="4" 
                      placeholder="请输入评语..."
                    />
                 </div>

                 <!-- Actions -->
                 <div class="action-buttons">
                    <el-button type="primary" @click="handleGrade">提交评分</el-button>
                    <el-button type="warning" plain @click="handleReturn">打回重做</el-button>
                 </div>
              </div>
           </div>
        </template>
        <div v-else class="empty-state">
           <div class="empty-content">
              <el-icon :size="64" class="mb-4"><EditPen /></el-icon>
              <p>请从左侧选择一名学生开始批改</p>
           </div>
        </div>
      </div>
    </div>

    <!-- Template Drawer -->
    <el-drawer v-model="showTemplates" title="评语模板库" size="400px">
       <div class="template-drawer-content">
          <div class="add-template-box">
             <el-input v-model="newTemplateContent" placeholder="输入新模板内容" />
             <el-button type="primary" @click="addTemplate">添加</el-button>
          </div>
          
          <div class="template-list">
             <div 
               v-for="tpl in templates" 
               :key="tpl.id"
               class="template-item group"
             >
                <div class="template-text" @click="applyTemplate(tpl)">
                   {{ tpl.content }}
                </div>
                <div class="template-meta">
                   <span>使用次数: {{ tpl.usageCount }}</span>
                   <el-button 
                     type="danger" 
                     link 
                     size="small" 
                     class="delete-btn"
                     @click.stop="removeTemplate(tpl.id)"
                   >
                      删除
                   </el-button>
                </div>
             </div>
          </div>
       </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getSubmissions, gradeSubmission, returnSubmission, getTemplates, createTemplate, deleteTemplate, useTemplate, getTask } from '../../api/task'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { Clock, CopyDocument, Document, Collection, EditPen } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const taskId = Number(route.params.taskId)

// State
const submissions = ref<any[]>([])
const templates = ref<any[]>([])
const taskTitle = ref('加载中...')
const taskPoints = ref(100) // Should fetch from task details
const currentSubmission = ref<any>(null)
const searchQuery = ref('')
const filterStatus = ref('ALL')
const showTemplates = ref(false)
const newTemplateContent = ref('')

const gradeForm = ref({
    score: 0,
    rating: 0,
    comment: ''
})

// Computed
const filteredSubmissions = computed(() => {
    return submissions.value.filter(sub => {
        const matchesSearch = sub.studentId.toString().includes(searchQuery.value)
        const matchesStatus = filterStatus.value === 'ALL' 
            ? true 
            : filterStatus.value === 'PENDING' ? sub.status === 0
            : sub.status !== 0 // GRADED or RETURNED
        return matchesSearch && matchesStatus
    })
})

const totalCount = computed(() => submissions.value.length)
const gradedCount = computed(() => submissions.value.filter(s => s.status === 1).length)
const progressPercentage = computed(() => totalCount.value ? Math.round((gradedCount.value / totalCount.value) * 100) : 0)

// Methods
const loadData = async () => {
    try {
        // Load task details
        const taskRes: any = await getTask(taskId)
        if (taskRes.data) {
            taskTitle.value = taskRes.data.title
            taskPoints.value = taskRes.data.rewardPoints || 100
        }

        const res: any = await getSubmissions(taskId, { size: 1000 }) // Fetch all for now
        submissions.value = res.data.records
    } catch (error) {
        console.error(error)
        ElMessage.error('加载数据失败')
    }
}

const loadTemplates = async () => {
    if (!userStore.user) return
    try {
        const res: any = await getTemplates(userStore.user.id)
        templates.value = res.data
    } catch (error) {
        console.error(error)
    }
}

const selectSubmission = (sub: any) => {
    currentSubmission.value = sub
    gradeForm.value = {
        score: sub.score || 0,
        rating: sub.rating || 0,
        comment: sub.comment || ''
    }
}

const handleGrade = async () => {
    if (!currentSubmission.value) return
    try {
        await gradeSubmission(currentSubmission.value.id, gradeForm.value)
        ElMessage.success('评分成功')
        // Update local state
        currentSubmission.value.status = 1
        currentSubmission.value.score = gradeForm.value.score
        currentSubmission.value.rating = gradeForm.value.rating
        currentSubmission.value.comment = gradeForm.value.comment
        // Move to next?
    } catch (error: any) {
        ElMessage.error(error.message || '评分失败')
    }
}

const handleReturn = async () => {
    if (!currentSubmission.value) return
    if (!gradeForm.value.comment) {
        ElMessage.warning('打回需要填写评语')
        return
    }
    try {
        await returnSubmission(currentSubmission.value.id, gradeForm.value.comment)
        ElMessage.success('已打回')
        currentSubmission.value.status = 2
    } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
    }
}

const addTemplate = async () => {
    if (!newTemplateContent.value || !userStore.user) return
    try {
        const res: any = await createTemplate({
            teacherId: userStore.user.id,
            content: newTemplateContent.value,
            category: 'General'
        })
        templates.value.unshift(res.data)
        newTemplateContent.value = ''
        ElMessage.success('模板添加成功')
    } catch (error) {
        ElMessage.error('添加失败')
    }
}

const removeTemplate = async (id: number) => {
    try {
        await deleteTemplate(id)
        templates.value = templates.value.filter(t => t.id !== id)
        ElMessage.success('删除成功')
    } catch (error) {
        ElMessage.error('删除失败')
    }
}

const applyTemplate = async (tpl: any) => {
    gradeForm.value.comment += (gradeForm.value.comment ? '\n' : '') + tpl.content
    // Increment usage
    try {
        await useTemplate(tpl.id)
        tpl.usageCount++
    } catch (e) {} // Ignore error
    showTemplates.value = false
}

const formatDate = (dateStr: string) => {
    if (!dateStr) return '-'
    return new Date(dateStr).toLocaleString()
}

const getStatusType = (status: number) => {
    switch(status) {
        case 0: return 'info'
        case 1: return 'success'
        case 2: return 'danger'
        default: return 'info'
    }
}

const getStatusText = (status: number) => {
    switch(status) {
        case 0: return '未批'
        case 1: return '已批'
        case 2: return '打回'
        default: return '未知'
    }
}

const viewHistory = () => {
    ElMessage.info('历史版本功能开发中')
}

onMounted(() => {
    loadData()
    loadTemplates()
})
</script>

<style scoped>
.grading-workbench {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background-color: #f9fafb;
}

.workbench-header {
    background-color: white;
    border-bottom: 1px solid #e5e7eb;
    padding: 1rem 1.5rem;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    z-index: 10;
}

.header-left, .header-right {
    display: flex;
    align-items: center;
}

.header-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: #1f2937;
    margin: 0;
}

.header-subtitle {
    font-size: 0.875rem;
    color: #6b7280;
    margin-top: 0.25rem;
}

.header-right .progress-info {
    margin-left: 1rem;
    font-size: 0.875rem;
}

.progress-info .label {
    color: #6b7280;
}

.progress-info .value {
    font-weight: 700;
    color: #1f2937;
}

.workbench-body {
    flex: 1;
    display: flex;
    overflow: hidden;
}

/* Sidebar */
.student-list-sidebar {
    width: 320px;
    background-color: white;
    border-right: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
}

.list-filter {
    padding: 1rem;
    border-bottom: 1px solid #e5e7eb;
}

.search-input {
    margin-bottom: 0.75rem;
}

.status-filter {
    width: 100%;
    display: flex;
}
.status-filter :deep(.el-radio-button) {
    flex: 1;
}
.status-filter :deep(.el-radio-button__inner) {
    width: 100%;
}

.list-content {
    flex: 1;
    overflow-y: auto;
}

.list-item {
    padding: 1rem;
    border-bottom: 1px solid #f3f4f6;
    cursor: pointer;
    transition: background-color 0.2s;
    position: relative;
}

.list-item:hover {
    background-color: #eff6ff;
}

.list-item.active {
    background-color: #eff6ff;
    border-left: 4px solid #3b82f6;
}

.item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.25rem;
}

.student-name {
    font-weight: 500;
    color: #111827;
}

.item-time {
    font-size: 0.75rem;
    color: #6b7280;
    margin-bottom: 0.25rem;
}

.item-preview {
    font-size: 0.75rem;
    color: #9ca3af;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Grading Area */
.grading-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    position: relative;
}

.submission-content {
    flex: 1;
    overflow-y: auto;
    padding: 2rem;
}

.content-wrapper {
    max-width: 56rem; /* max-w-4xl */
    margin: 0 auto;
    background-color: white;
    border-radius: 0.75rem;
    box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
    padding: 2rem;
    min-height: 100%;
}

.student-info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid #e5e7eb;
}

.student-basic {
    display: flex;
    align-items: center;
}

.student-title {
    font-size: 1.125rem;
    font-weight: 700;
    color: #111827;
    margin: 0;
}

.submission-meta {
    font-size: 0.875rem;
    color: #6b7280;
    display: flex;
    align-items: center;
    margin-top: 0.25rem;
}

.submission-meta .separator {
    margin: 0 0.5rem;
}

.similarity {
    color: #059669; /* text-green-600 */
    display: flex;
    align-items: center;
}

.content-body {
    margin-bottom: 2rem;
}

.section-label {
    color: #6b7280;
    font-size: 0.75rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    margin-bottom: 0.75rem;
}

.text-content {
    background-color: #f9fafb;
    padding: 1.5rem;
    border-radius: 0.5rem;
    color: #1f2937;
    white-space: pre-wrap;
    line-height: 1.625;
}

.attachments-section {
    margin-bottom: 2rem;
}

.attachment-list {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;
}

.attachment-item {
    display: flex;
    align-items: center;
    padding: 0.75rem;
    border: 1px solid #e5e7eb;
    border-radius: 0.25rem;
    color: #2563eb;
    text-decoration: none;
    transition: background-color 0.2s;
}

.attachment-item:hover {
    background-color: #f9fafb;
}

/* Grading Panel */
.grading-panel {
    background-color: white;
    border-top: 1px solid #e5e7eb;
    padding: 1rem;
    box-shadow: 0 -4px 6px -1px rgba(0, 0, 0, 0.1);
    z-index: 20;
}

.panel-inner {
    max-width: 56rem;
    margin: 0 auto;
    display: flex;
    align-items: flex-start;
    gap: 1.5rem;
}

.score-section {
    width: 12rem;
}

.form-label {
    display: block;
    font-size: 0.875rem;
    font-weight: 500;
    color: #374151;
    margin-bottom: 0.25rem;
}

.rating-wrapper {
    margin-top: 1rem;
}

.comment-section {
    flex: 1;
}

.comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.25rem;
}

.action-buttons {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-top: 1.5rem;
}

/* Empty State */
.empty-state {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f9fafb;
}

.empty-content {
    text-align: center;
    color: #9ca3af;
}

/* Drawer */
.template-drawer-content {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.add-template-box {
    margin-bottom: 1rem;
    display: flex;
    gap: 0.5rem;
}

.template-list {
    flex: 1;
    overflow-y: auto;
}

.template-item {
    padding: 0.75rem;
    border: 1px solid #e5e7eb;
    border-radius: 0.25rem;
    margin-bottom: 0.75rem;
    background-color: #f9fafb;
    position: relative;
    transition: box-shadow 0.2s;
}

.template-item:hover {
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.template-text {
    padding-right: 2rem;
    cursor: pointer;
}

.template-meta {
    margin-top: 0.5rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 0.75rem;
    color: #9ca3af;
}

.delete-btn {
    opacity: 0;
    transition: opacity 0.2s;
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
}

.template-item:hover .delete-btn {
    opacity: 1;
}
</style>
