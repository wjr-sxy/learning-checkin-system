<template>
  <div class="logs-view">
    <h2 class="section-title">操作日志</h2>
    
    <div class="filter-bar">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        @change="fetchLogs"
        :shortcuts="shortcuts"
      />
      <el-select v-model="selectedModule" placeholder="选择模块" clearable @change="fetchLogs" style="margin-left: 10px">
        <el-option label="用户模块" value="USER" />
        <el-option label="登录模块" value="AUTH" />
        <el-option label="学习模块" value="STUDY" />
        <el-option label="系统模块" value="SYSTEM" />
      </el-select>
      <el-button type="primary" @click="exportPDF" style="margin-left: auto">导出PDF</el-button>
    </div>

    <el-table :data="logs" style="width: 100%" v-loading="loading">
      <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
              {{ formatDate(row.createTime) }}
          </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="action" label="动作类型" width="120" />
      <el-table-column prop="status" label="结果" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">
            {{ row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="详情" />
    </el-table>

    <!-- Hidden Print Section -->
    <div id="print-section" class="print-only">
        <div class="watermark">Learning Checkin System</div>
        <h3>操作日志导出</h3>
        <p>导出时间: {{ new Date().toLocaleString() }}</p>
        <table class="print-table">
            <thead>
                <tr>
                    <th>时间</th>
                    <th>模块</th>
                    <th>动作</th>
                    <th>结果</th>
                    <th>详情</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="log in logs" :key="log.id">
                    <td>{{ formatDate(log.createTime) }}</td>
                    <td>{{ log.module }}</td>
                    <td>{{ log.action }}</td>
                    <td>{{ log.status === 0 ? '成功' : '失败' }}</td>
                    <td>{{ log.description }}</td>
                </tr>
            </tbody>
        </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getOperationLogs } from '../../api/user'

const userStore = useUserStore()
const logs = ref<any[]>([])
const loading = ref(false)
const dateRange = ref('')
const selectedModule = ref('')

const shortcuts = [
  {
    text: '今天',
    value: () => {
      const end = new Date()
      const start = new Date()
      return [start, end]
    },
  },
  {
    text: '本周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - start.getDay())
      return [start, end]
    },
  },
  {
    text: '本月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(1)
      return [start, end]
    },
  },
]

onMounted(() => {
  fetchLogs()
})

const fetchLogs = async () => {
  if (!userStore.user) return
  loading.value = true
  try {
    const params: any = {
      userId: userStore.user.id,
      module: selectedModule.value
    }
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res: any = await getOperationLogs(params)
    logs.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
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
.section-title {
  margin-bottom: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.filter-bar {
  display: flex;
  margin-bottom: 20px;
  align-items: center;
}

.print-only {
    display: none;
}

@media print {
    body * {
        visibility: hidden;
    }
    #print-section, #print-section * {
        visibility: visible;
    }
    #print-section {
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
    
    .print-table th {
        background-color: #f2f2f2;
    }
}
</style>
