<template>
  <div class="content-security">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold">内容安全</h2>
      <div class="flex gap-2">
        <el-button type="primary" @click="openAddDialog">添加敏感词</el-button>
        <el-upload
          class="upload-demo"
          action="#"
          :show-file-list="false"
          :http-request="handleImport"
          accept=".txt"
        >
          <el-button type="success">导入词库</el-button>
        </el-upload>
        <el-button type="warning" @click="handleExport">导出词库</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="demo-tabs">
      <el-tab-pane label="敏感词库" name="words">
        <div class="mb-4">
          <el-input
            v-model="searchQuery"
            placeholder="搜索敏感词"
            style="width: 300px"
            class="mr-2"
            clearable
            @clear="loadWords"
            @keyup.enter="loadWords"
          >
            <template #append>
              <el-button @click="loadWords">搜索</el-button>
            </template>
          </el-input>
        </div>

        <el-table :data="wordsList" v-loading="loading" border stripe>
          <el-table-column prop="value" label="敏感词" />
          <el-table-column prop="reason" label="来源/备注" />
          <el-table-column prop="createTime" label="添加时间">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-popconfirm title="确定删除该敏感词吗?" @confirm="handleDelete(scope.row.id)">
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="totalWords"
            v-model:current-page="wordPage"
            :page-size="wordSize"
            @current-change="loadWords"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="拦截日志" name="logs">
        <el-table :data="logsList" v-loading="logsLoading" border stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column prop="sourceType" label="来源" width="150">
            <template #default="scope">
              <el-tag>{{ scope.row.sourceType }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="detectedWords" label="命中敏感词">
            <template #default="scope">
              <el-tag type="danger" v-for="word in scope.row.detectedWords.split(',')" :key="word" class="mr-1">
                {{ word }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="contentSnippet" label="内容片段" show-overflow-tooltip />
          <el-table-column prop="createTime" label="拦截时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-4 flex justify-end">
          <el-pagination
            background
            layout="total, prev, pager, next"
            :total="totalLogs"
            v-model:current-page="logPage"
            :page-size="logSize"
            @current-change="loadLogs"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Add Dialog -->
    <el-dialog v-model="showAddDialog" title="添加敏感词" width="500px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="敏感词">
          <el-input v-model="addForm.value" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="addForm.reason" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="submitAdd">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getBlacklist, 
  addBlacklist, 
  deleteBlacklist, 
  importSensitiveWords, 
  exportSensitiveWords,
  getSensitiveLogs
} from '../../api/admin'

const activeTab = ref('words')

// Words Management
const wordsList = ref([])
const loading = ref(false)
const wordPage = ref(1)
const wordSize = ref(20)
const totalWords = ref(0)
const searchQuery = ref('')

const loadWords = async () => {
  loading.value = true
  try {
    const params = {
      page: wordPage.value,
      size: wordSize.value,
      type: 'SENSITIVE_WORD',
      keyword: searchQuery.value
    }
    const res: any = await getBlacklist(params)
    wordsList.value = res.data.records
    totalWords.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// Add
const showAddDialog = ref(false)
const addForm = reactive({
  value: '',
  reason: '',
  type: 'SENSITIVE_WORD'
})

const openAddDialog = () => {
  addForm.value = ''
  addForm.reason = ''
  showAddDialog.value = true
}

const submitAdd = async () => {
  if (!addForm.value) return
  try {
    await addBlacklist(addForm)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    loadWords()
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await deleteBlacklist(id)
    ElMessage.success('删除成功')
    loadWords()
  } catch (error) {
    console.error(error)
  }
}

// Import/Export
const handleImport = async (options: any) => {
  try {
    const res: any = await importSensitiveWords(options.file)
    ElMessage.success(res.data.message || '导入成功')
    loadWords()
  } catch (error) {
    ElMessage.error('导入失败')
    console.error(error)
  }
}

const handleExport = async () => {
  try {
    const res: any = await exportSensitiveWords()
    const blob = new Blob([res], { type: 'text/plain;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `sensitive_words_${new Date().getTime()}.txt`
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  }
}

// Logs
const logsList = ref([])
const logsLoading = ref(false)
const logPage = ref(1)
const logSize = ref(20)
const totalLogs = ref(0)

const loadLogs = async () => {
  logsLoading.value = true
  try {
    const res: any = await getSensitiveLogs({
      page: logPage.value,
      size: logSize.value
    })
    logsList.value = res.data.records
    totalLogs.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    logsLoading.value = false
  }
}

// Utils
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  loadWords()
  loadLogs()
})
</script>

<style scoped>
.content-security {
  padding: 20px;
}
</style>
