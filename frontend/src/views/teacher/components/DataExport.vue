<!--
  DataExport.vue
  教师端 - 数据导出模块
  支持导出课程的学生打卡数据为 Excel 报表
-->
<template>
  <div class="data-export">
    <div class="filter-section mb-4 flex-between">
      <div class="flex-start">
        <span class="mr-2">选择课程:</span>
        <el-select v-model="internalExportCourseId" placeholder="请选择课程" style="width: 200px">
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
      </div>
      <el-button type="primary" :disabled="!internalExportCourseId" @click="handleExport" :loading="exporting">
        <el-icon class="mr-2"><Download /></el-icon>导出Excel
      </el-button>
    </div>
    
    <div v-if="internalExportCourseId">
      <el-empty description="点击上方按钮导出班级数据" image-size="100">
        <template #image>
          <el-icon :size="60" color="#909399"><Document /></el-icon>
        </template>
      </el-empty>
    </div>
    <el-empty v-else description="请先选择一个课程" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Download, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { exportCourseData } from '@/api/export'

const props = defineProps<{
  courses: any[]
  exportCourseId: number | null
}>()

const emit = defineEmits(['update:exportCourseId'])

const internalExportCourseId = ref(props.exportCourseId)
const exporting = ref(false)

watch(() => props.exportCourseId, (newVal) => {
  internalExportCourseId.value = newVal
})

watch(internalExportCourseId, (newVal) => {
  emit('update:exportCourseId', newVal)
})

const handleExport = async () => {
  if (!internalExportCourseId.value) return
  exporting.value = true
  try {
    const res: any = await exportCourseData(internalExportCourseId.value)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `课程数据报表_${new Date().toLocaleDateString()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.mr-2 { margin-right: 0.5rem; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-start { display: flex; align-items: center; }
</style>
