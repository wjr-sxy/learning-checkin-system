<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="el-dropdown-link account-switcher">
      <div class="avatar-wrapper mr-2">
        <el-avatar :size="32" :src="userStore.user?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
        <img v-if="userStore.user?.currentAvatarFrame" :src="userStore.user.currentAvatarFrame" class="avatar-frame" />
      </div>
      <span class="username mr-1">{{ userStore.user?.username }}</span>
      <el-tag size="small" effect="plain" class="role-badge mr-1">{{ formatRole(userStore.user?.role) }}</el-tag>
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu class="account-dropdown-menu">
        <el-dropdown-item disabled class="current-account-label">当前账号</el-dropdown-item>
        
        <div v-if="otherAccounts.length > 0">
            <el-dropdown-item 
            v-for="acc in otherAccounts" 
            :key="acc.userId" 
            :command="{ type: 'switch', account: acc }"
            >
            <div class="account-item">
                <el-avatar :size="24" :src="acc.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
                <span class="ml-2 account-name">{{ acc.username }}</span>
                <el-tag size="small" class="ml-2 scale-75">{{ formatRole(acc.role) }}</el-tag>
            </div>
            </el-dropdown-item>
            <el-divider style="margin: 4px 0;" />
        </div>
        
        <el-dropdown-item :command="{ type: 'profile' }">
          <el-icon><User /></el-icon>个人中心
        </el-dropdown-item>
        <el-dropdown-item :command="{ type: 'add' }">
          <el-icon><Plus /></el-icon>添加账号 / 切换
        </el-dropdown-item>
        <el-dropdown-item :command="{ type: 'logout' }" divided>
          <el-icon><SwitchButton /></el-icon>退出当前账号
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../stores/user'
import { useAccountStore } from '../stores/account'
import { ArrowDown, Plus, SwitchButton, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const accountStore = useAccountStore()
const router = useRouter()

const otherAccounts = computed(() => {
  return accountStore.accounts.filter(a => a.userId !== userStore.user?.id)
})

const formatRole = (role: string) => {
    switch(role) {
        case 'ADMIN': return '管理员'
        case 'TEACHER': return '教师'
        case 'STUDENT': return '学生'
        default: return role
    }
}

const handleCommand = (cmd: any) => {
  if (cmd.type === 'switch') {
    switchAccount(cmd.account)
  } else if (cmd.type === 'profile') {
    router.push('/profile')
  } else if (cmd.type === 'add') {
    // Clear active session to force login page, but keep vault
    sessionStorage.removeItem('active_token')
    window.location.href = '/login'
  } else if (cmd.type === 'logout') {
    handleLogout()
  }
}

const switchAccount = (account: any) => {
  // Update active token
  userStore.setToken(account.token)
  // Reload page to refresh all state
  window.location.reload()
}

const handleLogout = () => {
  if (userStore.user) {
    accountStore.removeAccount(userStore.user.id)
  }
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.account-switcher {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: var(--el-text-color-primary);
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.avatar-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-frame {
  position: absolute;
  top: -20%;
  left: -20%;
  width: 140%;
  height: 140%;
  z-index: 10;
  pointer-events: none;
}
.account-switcher:hover {
    background-color: rgba(0,0,0,0.05);
}
.mr-2 { margin-right: 8px; }
.mr-1 { margin-right: 4px; }
.ml-2 { margin-left: 8px; }
.account-item {
  display: flex;
  align-items: center;
  min-width: 150px;
}
.account-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}
.role-badge {
    font-size: 10px;
    height: 18px;
    padding: 0 4px;
}
.scale-75 {
    transform: scale(0.9);
}
.username {
    font-weight: 500;
}
</style>
