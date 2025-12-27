<template>
  <div class="friends-container">
    <div class="header">
      <div class="header-left">
        <el-page-header @back="router.push('/dashboard')" class="page-header">
            <template #content>
                <div class="header-title-row">
                    <h2>管理我的学友</h2>
                    <span class="subtitle">与 {{ friendStore.friends.length }} 位好友一起学习</span>
                </div>
            </template>
        </el-page-header>
      </div>
      <el-button type="primary" class="add-btn" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon> 添加好友
      </el-button>
    </div>

    <el-tabs v-model="activeTab" class="friend-tabs">
      <el-tab-pane name="list">
        <template #label>
          <div class="tab-label">
            <el-icon><User /></el-icon>
            <span>好友列表</span>
          </div>
        </template>
        
        <div v-if="friendStore.loading" class="loading-state">
           <el-skeleton :rows="3" animated />
        </div>
        <el-empty v-else-if="friendStore.friends.length === 0" description="暂无好友，快去添加吧！" />
        
        <div v-else class="friend-grid">
          <el-card 
            v-for="friend in friendStore.friends" 
            :key="friend.id" 
            class="friend-card" 
            shadow="hover" 
            :body-style="{ padding: '0px' }"
          >
            <div class="card-content">
              <!-- Avatar Section -->
              <div class="avatar-wrapper">
                <AvatarFrame 
                   :avatar-url="friend.avatar || defaultAvatar" 
                   :frame-url="friend.currentAvatarFrame" 
                   :size="80" 
                />
                <div 
                  class="status-dot" 
                  :class="{ online: friend.isOnline }"
                  :title="friend.isOnline ? '在线' : '离线'"
                ></div>
              </div>
              
              <!-- Info Section -->
              <div class="info-wrapper">
                <div class="name-row">
                   <span class="name">{{ friend.fullName || friend.username }}</span>
                   <el-tag v-if="friend.college" size="small" effect="plain" round>{{ friend.college }}</el-tag>
                </div>
                <div class="status-text">
                   <span v-if="friend.isOnline" class="online-text">当前在线</span>
                   <span v-else>上次活跃: {{ formatLastActive(friend.lastActiveTime) }}</span>
                </div>
              </div>

              <!-- Action Section -->
              <div class="actions-wrapper">
                 <el-tooltip 
                   :content="friend.isCheckedIn ? '好友今日已打卡' : (friend.isReminded ? '今日已提醒' : '提醒好友去打卡')" 
                   placement="top"
                 >
                   <el-button 
                     :type="(friend.isCheckedIn || friend.isReminded) ? 'info' : 'primary'" 
                     :plain="friend.isCheckedIn || friend.isReminded"
                     :disabled="friend.isCheckedIn || friend.isReminded"
                     class="action-btn remind-btn"
                     @click="handleRemind(friend)"
                   >
                     <el-icon class="mr-1"><Bell /></el-icon>
                     {{ friend.isCheckedIn ? '今日已学' : (friend.isReminded ? '已提醒' : '催打卡') }}
                   </el-button>
                 </el-tooltip>
                 
                 <el-popconfirm title="确定删除该好友吗?" @confirm="confirmDelete(friend)">
                   <template #reference>
                     <el-button link type="danger" class="action-btn delete-btn">
                       <el-icon><Delete /></el-icon>
                     </el-button>
                   </template>
                 </el-popconfirm>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane name="requests">
        <template #label>
          <div class="tab-label">
            <el-icon><Message /></el-icon>
            <span>好友请求</span>
            <el-badge v-if="friendStore.requests.length > 0" :value="friendStore.requests.length" class="badge-item" />
          </div>
        </template>
        
        <el-empty v-if="friendStore.requests.length === 0" description="暂无新的好友请求" />
        <div v-else class="request-list">
           <el-card v-for="req in friendStore.requests" :key="req.requestId" class="request-card" shadow="hover">
              <div class="request-content">
                 <div class="requester-info">
                    <AvatarFrame 
                       :avatar-url="req.avatar || defaultAvatar" 
                       :size="50" 
                    />
                    <div class="req-details">
                       <div class="name">{{ req.fullName || req.username }}</div>
                       <div class="time">申请时间: {{ formatTime(req.createTime) }}</div>
                    </div>
                 </div>
                 <div class="req-actions">
                    <el-button type="success" @click="friendStore.handleRequest(req.requestId, 1)">
                       <el-icon class="mr-1"><Check /></el-icon> 接受
                    </el-button>
                    <el-button type="text" class="reject-btn" @click="friendStore.handleRequest(req.requestId, 2)">
                       拒绝
                    </el-button>
                 </div>
              </div>
           </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Add Friend Dialog -->
    <el-dialog v-model="showAddDialog" title="添加好友" width="600px" custom-class="search-dialog">
      <div class="search-container">
         <div class="search-inputs">
           <el-select 
             v-model="searchCollege" 
             placeholder="按学院筛选" 
             clearable 
             class="college-select"
             @change="handleSearch"
           >
              <el-option v-for="c in colleges" :key="c" :label="c" :value="c" />
           </el-select>
           <el-input 
              v-model="searchKeyword" 
              placeholder="输入用户名或姓名搜索" 
              class="keyword-input"
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
      </div>
      
      <div class="search-results-area">
         <div v-if="friendStore.loading" class="loading-state">
            <el-skeleton :rows="2" animated />
         </div>
         <div v-else-if="searchResults.length === 0 && hasSearched" class="empty-result">
            <el-empty description="没找到？尝试邀请室友加入吧" :image-size="100" />
         </div>
         <div v-else class="user-list">
            <div v-for="user in searchResults" :key="user.id" class="user-item">
               <div class="user-basic">
                  <AvatarFrame 
                     :avatar-url="user.avatar || defaultAvatar" 
                     :frame-url="user.currentAvatarFrame"
                     :size="40" 
                  />
                  <div class="user-text">
                    <span class="username">{{ user.fullName || user.username }}</span>
                    <span class="college" v-if="user.college">{{ user.college }}</span>
                  </div>
               </div>
               <el-button type="primary" size="small" round @click="friendStore.sendRequest(user.id)">
                 <el-icon class="mr-1"><Plus /></el-icon> 添加
               </el-button>
            </div>
         </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useFriendStore } from '../stores/friend'
