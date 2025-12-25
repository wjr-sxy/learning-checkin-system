<template>
  <div class="shop-container">
    <el-header class="main-header">
      <div class="header-content container">
        <div class="logo-section">
            <h1 @click="router.push('/')" style="cursor: pointer;">学习打卡系统</h1>
        </div>
        <div class="header-actions">
            <div class="user-info" v-if="userStore.user">
                <span class="points-badge">
                    <el-icon><Coin /></el-icon>
                    {{ formatPoints(userStore.user.points) }}
                </span>
            </div>
            <el-button @click="router.push('/')">返回仪表盘</el-button>
        </div>
      </div>
    </el-header>

    <el-main class="container main-content">
      <div class="page-header mb-4 flex-between">
          <h2>积分商城</h2>
          <div class="header-controls">
              <el-input
                  v-model="searchQuery"
                  placeholder="搜索商品..."
                  :prefix-icon="Search"
                  class="search-input mr-2"
                  clearable
              />
              <el-button type="info" @click="showHistory">
                  <el-icon class="el-icon--left"><List /></el-icon>兑换记录
              </el-button>
          </div>
      </div>

      <!-- Custom Scrollable Tabs -->
      <ScrollableTabs 
        v-model="activeTab" 
        :items="tabItems" 
        class="mb-4"
      />

      <!-- Product Grid with Swipe Detection -->
      <div 
        class="product-grid-container" 
        @touchstart="handleTouchStart"
        @touchend="handleTouchEnd"
      >
        <div class="product-grid" v-if="filteredProducts.length > 0">
            <div 
                v-for="product in filteredProducts" 
                :key="product.id" 
                class="product-card"
                @click="openPreview(product)"
            >
                <div class="card-image">
                    <LazyImage :src="product.imageUrl" :alt="product.name" />
                    
                    <!-- Rarity Tag for Avatar Frames -->
                    <div class="rarity-tag" v-if="product.type === 'AVATAR_FRAME'" :class="getRarityClass(product.price)">
                        {{ getRarityText(product.price) }}
                    </div>

                    <div class="product-type-tag" v-if="product.stock <= 0">
                        <div class="sold-out-mask">
                            <el-icon><SoldOut /></el-icon>
                        </div>
                    </div>
                </div>
                <div class="card-info">
                    <div class="product-title">{{ product.name }}</div>
                    <div class="flex justify-between items-center mt-1">
                        <div class="product-price">{{ product.price }} 积分</div>
                        <div class="text-xs text-gray-500">剩余: {{ product.stock }}</div>
                    </div>
                </div>
                <!-- Stock Badge or Tag? Requirement says "Sold out UI: semi-transparent mask + SVG icon" -->
            </div>
        </div>
        
        <el-empty v-else description="暂无相关商品" />
      </div>

      <!-- History Dialog -->
      <el-dialog v-model="historyVisible" title="兑换记录" width="700px">
          <el-table :data="exchangeHistory" style="width: 100%" stripe empty-text="暂无兑换记录">
              <el-table-column label="商品名称">
                  <template #default="scope">
                      {{ getProductName(scope.row.productId) }}
                  </template>
              </el-table-column>
              <el-table-column prop="price" label="消耗积分" width="100">
                  <template #default="scope">
                      <span class="text-danger">-{{ scope.row.price }}</span>
                  </template>
              </el-table-column>
              <el-table-column prop="createTime" label="兑换时间" width="160">
                   <template #default="scope">
                       {{ formatTime(scope.row.createTime) }}
                   </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'info' : 'success'">
                        {{ scope.row.status === 1 ? '已退款' : '成功' }}
                    </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="scope">
                    <el-button 
                        v-if="scope.row.status !== 1" 
                        type="danger" 
                        size="small" 
                        link 
                        @click="handleRefund(scope.row)"
                    >
                        退款
                    </el-button>
                </template>
            </el-table-column>
          </el-table>
      </el-dialog>
    </el-main>

    <!-- Product Preview Modal -->
    <ProductPreview 
        v-model:visible="previewVisible"
        :product="currentPreviewProduct"
        :user-points="userStore.user?.points || 0"
        @purchase="handleExchange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getProducts, exchangeProduct, getExchangeHistory, refundProduct } from '../api/shop'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Search, List, CircleClose as SoldOut } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import ScrollableTabs from '../components/ScrollableTabs.vue'
