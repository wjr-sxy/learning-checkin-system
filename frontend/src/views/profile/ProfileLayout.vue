<template>
  <div class="profile-root">
    <!-- Dynamic Background -->
    <div class="dynamic-background" v-if="activeBackgroundUrl">
      <video 
        v-if="isVideoBackground"
        :src="activeBackgroundUrl" 
        autoplay 
        loop 
        muted 
        playsinline
        class="bg-media"
      ></video>
      <div 
        v-else 
        class="bg-media bg-image"
        :style="{ backgroundImage: `url(${activeBackgroundUrl})` }"
      ></div>
    </div>

    <div class="profile-container" :class="{ 'has-background': !!activeBackgroundUrl }">
      <div class="profile-sidebar">
      <div class="sidebar-title">个人中心</div>
      <el-menu
        :default-active="activeMenu"
        class="profile-menu"
        router
      >
        <el-menu-item index="/profile/info">
          <el-icon><User /></el-icon>
          <span>我的资料</span>
        </el-menu-item>
        <el-menu-item index="/profile/security">
          <el-icon><Lock /></el-icon>
          <span>账号安全</span>
        </el-menu-item>
        <el-menu-item index="/profile/points">
          <el-icon><Coin /></el-icon>
          <span>我的积分</span>
        </el-menu-item>
        <el-menu-item index="/profile/decorations" v-if="isStudent">
          <el-icon><MagicStick /></el-icon>
          <span>我的装扮</span>
        </el-menu-item>
        <el-menu-item index="/profile/logs">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
      
      <div class="sidebar-footer">
        <el-button type="default" class="back-btn" @click="goBack">
          <el-icon style="margin-right: 5px"><Back /></el-icon>
          返回首页
        </el-button>
      </div>
    </div>
    <div class="profile-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { User, Lock, Coin, MagicStick, Document, Back } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const isStudent = computed(() => userStore.user?.role === 'USER')
const activeBackgroundUrl = computed(() => userStore.user?.profileBackground || '')
const isVideoBackground = computed(() => {
  const url = activeBackgroundUrl.value
  return url && (url.endsWith('.mp4') || url.endsWith('.webm'))
})

const goBack = () => {
  const role = userStore.user?.role
  if (role === 'ADMIN') {
    router.push('/admin-dashboard')
  } else if (role === 'TEACHER') {
    router.push('/teacher-dashboard')
  } else {
    router.push('/student-dashboard')
  }
}
</script>

<style scoped>
.profile-root {
  position: relative;
  min-height: calc(100vh - 60px);
  background-color: #f5f7fa;
}

.dynamic-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

.bg-media {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bg-image {
  background-size: cover;
  background-position: center;
}

.profile-container {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: calc(100vh - 60px); /* Adjust based on top nav height */
  /* background-color: #f5f7fa; Moved to root */
  padding: 20px;
  gap: 20px;
}

.profile-container.has-background {
  background-color: rgba(245, 247, 250, 0.4);
}

.profile-sidebar {
  width: 260px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px var(--shadow-color-light);
  border: 1px solid var(--border-color-extra-light);
  position: sticky;
  top: 80px; /* Adjusted for potential fixed header */
  height: fit-content;
  padding-bottom: 20px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.profile-sidebar:hover {
  box-shadow: 0 8px 24px var(--shadow-color-base);
}

.sidebar-title {
  padding: 24px;
  font-size: 20px;
  font-weight: bold;
  border-bottom: 1px solid var(--border-color-lighter);
  margin-bottom: 10px;
  color: var(--el-text-color-primary);
  background: var(--bg-color-base);
}

.profile-menu {
  border-right: none;
  padding: 0 12px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #eee;
}

.back-btn {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.profile-content {
  flex: 1;
  min-width: 0; /* Fix flex overflow issues */
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 30px;
  min-height: 600px;
}

/* Active menu item styling */
:deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}

:deep(.el-menu-item) {
  border-radius: 8px;
  margin: 4px 0;
  height: 50px;
  line-height: 50px;
  border-left: none; /* Remove left border */
}

:deep(.el-menu-item:hover) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
    padding: 10px;
  }
  
  .profile-sidebar {
    width: 100%;
    position: relative;
    top: 0;
  }
  
  .profile-content {
    min-width: 100%;
  }
}
</style>