import { Plus, Search, User, Message, Delete, Bell, Check } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import AvatarFrame from '@/components/AvatarFrame.vue'

const router = useRouter()
const friendStore = useFriendStore()
const activeTab = ref('list')
const showAddDialog = ref(false)
const searchKeyword = ref('')
const searchCollege = ref('')
const searchResults = ref<any[]>([])
const hasSearched = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// Common colleges list
const colleges = [
  '计算机学院',
  '软件学院',
  '信息工程学院',
  '外国语学院',
  '经济管理学院',
  '理学院',
  '艺术学院'
]

const formatLastActive = (time: string) => {
    if (!time) return '很久以前'
    return dayjs(time).fromNow()
}

const formatTime = (time: string) => {
    return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const handleSearch = async () => {
    if (!searchKeyword.value.trim() && !searchCollege.value) {
      if (hasSearched.value) {
        searchResults.value = []
        hasSearched.value = false
      }
      return
    }
    
    searchResults.value = await friendStore.searchUsers(searchKeyword.value, searchCollege.value)
    hasSearched.value = true
}

const handleRemind = (friend: any) => {
  if (friend.isCheckedIn || friend.isReminded) return
  console.log('Reminding friend:', friend.id)
  friendStore.remindFriend(friend.id)
    .then(() => {
        console.log('Remind success')
        ElMessage.success('催促成功！已通过系统消息提醒好友。')
        friend.isReminded = true
    })
    .catch((error: any) => {
        console.error('Remind error:', error)
        const msg = error.response?.data?.message || error.message || '提醒失败'
        ElMessage.error(msg)
    })
}

const confirmDelete = (friend: any) => {
    friendStore.deleteFriend(friend.id)
}

const goBack = () => {
    router.push('/dashboard')
}

onMounted(() => {
    friendStore.loadFriends()
    friendStore.loadRequests()
})
</script>

<style scoped>
.friends-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  min-height: 80vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
}

.header-title-row {
    display: flex;
    align-items: baseline;
    gap: 12px;
}

.header h2 {
    margin: 0;
    font-size: 28px;
    font-weight: 700;
    color: var(--text-primary);
    background: var(--student-theme);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin-left: 12px;
}

.add-btn {
  box-shadow: 0 4px 12px var(--shadow-color-light);
}

.friend-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Friend Grid Layout */
.friend-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    margin-top: 20px;
}

.friend-card {
  transition: all 0.3s;
}

.friend-card:hover {
  transform: translateY(-5px);
}

.card-content {
  display: flex;
  align-items: center;
  padding: 20px;
  gap: 20px;
}

.avatar-wrapper {
  position: relative;
}

.status-dot {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: var(--info-color-light);
  border: 2px solid #fff;
  transition: background-color 0.3s;
}

.status-dot.online {
  background-color: var(--success-color);
  box-shadow: 0 0 4px var(--success-color);
}

.name-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.status-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.online-text {
  color: var(--success-color);
  font-weight: 500;
}

.actions-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  width: 100%;
  justify-content: center;
}

.remind-btn {
  flex: 1;
  max-width: 120px;
}

/* Request List */
.request-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.request-card {
  border: 1px solid var(--border-color-lighter);
}

.request-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.requester-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.req-details .name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.req-details .time {
  font-size: 12px;
  color: var(--text-secondary);
}

.reject-btn {
  color: var(--text-secondary);
}

.reject-btn:hover {
  color: var(--text-regular);
}

/* Search Dialog */
.search-inputs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.college-select {
  width: 140px;
}

.keyword-input {
  flex: 1;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid var(--border-color-lighter);
  transition: background-color 0.2s;
}

.user-item:hover {
  background-color: var(--bg-color-base);
}

.user-basic {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-text {
  display: flex;
  flex-direction: column;
}

.user-text .username {
  font-weight: 500;
}

.user-text .college {
  font-size: 12px;
  color: var(--text-secondary);
}

.mr-1 {
  margin-right: 4px;
}
</style>
