<template>
  <div class="notification-center">
    <el-popover
      placement="bottom"
      :width="350"
      trigger="click"
      popper-class="notification-popover"
    >
      <template #reference>
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
          <el-button circle size="small">
            <el-icon><Bell /></el-icon>
          </el-button>
        </el-badge>
      </template>

      <div class="notification-header">
        <span class="title">消息中心</span>
        <el-button link type="primary" size="small" @click="markAllRead" v-if="unreadCount > 0">全部已读</el-button>
      </div>

      <el-tabs v-model="activeTab" class="notification-tabs">
        <el-tab-pane label="全部" name="all">
          <div class="notification-list" v-loading="loading">
            <el-empty v-if="notifications.length === 0" description="暂无消息" :image-size="60" />
            <div
              v-else
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ 'unread': !item.isRead }"
              @click="handleRead(item)"
            >
              <div class="item-icon">
                <el-icon v-if="item.type === 'SYSTEM'" color="#409EFF"><InfoFilled /></el-icon>
                <el-icon v-else-if="item.type === 'ALERT'" color="#F56C6C"><WarningFilled /></el-icon>
                <el-icon v-else color="#E6A23C"><BellFilled /></el-icon>
              </div>
              <div class="item-content">
                <div class="item-title">{{ item.title }}</div>
                <div class="item-desc">{{ item.content }}</div>
                <div class="item-time">{{ formatTime(item.createTime) }}</div>
              </div>
              <div class="item-status" v-if="!item.isRead">
                <div class="dot"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="未读" name="unread">
          <div class="notification-list" v-loading="loading">
            <el-empty v-if="unreadList.length === 0" description="暂无未读消息" :image-size="60" />
            <div
              v-else
              v-for="item in unreadList"
              :key="item.id"
              class="notification-item unread"
              @click="handleRead(item)"
            >
              <div class="item-icon">
                 <el-icon v-if="item.type === 'SYSTEM'" color="#409EFF"><InfoFilled /></el-icon>
                <el-icon v-else-if="item.type === 'ALERT'" color="#F56C6C"><WarningFilled /></el-icon>
                <el-icon v-else color="#E6A23C"><BellFilled /></el-icon>
              </div>
              <div class="item-content">
                <div class="item-title">{{ item.title }}</div>
                <div class="item-desc">{{ item.content }}</div>
                <div class="item-time">{{ formatTime(item.createTime) }}</div>
              </div>
              <div class="item-status">
                <div class="dot"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { Bell, InfoFilled, WarningFilled, BellFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import { ElMessage, ElNotification } from 'element-plus'
import JSConfetti from 'js-confetti' // We need to install this or implement fireworks manually. For now, use simple fallback or prompt.

const userStore = useUserStore()
const activeTab = ref('all')
const loading = ref(false)
const notifications = ref<any[]>([])
const timer = ref<any>(null)
const socket = ref<WebSocket | null>(null)

const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.isRead).length
})

const unreadList = computed(() => {
  return notifications.value.filter(n => !n.isRead)
})

const loadNotifications = async () => {
  if (!userStore.user) return
  loading.value = true
  try {
    const res: any = await request.get('/notification/all', {
      params: { userId: userStore.user.id }
    })
    notifications.value = res.data || []
  } catch (error) {
    console.error('Failed to load notifications', error)
  } finally {
    loading.value = false
  }
}

// Background polling for new messages (every 30s)
const startPolling = () => {
  timer.value = setInterval(async () => {
      if (!userStore.user) return
      try {
        const res: any = await request.get('/notification/all', {
          params: { userId: userStore.user.id }
        })
        notifications.value = res.data || []
      } catch (e) {}
  }, 30000)
}

const showFireworks = () => {
    // Simple canvas confetti implementation or just CSS
    // Since we didn't install a library, let's skip complex animation or try to load a CDN script if possible, 
    // but in this environment we stick to Vue/Element.
    // We can use ElNotification with a custom class to show a big emoji.
}

const connectWebSocket = () => {
  const token = sessionStorage.getItem('active_token')
  if (!userStore.user || !token) return
  
  // Use configured backend port 8081
  const wsUrl = `ws://localhost:8081/ws/notification?token=${token}`
  
  socket.value = new WebSocket(wsUrl)
  
  socket.value.onopen = () => {
    console.log('Notification WebSocket connected')
  }
  
  socket.value.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      // Ignore chat messages
      if (data.type === 'CHAT') return

      // Handle ACHIEVEMENT_UNLOCKED
      if (data.type === 'ACHIEVEMENT_UNLOCKED') {
          ElNotification({
              title: '🎉 恭喜获得成就！',
              message: `解锁勋章：${data.name} (+${data.points} 积分)`,
              type: 'success',
              duration: 8000,
              offset: 100
          })
          // Trigger fireworks if possible
          showFireworks()
          // Reload notifications
          loadNotifications()
          return
      }

      // If it's a known notification structure
      if (data.title || data.content) {
        ElNotification({
          title: data.title || '新消息',
          message: data.content || '',
          type: data.type === 'ALERT' ? 'warning' : 'info',
          duration: 5000
        })
        // Reload list to get the full record from DB
        loadNotifications()
      } else {
        // Handle other types like FRIEND_REQUEST which might not match exact structure
        if (data.type === 'FRIEND_REQUEST') {
             ElNotification({
                title: '好友请求',
                message: '您收到一个新的好友请求',
                type: 'info'
             })
             // Optionally trigger friend request reload if that component listens to something or we can emit event
        } else if (data.type === 'NEW_TASK') {
            ElNotification({
                title: '新任务发布',
                message: `任务: ${data.title}`,
                type: 'success'
            })
            loadNotifications()
        } else if (data.type === 'TASK_GRADED') {
            ElNotification({
                title: '作业已评分',
                message: `分数: ${data.score}`,
                type: 'success'
            })
            loadNotifications()
        } else if (data.type === 'REMIND_CHECKIN') {
            ElNotification({
                title: data.title || '好友提醒',
                message: data.content,
                type: 'warning',
                duration: 6000
            })
            loadNotifications()
        }
      }
    } catch (e) {
      console.error('WebSocket message parse error', e)
    }
  }
  
  socket.value.onclose = () => {
    console.log('Notification WebSocket closed')
  }
  
  socket.value.onerror = (error) => {
    console.error('Notification WebSocket error', error)
  }
}

const handleRead = async (item: any) => {
  if (item.isRead) return
  try {
    await request.post(`/notification/read/${item.id}`)
    item.isRead = true
  } catch (error) {
    // ignore
  }
}

const markAllRead = async () => {
  if (!userStore.user) return
  try {
    await request.post('/notification/read-all', null, {
      params: { userId: userStore.user.id }
    })
    notifications.value.forEach(n => n.isRead = true)
    ElMessage.success('全部已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

onMounted(() => {
  loadNotifications()
  startPolling()
  connectWebSocket()
})

onUnmounted(() => {
  if (timer.value) clearInterval(timer.value)
  if (socket.value) socket.value.close()
})
</script>

<style scoped>
.notification-center {
  margin-right: 15px;
  display: inline-block;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.title {
  font-weight: bold;
  font-size: 16px;
}

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f9f9f9;
}

.notification-item.unread {
  background-color: #f0f9eb;
}

.item-icon {
  margin-right: 10px;
  display: flex;
  align-items: center;
}

.item-content {
  flex: 1;
}

.item-title {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.4;
}

.item-time {
  font-size: 11px;
  color: #999;
}

.item-status {
  display: flex;
  align-items: center;
  margin-left: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}
</style>
