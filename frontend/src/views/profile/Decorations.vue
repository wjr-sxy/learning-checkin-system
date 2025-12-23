<template>
  <div class="decorations-view">
    <div class="header-row">
        <h2 class="section-title">我的装扮</h2>
        <el-select v-model="sortPref" placeholder="排序" size="small" style="width: 120px" @change="handleSortChange">
            <el-option label="时间升序" value="time_asc" />
            <el-option label="时间降序" value="time_desc" />
            <el-option label="价格升序" value="price_asc" />
            <el-option label="价格降序" value="price_desc" />
        </el-select>
    </div>
    
    <div class="custom-tabs">
        <div 
            v-for="tab in tabs" 
            :key="tab.name"
            class="custom-tab-item" 
            :class="{ active: activeTab === tab.name }"
            @click="activeTab = tab.name"
        >
            {{ tab.label }}
        </div>
    </div>
    
    <div class="decoration-content" v-loading="loading">
        <!-- Permanent Items -->
        <div v-if="permanentItems.length > 0" class="group-section">
            <h3 class="group-title">永久物品</h3>
            <div class="decoration-grid">
                <div 
                    v-for="item in permanentItems" 
                    :key="item.id" 
                    class="decoration-item"
                    :class="{ active: isEquipped(activeTab, item) }"
                    @click="handleItemClick(item)"
                >
                    <div class="img-wrapper">
                        <img :src="item.imageUrl" alt="item" />
                        <div class="permanent-badge">永久</div>
                    </div>
                    <div class="item-name">{{ item.name }}</div>
                    <div class="item-status" v-if="isEquipped(activeTab, item)">当前使用</div>
                    <div class="item-actions">
                        <el-button 
                            v-if="!isEquipped(activeTab, item)" 
                            type="primary" 
                            size="small" 
                            @click.stop="handleEquip(activeTab, item)"
                        >
                            装备
                        </el-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Limited Time Items -->
        <div v-if="limitedItems.length > 0" class="group-section">
            <h3 class="group-title">限时物品</h3>
            <div class="decoration-grid">
                <div 
                    v-for="item in limitedItems" 
                    :key="item.id" 
                    class="decoration-item"
                    :class="{ active: isEquipped(activeTab, item), expired: isExpired(item) }"
                    @click="handleItemClick(item)"
                >
                    <div class="img-wrapper">
                        <img :src="item.imageUrl" alt="item" />
                        <div class="countdown-badge" :class="{ 'urgent': isUrgent(item.expireTime) }" v-if="!isExpired(item)">
                            {{ formatCountdown(item.expireTime) }}
                        </div>
                        <div class="expired-mask" v-else>
                            <span>已过期</span>
                        </div>
                    </div>
                    <div class="item-name">{{ item.name }}</div>
                    <div class="item-status" v-if="isEquipped(activeTab, item)">当前使用</div>
                    
                    <div class="item-actions">
                        <el-button 
                            v-if="!isExpired(item) && !isEquipped(activeTab, item)" 
                            type="primary" 
                            size="small" 
                            @click.stop="handleEquip(activeTab, item)"
                        >
                            装备
                        </el-button>
                        <el-button 
                            v-if="isExpired(item) || isUrgent(item.expireTime)" 
                            type="warning" 
                            size="small" 
                            @click.stop="handleRenew(item)"
                        >
                            续费 <span class="saved-amount" v-if="isExpired(item) || isUrgent(item.expireTime)">(省{{ (item.price * 0.2).toFixed(0) }})</span>
                        </el-button>
                    </div>
                </div>
            </div>
        </div>

        <el-empty v-if="permanentItems.length === 0 && limitedItems.length === 0" description="暂无此类装扮" />
    </div>
    
    <div class="shop-link">
        <el-button type="primary" plain @click="openShop">前往商城获取更多装扮</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getOwnedProducts, exchangeProduct } from '../../api/shop'
import { equipDecoration } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import duration from 'dayjs/plugin/duration'

dayjs.extend(duration)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('AVATAR_FRAME')
const ownedItems = ref<any[]>([])
const loading = ref(false)
const sortPref = ref(localStorage.getItem('user_skin_sort_pref') || 'time_desc')

const tabs = [
    { label: '头像框', name: 'AVATAR_FRAME' },
    { label: '皮肤', name: 'SKIN' },
    { label: '背景', name: 'BACKGROUND' },
    { label: '勋章', name: 'BADGE' }
]

