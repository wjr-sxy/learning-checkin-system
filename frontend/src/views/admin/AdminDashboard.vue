<template>
  <div class="dashboard-container">
    <el-container>
      <el-header class="main-header">
        <div class="header-content">
          <div class="logo-section">
            <h1>学习打卡系统 - 管理后台</h1>
          </div>
          <div class="header-actions">
            <div class="user-info" v-if="userStore.user">
              <el-avatar :size="40" :src="userStore.user.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
              <span class="username">{{ userStore.user.username }}</span>
            </div>
            <el-button type="danger" plain size="small" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>

      <el-container class="content-container">
        <el-aside width="200px" class="aside-menu">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            @select="handleMenuSelect"
          >
            <el-menu-item index="overview">
              <el-icon><DataLine /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="audit">
              <el-icon><Monitor /></el-icon>
              <span>审计中心</span>
            </el-menu-item>
            <el-menu-item index="points">
              <el-icon><Coin /></el-icon>
              <span>积分央行</span>
            </el-menu-item>
            <el-menu-item index="mall">
              <el-icon><Shop /></el-icon>
              <span>商城运营</span>
            </el-menu-item>
            <el-menu-item index="content">
              <el-icon><Warning /></el-icon>
              <span>内容安全</span>
            </el-menu-item>
            <el-menu-item index="system">
              <el-icon><Setting /></el-icon>
              <span>系统工具</span>
            </el-menu-item>
            <el-menu-item index="logs">
              <el-icon><Document /></el-icon>
              <span>日志监控</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <el-main>
          <div class="admin-content">
             <component :is="currentComponent" />
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { DataLine, User, Monitor, Coin, Shop, Setting, Document, Warning } from '@element-plus/icons-vue'

import Overview from './components/Overview.vue'
import UserManagement from './components/UserManagement.vue'
import AuditCenter from './components/AuditCenter.vue'
import PointsCentral from './components/PointsCentral.vue'
import MallOperations from './MallOperations.vue'
import SystemTools from './components/SystemTools.vue'
import LogMonitoring from './components/LogMonitoring.vue'
import ContentSecurity from './ContentSecurity.vue'

const userStore = useUserStore()
const router = useRouter()

const activeMenu = ref('overview')
const currentComponent = shallowRef(Overview)

const menuMap: any = {
  'overview': Overview,
  'users': UserManagement,
  'audit': AuditCenter,
  'points': PointsCentral,
  'mall': MallOperations,
  'content': ContentSecurity,
  'system': SystemTools,
  'logs': LogMonitoring
}

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  currentComponent.value = menuMap[index]
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background-color: var(--bg-color-base);
  display: flex;
  flex-direction: column;

  /* Admin Theme Overrides - Purple */
  --primary-color: #9C27B0;
  --primary-color-light: #BA68C8;
  --primary-color-lighter: #E1BEE7;
  --primary-color-dark: #7B1FA2;
  --el-color-primary: #9C27B0;
  --el-menu-active-color: #9C27B0;
}

.main-header {
  background-color: var(--bg-color-white);
  box-shadow: 0 2px 4px var(--shadow-color-light);
  display: flex;
  align-items: center;
  z-index: 10;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content-container {
  flex: 1;
  display: flex;
}

.aside-menu {
  background-color: var(--bg-color-white);
  border-right: 1px solid var(--border-color-lighter);
}

.el-menu-vertical-demo {
  border-right: none;
}

.admin-content {
  background: var(--bg-color-white);
  padding: 24px;
  border-radius: 8px;
  min-height: 100%;
  box-shadow: 0 2px 12px var(--shadow-color-light);
}
</style>
