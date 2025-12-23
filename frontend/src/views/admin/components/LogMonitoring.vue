<template>
  <div class="log-monitoring">
    <el-alert
      v-if="health.status === 'Warning'"
      title="系统健康预警：检测到异常指标"
      type="warning"
      show-icon
      class="mb-4"
    />

    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
             <div class="card-header">
               <span>登录失败 (1h)</span>
               <el-tag :type="health.loginFailures > 5 ? 'danger' : 'success'">{{ health.loginFailures > 5 ? '异常' : '正常' }}</el-tag>
             </div>
          </template>
          <div class="stat-value" :class="{'text-danger': health.loginFailures > 5}">{{ health.loginFailures || 0 }}</div>
          <div class="stat-desc">最近1小时登录失败次数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>接口异常 (1h)</span>
              <el-tag :type="health.interfaceErrors > 10 ? 'danger' : 'success'">{{ health.interfaceErrors > 10 ? '异常' : '正常' }}</el-tag>
            </div>
          </template>
          <div class="stat-value" :class="{'text-danger': health.interfaceErrors > 10}">{{ health.interfaceErrors || 0 }}</div>
          <div class="stat-desc">最近1小时接口调用失败次数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>慢查询 (1h)</span>
              <el-tag :type="health.slowQueries > 5 ? 'danger' : 'success'">{{ health.slowQueries > 5 ? '异常' : '正常' }}</el-tag>
            </div>
          </template>
          <div class="stat-value" :class="{'text-danger': health.slowQueries > 5}">{{ health.slowQueries || 0 }}</div>
          <div class="stat-desc">最近1小时耗时>1s的请求</div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
        <el-tab-pane label="系统日志" name="logs">
             <!-- Use existing Log components or just list logs here -->
             <el-alert title="请前往审计中心查看详细日志记录" type="info" :closable="false" />
        </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getSystemHealth } from '../../../api/admin'

const health = ref({
    loginFailures: 0,
    interfaceErrors: 0,
    slowQueries: 0,
    status: 'Normal'
})
const activeTab = ref('logs')
let timer: any = null

const loadHealth = async () => {
    try {
        const res: any = await getSystemHealth()
        health.value = res.data
    } catch (e) {}
}

onMounted(() => {
    loadHealth()
    // Auto refresh every 30s
    timer = setInterval(loadHealth, 30000)
})

onUnmounted(() => {
    if (timer) clearInterval(timer)
})
</script>

<style scoped>
.stat-value {
    font-size: 28px;
    font-weight: bold;
    margin-bottom: 10px;
}
.stat-desc {
    font-size: 12px;
    color: #909399;
}
.text-danger {
    color: #F56C6C;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