const fetchOwnedItems = async () => {
  if (!userStore.user) return
  loading.value = true
  try {
    const res: any = await getOwnedProducts(userStore.user.id)
    ownedItems.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// Filtering and Sorting
const currentTabItems = computed(() => {
    let items = ownedItems.value.filter(item => {
        // Mapping logic
        if (activeTab.value === 'AVATAR_FRAME') return item.type === 'AVATAR_FRAME'
        if (activeTab.value === 'SKIN') return item.type === 'SKIN'
        if (activeTab.value === 'BACKGROUND') return item.type === 'STATIC_BG' || item.type === 'DYNAMIC_BG'
        if (activeTab.value === 'BADGE') return item.type === 'BADGE' || item.type === 'TITLE'
        return false
    })
    
    // Sort
    items.sort((a, b) => {
        const timeA = new Date(a.createTime || 0).getTime()
        const timeB = new Date(b.createTime || 0).getTime()
        const priceA = a.price || 0
        const priceB = b.price || 0
        
        switch (sortPref.value) {
            case 'time_asc': return timeA - timeB
            case 'time_desc': return timeB - timeA
            case 'price_asc': return priceA - priceB
            case 'price_desc': return priceB - priceA
            default: return 0
        }
    })
    
    return items
})

const permanentItems = computed(() => {
    return currentTabItems.value.filter(item => !item.expireTime)
})

const limitedItems = computed(() => {
    const items = currentTabItems.value.filter(item => item.expireTime)
    
    return items.sort((a, b) => {
        const timeA = new Date(a.expireTime).getTime()
        const timeB = new Date(b.expireTime).getTime()
        const now = Date.now()
        const urgentA = timeA - now > 0 && timeA - now < 24 * 60 * 60 * 1000
        const urgentB = timeB - now > 0 && timeB - now < 24 * 60 * 60 * 1000
        
        if (urgentA && !urgentB) return -1
        if (!urgentA && urgentB) return 1
        
        return timeA - timeB // Default ASC
    })
})

const handleSortChange = (val: string) => {
    localStorage.setItem('user_skin_sort_pref', val)
}

const isExpired = (item: any) => {
    if (!item.expireTime) return false
    return dayjs(item.expireTime).isBefore(dayjs())
}

const isUrgent = (expireTime: string) => {
    if (!expireTime) return false
    const diff = dayjs(expireTime).diff(dayjs())
    return diff > 0 && diff < 24 * 60 * 60 * 1000
}

const formatCountdown = (expireTime: string) => {
  const end = dayjs(expireTime)
  const now = dayjs()
  const diff = end.diff(now)
  if (diff <= 0) return '已过期'
  const d = dayjs.duration(diff)
  const days = Math.floor(d.asDays())
  const hours = d.hours()
  const minutes = d.minutes()
  if (days > 0) return `${days}天${hours}小时`
  if (days === 0 && hours > 0) return `${hours}小时${minutes}分`
  return `剩余 ${minutes} 分钟`
}

const isEquipped = (tab: string, item: any) => {
    if (!userStore.user) return false
    if (tab === 'AVATAR_FRAME') {
        return userStore.user.currentAvatarFrame === item.imageUrl
    }
    if (tab === 'SKIN') {
        return userStore.user.currentSkin === item.imageUrl
    }
    if (tab === 'BACKGROUND') {
        const bg = userStore.user.profileBackground
        return bg === item.videoUrl || bg === item.imageUrl
    }
    return false
}

const handleItemClick = (_item: any) => {
    // Optional: Preview logic
}

const handleEquip = async (tab: string, item: any) => {
    if (isExpired(item)) {
        ElMessage.warning('该物品已过期，请续费')
        return
    }
    
    let type = ''
    if (tab === 'AVATAR_FRAME') type = 'avatar'
    else if (tab === 'SKIN') type = 'skin'
    else if (tab === 'BACKGROUND') type = 'background'
    else return

    try {
        await equipDecoration({
            userId: userStore.user!.id,
            productId: item.id,
            type: type
        })
        ElMessage.success('装备成功')
        // Update local user store
        if (type === 'avatar') userStore.user.currentAvatarFrame = item.imageUrl
        if (type === 'skin') userStore.user.currentSkin = item.imageUrl
        if (type === 'background') userStore.user.profileBackground = item.videoUrl || item.imageUrl
    } catch (error: any) {
        ElMessage.error(error.message || '装备失败')
    }
}

const handleRenew = async (item: any) => {
    try {
        await ElMessageBox.confirm(
            `续费需要 ${Math.floor(item.price * 0.8)} 积分（原价 ${item.price}，省 20%），确认续费吗？`,
            '续费确认',
            {
                confirmButtonText: '确认',
                cancelButtonText: '取消',
                type: 'warning'
            }
        )
        
        await exchangeProduct(userStore.user!.id, item.id)
        ElMessage.success('续费成功')
        fetchOwnedItems() // Refresh list
    } catch (error: any) {
        if (error !== 'cancel') {
            ElMessage.error(error.message || '续费失败')
        }
    }
}

const openShop = () => {
    router.push('/shop')
}

watch(() => userStore.user, fetchOwnedItems, { immediate: true })
onMounted(fetchOwnedItems)
</script>

<style scoped>
.decorations-view {
  padding: 20px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.custom-tabs {
  display: flex;
  justify-content: space-evenly;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.custom-tab-item {
  padding: 8px 20px;
  cursor: pointer;
  border-radius: 4px;
  font-weight: 500;
  color: #666;
  transition: all 0.3s;
}

.custom-tab-item.active {
  background: #fff;
  color: #409EFF;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.group-section {
  margin-bottom: 30px;
}

.group-title {
  font-size: 16px;
  color: #666;
  margin-bottom: 15px;
  padding-left: 10px;
  border-left: 4px solid #409EFF;
}

.decoration-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 15px;
}

.decoration-item {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.decoration-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.decoration-item.active {
  border-color: #409EFF;
  background-color: #ecf5ff;
}

.decoration-item.expired {
  opacity: 0.7;
}

.img-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* 1:1 Aspect Ratio */
  margin-bottom: 10px;
  background: #f8f8f8;
  border-radius: 4px;
  overflow: hidden;
}

.img-wrapper img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.permanent-badge, .countdown-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  color: #fff;
}

.permanent-badge {
  background-color: #67C23A;
}

.countdown-badge {
  background-color: #E6A23C;
}

.countdown-badge.urgent {
  background-color: #F56C6C;
  animation: blink 2s infinite;
}

.expired-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  font-size: 14px;
}

.item-name {
  font-size: 14px;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-status {
  font-size: 12px;
  color: #409EFF;
  margin-bottom: 5px;
}

.item-actions {
  margin-top: 5px;
  display: flex;
  justify-content: center;
}

.saved-amount {
  font-size: 10px;
  margin-left: 2px;
}

.shop-link {
  margin-top: 30px;
  text-align: center;
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.6; }
  100% { opacity: 1; }
}
</style>
