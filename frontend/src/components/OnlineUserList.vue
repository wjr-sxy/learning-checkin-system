<template>
  <el-card class="online-users-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>当前在线用户 ({{ onlineUsers.length }})</span>
        <el-button link type="primary" size="small" @click="fetchOnlineUsers">
          <el-icon><Refresh /></el-icon>
        </el-button>
      </div>
    </template>
    
    <div class="user-list" v-loading="loading">
      <el-empty v-if="onlineUsers.length === 0" description="暂无在线用户" :image-size="60" />
      <div v-else class="user-grid">
        <div v-for="user in onlineUsers" :key="user.id" class="user-item">
          <el-avatar :size="40" :src="user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
          <div class="user-info">
            <span class="username" :title="user.username">{{ user.full_name || user.username }}</span>
            <el-tag size="small" :type="getRoleType(user.role)">{{ formatRole(user.role) }}</el-tag>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getOnlineUsersList } from '../api/stats'
import { Refresh } from '@element-plus/icons-vue'

const onlineUsers = ref<any[]>([])
const loading = ref(false)
const timer = ref<any>(null)

const fetchOnlineUsers = async () => {
  loading.value = true
  try {
    const res: any = await getOnlineUsersList()
    onlineUsers.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch online users', error)
  } finally {
    loading.value = false
  }
}

const getRoleType = (role: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'TEACHER': return 'warning'
    default: return 'success'
  }
}

const formatRole = (role: string) => {
  switch (role) {
    case 'ADMIN': return '管理员'
    case 'TEACHER': return '教师'
    case 'STUDENT': return '学生'
    default: return role
  }
}

onMounted(() => {
  fetchOnlineUsers()
  timer.value = setInterval(fetchOnlineUsers, 30000) // Refresh every 30s
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<style scoped>
.online-users-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-list {
  max-height: 400px;
  overflow-y: auto;
}

.user-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.user-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  background-color: #f9f9f9;
  transition: transform 0.2s;
}

.user-item:hover {
  transform: translateY(-2px);
  background-color: #f0f2f5;
}

.user-info {
  margin-top: 8px;
  text-align: center;
  width: 100%;
}

.username {
  display: block;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
