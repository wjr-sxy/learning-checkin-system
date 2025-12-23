<template>
  <div class="user-info">
    <h2 class="section-title">我的资料</h2>
    
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="info-form"
    >
      <el-form-item label="头像" prop="avatar">
        <el-upload
          class="avatar-uploader"
          action=""
          :show-file-list="false"
          :auto-upload="false"
          :on-change="handleAvatarChange"
        >
          <img v-if="form.avatar" :src="form.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
        <div class="avatar-tip">支持 jpg/png，大小不超过 2MB</div>
      </el-form-item>

      <el-form-item label="姓名" prop="fullName">
        <el-input v-model="form.fullName" maxlength="20" show-word-limit />
      </el-form-item>

      <el-form-item label="学院" prop="college">
        <el-select v-model="form.college" placeholder="请选择学院" style="width: 100%">
          <el-option label="计算机学院" value="计算机学院" />
          <el-option label="软件学院" value="软件学院" />
          <el-option label="信息工程学院" value="信息工程学院" />
          <el-option label="数理学院" value="数理学院" />
          <el-option label="外国语学院" value="外国语学院" />
        </el-select>
      </el-form-item>

      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号">
          <template #prepend>+86</template>
        </el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="loading">保存修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useUserStore } from '../../stores/user'
import { updateProfile } from '../../api/user'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  id: 0,
  avatar: '',
  fullName: '',
  college: '',
  phone: ''
})

const rules = reactive<FormRules>({
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 20, message: '长度不能超过 20 个字符', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
})

watch(() => userStore.user, (newUser) => {
  console.log('UserInfo watch newUser:', newUser)
  if (newUser && typeof newUser === 'object') {
    try {
      form.id = newUser.id || 0
      form.avatar = newUser.avatar || ''
      form.fullName = newUser.fullName || ''
      form.college = newUser.college || ''
      form.phone = newUser.phone || ''
    } catch (e) {
      console.error('Error assigning user info:', e)
    }
  }
}, { immediate: true })

const handleAvatarChange = (file: any) => {
  const isJPG = file.raw.type === 'image/jpeg' || file.raw.type === 'image/png'
  if (!isJPG) {
    ElMessage.error('Avatar picture must be JPG or PNG format!')
    return
  }
  
  // Compress if > 2MB or just resize to standard
  // Always compress/resize for consistency
  compressImage(file.raw)
}

const compressImage = (file: File) => {
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = (e: any) => {
    const img = new Image()
    img.src = e.target.result
    img.onload = () => {
      const canvas = document.createElement('canvas')
      let width = img.width
      let height = img.height
      
      // Resize to max 800x800
      const maxSize = 800
      if (width > maxSize || height > maxSize) {
        if (width > height) {
          height = Math.round((height * maxSize) / width)
          width = maxSize
        } else {
          width = Math.round((width * maxSize) / height)
          height = maxSize
        }
      }
      
      canvas.width = width
      canvas.height = height
      const ctx = canvas.getContext('2d')
      if (ctx) {
        ctx.drawImage(img, 0, 0, width, height)
        form.avatar = canvas.toDataURL('image/jpeg', 0.8)
      }
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await updateProfile(form)
        userStore.setUser(res.data)
        ElMessage.success('个人资料已更新')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.section-title {
  margin-bottom: 30px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.info-form {
  max-width: 500px;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar-uploader-icon:hover {
  border-color: var(--el-color-primary);
}

.avatar-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}
</style>
