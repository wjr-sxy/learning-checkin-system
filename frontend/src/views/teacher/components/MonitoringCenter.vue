<!--
  MonitoringCenter.vue
  教师端 - 监控中心模块
  用于查看课程数据统计、打卡趋势、学生画像和积分排行榜
-->
<template>
  <div class="monitoring-center">
    <div class="filter-section mb-4">
      <span class="mr-2">选择课程:</span>
      <el-select v-model="internalMonitorCourseId" placeholder="请选择课程" @change="handleMonitorCourseChange" style="width: 200px">
        <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
      </el-select>
    </div>

    <div v-if="internalMonitorCourseId" v-loading="loadingMonitor">
      <el-row :gutter="20">
        <!-- Stats Cards -->
        <el-col :span="6" v-for="stat in monitorStats" :key="stat.label">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-label text-muted">{{ stat.label }}</div>
            <div class="stat-value" :style="{ color: stat.color }">{{ stat.value }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mt-4">
        <el-col :span="16">
          <el-card shadow="hover">
            <template #header>
              <div class="flex-between">
                <span>打卡趋势 (最近7天)</span>
                <el-button link type="primary" @click="handleExport">
                  <el-icon class="mr-1"><Download /></el-icon>导出报表
                </el-button>
              </div>
            </template>
            <div ref="trendChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="flex-between">
                <span>积分排行</span>
                <el-icon><Trophy /></el-icon>
              </div>
            </template>
            <el-table :data="leaderboard" size="small" stripe>
              <el-table-column type="index" width="50" />
              <el-table-column prop="username" label="姓名" />
              <el-table-column prop="points" label="总积分" width="80" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <el-empty v-else description="请先选择一个课程查看数据统计" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { Download, Trophy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDailyStats, getCompletionTrend, getLeaderboard } from '@/api/statistics'
import { exportCourseData } from '@/api/export'

const props = defineProps<{
  courses: any[]
  monitorCourseId: number | null
}>()

const emit = defineEmits(['update:monitorCourseId'])

const internalMonitorCourseId = ref(props.monitorCourseId)
const loadingMonitor = ref(false)
const trendChart = ref<HTMLElement | null>(null)
let chartInstance: echarts.ECharts | null = null
const dailyStats = ref<any>({})
const leaderboard = ref<any[]>([])

const monitorStats = computed(() => [
  { label: '今日打卡率', value: `${dailyStats.value.checkinRate || 0}%`, color: '#409EFF' },
  { label: '人均积分', value: dailyStats.value.avgPoints || 0, color: '#67C23A' },
  { label: '待批改作业', value: dailyStats.value.pendingGrading || 0, color: '#E6A23C' },
  { label: '异常打卡', value: dailyStats.value.abnormalCount || 0, color: '#F56C6C' }
])

watch(() => props.monitorCourseId, (newVal) => {
  internalMonitorCourseId.value = newVal
  if (newVal) handleMonitorCourseChange(newVal)
})

const handleMonitorCourseChange = async (courseId: number) => {
  if (!courseId) return
  emit('update:monitorCourseId', courseId)
  loadingMonitor.value = true
  try {
    const [statsRes, trendRes, boardRes] = await Promise.all([
      getDailyStats(courseId),
      getCompletionTrend(courseId),
      getLeaderboard(courseId)
    ])
    
    dailyStats.value = statsRes.data
    leaderboard.value = boardRes.data
    
    initChart(trendRes.data)
  } catch (error) {
    ElMessage.error('加载监控数据失败')
  } finally {
    loadingMonitor.value = false
  }
}

const initChart = (data: any) => {
  if (!trendChart.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(trendChart.value)
  }
  
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.dates },
    yAxis: { type: 'value' },
    series: [
      {
        name: '打卡人数',
        type: 'line',
        data: data.counts,
        smooth: true,
        areaStyle: { opacity: 0.1 },
        itemStyle: { color: '#409EFF' }
      }
    ]
  }
  chartInstance.setOption(option)
}

const handleExport = async () => {
  if (!internalMonitorCourseId.value) return
  try {
    const res: any = await exportCourseData(internalMonitorCourseId.value)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `课程数据报表_${new Date().toLocaleDateString()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.mt-4 { margin-top: 1rem; }
.mr-2 { margin-right: 0.5rem; }
.mr-1 { margin-right: 0.25rem; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.stat-card { text-align: center; }
.stat-label { font-size: 14px; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; }
.text-muted { color: #909399; }
</style>
