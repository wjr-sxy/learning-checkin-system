<template>
  <div class="file-preview-container">
    <vue-office-docx
      v-if="fileType === 'docx'"
      :src="url"
      class="preview-component"
      @rendered="rendered"
      @error="onError"
    />
    <vue-office-excel
      v-else-if="fileType === 'xlsx'"
      :src="url"
      class="preview-component"
      @rendered="rendered"
      @error="onError"
    />
    <vue-office-pdf
      v-else-if="fileType === 'pdf'"
      :src="url"
      class="preview-component"
      @rendered="rendered"
      @error="onError"
    />
    <div v-else class="unsupported">
        <el-empty description="不支持的预览格式">
            <el-button type="primary" tag="a" :href="url" target="_blank">下载查看</el-button>
        </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
// @ts-ignore
import VueOfficeDocx from '@vue-office/docx'
// @ts-ignore
import VueOfficeExcel from '@vue-office/excel'
// @ts-ignore
import VueOfficePdf from '@vue-office/pdf'
import '@vue-office/docx/lib/index.css'
import '@vue-office/excel/lib/index.css'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  url: string
}>()

const fileType = computed(() => {
  if (!props.url) return ''
  const lowerUrl = props.url.toLowerCase()
  if (lowerUrl.endsWith('.docx')) return 'docx'
  if (lowerUrl.endsWith('.xlsx')) return 'xlsx'
  if (lowerUrl.endsWith('.pdf')) return 'pdf'
  return 'other'
})

const rendered = () => {
  // console.log('Rendered')
}

const onError = (e: any) => {
  console.error('Preview error', e)
  ElMessage.error('预览失败，请尝试下载')
}
</script>

<style scoped>
.file-preview-container {
    height: 60vh;
    overflow: auto;
    background: #fff;
    border: 1px solid #eee;
}
.preview-component {
    width: 100%;
    height: 100%;
}
.unsupported {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
}
</style>
