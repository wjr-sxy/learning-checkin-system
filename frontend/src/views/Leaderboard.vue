<template>
  <div class="ranking-container">
    <el-header class="main-header">
      <div class="header-content container">
        <div class="logo-section">
            <h1 @click="router.push('/')" style="cursor: pointer;">学习打卡系统</h1>
        </div>
        <div class="header-actions">
            <el-button @click="router.push('/')">返回仪表盘</el-button>
        </div>
      </div>
    </el-header>

    <el-main class="container">
        <div class="page-header mb-4 text-center">
            <h2 style="display: flex; align-items: center; justify-content: center;">
                <el-icon class="mr-2" color="#E6A23C"><Trophy /></el-icon>
                <span>积分排行榜</span>
            </h2>
        </div>

        <el-card class="ranking-card">
            <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="ranking-tabs" type="card">
                <el-tab-pane label="今日榜单" name="daily">
                    <template #label>
                        <span class="custom-tabs-label">
                            <el-icon><Timer /></el-icon>
                            <span style="margin-left: 5px">今日榜单</span>
                        </span>
                    </template>
                </el-tab-pane>
                <el-tab-pane label="本周榜单" name="weekly">
                    <template #label>
                         <span class="custom-tabs-label">
                            <el-icon><Calendar /></el-icon>
                            <span style="margin-left: 5px">本周榜单</span>
                        </span>
                    </template>
                </el-tab-pane>
                <el-tab-pane label="在线时长" name="online">
                    <template #label>
                         <span class="custom-tabs-label">
                            <el-icon><Watch /></el-icon>
                            <span style="margin-left: 5px">在线时长 Top100</span>
                        </span>
                    </template>
                </el-tab-pane>
            </el-tabs>

            <div v-if="activeTab === 'online'" class="filter-bar" style="margin-bottom: 15px; text-align: center;">
                <el-radio-group v-model="onlineTimeRange" @change="loadRanking" size="default">
                    <el-radio-button label="week">本周</el-radio-button>
                    <el-radio-button label="month">本月</el-radio-button>
                </el-radio-group>
            </div>

            <el-table :data="rankingList" style="width: 100%" stripe v-loading="loading">
                <el-table-column prop="rank" label="排名" width="80" align="center">
                    <template #default="scope">
                        <div v-if="scope.row.rank === 1">
                             <el-icon color="#F56C6C" size="24"><Trophy /></el-icon>
                        </div>
                        <div v-else-if="scope.row.rank === 2">
                             <el-icon color="#E6A23C" size="24"><Medal /></el-icon>
                        </div>
                        <div v-else-if="scope.row.rank === 3">
                             <el-icon color="#409EFF" size="24"><Medal /></el-icon>
                        </div>
                        <span v-else class="rank-number">{{ scope.row.rank || (scope.$index + 1) }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="用户" min-width="200">
                    <template #default="scope">
                        <div class="user-cell">
                            <el-avatar :size="40" :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" />
                            <span class="username ml-2">{{ scope.row.username || scope.row.fullName }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column v-if="activeTab !== 'online'" prop="score" label="获得积分" align="right" min-width="100">
                     <template #default="scope">
                        <span class="score-text">+{{ scope.row.score }}</span>
                    </template>
                </el-table-column>
                <el-table-column v-else prop="total_seconds" label="在线时长" align="right" min-width="150">
                     <template #default="scope">
                        <span class="online-time-text">{{ formatDuration(scope.row.total_seconds) }}</span>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDailyRanking, getWeeklyRanking } from '../api/ranking'
import { getOnlineLeaderboard } from '../api/stats'
import { Trophy, Timer, Calendar, Medal, Watch } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('daily')
const onlineTimeRange = ref('month')
const rankingList = ref<any[]>([])
const loading = ref(false)

const loadRanking = async () => {
    loading.value = true
    try {
        let res: any
        if (activeTab.value === 'daily') {
            res = await getDailyRanking()
            rankingList.value = res.data
        } else if (activeTab.value === 'weekly') {
            res = await getWeeklyRanking()
            rankingList.value = res.data
        } else if (activeTab.value === 'online') {
            res = await getOnlineLeaderboard(onlineTimeRange.value)
            rankingList.value = res.data
        }
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

const formatDuration = (seconds: number) => {
    if (!seconds) return '0h 0m'
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    return `${hours}h ${minutes}m`
}

const handleTabChange = () => {
    loadRanking()
}

onMounted(() => {
    loadRanking()
})
</script>

<style scoped>
.ranking-container {
    min-height: 100vh;
    background-color: #f5f7fa;
}
.main-header {
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    display: flex;
    align-items: center;
}
.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
}
.container {
    max-width: 1200px;
    margin: 0 auto;
}
.ranking-card {
    max-width: 800px;
    margin: 0 auto;
    min-height: 500px;
}
.user-cell {
    display: flex;
    align-items: center;
}
.username {
    margin-left: 10px;
    font-weight: 500;
}
.score-text {
    color: #f56c6c;
    font-weight: bold;
    font-size: 16px;
}
.rank-number {
    font-weight: bold;
    color: #909399;
    font-size: 18px;
}

.online-time-text {
    font-family: 'Courier New', Courier, monospace;
    font-weight: bold;
    color: var(--el-color-primary);
}
</style>
