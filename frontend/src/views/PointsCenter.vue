<template>
  <div class="points-center-container container">
    <div class="page-header">
      <h2>积分中心</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-row :gutter="20">
      <!-- Points Overview -->
      <el-col :span="8">
        <el-card shadow="hover" class="points-card">
          <template #header>
            <div class="card-header">
              <span>我的积分</span>
            </div>
          </template>
          <div class="points-display">
            <el-icon class="coin-icon" :size="40" color="#E6A23C"><Coin /></el-icon>
            <span class="points-value">{{ userStore.user?.points || 0 }}</span>
          </div>
          <div class="points-expiration mt-2">
            <el-tag type="warning" size="small">积分有效期: 365天</el-tag>
            <p class="text-secondary text-xs mt-1">系统每月1日自动清理过期积分</p>
          </div>
        </el-card>

        <el-card shadow="hover" class="mt-4">
            <template #header>
                <span>积分规则</span>
            </template>
            <div class="rules-list">
                <p>1. <strong>每日打卡</strong>: +10 积分</p>
                <p>2. <strong>连续打卡</strong>: 暂无额外奖励 (可配置)</p>
                <p>3. <strong>补卡</strong>: -20 积分/次</p>
                <p>4. <strong>计划完成</strong>: ≥80% 全额奖励</p>
            </div>
        </el-card>
      </el-col>

      <!-- Points Log -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>积分流水</span>
            </div>
          </template>
          <el-table :data="logs" style="width: 100%" stripe height="500">
            <el-table-column prop="createTime" label="时间" width="180">
                <template #default="scope">
                    {{ formatTime(scope.row.createTime) }}
                </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="100">
                <template #default="scope">
                    <el-tag :type="scope.row.type === 1 ? 'success' : 'danger'">
                        {{ scope.row.type === 1 ? '获取' : '消费' }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="amount" label="变动" width="100">
                <template #default="scope">
                    <span :class="scope.row.type === 1 ? 'text-success' : 'text-danger'">
                        {{ scope.row.type === 1 ? '+' : '-' }}{{ scope.row.amount }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { getPointsLog } from '../api/points'
import { Coin } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const router = useRouter()
const logs = ref([])

const loadLogs = async () => {
    if (userStore.user) {
        try {
            const res: any = await getPointsLog(userStore.user.id)
            logs.value = res.data
        } catch (error) {
            console.error(error)
        }
    }
}

const formatTime = (time: string) => {
    return dayjs(time).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
    loadLogs()
})
</script>

<style scoped>
.points-center-container {
    padding-top: 20px;
    padding-bottom: 20px;
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
.points-display {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    padding: 20px 0;
}
.points-value {
    font-size: 36px;
    font-weight: bold;
    color: #303133;
}
.text-success {
    color: var(--success-color);
    font-weight: bold;
}
.text-danger {
    color: var(--danger-color);
    font-weight: bold;
}
.text-xs {
    font-size: 12px;
}
.rules-list p {
    margin-bottom: 10px;
    font-size: 14px;
    color: #606266;
}
</style>
