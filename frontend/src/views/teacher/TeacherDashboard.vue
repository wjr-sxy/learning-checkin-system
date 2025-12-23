<template>
  <div class="dashboard-container">
    <el-header class="main-header">
      <div class="header-content container">
        <div class="logo-section">
          <h1>学习打卡系统</h1>
        </div>
        <div class="header-actions">
          <NotificationCenter />
          <AccountSwitcher />
          <span class="points-badge" style="margin-left: 10px;">
            <el-icon><Coin /></el-icon>
            {{ userStore.user?.points || 0 }}
          </span>
        </div>
      </div>
    </el-header>

    <el-main class="container">
      <div class="teacher-dashboard">
          <div class="flex-between mb-4">
               <div>
                    <h1>教师管理后台</h1>
                    <p class="text-muted">欢迎回来，管理您的课程和学生</p>
               </div>
          </div>

          <el-tabs v-model="activeTab" class="teacher-tabs" type="border-card">
            <!-- 1. 课程中心 -->
            <el-tab-pane label="课程中心" name="course">
              <CourseCenter 
                :courses="courses" 
                @refresh="loadCourses" 
                @switch-to-class="switchToClassTab" 
              />
            </el-tab-pane>

            <!-- 2. 班级管理 -->
            <el-tab-pane label="班级管理" name="class">
              <ClassManagement 
                :courses="courses" 
                v-model:selectedCourseId="selectedCourseId"
              />
            </el-tab-pane>

            <!-- 3. 任务管理 -->
            <el-tab-pane label="任务管理" name="task">
              <TaskManagement />
            </el-tab-pane>

            <!-- 4. 教学计划 -->
            <el-tab-pane label="教学计划" name="plan">
              <TeachingPlan 
                :courses="courses" 
                v-model:planSelectedCourseId="planSelectedCourseId"
              />
            </el-tab-pane>

            <!-- 5. 监控中心 -->
            <el-tab-pane label="监控中心" name="monitor">
              <MonitoringCenter 
                :courses="courses"
                v-model:monitorCourseId="monitorCourseId"
              />
            </el-tab-pane>

            <!-- 6. 催学中心 -->
            <el-tab-pane label="催学中心" name="remind">
              <ReminderCenter 
                :courses="courses"
                v-model:remindCourseId="remindCourseId"
              />
            </el-tab-pane>

            <!-- 7. 数据导出 -->
            <el-tab-pane label="数据导出" name="data">
              <DataExport 
                :courses="courses"
                v-model:exportCourseId="exportCourseId"
              />
            </el-tab-pane>

            <!-- 8. 排行榜 -->
            <el-tab-pane label="排行榜" name="leaderboard">
              <TeacherLeaderboard 
                :courses="courses"
                v-model:leaderboardCourseId="leaderboardCourseId"
              />
            </el-tab-pane>

            <!-- 9. 个人设置 -->
            <el-tab-pane label="个人设置" name="settings">
                <div class="profile-container">
                    <el-card shadow="hover" class="mb-4">
                        <template #header>
                            <div class="card-header">
                                <span>基本资料</span>
                            </div>
                        </template>
                        <el-form :model="profileForm" label-width="100px">
                            <el-form-item label="用户名">
                                <el-input v-model="profileForm.username" disabled />
                            </el-form-item>
                            <el-form-item label="邮箱">
                                <el-input v-model="profileForm.email" />
                            </el-form-item>
                            <el-form-item label="头像URL">
                                <el-input v-model="profileForm.avatar" />
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="handleUpdateProfile" :loading="updatingProfile">保存修改</el-button>
                            </el-form-item>
                        </el-form>
                    </el-card>

                    <el-card shadow="hover">
                        <template #header>
                            <div class="card-header">
                                <span>修改密码</span>
                            </div>
                        </template>
                        <el-form :model="passwordForm" label-width="100px">
                            <el-form-item label="旧密码">
                                <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                            </el-form-item>
                            <el-form-item label="新密码">
                                <el-input v-model="passwordForm.newPassword" type="password" show-password />
                            </el-form-item>
                            <el-form-item label="确认新密码">
                                <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                            </el-form-item>
                            <el-form-item>
                                <el-button type="danger" @click="handleUpdatePassword" :loading="updatingPassword">修改密码</el-button>
                            </el-form-item>
                        </el-form>
                    </el-card>
                </div>
            </el-tab-pane>
          </el-tabs>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
/**
 * TeacherDashboard.vue
 * 教师端主工作台，采用模块化组件拆分以提高可维护性。
 * 各子模块位于 ./components/ 目录下。
 */