import LazyImage from '../components/LazyImage.vue'
import ProductPreview from '../components/ProductPreview.vue'

const router = useRouter()
const userStore = useUserStore()
const products = ref<any[]>([])
const searchQuery = ref('')
const activeTab = ref('AVATAR_FRAME')
const historyVisible = ref(false)
const exchangeHistory = ref<any[]>([])
// const gridContainer = ref<HTMLElement | null>(null)

// Preview State
const previewVisible = ref(false)
const currentPreviewProduct = ref<any>(null)

// Tabs Configuration
const tabItems = [
    { label: '头像框', value: 'AVATAR_FRAME' },
    { label: '静态背景', value: 'STATIC_BG' }, // Maps to SKIN
    { label: '动态背景', value: 'DYNAMIC_BG' },
    { label: '称号', value: 'TITLE' },
    { label: '实物周边', value: 'PHYSICAL' },
    { label: '优惠券', value: 'COUPON' }
]

// Map existing types to new requirements for demo purposes
// In real app, backend should support these. 
// I will map 'SKIN' to 'STATIC_BG' and 'DYNAMIC_BG' randomly or by some logic if needed.
// For now, I'll stick to the type strings.

const loadProducts = async () => {
    try {
        const res: any = await getProducts()
        products.value = res.data
    } catch (error) {
        console.error(error)
    }
}

const filteredProducts = computed(() => {
    let result = products.value
    // Filtering logic
    // For demo, I might need to map the types if the backend data doesn't match new tabs
    // Let's assume strict matching first.
    if (activeTab.value !== 'ALL') {
         // Special handling for mapping old data to new tabs if needed
         if (activeTab.value === 'STATIC_BG') {
             result = result.filter(p => p.type === 'SKIN' || p.type === 'STATIC_BG')
         } else {
             result = result.filter(p => p.type === activeTab.value)
         }
    }
    if (searchQuery.value) {
        result = result.filter(p => p.name.toLowerCase().includes(searchQuery.value.toLowerCase()))
    }
    return result
})

// Swipe Logic
const touchStart = ref({ x: 0, t: 0 })

const handleTouchStart = (e: TouchEvent) => {
    touchStart.value = { x: e.touches[0]!.clientX, t: Date.now() }
}

const handleTouchEnd = (e: TouchEvent) => {
    const dx = e.changedTouches[0]!.clientX - touchStart.value.x
    const dt = Date.now() - touchStart.value.t
    const width = window.innerWidth
    
    // Threshold 30% of screen width (approx)
    // Or Velocity check (dx/dt)
    const velocity = Math.abs(dx / dt)
    
    // Condition: >30% width OR (>50px AND velocity > 0.5)
    if (Math.abs(dx) > width * 0.3 || (Math.abs(dx) > 50 && velocity > 0.5)) {
        if (dx > 0) {
            // Swipe Right -> Prev Tab
            switchTab(-1)
        } else {
            // Swipe Left -> Next Tab
            switchTab(1)
        }
    }
}

const switchTab = (direction: number) => {
    const currentIndex = tabItems.findIndex(item => item.value === activeTab.value)
    if (currentIndex === -1) return
    
    let newIndex = currentIndex + direction
    if (newIndex < 0) newIndex = 0
    if (newIndex >= tabItems.length) newIndex = tabItems.length - 1
    
    if (tabItems[newIndex]) {
        activeTab.value = tabItems[newIndex]!.value
    }
}

