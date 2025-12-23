<template>
  <div class="dashboard-container">
    <el-header class="main-header">
      <div class="header-content container">
        <div class="logo-section">
          <h1>学习打卡系统</h1>
        </div>
        <div class="header-actions">
          <div class="user-info" v-if="userStore.user">
            <span class="points-badge" @click="router.push('/points')" style="cursor: pointer; margin-right: 15px;">
              <el-icon><Coin /></el-icon>
              {{ userStore.user.points }}
            </span>
            <NotificationCenter />
            <AccountSwitcher />
          </div>
        </div>
      </div>
    </el-header>

    <el-main class="container">
      <div class="welcome-section mb-4">
        <h2 class="welcome-title">欢迎回来, {{ userStore.user?.username }}!</h2>
        <p class="welcome-subtitle">准备好完成今天的学习目标了吗？</p>
      </div>

      <el-row :gutter="20">
        <!-- Check-in Card -->
        <el-col :xs="24" :sm="12" :md="8" class="mb-4">
          <el-card class="action-card h-100" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>每日打卡</span>
                <el-tag :type="isCheckedIn ? 'success' : 'warning'" effect="dark">
                  {{ isCheckedIn ? '已完成' : '未完成' }}
                </el-tag>
              </div>
            </template>
            <div class="card-body flex-center flex-column">
              <div class="checkin-circle" :class="{ 'checked': isCheckedIn }" @click="handleCheckin">
                <el-icon v-if="isCheckedIn" class="check-icon"><Select /></el-icon>
                <span v-else>打卡</span>
              </div>
              <p class="checkin-tip mt-2">{{ isCheckedIn ? '真棒！你已获得积分。' : '点击打卡 +10 积分' }}</p>
              <div class="mt-2">
                 <el-tag type="info" effect="plain">已连续打卡 {{ userStore.user?.continuousCheckinDays || 0 }} 天</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- Quick Actions -->
        <el-col :xs="24" :sm="12" :md="16" class="mb-4">
          <el-card class="action-card h-100" shadow="hover">
            <template #header>
              <div class="card-header">
                <span>快捷操作</span>
              </div>
            </template>
            <div class="quick-actions-grid">
              <div class="quick-action-item" @click="router.push('/study-plan')">
                <el-icon class="action-icon" color="#409EFF"><Collection /></el-icon>
                <span>我的计划</span>
              </div>
              <div class="quick-action-item" @click="router.push('/shop')">
                <el-icon class="action-icon" color="#E6A23C"><Shop /></el-icon>
                <span>积分商城</span>
              </div>
              <div class="quick-action-item" @click="router.push('/leaderboard')">
                <el-icon class="action-icon" color="#F56C6C"><Trophy /></el-icon>
                <span>排行榜</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Statistics Section -->
      <el-row :gutter="20" class="mb-4">
          <el-col :span="24">
              <el-card shadow="hover">
                  <template #header>
                      <div class="card-header">
                          <span>学习统计</span>
                      </div>
                  </template>
                  <div id="study-chart" style="width: 100%; height: 300px;"></div>
              </el-card>
          </el-col>
      </el-row>

      <!-- Calendar & History Section -->
      <el-row :gutter="20">
          <!-- Calendar View -->
          <el-col :xs="24" :lg="12" class="mb-4">
             <el-card class="history-card h-100" shadow="hover">
                 <template #header>
                     <div class="card-header">
                         <span>打卡日历</span>
                     </div>
                 </template>
                 <el-calendar v-model="currentDate">
                     <template #dateCell="{ data }">
                        <div class="calendar-cell">
                            <p :class="{ 'is-today': data.isSelected }">
                                {{ data.day.split('-').slice(1).join('-') }}
                                <span v-if="isDateCheckedIn(data.day)" class="check-mark">✔️</span>
                                <span v-else-if="canReCheckin(data.day)" class="recheck-mark" @click.stop="handleReCheckin(data.day)">补</span>
                            </p>
                        </div>
                     </template>
                 </el-calendar>
             </el-card>
          </el-col>

          <!-- History Table -->
          <el-col :xs="24" :lg="12" class="mb-4">
              <el-card class="history-card h-100" shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>最近动态</span>
                  </div>
                </template>
                <el-table :data="history" style="width: 100%" stripe :empty-text="'暂无打卡记录'" height="400">
                  <el-table-column prop="checkinDate" label="日期">
                     <template #default="scope">
                        <el-icon><Calendar /></el-icon> {{ scope.row.checkinDate }}
                     </template>
                  </el-table-column>
                  <el-table-column prop="checkinTime" label="时间">
                      <template #default="scope">
                          {{ formatTime(scope.row.checkinTime) }}
                      </template>
                  </el-table-column>
                  <el-table-column prop="studyDuration" label="时长">
                    <template #default="scope">
                      <el-tag size="small" type="info">{{ scope.row.studyDuration }} 分钟</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
          </el-col>
      </el-row>

      <!-- Online Users & Chat Section -->
      <el-row :gutter="20" class="mb-4">
          <el-col :xs="24" :md="8" class="mb-4">
              <OnlineUserList />
          </el-col>
          <el-col :xs="24" :md="16" class="mb-4">
              <ChatRoom />
          </el-col>
      </el-row>
    </el-main>

  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { onMounted, ref, computed } from 'vue'
