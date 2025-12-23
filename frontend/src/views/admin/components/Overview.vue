<template>
  <div class="stats-section">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日打卡</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.todayCheckins || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>活跃用户</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.activeUsers || 0 }}</div>
        </el-card>
      </el-col>
       <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总订单数</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
            <el-card shadow="hover">
                <template #header>
                    <div class="card-header flex-between">
                        <span>实时在线监控</span>
                        <el-button size="small" @click="exportOnlineStats">导出统计</el-button>
                    </div>
                </template>
                <div ref="onlineChartRef" style="height: 300px;"></div>
                <div class="online-stats-summary" style="margin-top: 10px; text-align: center;">
                    <el-tag size="large" effect="plain">平均在线时长: {{ formatDuration(avgOnlineTime) }}</el-tag>
                </div>
            </el-card>
        </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getDashboardStats } from '../../../api/admin'
import { getPlatformOnlineStats } from '../../../api/stats'
import * as echarts from 'echarts'

const stats = ref({
  totalUsers: 0,
  todayCheckins: 0,
  activeUsers: 0,
  totalOrders: 0
})

const avgOnlineTime = ref(0)
const onlineChartRef = ref(null)
let chartInstance: any = null
let pollingTimer: any = null

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    stats.value = res.data
  } catch (error) {
    console.error('Failed to load stats', error)
  }
}

const formatDuration = (seconds: number) => {
    if (!seconds) return '0h 0m'
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    return `${hours}h ${minutes}m`
}

const initChart = () => {
    if (onlineChartRef.value) {
        chartInstance = echarts.init(onlineChartRef.value)
        updateChart(0, 0)
    }
}

const updateChart = (current: number, peak: number) => {
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
            data: ['当前在线', '离线用户', '峰值在线', '非峰值']
        },
        series: [
            {
                name: '实时在线',
                type: 'pie',
                selectedMode: 'single',
                radius: [0, '30%'],
                label: {
                    position: 'inner',
                    fontSize: 14
                },
                labelLine: {
                    show: false
                },
                data: [
                    { value: current, name: '当前在线' },
                    { value: Math.max(0, stats.value.totalUsers - current), name: '离线用户' }
                ]
            },
            {
                name: '峰值对比',
                type: 'pie',
                radius: ['45%', '60%'],
                labelLine: {
                    length: 30
                },
                label: {
                    formatter: '{a|{a}}{abg|}\n{hr|}\n  {b|{b}：}{c}  {per|{d}%}  ',
                    backgroundColor: '#F6F8FC',
                    borderColor: '#8C8D8E',
                    borderWidth: 1,
                    borderRadius: 4,
                    rich: {
                        a: {
                            color: '#6E7079',
                            lineHeight: 22,
                            align: 'center'
                        },
                        hr: {
                            borderColor: '#8C8D8E',
                            width: '100%',
                            borderWidth: 1,
                            height: 0
                        },
                        b: {
                            color: '#4C5058',
                            fontSize: 14,
                            fontWeight: 'bold',
                            lineHeight: 33
                        },
                        per: {
                            color: '#fff',
                            backgroundColor: '#4C5058',
                            padding: [3, 4],
                            borderRadius: 4
                        }
                    }
                },
                data: [
                    { value: peak, name: '峰值在线' },
                    { value: Math.max(0, stats.value.totalUsers - peak), name: '非峰值' }
                ]
            }
        ]
    };
    chartInstance.setOption(option);
}

const fetchOnlineStats = async () => {
    try {
        const res: any = await getPlatformOnlineStats()
    if (!res || !res.data) {
        console.warn('Platform stats response empty')
        return
    }
    const { currentOnline = 0, peakOnline = 0, avgOnlineTime: avg = 0 } = res.data || {}
    avgOnlineTime.value = avg
    if (chartInstance) {
        updateChart(currentOnline, peakOnline)
    }
    } catch (error) {
        console.error(error)
    }
}

const startPolling = () => {
    fetchOnlineStats()
    pollingTimer = setInterval(fetchOnlineStats, 5000)
}

const exportOnlineStats = () => {
    // Mock export
    alert('正在导出在线统计数据...')
}

onMounted(() => {
  loadStats()
  initChart()
  startPolling()
  window.addEventListener('resize', () => chartInstance?.resize())
})

onUnmounted(() => {
    if (pollingTimer) clearInterval(pollingTimer)
    window.removeEventListener('resize', () => chartInstance?.resize())
    chartInstance?.dispose()
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  margin-bottom: 20px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
.card-header {
  font-weight: bold;
}
.flex-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>
