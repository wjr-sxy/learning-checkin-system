<template>
  <div class="system-tools">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="公告管理" name="announcements">
        <el-card shadow="never">
          <div class="mb-4">
            <el-button type="primary" @click="handleAdd">发布公告</el-button>
          </div>
          <el-table :data="announcements" border>
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column prop="createTime" label="发布时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
                <el-popconfirm title="确定删除?" @confirm="handleDelete(scope.row.id)">
                  <template #reference>
                    <el-button size="small" type="danger">删除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="mt-4"
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="pageSize"
            @current-change="handlePageChange"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="黑名单管理" name="blacklist">
        <el-card shadow="never">
          <div class="mb-4">
            <el-select v-model="blacklistType" placeholder="类型" class="mr-2" style="width: 150px;" @change="loadBlacklist">
              <el-option label="全部" value="" />
              <el-option label="JWT Token" value="JWT" />
              <el-option label="IP地址" value="IP" />
              <el-option label="敏感词" value="SENSITIVE_WORD" />
            </el-select>
            <el-button type="primary" @click="openBlacklistDialog">添加黑名单</el-button>
          </div>
          <el-table :data="blacklist" border>
            <el-table-column prop="type" label="类型" width="120" />
            <el-table-column prop="value" label="值" />
            <el-table-column prop="reason" label="原因" />
            <el-table-column prop="createTime" label="添加时间" width="180" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-popconfirm title="确定移除?" @confirm="removeBlacklist(scope.row.id)">
                  <template #reference>
                    <el-button size="small" type="danger">移除</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="mt-4"
            background
            layout="prev, pager, next"
            :total="blacklistTotal"
            :page-size="blacklistPageSize"
            @current-change="handleBlacklistPageChange"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- Announcement Dialog -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'">
      <el-form :model="form">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="form.content" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Blacklist Dialog -->
    <el-dialog v-model="blacklistDialogVisible" title="添加黑名单">
      <el-form :model="blacklistForm">
        <el-form-item label="类型">
          <el-select v-model="blacklistForm.type">
            <el-option label="JWT Token" value="JWT" />
            <el-option label="IP地址" value="IP" />
            <el-option label="敏感词" value="SENSITIVE_WORD" />
          </el-select>
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model="blacklistForm.value" placeholder="Token / IP / 敏感词" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="blacklistForm.reason" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blacklistDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBlacklist">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getAnnouncements, saveAnnouncement, deleteAnnouncement, getBlacklist, addBlacklist, deleteBlacklist } from '../../../api/admin'
import { ElMessage } from 'element-plus'

const activeTab = ref('announcements')

// Announcements
const announcements = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const form = reactive({
    id: null,
    title: '',
    content: '',
    status: 1
})

const loadData = async () => {
    try {
        const res: any = await getAnnouncements({ page: currentPage.value, size: pageSize.value })
        announcements.value = res.data.records
        total.value = res.data.total
    } catch (e) {}
}

const handlePageChange = (page: number) => {
    currentPage.value = page
    loadData()
}

const handleAdd = () => {
    form.id = null
    form.title = ''
    form.content = ''
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    Object.assign(form, row)
    dialogVisible.value = true
}

const handleDelete = async (id: number) => {
    try {
        await deleteAnnouncement(id)
        ElMessage.success('删除成功')
        loadData()
    } catch (e) {}
}

const submit = async () => {
    try {
        await saveAnnouncement(form)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
    } catch (e: any) {
        ElMessage.error(e.message)
    }
}

// Blacklist
const blacklist = ref([])
const blacklistTotal = ref(0)
const blacklistPage = ref(1)
const blacklistPageSize = ref(10)
const blacklistType = ref('')
const blacklistDialogVisible = ref(false)
const blacklistForm = reactive({
  type: 'IP',
  value: '',
  reason: ''
})

const loadBlacklist = async () => {
  try {
    const res: any = await getBlacklist({
      page: blacklistPage.value,
      size: blacklistPageSize.value,
      type: blacklistType.value
    })
    blacklist.value = res.data.records
    blacklistTotal.value = res.data.total
  } catch (e) {}
}

const handleBlacklistPageChange = (page: number) => {
  blacklistPage.value = page
  loadBlacklist()
}

const openBlacklistDialog = () => {
  blacklistForm.value = ''
  blacklistForm.reason = ''
  blacklistDialogVisible.value = true
}

const submitBlacklist = async () => {
  try {
    await addBlacklist(blacklistForm)
    ElMessage.success('添加成功')
    blacklistDialogVisible.value = false
    loadBlacklist()
  } catch (e: any) {
    ElMessage.error(e.message)
  }
}

const removeBlacklist = async (id: number) => {
  try {
    await deleteBlacklist(id)
    ElMessage.success('移除成功')
    loadBlacklist()
  } catch (e) {}
}

onMounted(() => {
    loadData()
    loadBlacklist()
})
</script>
