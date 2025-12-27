<template>
  <div class="achievement-wall p-4 bg-white rounded-lg shadow-sm">
    <h3 class="font-bold mb-4 flex items-center">
      <el-icon class="mr-2 text-yellow-500"><Trophy /></el-icon>
      成就墙
    </h3>
    <div class="grid grid-cols-4 gap-4">
      <div v-for="badge in badges" :key="badge.id" class="badge-item flex flex-col items-center">
        <el-tooltip
          class="box-item"
          effect="dark"
          :content="badge.reason || badge.description"
          placement="top"
        >
          <div class="badge-icon-wrapper relative cursor-help" :class="{ 'grayscale': !badge.obtained }">
            <el-image 
              :src="badge.iconUrl" 
              class="w-12 h-12 rounded-full border-2 border-yellow-200 p-1"
              fit="cover"
            >
              <template #error>
                <div class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center text-gray-400">
                  <el-icon><Medal /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-if="badge.obtained" class="absolute -bottom-1 -right-1 bg-yellow-400 text-white text-xs px-1 rounded-full">
              Get
            </div>
          </div>
        </el-tooltip>
        <span class="text-xs mt-2 text-gray-600 truncate w-full text-center">{{ badge.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Trophy, Medal } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

interface Badge {
  id: number
  name: string
  description: string
  iconUrl: string
  obtained: boolean
  reason?: string
}

const badges = ref<Badge[]>([
  { id: 101, name: '光影学者', description: '累计完成5个视频任务', iconUrl: '/badges/light_scholar.png', obtained: false },
  { id: 102, name: '自律先锋', description: '连续打卡满7天', iconUrl: '/badges/discipline.png', obtained: false },
  { id: 103, name: '任务达人', description: '完成10个学习任务', iconUrl: '/badges/task_master.png', obtained: false },
  { id: 104, name: '学霸认证', description: '总积分超过1000', iconUrl: '/badges/scholar.png', obtained: false }
])

onMounted(async () => {
  // In a real app, fetch from API. 
  // For demo, we check userStore or mock based on prompt requirements.
  // The userStore has badges (non-persistent list) if we fetched user with badges.
  // Or we can fetch specific user badges API.
  
  // Mock logic for demo based on Phase 3 requirement:
  // "Use colored/grayscale icons to show obtained/unobtained"
  
  // Assume we fetch user badges here
  // const res = await getUserBadges()
  // const obtainedIds = res.data.map(b => b.badgeId)
  // badges.value.forEach(b => b.obtained = obtainedIds.includes(b.id))
  
  // For now, let's just mock one obtained badge for visualization
  if (userStore.user?.continuousCheckinDays >= 7) {
      const b = badges.value.find(x => x.id === 102)
      if (b) {
          b.obtained = true
          b.reason = "已连续打卡 " + userStore.user.continuousCheckinDays + " 天"
      }
  }
})
</script>

<style scoped>
.grayscale {
  filter: grayscale(100%);
  opacity: 0.6;
}
</style>
