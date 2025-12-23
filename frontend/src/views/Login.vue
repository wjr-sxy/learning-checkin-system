<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>{{ isRegister ? '用户注册' : '用户登录' }}</h2>
      </template>
      <el-form :model="form" label-width="0px" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名" 
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            size="large"
            :prefix-icon="Lock"
          />
        </el-form-item>

        <transition name="el-zoom-in-top">
          <div v-if="isRegister">
            <el-form-item prop="email">
              <el-input 
                v-model="form.email" 
                placeholder="请输入邮箱"
                size="large"
                :prefix-icon="Message"
              />
            </el-form-item>
            
            <el-form-item label="上传头像">
                 <el-upload
                     class="avatar-uploader"
                     action="/api/file/upload"
                     :show-file-list="false"
                     :on-success="handleAvatarSuccess"
                     :before-upload="beforeAvatarUpload"
                 >
                     <img v-if="form.avatar" :src="form.avatar" class="avatar" />
                     <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                 </el-upload>
            </el-form-item>
          </div>
        </transition>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleAction" 
            :loading="loading" 
            class="w-100 login-btn"
            size="large"
            round
          >
            {{ isRegister ? '立即注册' : '立即登录' }}
          </el-button>
        </el-form-item>
        
        <div class="text-center">
          <el-button link type="primary" @click="toggleMode" class="toggle-btn">
            {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Plus, User, Lock, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isRegister = ref(false)
const loading = ref(false)

const form = ref({
  username: '',
  password: '',
  email: '',
  avatar: ''
})

const toggleMode = () => {
  isRegister.value = !isRegister.value
  form.value = { username: '', password: '', email: '', avatar: '' }
}

const handleAvatarSuccess = (response: any) => {
    if (response.code === 200) {
        form.value.avatar = response.data
        ElMessage.success('头像上传成功')
    } else {
        ElMessage.error('头像上传失败')
    }
}

const beforeAvatarUpload = (rawFile: any) => {
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAction = async () => {
  if (isRegister.value) {
    await handleRegister()
  } else {
    await handleLogin()
  }
}

const handleLogin = async () => {
  loading.value = true
  try {
    const res: any = await request.post('/auth/login', {
        username: form.value.username,
        password: form.value.password
    })
    userStore.setUser(res.data.user)
    userStore.setToken(res.data.token)
    ElMessage.success('登录成功')
    
    // Redirect based on role
    const role = res.data.user.role
    if (role === 'ADMIN') {
        router.push('/admin-dashboard')
    } else if (role === 'TEACHER') {
        router.push('/teacher-dashboard')
    } else {
        router.push('/student-dashboard')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
    // Basic validation
    if (!form.value.username || !form.value.password || !form.value.email) {
        ElMessage.warning('请填写所有字段')
        return
    }
    loading.value = true
    try {
    await request.post('/auth/register', form.value)
    ElMessage.success('注册成功，请登录')
    toggleMode()
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: var(--login-bg-gradient);
  position: relative;
  overflow: hidden;
}

/* Background Decorations */
.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.login-container::before {
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.login-container::after {
  bottom: -150px;
  right: -150px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

.login-card {
  width: 420px;
  max-width: 90%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  border: none;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.25);
}

.login-card :deep(.el-card__header) {
  background: var(--login-bg-gradient);
  color: white;
  border-radius: 16px 16px 0 0;
  text-align: center;
  padding: 24px !important;
}

.login-card :deep(.el-card__header h2) {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.login-card :deep(.el-card__body) {
  padding: 32px !important;
}

.avatar-uploader .el-upload {
  border: 2px dashed var(--border-color-light);
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background: var(--bg-color-base);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--primary-color);
  background: var(--primary-color-lightest);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
}

.avatar-uploader-icon {
  font-size: 32px;
  color: var(--text-placeholder);
  width: 120px;
  height: 120px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

/* Form Styles */
.login-card :deep(.el-form) {
  max-width: 100%;
}

.login-card :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-card :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
}

.toggle-btn {
  font-weight: 600;
  font-size: 15px;
  color: #667eea !important; /* Matches gradient start color */
  transition: all 0.3s ease;
  margin-top: 8px;
}

.toggle-btn:hover {
  color: #764ba2 !important; /* Matches gradient end color */
  transform: scale(1.05);
  text-decoration: underline;
}

.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  transition: all 0.3s ease;
}

.login-card :deep(.el-button) {
  width: 100%;
  margin-top: 8px;
  padding: 12px;
  font-size: 15px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.login-card :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.login-card :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6);
}

.login-card :deep(.el-button + .el-button) {
  margin-left: 12px;
}

/* Responsive Adjustments */
@media (max-width: 768px) {
  .login-card {
    margin: 0 20px;
  }
  
  .login-card :deep(.el-card__body) {
    padding: 24px !important;
  }
  
  .login-card :deep(.el-button + .el-button) {
    margin-left: 0;
    margin-top: 12px;
    width: 100%;
  }
}
</style>
