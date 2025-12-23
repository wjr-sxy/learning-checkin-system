<template>
  <div class="dashboard-container">
    <el-header class="main-header">
      <div class="header-content container">
        <div class="logo-section">
          <h1>学习打卡系统</h1>
        </div>
        <div class="header-actions">
          <div class="user-info" v-if="userStore.user">
            <NotificationCenter />
            <AccountSwitcher />
            <span class="points-badge" @click="router.push('/profile/points')" style="cursor: pointer; margin-left: 15px;">
              <el-icon><Coin /></el-icon>
              {{ userStore.user.points }}
            </span>
          </div>
        </div>
      </div>
    </el-header>

    <el-main class="container">
      <div class="student-dashboard">
        <!-- Welcome Section -->
        <div class="welcome-section mb-5">
          <div class="welcome-card">
            <div class="welcome-content">
              <h1>欢迎回来，{{ userStore.user?.username }}!</h1>
              <p class="welcome-subtitle">继续你的学习之旅，今天也要加油哦！</p>
              <div class="welcome-stats">
                <div class="stat-item">
                  <div class="stat-value">{{ userStore.user?.continuousCheckinDays || 0 }}</div>
                  <div class="stat-label">连续打卡天数</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-value">{{ userStore.user?.points || 0 }}</div>
                  <div class="stat-label">总积分</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item" @click="showOnlineDetails" style="cursor: pointer">
                  <div class="stat-value">{{ formattedTodayOnline }}</div>
                  <div class="stat-label">今日在线</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-value">{{ courses.length }}</div>
                  <div class="stat-label">已加入课程</div>
                </div>
              </div>
            </div>
            <div class="welcome-actions">
              <el-button type="primary" size="large" @click="joinDialogVisible = true">
                <el-icon><Plus /></el-icon>
                加入课程
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- Quick Actions -->
        <div class="quick-actions-section mb-5">
          <h2 class="section-title">快捷操作</h2>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="6" class="mb-4">
              <el-card shadow="hover" @click="router.push('/study-plan')" class="action-card card-hover">
                <div class="card-content">
                  <div class="action-icon-wrapper study-plan-icon">
                    <el-icon size="48"><List /></el-icon>
                  </div>
                  <h3>学习计划</h3>
                  <p>管理你的学习进度</p>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" class="mb-4">
              <el-card shadow="hover" @click="router.push('/checkin')" class="action-card card-hover">
                <div class="card-content">
                  <div class="action-icon-wrapper checkin-icon">
                    <el-icon size="48"><Calendar /></el-icon>
                  </div>
                  <h3>每日打卡</h3>
                  <p>记录你的学习时间</p>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" class="mb-4">
              <el-card shadow="hover" @click="router.push('/leaderboard')" class="action-card card-hover">
                <div class="card-content">
                  <div class="action-icon-wrapper leaderboard-icon">
                    <el-icon size="48"><Trophy /></el-icon>
                  </div>
                  <h3>排行榜</h3>
                  <p>查看你的排名</p>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" class="mb-4">
              <el-card shadow="hover" @click="router.push('/shop')" class="action-card card-hover">
                <div class="card-content">
                  <div class="action-icon-wrapper shop-icon">
                    <el-icon size="48"><Shop /></el-icon>
                  </div>
                  <h3>积分商城</h3>
                  <p>兑换精美礼品</p>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" class="mb-4">
              <el-card shadow="hover" @click="router.push('/friends')" class="action-card card-hover">
                <div class="card-content">
                  <div class="action-icon-wrapper friends-icon">
                    <el-icon size="48"><User /></el-icon>
                  </div>
                  <h3>好友系统</h3>
                  <p>查看好友与请求</p>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- Teacher Tasks -->
        <TeacherTasks class="mb-5" />

        <!-- Course List -->
        <div class="my-courses-section">
          <div class="section-header flex-between">
            <h2 class="section-title">我的课程</h2>
            <el-text type="secondary" size="small">{{ courses.length }} 门课程</el-text>
          </div>
          <el-empty v-if="!courses.length" description="暂未加入任何课程" />
          <el-row v-else :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" v-for="course in courses" :key="course.id" class="mb-4">
              <el-card shadow="hover" class="course-card card-hover">
                <template #header>
                  <div class="card-header flex-between">
                    <h3 class="course-name">{{ course.name }}</h3>
                  </div>
                </template>
                <div class="course-content">
                  <p class="course-description">{{ course.description || '暂无描述' }}</p>
                  <div class="course-footer">
                    <el-tag size="small" type="info" effect="light">课程码: {{ course.code }}</el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <!-- Join Course Dialog -->
      <el-dialog v-model="joinDialogVisible" title="加入课程" width="500px" center>
        <el-form :model="joinForm" :rules="joinFormRules" ref="joinFormRef" label-position="top">
          <el-form-item label="课程码" prop="courseCode">
            <el-input 
              v-model="joinForm.courseCode" 
              placeholder="请输入8位课程码" 
              maxlength="8"
              size="large"
              clearable
              @keyup.enter="handleJoinCourse"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="joinDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleJoinCourse">确认加入</el-button>
          </span>
        </template>
      </el-dialog>

    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { Coin, List, Calendar, Shop, Trophy, Plus, User } from '@element-plus/icons-vue'
import { joinCourse, getStudentCourses } from '../../api/course'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import TeacherTasks from './TeacherTasks.vue'
import AccountSwitcher from '../../components/AccountSwitcher.vue'

