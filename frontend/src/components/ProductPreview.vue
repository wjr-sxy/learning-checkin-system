<template>
  <Transition name="modal-fade">
    <div v-if="visible" class="product-preview-overlay" @click.self="close">
      <div class="product-preview-modal" :class="{ closing: isClosing }">
        <!-- Close Button -->
        <div class="close-btn" @click="close">
            <el-icon><Close /></el-icon>
        </div>

        <!-- Countdown -->
        <div class="countdown-timer" v-if="timeLeft > 0">
            自动关闭(<span :class="{ 'text-blink': true }">{{ timeLeft }}s</span>)
        </div>

        <!-- Content Area -->
        <div class="preview-content">
            <!-- Video Player -->
            <div v-if="isVideo" class="media-container">
                <video 
                    :src="product.videoUrl" 
                    controls 
                    playsinline
                    class="preview-video"
                    @waiting="isLoading = true"
                    @playing="isLoading = false"
                ></video>
                <div v-if="isLoading" class="loading-dots">
                    <span></span><span></span><span></span>
                </div>
            </div>

            <!-- Trial Area (Image/Canvas/Frame) -->
            <div 
                v-else 
                class="trial-area" 
                @touchstart="handleTouchStart"
                @touchmove="handleTouchMove"
                @touchend="handleTouchEnd"
            >
                <div 
                    class="trial-target" 
                    :style="trialStyle"
                >
                    <!-- Avatar Frame Layering -->
                    <div v-if="product.type === 'AVATAR_FRAME'" class="avatar-frame-preview">
                         <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" class="base-avatar" />
                         <img :src="product.imageUrl" class="frame-overlay" />
                    </div>

                    <!-- Standard Image -->
                    <img v-else :src="product.imageUrl" class="trial-image" />
                </div>
                <div class="trial-hint">双指缩放/旋转</div>
            </div>
        </div>

        <!-- Footer / Purchase -->
        <div class="preview-footer">
            <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="description">{{ product.description }}</p>
                <div class="price-info">
                    <span class="price">{{ formatPrice(product.price) }} 积分</span>
                    <span class="stock-info" v-if="product.stock > 0">剩余: {{ product.stock }}</span>
                    <span class="stock-label" v-if="product.stock <= 0">已售罄</span>
                </div>
            </div>
            <div class="action-btn">
                <el-button 
                    type="primary" 
                    size="large" 
                    :disabled="product.stock <= 0"
                    @click="handleBuy"
                >
                    立即兑换
                </el-button>
            </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Confirm Dialog -->
  <el-dialog 
    v-model="confirmVisible" 
    title="确认兑换" 
    width="320px"
    class="confirm-dialog"
    append-to-body
  >
    <div class="confirm-content">
        <img :src="product?.imageUrl" class="confirm-img" />
        <div class="confirm-details">
            <p>当前积分: <span class="current-points">{{ userPoints }}</span></p>
            <p>兑换所需: {{ product?.price }} 积分</p>
            <p v-if="balanceDiff < 0" class="balance-warning">
                <el-icon><Warning /></el-icon> 积分不足 (差额 {{ Math.abs(balanceDiff) }})
            </p>
        </div>
    </div>

    <!-- Shipping Form for Physical Products -->
    <div v-if="product?.type === 'PHYSICAL'" class="shipping-form mt-4">
        <el-divider>收货信息</el-divider>
        <el-form :model="shippingForm" label-width="60px" size="small">
            <el-form-item label="收货人">
                <el-input v-model="shippingForm.name" placeholder="姓名" />
            </el-form-item>
            <el-form-item label="电话">
                <el-input v-model="shippingForm.phone" placeholder="手机号" />
            </el-form-item>
            <el-form-item label="地址">
                <el-input v-model="shippingForm.address" type="textarea" :rows="2" placeholder="详细地址" />
            </el-form-item>
        </el-form>
    </div>

    <template #footer>
        <span class="dialog-footer">
            <el-button @click="confirmVisible = false">取消</el-button>
            <el-button 
                type="primary" 
                v-if="balanceDiff >= 0"
                @click="confirmPurchase"
            >
                确认
            </el-button>
            <el-button 
                type="danger" 
                v-else
                @click="goToRecharge"
            >
                去充值
            </el-button>
        </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted, reactive } from 'vue'
