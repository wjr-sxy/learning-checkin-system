<template>
  <div class="points-view">
    <div class="balance-card">
      <div class="balance-title">当前积分余额</div>
      <div class="balance-amount">{{ userStore.user?.points || 0 }}</div>
      <el-button type="primary" @click="$router.push('/shop')">去兑换</el-button>
    </div>

    <div class="charts-card">
      <div class="card-header">
        <span>积分趋势</span>
        <el-radio-group v-model="trendDays" size="small" @change="fetchTrend">
          <el-radio-button :label="7">近7天</el-radio-button>
          <el-radio-button :label="30">近30天</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>

    <div class="history-card">
      <div class="card-header">
        <span>积分明细</span>
        <el-button type="default" size="small" @click="exportPDF">导出明细</el-button>
      </div>
      
      <el-table :data="paginatedData" style="width: 100%" v-loading="loading">
        <el-table-column prop="createTime" label="时间" width="180">
            <template #default="{ row }">
                {{ formatDate(row.createTime) }}
            </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'">
              {{ row.type === 1 ? '获取' : '消费' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="变动数量" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.type === 1 ? '#67C23A' : '#E6A23C', fontWeight: 'bold' }">
              {{ row.type === 1 ? '+' : '-' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="详情" />
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- Print Section -->
    <div id="print-points" class="print-only">
        <div class="watermark">Learning Checkin System</div>
        <h3>积分交易流水</h3>
        <p>用户: {{ userStore.user?.username }}</p>
        <p>导出时间: {{ new Date().toLocaleString() }}</p>
        <table class="print-table">
            <thead>
                <tr>
                    <th>时间</th>
                    <th>类型</th>
                    <th>变动</th>
                    <th>详情</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="row in historyData" :key="row.id">
                    <td>{{ formatDate(row.createTime) }}</td>
                    <td>{{ row.type === 1 ? '获取' : '消费' }}</td>
                    <td>{{ row.type === 1 ? '+' : '-' }}{{ row.amount }}</td>
                    <td>{{ row.description }}</td>
                </tr>
            </tbody>
        </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getPointsLog, getPointsTrend } from '../../api/points'
import * as echarts from 'echarts'

const userStore = useUserStore()
const loading = ref(false)
const historyData = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const trendDays = ref(7)
const chartRef = ref<HTMLElement>()
let myChart: echarts.ECharts | null = null

onMounted(() => {
  fetchData()
  initChart()
  window.addEventListener('resize', resizeChart)
})

const fetchData = async () => {
  if (!userStore.user) return
  loading.value = true
  try {
    const res: any = await getPointsLog(userStore.user.id)
    historyData.value = res.data
    fetchTrend()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const fetchTrend = async () => {
  if (!userStore.user) return
  try {
    const res: any = await getPointsTrend(userStore.user.id, trendDays.value)
    updateChart(res.data)
  } catch (error) {
    console.error(error)
  }
}

const initChart = () => {
  if (chartRef.value) {
    myChart = echarts.init(chartRef.value)
  }
}

const resizeChart = () => {
  myChart?.resize()
}

const updateChart = (data: any[]) => {
  if (!myChart) return
  
  const dates = data.map(item => item.date)
  const values = data.map(item => item.value)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '积分余额',
        type: 'line',
        smooth: true,
        data: values,
        areaStyle: {
          opacity: 0.3,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#fff' }
          ])
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  myChart.setOption(option)
}

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return historyData.value.slice(start, end)
})

const total = computed(() => historyData.value.length)

const handlePageChange = (val: number) => {
  currentPage.value = val
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

const exportPDF = () => {
  window.print()
}
</script>

<style scoped>
.points-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.balance-card {
  background: linear-gradient(135deg, #409EFF 0%, #36d1dc 100%);
  color: white;
  padding: 30px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.balance-title {
  font-size: 16px;
  opacity: 0.9;
}

.balance-amount {
  font-size: 48px;
  font-weight: bold;
  margin: 10px 0 20px;
}

.charts-card, .history-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-weight: bold;
  font-size: 16px;
}

.chart-container {
  height: 300px;
  width: 100%;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.print-only {
    display: none;
}

@media print {
    body * {
        visibility: hidden;
    }
    #print-points, #print-points * {
        visibility: visible;
    }
    #print-points {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        display: block;
    }
    
    .watermark {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%) rotate(-45deg);
        font-size: 80px;
        color: rgba(0,0,0,0.05);
        z-index: -1;
        pointer-events: none;
    }

    .print-table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 20px;
    }
    
    .print-table th, .print-table td {
        border: 1px solid #ddd;
        padding: 8px;
        text-align: left;
    }
}
</style>
