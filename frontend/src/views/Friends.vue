<template>
  <div class="friends-container">
    <div class="header">
      <h2>我的好友</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon> 添加好友
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="friend-tabs">
      <el-tab-pane label="好友列表" name="list">
        <div v-if="friendStore.loading" class="loading-state">
           <el-skeleton :rows="3" animated />
        </div>
        <el-empty v-else-if="friendStore.friends.length === 0" description="暂无好友，快去添加吧！" />
        <div v-else class="friend-grid">
          <div v-for="friend in friendStore.friends" :key="friend.id" class="friend-card">
            <div class="friend-info">
               <el-avatar :size="50" :src="friend.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
               <div class="text-info">
                 <div class="name">{{ friend.fullName || friend.username }}</div>
                 <div class="status">
                   <span class="status-dot" :class="{ online: isOnline(friend.lastActiveTime) }"></span>
                   {{ isOnline(friend.lastActiveTime) ? '在线' : '离线' }}
                 </div>
               </div>
            </div>
            <div class="actions">
               <el-button type="danger" link @click="confirmDelete(friend)">删除</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane name="requests">
        <template #label>
          <span class="custom-tabs-label">
            <span>好友请求</span>
            <el-badge v-if="friendStore.requests.length > 0" :value="friendStore.requests.length" class="badge-item" />
          </span>
        </template>
        
        <el-empty v-if="friendStore.requests.length === 0" description="暂无新的好友请求" />
        <div v-else class="request-list">
           <div v-for="req in friendStore.requests" :key="req.requestId" class="request-item">
              <div class="requester-info">
                 <el-avatar :size="40" :src="req.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                 <div class="req-details">
                    <div class="name">{{ req.fullName || req.username }}</div>
                    <div class="time">{{ formatTime(req.createTime) }}</div>
                 </div>
              </div>
              <div class="req-actions">
                 <el-button type="success" size="small" @click="friendStore.handleRequest(req.requestId, 1)">接受</el-button>
                 <el-button type="info" size="small" @click="friendStore.handleRequest(req.requestId, 2)">拒绝</el-button>
              </div>
           </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Add Friend Dialog -->
    <el-dialog v-model="showAddDialog" title="添加好友" width="500px">
      <div class="search-box">
        <el-input 
          v-model="searchKeyword" 
          placeholder="输入用户名或姓名搜索" 
          @keyup.enter="handleSearch"
          clearable
        >
          <template #append>
            <el-button @click="handleSearch">
               <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      
      <div class="search-results mt-4">
         <div v-if="searchResults.length === 0 && hasSearched" class="no-results">未找到用户</div>
         <div v-else class="user-list">
            <div v-for="user in searchResults" :key="user.id" class="user-item">
               <div class="user-basic">
                  <el-avatar :size="36" :src="user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                  <span>{{ user.fullName || user.username }}</span>
               </div>
               <el-button type="primary" size="small" @click="friendStore.sendRequest(user.id)">添加</el-button>
            </div>
         </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useFriendStore } from '../stores/friend'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const friendStore = useFriendStore()
const activeTab = ref('list')
const showAddDialog = ref(false)
const searchKeyword = ref('')
const searchResults = ref<any[]>([])
const hasSearched = ref(false)

const isOnline = (lastActiveTime: string) => {
    if (!lastActiveTime) return false
    // If active within last 5 minutes, consider online
    const diff = dayjs().diff(dayjs(lastActiveTime), 'minute')
    return diff < 5
}

const formatTime = (time: string) => {
    return dayjs(time).format('MM-DD HH:mm')
}

const handleSearch = async () => {
    if (!searchKeyword.value.trim()) return
    searchResults.value = await friendStore.searchUsers(searchKeyword.value)
    hasSearched.value = true
}

const confirmDelete = (friend: any) => {
    ElMessageBox.confirm(
        `确定要删除好友 ${friend.fullName || friend.username} 吗?`,
        '提示',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(() => {
        friendStore.deleteFriend(friend.id)
    })
}

onMounted(() => {
    friendStore.loadFriends()
    friendStore.loadRequests()
})
</script>

<style scoped>
.friends-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
  background-color: var(--bg-color-white);
  border-radius: 8px;
  min-height: 80vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
    margin: 0;
    font-size: 24px;
    color: var(--text-primary);
}

.friend-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 20px;
    margin-top: 20px;
}

.friend-card {
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 15px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.3s;
}

.friend-card:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.friend-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.text-info .name {
    font-weight: 600;
    margin-bottom: 4px;
}

.status {
    font-size: 12px;
    color: var(--text-secondary);
    display: flex;
    align-items: center;
    gap: 4px;
}

.status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #909399;
}

.status-dot.online {
    background-color: #67C23A;
}

.badge-item {
    margin-left: 5px;
}

.request-list {
    max-width: 600px;
    margin: 0 auto;
}

.request-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid var(--border-color);
}

.requester-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.req-details .name {
    font-weight: 600;
}

.req-details .time {
    font-size: 12px;
    color: var(--text-secondary);
}

.user-list {
    max-height: 300px;
    overflow-y: auto;
}

.user-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid var(--border-color-lighter);
}

.user-basic {
    display: flex;
    align-items: center;
    gap: 10px;
}

.no-results {
    text-align: center;
    color: var(--text-secondary);
    padding: 20px;
}
</style>