const userStore = useUserStore()
const router = useRouter()
const joinDialogVisible = ref(false)
const joinFormRef = ref<FormInstance>()

// Online Time
const formattedTodayOnline = computed(() => {
  const seconds = userStore.todayOnlineSeconds
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${hours}h ${minutes}m`
})

const showOnlineDetails = () => {
    const seconds = userStore.todayOnlineSeconds
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    
    ElMessageBox.alert(
        `今日已在线：${hours}小时 ${minutes}分 ${secs}秒`, 
        '在线时长详情', 
        { confirmButtonText: '确定' }
    )
}

// Define course type
interface Course {
  id: number
  name: string
  description: string
  code: string
  semester: string
}

const courses = ref<Course[]>([])

// Join Course Form
const joinForm = reactive({
  courseCode: ''
})

const joinFormRules = reactive<FormRules>({
  courseCode: [
    { required: true, message: '请输入课程码', trigger: 'blur' },
    { min: 8, max: 8, message: '课程码必须为8位', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]+$/, message: '课程码必须为字母或数字', trigger: 'blur' }
  ]
})

const loadCourses = async () => {
    if (userStore.user) {
        try {
            const res = await getStudentCourses(userStore.user.id)
            courses.value = res.data
        } catch (error) {
            console.error(error)
            ElMessage.error('加载课程失败')
        }
    }
}

const handleJoinCourse = async () => {
    if (!joinFormRef.value) return
    
    await joinFormRef.value.validate(async (valid) => {
        if (valid) {
            if (!userStore.user) return
            
            try {
                await joinCourse(userStore.user.id, joinForm.courseCode)
                ElMessage.success('成功加入课程')
                joinDialogVisible.value = false
                joinForm.courseCode = ''
                loadCourses()
            } catch (error: any) {
                ElMessage.error(error.message || '加入失败')
            }
        }
    })
}

onMounted(() => {
    loadCourses()
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: var(--bg-color-base);
}

.student-dashboard {
  padding: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
}

.avatar-wrapper {
  position: relative;
  width: 40px;
  height: 40px;
}

.username {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.points-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #E6A23C 0%, #F3D19E 100%);
  color: #FFF;
  padding: 4px 12px;
  border-radius: 16px;
  font-weight: bold;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(230, 162, 60, 0.4);
}

/* Welcome Section */
.welcome-section {
  margin-bottom: 32px;
}

.welcome-card {
  background: var(--student-theme);
  border-radius: 16px;
  padding: 32px;
  color: white;
  box-shadow: 0 10px 40px rgba(64, 158, 255, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  overflow: hidden;
  position: relative;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: welcomeFloat 8s ease-in-out infinite;
}

@keyframes welcomeFloat {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.welcome-content h1 {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 600;
  color: white;
}

.welcome-subtitle {
  margin: 0 0 24px 0;
  font-size: 16px;
  opacity: 0.9;
  color: white;
}

.welcome-stats {
  display: flex;
  gap: 32px;
  margin-bottom: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  color: white;
}

.stat-divider {
  width: 1px;
  background: rgba(255, 255, 255, 0.3);
}

.welcome-actions {
  display: flex;
  gap: 12px;
}

/* Section Title */
.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title::before {
  content: '';
  display: block;
  width: 4px;
  height: 20px;
  background: var(--primary-color);
  border-radius: 2px;
}

/* Quick Actions */
.quick-actions-section {
  margin-bottom: 32px;
}

.action-card {
  cursor: pointer;
  text-align: center;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-color-white) !important;
}

.action-card .card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
}

.action-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  background: var(--bg-color-base);
  transition: all 0.3s ease;
}

.action-card:hover .action-icon-wrapper {
  transform: scale(1.1);
  box-shadow: 0 8px 24px var(--shadow-color-light);
}

.study-plan-icon {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  color: #1976D2;
}

.checkin-icon {
  background: linear-gradient(135deg, #E8F5E8 0%, #C8E6C9 100%);
  color: #388E3C;
}

.shop-icon {
  background: linear-gradient(135deg, #FFF8E1 0%, #FFECB3 100%);
  color: #F57C00;
}

.leaderboard-icon {
  background: linear-gradient(135deg, #FCE4EC 0%, #F8BBD0 100%);
  color: #C2185B;
}

.friends-icon {
  background: linear-gradient(135deg, #FDE6E6 0%, #F5C6C6 100%);
  color: #F56C6C;
}

.action-card h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.action-card p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* Courses Section */
.section-header {
  margin-bottom: 20px;
}

.course-card {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: var(--bg-color-white) !important;
}

.course-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.course-description {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  margin-bottom: 20px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.course-footer {
  margin-top: auto;
}

/* Responsive Adjustments */
@media (max-width: 992px) {
  .welcome-card {
    flex-direction: column;
    text-align: center;
  }
  
  .welcome-stats {
    justify-content: center;
  }
  
  .welcome-actions {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .welcome-card {
    padding: 24px;
  }
  
  .welcome-content h1 {
    font-size: 24px;
  }
  
  .welcome-stats {
    gap: 20px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .section-title {
    font-size: 18px;
  }
}

@media (max-width: 576px) {
  .welcome-stats {
    flex-direction: column;
    gap: 16px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .action-icon-wrapper {
    width: 64px;
    height: 64px;
    margin-bottom: 12px;
  }
  
  .action-icon-wrapper .el-icon {
    font-size: 32px !important;
  }
}
</style>