// Helpers
const formatPoints = (points: number) => {
    // "1,000.00"
    return points ? points.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",") : '0.00'
}

const formatTime = (time: string) => {
    return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const getProductName = (id: number) => {
    const p = products.value.find(x => x.id === id)
    return p ? p.name : '未知商品'
}

const showHistory = async () => {
    if (!userStore.user) return
    try {
        const res: any = await getExchangeHistory(userStore.user.id)
        exchangeHistory.value = res.data
        historyVisible.value = true
    } catch (error) {
        console.error(error)
    }
}

const handleRefund = async (record: any) => {
    try {
        await ElMessageBox.confirm('确定要申请退款吗？', '提示', { type: 'warning' })
        await refundProduct(record.id)
        ElMessage.success('退款成功')
        showHistory()
        loadProducts() // Update stock/points
        // Update user points
        await userStore.fetchUser()
    } catch (error) {
        if (error !== 'cancel') console.error(error)
    }
}

const openPreview = (product: any) => {
    currentPreviewProduct.value = product
    previewVisible.value = true
}

const getRarityClass = (price: number) => {
    if (price >= 4000) return 'rarity-legendary'
    if (price >= 1000) return 'rarity-rare'
    return 'rarity-common'
}

const getRarityText = (price: number) => {
    if (price >= 4000) return '传说'
    if (price >= 1000) return '稀有'
    return '普通'
}

const handleExchange = async (product: any, shippingInfo?: any) => {
    if (!userStore.user) {
        ElMessage.warning('请先登录')
        return
    }
    if (userStore.user.points < product.price) {
        ElMessage.error('积分不足')
        return
    }
    
    try {
        // Pass shippingInfo if available
        const payload = shippingInfo ? { ...shippingInfo } : undefined
        await exchangeProduct(userStore.user.id, product.id, payload)
        
        ElMessage.success('兑换成功')
        await loadProducts()
        // Refresh user points
        await userStore.fetchUser() 
        showHistory()
    } catch (error: any) {
        console.error(error)
        ElMessage.error(error.message || '兑换失败')
    }
}

onMounted(() => {
    loadProducts()
})
</script>

<style scoped>
.shop-container {
    min-height: 100vh;
    background-color: #fff;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr); /* Base 3 columns */
    gap: 8px;
    padding-bottom: 20px;
}

@media (max-width: 768px) {
    .product-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 480px) {
    .product-grid {
        grid-template-columns: 1fr;
    }
}

.product-card {
    height: 180px; /* Fixed height */
    border: 2px solid #eee; /* "Fixed height 180px (including 2px border)" */
    border-radius: 8px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    cursor: pointer;
    background: #fff;
    transition: transform 0.2s;
    box-sizing: border-box;
}

.product-card:active {
    transform: scale(0.98);
}

.card-image {
    width: 100%;
    aspect-ratio: 4/3; /* Thumbnail 4:3 */
    position: relative;
    background: #f0f0f0;
}

.sold-out-mask {
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(255, 255, 255, 0.7);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    color: #999;
}

.card-info {
    padding: 8px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.product-title {
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis; /* Single line ellipsis */
    color: #333;
}

.product-price {
    font-size: 14px;
    color: #FF5722;
    text-align: right;
    font-weight: 600;
}

.points-badge {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 16px;
    font-weight: bold;
    color: #FF9800;
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
}

.flex-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

/* Rarity Tags */
.rarity-tag {
    position: absolute;
    top: 6px;
    right: 6px;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 10px;
    color: #fff;
    font-weight: bold;
    box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.rarity-common {
    background: #909399; /* Grey */
}

.rarity-rare {
    background: linear-gradient(135deg, #409EFF, #337ecc); /* Blue */
}

.rarity-legendary {
    background: linear-gradient(135deg, #FFD700, #FFA500); /* Gold */
    box-shadow: 0 0 8px rgba(255, 215, 0, 0.6);
}
</style>