import { Close, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
// import { useRouter } from 'vue-router'

const props = defineProps<{
  visible: boolean
  product: any
  userPoints: number
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'purchase', product: any, shippingInfo?: any): void
}>()

// const router = useRouter()
const isClosing = ref(false)
const timeLeft = ref(30)
const timer = ref<any>(null)
const isLoading = ref(false)
// const videoRef = ref<HTMLVideoElement | null>(null)

// Trial Logic
const scale = ref(1)
const rotation = ref(0)
// const trialArea = ref<HTMLElement | null>(null)

// Gesture State
let startDist = 0
let startAngle = 0
let initialScale = 1
let initialRotation = 0

const isVideo = computed(() => {
    // Basic check, ideally strictly typed or field
    return props.product?.type === 'DYNAMIC_BG' || props.product?.videoUrl
})

const balanceDiff = computed(() => {
    return props.userPoints - (props.product?.price || 0)
})

const trialStyle = computed(() => ({
    transform: `scale(${scale.value}) rotate(${rotation.value}deg) translateZ(0)`,
    willChange: 'transform'
}))

// Timer
watch(() => props.visible, (val) => {
    if (val) {
        timeLeft.value = 30
        startTimer()
        // Reset trial
        scale.value = 1
        rotation.value = 0
    } else {
        stopTimer()
    }
})

const startTimer = () => {
    stopTimer()
    timer.value = setInterval(() => {
        // Optimization: Only update DOM if tab is visible? 
        // Browser throttles intervals in background tabs anyway.
        if (document.hidden) return
        
        timeLeft.value--
        if (timeLeft.value <= 0) {
            close()
        }
    }, 1000)
}

const stopTimer = () => {
    if (timer.value) clearInterval(timer.value)
    timer.value = null
}

const close = () => {
    emit('update:visible', false)
}

// Gesture Handling
const getDistance = (p1: Touch, p2: Touch) => {
    const dx = p1.clientX - p2.clientX
    const dy = p1.clientY - p2.clientY
    return Math.sqrt(dx * dx + dy * dy)
}

const getAngle = (p1: Touch, p2: Touch) => {
    return Math.atan2(p2.clientY - p1.clientY, p2.clientX - p1.clientX) * 180 / Math.PI
}

const handleTouchStart = (e: TouchEvent) => {
    if (e.touches.length === 2) {
        e.preventDefault() // Prevent scroll
        startDist = getDistance(e.touches[0]!, e.touches[1]!)
        startAngle = getAngle(e.touches[0]!, e.touches[1]!)
        initialScale = scale.value
        initialRotation = rotation.value
    }
}

const handleTouchMove = (e: TouchEvent) => {
    if (e.touches.length === 2) {
        e.preventDefault()
        const currentDist = getDistance(e.touches[0]!, e.touches[1]!)
        const currentAngle = getAngle(e.touches[0]!, e.touches[1]!)
        
        // Scale
        const scaleRatio = currentDist / startDist
        let newScale = initialScale * scaleRatio
        // Clamp 0.8 - 1.5
        newScale = Math.min(Math.max(newScale, 0.8), 1.5)
        // Step 0.1? "Step 0.1" implies discrete steps.
        // Let's make it smooth but maybe snap? 
        // "缩放范围：0.8-1.5（步进0.1）" -> Usually means discrete steps or snapping.
        // I will just clamp for smoothness, or round.
        newScale = Math.round(newScale * 10) / 10
        scale.value = newScale
        
        // Rotate
        const angleDiff = currentAngle - startAngle
        let newRotation = initialRotation + angleDiff
        // 0-360 normalization? Not strictly required by CSS rotate, but good for cleanliness.
        // CSS handles > 360 fine.
        rotation.value = newRotation
    }
}

const handleTouchEnd = () => {
    // Reset or keep? Usually keep state.
}

const formatPrice = (price: number) => {
    return price ? price.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ",") : '0.00'
}

// Purchase Flow
const confirmVisible = ref(false)
const shippingForm = reactive({
    name: '',
    phone: '',
    address: ''
})

const handleBuy = () => {
    confirmVisible.value = true
}

