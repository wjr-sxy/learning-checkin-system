<template>
  <el-card class="chat-room-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>公共聊天室</span>
        <el-tag size="small" type="success" effect="dark">在线互动</el-tag>
      </div>
    </template>
    
    <div class="chat-messages" ref="messagesRef">
      <el-empty v-if="messages.length === 0" description="暂无消息，打个招呼吧！" :image-size="60" />
      <div v-for="(msg, index) in messages" :key="index" class="message-item" :class="{ 'self': msg.senderId === userStore.user?.id }">
        <el-avatar :size="36" :src="msg.senderAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" class="msg-avatar" />
        <div class="msg-content-wrapper">
          <div class="msg-info">
            <span class="msg-name">{{ msg.senderName }}</span>
            <el-tag size="small" :type="getRoleType(msg.senderRole)" effect="light" class="role-tag">{{ formatRole(msg.senderRole) }}</el-tag>
            <span class="msg-time">{{ formatTime(msg.time) }}</span>
          </div>
          <div class="msg-bubble">
            {{ msg.content }}
          </div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        placeholder="输入消息，与大家互动..."
        @keyup.enter="sendMessage"
        :disabled="!isConnected"
      >
        <template #append>
          <el-button @click="sendMessage" :loading="sending" :icon="Position">发送</el-button>
        </template>
      </el-input>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import { Position } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const messages = ref<any[]>([])
const inputMessage = ref('')
const sending = ref(false)
const isConnected = ref(false)
const messagesRef = ref<HTMLElement | null>(null)
let socket: WebSocket | null = null

const getRoleType = (role: string) => {
  switch (role) {
    case 'ADMIN': return 'danger'
    case 'TEACHER': return 'warning'
    default: return 'primary'
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

const formatTime = (time: string) => {
  return dayjs(time).format('HH:mm')
}

const connectWebSocket = () => {
  const token = sessionStorage.getItem('active_token')
  if (!userStore.user || !token) return
  
  const wsUrl = `ws://localhost:8081/ws/notification?token=${token}`
  
  socket = new WebSocket(wsUrl)
  
  socket.onopen = () => {
    console.log('Chat WebSocket connected')
    isConnected.value = true
  }
  
  socket.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'CHAT') {
        messages.value.push(data)
        scrollToBottom()
      }
    } catch (e) {
      console.error('Chat parse error', e)
    }
  }
  
  socket.onclose = () => {
    console.log('Chat WebSocket closed')
    isConnected.value = false
  }
  
  socket.onerror = (error) => {
    console.error('Chat WebSocket error', error)
    isConnected.value = false
  }
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || !socket || !isConnected.value) return
  
  const content = inputMessage.value.trim()
  sending.value = true
  
  try {
    const msg = {
      type: 'CHAT',
      content: content
    }
    socket.send(JSON.stringify(msg))
    inputMessage.value = ''
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  if (socket) {
    socket.close()
  }
})
</script>

<style scoped>
.chat-room-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 15px;
  overflow: hidden;
  height: 400px; /* Fixed height for dashboard layout */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 15px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.message-item.self {
  flex-direction: row-reverse;
}

.msg-avatar {
  margin: 0 10px;
  flex-shrink: 0;
}

.msg-content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-item.self .msg-content-wrapper {
  align-items: flex-end;
}

.msg-info {
  margin-bottom: 4px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
}

.role-tag {
  margin: 0 5px;
  transform: scale(0.85);
}

.msg-bubble {
  background-color: white;
  padding: 8px 12px;
  border-radius: 0 12px 12px 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  font-size: 14px;
  line-height: 1.4;
  word-break: break-all;
}

.message-item.self .msg-bubble {
  background-color: #409EFF;
  color: white;
  border-radius: 12px 0 12px 12px;
}

.chat-input {
  margin-top: auto;
}
</style>
