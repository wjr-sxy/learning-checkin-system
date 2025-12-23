<!--
  CourseCenter.vue
  教师端 - 课程中心模块
  用于展示教师创建的所有课程，支持新建课程和刷新邀请码
-->
<template>
  <div class="course-center">
    <div class="tab-actions mb-4">
      <el-button type="primary" size="large" @click="createDialogVisible = true">
        <el-icon class="mr-2"><Plus /></el-icon>新建课程
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="24" v-if="courses.length === 0">
        <el-empty description="暂无课程，快去创建吧" />
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" v-for="course in courses" :key="course.id" class="mb-4">
        <el-card shadow="hover" class="course-card">
          <template #header>
            <div class="card-header flex-between">
              <span class="course-title">{{ course.name }}</span>
              <div class="flex-start">
                <el-tooltip :content="getInviteCodeTip(course)" placement="top">
                  <el-tag :type="isCodeExpired(course) ? 'danger' : 'success'" effect="dark" class="mr-2">{{ course.code }}</el-tag>
                </el-tooltip>
                <el-tooltip content="刷新邀请码（延长7天有效期）" placement="top">
                  <el-button link type="primary" size="small" @click.stop="handleRefreshCode(course)">
                    <el-icon><Refresh /></el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </template>
          <div class="course-cover" v-if="parseDescription(course.description).cover">
            <el-image :src="parseDescription(course.description).cover" fit="cover" style="width: 100%; height: 150px; border-radius: 4px;" />
          </div>
          <div class="course-content">
            <div class="course-info">
              <p><strong>学期:</strong> {{ course.semester || '默认学期' }}</p>
              <p class="description">{{ parseDescription(course.description).content || '暂无描述' }}</p>
            </div>
            <div class="course-footer mt-4 flex-between">
              <el-button type="primary" plain size="small" @click="$emit('switch-to-class', course)">
                管理班级
              </el-button>
              <el-button type="warning" plain size="small" @click="handleRemind(course)">
                一键提醒
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Create Course Dialog -->
    <el-dialog v-model="createDialogVisible" title="新建课程" width="500px">
      <el-form :model="newCourse" :rules="courseRules" ref="createCourseFormRef" label-width="80px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="newCourse.name" placeholder="如：Vue.js 实战开发" />
        </el-form-item>
        <el-form-item label="所属学期" prop="semester">
          <el-select v-model="newCourse.semester" placeholder="选择学期" style="width: 100%">
            <el-option label="2025年春季学期" value="2025-Spring" />
            <el-option label="2025年秋季学期" value="2025-Autumn" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="newCourse.description" type="textarea" :rows="3" placeholder="简要介绍课程内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateCourse(createCourseFormRef)">确定创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { createCourse, refreshCourseCode, remindStudents, checkCourseName } from '@/api/course'
import { useUserStore } from '@/stores/user'

defineProps<{
  courses: any[]
}>()

const emit = defineEmits(['refresh', 'switch-to-class'])

const userStore = useUserStore()
const createDialogVisible = ref(false)
const createCourseFormRef = ref<FormInstance>()

const newCourse = ref({
  name: '',
  semester: '',
  description: ''
})

const validateCourseName = async (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入课程名称'))
  } else if (value.length > 20) {
    callback(new Error('课程名称不能超过20个字符'))
  } else {
    if (userStore.user) {
      try {
        const res: any = await checkCourseName(userStore.user.id, value)
        if (res.data) {
          callback(new Error('该课程名称已存在'))
        } else {
          callback()
        }
      } catch (error) {
        callback()
      }
    } else {
      callback()
    }
  }
}

const courseRules = {
  name: [{ required: true, validator: validateCourseName, trigger: 'blur' }],
  semester: [{ required: true, message: '请选择所属学期', trigger: 'change' }]
}

const handleCreateCourse = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid) => {
    if (valid) {
      if (!userStore.user) return
      try {
        await createCourse(userStore.user.id, newCourse.value.name, newCourse.value.description, newCourse.value.semester)
        ElMessage.success('创建成功，邀请码已生成')
        createDialogVisible.value = false
        newCourse.value = { name: '', semester: '', description: '' }
        emit('refresh')
      } catch (error: any) {
        ElMessage.error(error.message || '创建失败')
      }
    }
  })
}

const handleRefreshCode = async (course: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要刷新 "${course.name}" 的邀请码吗？\n旧邀请码将失效，新邀请码有效期为7天。`,
      '刷新邀请码',
      { confirmButtonText: '刷新', cancelButtonText: '取消', type: 'warning' }
    )
    const res: any = await refreshCourseCode(course.id)
    course.code = res.data
    course.codeExpireTime = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString()
    ElMessage.success('邀请码已更新，有效期延长7天')
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '刷新失败')
  }
}

const handleRemind = async (course: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要给 "${course.name}" 的所有学生发送打卡提醒吗？`,
      '发送提醒',
      { confirmButtonText: '发送', cancelButtonText: '取消', type: 'warning' }
    )
    await remindStudents(course.id)
    ElMessage.success('提醒发送成功')
  } catch (error: any) {
    if (error !== 'cancel') ElMessage.error(error.message || '发送失败')
  }
}

const parseDescription = (desc: string) => {
  try {
    return JSON.parse(desc)
  } catch (e) {
    return { content: desc }
  }
}

const isCodeExpired = (course: any) => {
  if (!course.codeExpireTime) return false
  return new Date(course.codeExpireTime).getTime() < Date.now()
}

const getInviteCodeTip = (course: any) => {
  if (!course.codeExpireTime) return '长期有效'
  if (isCodeExpired(course)) return '邀请码已过期，请刷新'
  return `有效期至：${new Date(course.codeExpireTime).toLocaleString()}`
}
</script>

<style scoped>
.course-card { height: 100%; display: flex; flex-direction: column; }
.course-title { font-weight: bold; font-size: 16px; }
.description { color: #666; font-size: 14px; margin-top: 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.mb-4 { margin-bottom: 1rem; }
.mt-4 { margin-top: 1rem; }
.mr-2 { margin-right: 0.5rem; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-start { display: flex; align-items: center; }
</style>
