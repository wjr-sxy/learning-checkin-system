<template>
  <div class="user-management">
    <div class="mb-4">
      <el-input v-model="searchQuery" placeholder="搜索用户名" style="width: 200px;" class="mr-2" @keyup.enter="loadUsers" />
      <el-button type="primary" @click="loadUsers">搜索</el-button>
      <el-button type="success" @click="handleExport">导出CSV</el-button>
    </div>

    <el-table :data="users" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="points" label="积分" width="100" />
      <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
              <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'">{{ scope.row.role }}</el-tag>
          </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'danger' : 'success'">
            {{ scope.row.status === 1 ? '封禁' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="scope">
          <el-button size="small" :type="scope.row.status === 1 ? 'success' : 'danger'" @click="handleStatus(scope.row)">
            {{ scope.row.status === 1 ? '解封' : '封禁' }}
          </el-button>
          <el-button size="small" type="warning" @click="handleResetPwd(scope.row)">重置密码</el-button>
          <el-button size="small" type="primary" @click="handlePoints(scope.row)">调整积分</el-button>
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

    <!-- Points Dialog -->
    <el-dialog v-model="pointsDialogVisible" title="调整积分" width="400px">
        <el-form :model="pointsForm">
            <el-form-item label="类型">
                <el-radio-group v-model="pointsForm.type">
                    <el-radio label="ADD">增加</el-radio>
                    <el-radio label="DEDUCT">扣除</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="数量">
                <el-input-number v-model="pointsForm.amount" :min="1" />
            </el-form-item>
            <el-form-item label="原因">
                <el-input v-model="pointsForm.reason" placeholder="调整原因" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="pointsDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitPoints">确定</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getUsers, updateUserStatus, resetUserPassword, adjustUserPoints, exportUsers } from '../../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchQuery = ref('')

const pointsDialogVisible = ref(false)
const pointsForm = reactive({
    userId: null,
    type: 'ADD',
    amount: 0,
    reason: ''
})

const loadUsers = async () => {
    loading.value = true
    try {
        const res: any = await getUsers({
            page: currentPage.value,
            size: pageSize.value,
            username: searchQuery.value
        })
        users.value = res.data.records
        total.value = res.data.total
    } catch (error) {
        console.error(error)
    } finally {
        loading.value = false
    }
}

const handlePageChange = (page: number) => {
    currentPage.value = page
    loadUsers()
}

const handleExport = async () => {
    try {
        const res: any = await exportUsers(searchQuery.value)
        const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `users_${new Date().getTime()}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
    } catch (error) {
        ElMessage.error('导出失败')
    }
}

const handleStatus = async (row: any) => {
    const newStatus = row.status === 1 ? 0 : 1
    const action = newStatus === 1 ? '封禁' : '解封'
    try {
        await ElMessageBox.confirm(`确定要${action}用户 ${row.username} 吗？`, '提示', {
            type: 'warning'
        })
        await updateUserStatus(row.id, newStatus)
        ElMessage.success('操作成功')
        loadUsers()
    } catch (e) {}
}

const handleResetPwd = async (row: any) => {
    try {
        const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
            inputPattern: /.{6,}/,
            inputErrorMessage: '密码至少6位'
        })
        await resetUserPassword(row.id, value)
        ElMessage.success('密码重置成功')
    } catch (e) {}
}

const handlePoints = (row: any) => {
    pointsForm.userId = row.id
    pointsForm.amount = 0
    pointsForm.reason = ''
    pointsForm.type = 'ADD'
    pointsDialogVisible.value = true
}

const submitPoints = async () => {
    if (!pointsForm.amount || !pointsForm.reason) {
        ElMessage.warning('请填写完整')
        return
    }
    try {
        await adjustUserPoints(pointsForm.userId!, pointsForm)
        ElMessage.success('调整成功')
        pointsDialogVisible.value = false
        loadUsers()
    } catch (error: any) {
        ElMessage.error(error.message || '操作失败')
    }
}

onMounted(() => {
    loadUsers()
})
</script>
