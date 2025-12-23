<template>
  <div class="points-central">
    <el-alert
      v-if="stats.inflationWarning"
      :title="stats.inflationAdvice"
      type="error"
      show-icon
      class="mb-4"
    />

    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>今日积分发行</template>
          <div class="stat-value text-success">+{{ stats.todayIssuance || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>总流通积分</template>
          <div class="stat-value text-primary">{{ stats.totalCirculation || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>周环比通胀率</template>
          <div class="stat-value" :class="{'text-danger': stats.inflationWarning, 'text-success': !stats.inflationWarning}">
            {{ stats.inflationRate || '0.00' }}%
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card header="积分调控 (全局倍率)" class="mb-4">
        <div class="flex items-center">
            <span class="mr-4">全局积分倍率 (默认 1.5):</span>
            <el-input-number v-model="multiplier" :min="0.1" :max="5.0" :step="0.1" :precision="1" />
            <el-button type="primary" class="ml-4" @click="saveMultiplier">保存设置</el-button>
            <span class="text-gray-500 ml-4 text-sm">生效范围：新发布任务立即应用，历史任务保持不变</span>
        </div>
    </el-card>

    <el-card header="积分规则调控">
      <el-table :data="rules" border>
        <el-table-column prop="ruleKey" label="规则键" />
        <el-table-column prop="description" label="说明" />
        <el-table-column prop="ruleValue" label="当前值" />
        <el-table-column prop="updateTime" label="最后更新时间" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">修改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="修改规则" width="400px">
        <el-form :model="form">
            <el-form-item label="说明">
                <el-input v-model="form.description" disabled />
            </el-form-item>
            <el-form-item label="值">
                <el-input v-model="form.ruleValue" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitRule">确定</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getPointsStats, getPointsRules, savePointsRule, getPointsMultiplier, setPointsMultiplier } from '../../../api/admin'
import { ElMessage } from 'element-plus'

const stats = ref({
    todayIssuance: 0,
    totalCirculation: 0,
    inflationWarning: false,
    inflationAdvice: '',
    inflationRate: '0.00'
})
const rules = ref([])
const multiplier = ref(1.5)
const dialogVisible = ref(false)
const form = reactive({
    id: null,
    ruleKey: '',
    ruleValue: '',
    description: ''
})

const loadData = async () => {
    try {
        const s = await getPointsStats()
        stats.value = s.data
        const r = await getPointsRules()
        rules.value = r.data
        const m = await getPointsMultiplier()
        multiplier.value = m.data
    } catch (e) { console.error(e) }
}

const saveMultiplier = async () => {
    try {
        await setPointsMultiplier(multiplier.value)
        ElMessage.success('全局倍率已更新')
    } catch (e: any) {
        ElMessage.error(e.message || '更新失败')
    }
}

const handleEdit = (row: any) => {
    Object.assign(form, row)
    dialogVisible.value = true
}

const submitRule = async () => {
    try {
        await savePointsRule(form)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        loadData()
    } catch (e: any) {
        ElMessage.error(e.message || '保存失败')
    }
}

onMounted(() => {
    loadData()
})
</script>

<style scoped>
.stat-value {
    font-size: 28px;
    font-weight: bold;
}
.text-success { color: #67C23A; }
.text-primary { color: #409EFF; }
</style>
