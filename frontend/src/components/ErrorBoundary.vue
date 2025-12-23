<template>
  <div v-if="error" class="error-boundary">
    <el-result
        icon="error"
        title="出错了"
        :sub-title="error.message || '系统发生未知错误，请稍后重试'"
    >
        <template #extra>
            <el-button type="primary" @click="reloadPage">刷新页面</el-button>
        </template>
    </el-result>
  </div>
  <slot v-else></slot>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured } from 'vue'

const error = ref<Error | null>(null)

onErrorCaptured((err) => {
  console.error('Error captured:', err)
  error.value = err as Error
  return false // Stop propagation
})

const reloadPage = () => {
    window.location.reload()
}
</script>

<style scoped>
.error-boundary {
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f5f7fa;
}
</style>