import { getCheckinStatus, dailyCheckin, getCheckinHistory, reCheckin } from '../api/checkin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Select, Collection, Shop, Trophy, Calendar } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'

import OnlineUserList from '../components/OnlineUserList.vue'
import ChatRoom from '../components/ChatRoom.vue'
import AccountSwitcher from '../components/AccountSwitcher.vue'

const userStore = useUserStore()
const router = useRouter()
const isCheckedIn = ref(false)
const history = ref([])
const currentDate = ref(new Date())
let chartInstance: any = null

const initChart = () => {
    const chartDom = document.getElementById('study-chart')
    if (!chartDom) return
    
    if (chartInstance) {
        chartInstance.dispose()
    }
    chartInstance = echarts.init(chartDom)
    
    // Process history data for chart
    // Assume history is sorted by date desc, take last 7 days
    const last7Days = history.value.slice(0, 7).reverse()
    const dates = last7Days.map((item: any) => item.checkinDate)
    const durations = last7Days.map((item: any) => item.studyDuration)

    const option = {
        title: {
            text: '近7天学习时长',
            left: 'center'
        },
        tooltip: {
            trigger: 'axis'
        },
        xAxis: {
            type: 'category',
            data: dates
        },
        yAxis: {
            type: 'value',
            name: '分钟'
        },
        series: [
            {
                data: durations,
                type: 'bar',
                showBackground: true,
                backgroundStyle: {
                    color: 'rgba(180, 180, 180, 0.2)'
                },
                itemStyle: {
                    color: '#409EFF'
                }
            }
        ]
    }
    chartInstance.setOption(option)
}

const loadData = async () => {
    if (userStore.user) {
        try {
            const statusRes: any = await getCheckinStatus(userStore.user.id)
            isCheckedIn.value = statusRes.data
            
            const historyRes: any = await getCheckinHistory(userStore.user.id)
            history.value = historyRes.data
            
            // Init chart after data loaded
            setTimeout(() => {
                initChart()
            }, 100)
        } catch (error) {
            console.error(error)
        }
    }
}

const checkinDates = computed(() => {
    return new Set(history.value.map((item: any) => item.checkinDate))
})

const isDateCheckedIn = (day: string) => {
    return checkinDates.value.has(day)
}

const canReCheckin = (day: string) => {
    const date = dayjs(day)
    const today = dayjs().startOf('day')
    // Past date, not checked in, same month as today (simplification for now, backend checks logic too)
    // Actually backend allows recheckin for current month.
    // So date < today AND date.month() === today.month()
    return date.isBefore(today) && date.month() === today.month() && !checkinDates.value.has(day)
}

const formatTime = (time: string) => {
    return dayjs(time).format('HH:mm:ss')
}

onMounted(() => {
    loadData()
    window.addEventListener('resize', () => {
        chartInstance && chartInstance.resize()
    })
})

const handleCheckin = async () => {
  if (isCheckedIn.value) return
  if (!userStore.user) return
  try {
      await dailyCheckin(userStore.user.id)
      ElMessage.success('打卡成功！获得 +10 积分')
      userStore.user.points += 10
      // Optimistic update
      userStore.user.continuousCheckinDays = (userStore.user.continuousCheckinDays || 0) + 1
      isCheckedIn.value = true
      loadData()
  } catch (error: any) {
      ElMessage.error(error.message || '打卡失败')
  }
}

const handleReCheckin = async (date: string) => {
    if (!userStore.user) return
    try {
        await ElMessageBox.confirm(
            `补卡将消耗 20 积分，确定要补卡吗？(日期: ${date})`,
            '补卡确认',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }
        )
        
        await reCheckin(userStore.user.id, date)
        ElMessage.success('补卡成功！消耗 20 积分')
        userStore.user.points -= 20
        loadData()
    } catch (error: any) {
        if (error !== 'cancel') {
             ElMessage.error(error.message || '补卡失败')
        }
    }
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: var(--bg-color-base);
}

.welcome-section {
  margin-top: 20px;
}

.welcome-title {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.welcome-subtitle {
  margin: 8px 0 0;
  color: var(--text-secondary);
}

.checkin-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #36d1dc);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

.checkin-circle:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.4);
}

.checkin-circle.checked {
  background: linear-gradient(135deg, #67C23A, #a8e063);
  cursor: default;
  box-shadow: none;
}

.check-icon {
  font-size: 40px;
}

.checkin-tip {
  font-size: 13px;
  color: var(--text-secondary);
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 20px 0;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s;
  padding: 10px;
  border-radius: 8px;
}

.quick-action-item:hover {
  transform: translateY(-5px);
  background-color: var(--bg-color-base);
}

.action-icon {
  font-size: 32px;
}

.h-100 {
  height: 100%;
}

.calendar-cell {
    text-align: center;
    height: 100%;
}
.check-mark {
    color: var(--success-color);
    font-weight: bold;
    display: block;
    margin-top: 4px;
}
.recheck-mark {
    color: var(--el-color-warning);
    font-weight: bold;
    display: block;
    margin-top: 4px;
    cursor: pointer;
}
.recheck-mark:hover {
    text-decoration: underline;
}
:deep(.el-calendar-table .el-calendar-day) {
    height: 60px;
    padding: 8px;
}

@media (max-width: 768px) {
  .quick-actions-grid {
    grid-template-columns: repeat(1, 1fr);
  }
}
</style>
