<template>
  <ErrorBoundary>
    <router-view />
  </ErrorBoundary>
</template>

<script setup lang="ts">
import ErrorBoundary from './components/ErrorBoundary.vue'
import { onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from './stores/user'
import { useFriendStore } from './stores/friend'
import { sendHeartbeat, getUserOnlineStats } from './api/stats'
import { ElNotification } from 'element-plus'

const userStore = useUserStore()
const friendStore = useFriendStore()
let heartbeatTimer: any = null
let websocket: WebSocket | null = null

const connectWebSocket = () => {
    if (websocket) {
        websocket.close()
        websocket = null
    }
    if (!userStore.token) return

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    // Assuming backend is on localhost:8081. In production, this should be configurable.
    const host = 'localhost:8081' 
    const url = `${protocol}//${host}/ws/notification?token=${userStore.token}`
    
    websocket = new WebSocket(url)
    
    websocket.onopen = () => {
        console.log('WebSocket connected')
    }
    
    websocket.onmessage = (event) => {
        try {
            const data = JSON.parse(event.data)
            if (data.type === 'FRIEND_REQUEST') {
                ElNotification({
                    title: '好友请求',
                    message: '您有一个新的好友请求',
                    type: 'info',
                    duration: 5000
                })
                friendStore.loadRequests()
            } else if (data.type === 'FRIEND_ACCEPT') {
                 ElNotification({
                    title: '好友通知',
                    message: '您的好友请求已被接受',
                    type: 'success'
                })
                friendStore.loadFriends()
            } else if (data.type === 'TASK_REMINDER') {
                ElNotification({
                    title: '作业提醒',
                    message: `任务 "${data.title}" 还有 ${data.hoursLeft} 小时截止，请尽快提交！`,
                    type: 'warning',
                    duration: 0 // No auto-close
                })
            } else if (data.type === 'NEW_TASK') {
                ElNotification({
                    title: '新任务发布',
                    message: `新任务 "${data.title}" 已发布，请及时查看`,
                    type: 'info',
                    duration: 0
                })
            } else if (data.type === 'TASK_GRADED') {
                ElNotification({
                    title: '作业评分',
                    message: `您的作业 (ID: ${data.taskId}) 已评分，得分: ${data.score}`,
                    type: 'success'
                })
            } else if (data.type === 'TASK_RETURNED') {
                ElNotification({
                    title: '作业被打回',
                    message: `您的作业 (ID: ${data.taskId}) 已被打回，请修改后重新提交`,
                    type: 'error',
                    duration: 0
                })
            } else if (data.type === 'REMIND_CHECKIN') {
                ElNotification({
                    title: data.title || '好友提醒',
                    message: data.content,
                    type: 'warning',
                    duration: 6000
                })
            }
        } catch (e) {
            console.error('WS message parse error', e)
        }
    }
    
    websocket.onclose = () => {
        console.log('WebSocket closed')
    }
    
    websocket.onerror = (err) => {
        console.error('WebSocket error', err)
    }
}

const startHeartbeat = () => {
  if (heartbeatTimer) clearInterval(heartbeatTimer)
  
  // Connect WS
  connectWebSocket()

  // Initial fetch
  if (userStore.user) {
      getUserOnlineStats(userStore.user.id).then((res: any) => {
          userStore.setOnlineStats(res.data.totalSeconds, res.data.todaySeconds)
      })
  }

  heartbeatTimer = setInterval(() => {
    if (userStore.user && userStore.token) {
      const duration = 60 // 60 seconds
      sendHeartbeat({ userId: userStore.user.id, duration }).then(() => {
        userStore.updateOnlineTime(duration)
      }).catch(err => console.error('Heartbeat failed:', err.response || err))
    }
  }, 60000)
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
  if (websocket) {
      websocket.close()
      websocket = null
  }
}

watch(() => userStore.token, (newToken) => {
  if (newToken) {
    startHeartbeat()
  } else {
    stopHeartbeat()
  }
})

onMounted(() => {
  if (userStore.token) {
    startHeartbeat()
  }
})

onUnmounted(() => {
  stopHeartbeat()
})
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