import { ref, onMounted } from 'vue'
import NotificationCenter from '../../components/NotificationCenter.vue'
import AccountSwitcher from '@/components/AccountSwitcher.vue'
import TaskManagement from './TaskManagement.vue'
import CourseCenter from './components/CourseCenter.vue'
import ClassManagement from './components/ClassManagement.vue'
import TeachingPlan from './components/TeachingPlan.vue'
import MonitoringCenter from './components/MonitoringCenter.vue'
import ReminderCenter from './components/ReminderCenter.vue'
import DataExport from './components/DataExport.vue'
import TeacherLeaderboard from './components/TeacherLeaderboard.vue'

import { useUserStore } from '../../stores/user'
import { Coin } from '@element-plus/icons-vue'
import { getTeacherCourses } from '../../api/course'
import { updateProfile, updatePassword } from '../../api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// 共享状态
const activeTab = ref('course')
const courses = ref<any[]>([])
const selectedCourseId = ref<number | null>(null)
const planSelectedCourseId = ref<number | null>(null)
const monitorCourseId = ref<number | null>(null)
const remindCourseId = ref<number | null>(null)
const exportCourseId = ref<number | null>(null)
const leaderboardCourseId = ref<number | null>(null)

// 个人设置状态
const updatingProfile = ref(false)
const updatingPassword = ref(false)
const profileForm = ref({
    username: userStore.user?.username || '',
    email: userStore.user?.email || '',
    avatar: userStore.user?.avatar || ''
})
const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})

// 加载课程数据
const loadCourses = async () => {
    if (!userStore.user) return
    try {
        const res: any = await getTeacherCourses(userStore.user.id)
        courses.value = res.data
    } catch (error) {
        console.error('Failed to load courses:', error)
    }
}

// 切换到班级管理标签
const switchToClassTab = (courseId: number) => {
    activeTab.value = 'class'
    selectedCourseId.value = courseId
}

// 更新个人资料
const handleUpdateProfile = async () => {
    if (!userStore.user) return
    updatingProfile.value = true
    try {
        await updateProfile(profileForm.value)
        ElMessage.success('个人资料更新成功')
        userStore.setUser({ ...userStore.user, ...profileForm.value })
    } catch (error: any) {
        ElMessage.error(error.message || '更新失败')
    } finally {
        updatingProfile.value = false
    }
}

// 修改密码
const handleUpdatePassword = async () => {
    if (!userStore.user) return
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        ElMessage.warning('两次输入的新密码不一致')
        return
    }
    updatingPassword.value = true
    try {
        await updatePassword({
            oldPassword: passwordForm.value.oldPassword,
            newPassword: passwordForm.value.newPassword
        })
        ElMessage.success('密码修改成功')
        passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    } catch (error: any) {
        ElMessage.error(error.message || '修改失败')
    } finally {
        updatingPassword.value = false
    }
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

.teacher-dashboard {
  padding: 0;
}

/* Header Section */
.teacher-dashboard > .flex-between {
  margin-bottom: 24px;
  padding: 24px 0;
}

.teacher-dashboard > .flex-between h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  background: var(--teacher-theme);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.teacher-dashboard > .flex-between p {
  margin: 0;
  font-size: 16px;
  color: var(--text-secondary);
}

/* Tabs */
.teacher-tabs {
  background: var(--bg-color-white);
  border-radius: 12px;
  box-shadow: 0 4px 20px var(--shadow-color-light);
  border: none !important;
  overflow: hidden;
}

.teacher-tabs :deep(.el-tabs__header) {
  background: var(--bg-color-card);
  border-bottom: 1px solid var(--border-color-lighter);
  padding: 0 24px;
}

.teacher-tabs :deep(.el-tabs__nav) {
  margin: 0;
  height: 60px;
}

.teacher-tabs :deep(.el-tabs__item) {
  height: 60px;
  line-height: 60px;
  padding: 0 24px;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-regular);
  transition: all 0.3s ease;
}

.teacher-tabs :deep(.el-tabs__item:hover) {
  color: var(--primary-color);
  background: rgba(64, 158, 255, 0.05);
}

.teacher-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
  background: var(--bg-color-white);
  border-bottom: 2px solid var(--primary-color);
  font-weight: 600;
}

.teacher-tabs :deep(.el-tabs__content) {
  padding: 24px;
}

/* Profile Section */
.profile-container {
  max-width: 600px;
  margin: 0 auto;
}

/* Responsive Design */
@media (max-width: 768px) {
  .teacher-tabs :deep(.el-tabs__header) {
    padding: 0;
  }
  
  .teacher-tabs :deep(.el-tabs__nav) {
    overflow-x: auto;
    overflow-y: hidden;
    white-space: nowrap;
  }
  
  .teacher-tabs :deep(.el-tabs__content) {
    padding: 16px;
  }
}

.mb-4 {
  margin-bottom: 24px;
}
</style>