const confirmPurchase = () => {
    if (props.product?.type === 'PHYSICAL') {
        if (!shippingForm.name || !shippingForm.phone || !shippingForm.address) {
            ElMessage.warning('请填写完整的收货信息')
            return
        }
        // Basic phone validation
        if (!/^1[3-9]\d{9}$/.test(shippingForm.phone)) {
             ElMessage.warning('请输入有效的手机号')
             return
        }
        emit('purchase', props.product, { ...shippingForm })
    } else {
        emit('purchase', props.product)
    }
    
    confirmVisible.value = false
    close()
}

const goToRecharge = () => {
    // URL param: source=market_${item_id}
    // Track event: recharge_redirect
    console.log('Track event: recharge_redirect')
    // Assuming recharge page is /recharge or similar. 
    // I'll push to a route or simulate redirect
    // router.push({ path: '/recharge', query: { source: `market_${props.product.id}` } })
    
    // For now just close
    confirmVisible.value = false
    close()
    alert('跳转充值页面 (模拟)')
}

onUnmounted(() => {
    stopTimer()
})
</script>

<style scoped>
.product-preview-overlay {
    position: fixed;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
}

.product-preview-modal {
    width: 90%;
    max-width: 400px;
    background: #fff;
    border-radius: 16px;
    position: relative;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    max-height: 90vh;
}

/* Animation */
.modal-fade-enter-active,
.modal-fade-leave-active {
    transition: all 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}

.modal-fade-enter-from,
.modal-fade-leave-to {
    transform: scale(0.9);
    opacity: 0;
}

.modal-fade-enter-to,
.modal-fade-leave-from {
    transform: scale(1);
    opacity: 1;
}

.close-btn {
    position: absolute;
    top: 10px;
    right: 10px;
    width: 40px; /* Visual size */
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    background: rgba(0,0,0,0.1);
    border-radius: 50%;
}
/* Hotspot extension */
.close-btn::after {
    content: '';
    position: absolute;
    top: -5px; left: -5px; right: -5px; bottom: -5px; /* 50x50 approx */
}

.countdown-timer {
    position: absolute;
    top: 15px;
    left: 15px;
    background: rgba(0,0,0,0.6);
    color: #fff;
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    z-index: 10;
}

.text-blink {
    color: #FF5722; /* Red */
    animation: blink 1s infinite;
}

@keyframes blink {
    50% { opacity: 0.5; }
}

.preview-content {
    flex: 1;
    background: #000;
    min-height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
}

.media-container, .trial-area {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.preview-video {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.loading-dots {
    position: absolute;
    display: flex;
    gap: 4px;
}
.loading-dots span {
    width: 8px; height: 8px;
    background: #fff;
    border-radius: 50%;
    animation: bounce 0.6s infinite alternate;
}
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
    to { transform: translateY(-10px); }
}

.trial-image {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.avatar-frame-preview {
    position: relative;
    width: 200px;
    height: 200px;
}

.base-avatar {
    position: absolute;
    top: 15%;
    left: 15%;
    width: 70%;
    height: 70%;
    border-radius: 50%;
    object-fit: cover;
}

.frame-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: contain;
    z-index: 2;
}

.trial-hint {
    position: absolute;
    bottom: 20px;
    color: rgba(255,255,255,0.7);
    font-size: 12px;
    pointer-events: none;
}

.preview-footer {
    padding: 20px;
    background: #fff;
}

.product-info h3 {
    margin: 0 0 8px;
    font-size: 18px;
}
.description {
    color: #666;
    font-size: 14px;
    margin-bottom: 12px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.price-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.price {
    color: #FF5722;
    font-size: 20px;
    font-weight: bold;
}

.stock-info {
    color: #999;
    font-size: 14px;
}

.stock-label {
    background: #eee;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    color: #999;
}

.action-btn .el-button {
    width: 100%;
}

/* Confirm Dialog Styles */
.confirm-content {
    display: flex;
    gap: 16px;
}
.confirm-img {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    object-fit: cover;
}
.confirm-details p {
    margin: 4px 0;
}
.current-points {
    color: #FF9800;
    font-weight: 600;
}
.balance-warning {
    color: #F56C6C;
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
}
</style>
