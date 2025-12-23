<!--
  TeacherLeaderboard.vue
  教师端 - 排行榜模块
  用于查看课程内的学生积分总榜或本周榜单
-->
<template>
  <div class="teacher-leaderboard">
    <div class="filter-section mb-4 flex-between">
      <div class="flex-start">
        <span class="mr-2">选择课程:</span>
        <el-select v-model="internalLeaderboardCourseId" placeholder="请选择课程" @change="fetchLeaderboardData" style="width: 200px">
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>

        <el-divider direction="vertical" />

        <el-radio-group v-model="leaderboardType" @change="fetchLeaderboardData">
          <el-radio-button label="points">积分总榜</el-radio-button>
          <el-radio-button label="weekly">本周榜单</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-if="internalLeaderboardCourseId">
      <el-table :data="leaderboardData" stripe style="width: 100%" v-loading="loadingLeaderboard">
        <el-table-column type="index" label="排名" width="80">
          <template #default="scope">
            <div class="rank-icon" :class="'rank-' + (scope.$index + 1)" v-if="scope.$index < 3">
              {{ scope.$index + 1 }}
            </div>
            <span v-else>{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生" min-width="200">
          <template #default="scope">
            <div class="flex-start">
              <el-avatar :size="32" :src="scope.row.avatar" class="mr-2" />
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :prop="leaderboardType === 'points' ? 'points' : 'weeklyPoints'" label="积分" width="120" sortable />
        <el-table-column prop="checkinCount" label="打卡次数" width="120" sortable />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="$emit('open-portrait', scope.row)">
              查看画像
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-empty v-else description="请先选择一个课程查看排行榜" />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getLeaderboard } from '@/api/statistics'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  courses: any[]
  leaderboardCourseId: number | null
}>()

const emit = defineEmits(['update:leaderboardCourseId', 'open-portrait'])

const internalLeaderboardCourseId = ref(props.leaderboardCourseId)
const leaderboardType = ref('points')
const leaderboardData = ref<any[]>([])
const loadingLeaderboard = ref(false)

watch(() => props.leaderboardCourseId, (newVal) => {
  internalLeaderboardCourseId.value = newVal
  if (newVal) fetchLeaderboardData()
})

const fetchLeaderboardData = async () => {
  if (!internalLeaderboardCourseId.value) return
  emit('update:leaderboardCourseId', internalLeaderboardCourseId.value)
  loadingLeaderboard.value = true
  try {
    const res: any = await getLeaderboard(internalLeaderboardCourseId.value, leaderboardType.value)
    leaderboardData.value = res.data || []
  } catch (error) {
    ElMessage.error('获取排行榜失败')
  } finally {
    loadingLeaderboard.value = false
  }
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.mr-2 { margin-right: 0.5rem; }
.flex-start { display: flex; align-items: center; }
.rank-icon { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 12px; }
.rank-1 { background: #FFD700; }
.rank-2 { background: #C0C0C0; }
.rank-3 { background: #CD7F32; }
</style>
