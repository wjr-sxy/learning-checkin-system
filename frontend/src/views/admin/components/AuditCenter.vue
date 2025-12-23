<template>
  <div class="audit-center">
    <el-tabs type="border-card">
        <el-tab-pane label="操作日志">
            <div style="text-align: right; margin-bottom: 10px;">
                <el-button type="success" size="small" @click="handleExportOp">导出CSV</el-button>
            </div>
            <el-table :data="opLogs" border>
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="userId" label="用户ID" width="100" />
                <el-table-column prop="module" label="模块" width="100" />
                <el-table-column prop="action" label="动作" width="100" />
                <el-table-column prop="description" label="描述" show-overflow-tooltip />
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="scope">
                        <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                            {{ scope.row.status === 0 ? '成功' : '失败' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间" width="180" />
            </el-table>
            <el-pagination class="mt-4" background layout="prev, pager, next" :total="opTotal" @current-change="loadOpLogs" />
        </el-tab-pane>
        <el-tab-pane label="登录日志">
             <div style="text-align: right; margin-bottom: 10px;">
                <el-button type="success" size="small" @click="handleExportLogin">导出CSV</el-button>
            </div>
             <el-table :data="loginLogs" border>
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="username" label="用户名" width="150" />
                <el-table-column prop="ip" label="IP" width="150" />
                <el-table-column prop="message" label="信息" />
                <el-table-column prop="status" label="状态" width="80">
                    <template #default="scope">
                        <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
                            {{ scope.row.status === 0 ? '成功' : '失败' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="时间" width="180" />
            </el-table>
            <el-pagination class="mt-4" background layout="prev, pager, next" :total="loginTotal" @current-change="loadLoginLogs" />
        </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getOperationLogs, getLoginLogs, exportOperationLogs, exportLoginLogs } from '../../../api/admin'
import { ElMessage } from 'element-plus'

const opLogs = ref([])
const opTotal = ref(0)
const loginLogs = ref([])
const loginTotal = ref(0)

const loadOpLogs = async (page = 1) => {
    try {
        const res: any = await getOperationLogs({ page, size: 10 })
        opLogs.value = res.data.records
        opTotal.value = res.data.total
    } catch (e) {}
}

const loadLoginLogs = async (page = 1) => {
    try {
        const res: any = await getLoginLogs({ page, size: 10 })
        loginLogs.value = res.data.records
        loginTotal.value = res.data.total
    } catch (e) {}
}

const handleExportOp = async () => {
    try {
        const res: any = await exportOperationLogs()
        const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `operation_logs_${new Date().getTime()}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
    } catch (error) {
        ElMessage.error('导出失败')
    }
}

const handleExportLogin = async () => {
    try {
        const res: any = await exportLoginLogs()
        const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `login_logs_${new Date().getTime()}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
    } catch (error) {
        ElMessage.error('导出失败')
    }
}

onMounted(() => {
    loadOpLogs()
    loadLoginLogs()
})
</script>
