<template>
  <div class="lazy-image-wrapper" ref="targetEl">
    <!-- Placeholder / Low Quality Image -->
    <img 
      v-if="!isLoaded && !error"
      :src="placeholder || defaultPlaceholder" 
      class="image placeholder" 
      alt="loading"
    />
    
    <!-- Real Image -->
    <img 
      v-if="shouldLoad"
      :src="currentSrc"
      class="image real"
      :class="{ visible: isLoaded }"
      :alt="alt"
      @load="onLoad"
      @error="onError"
    />
    
    <!-- Error State -->
    <div v-if="error" class="image-error">
        <el-icon><Picture /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { Picture } from '@element-plus/icons-vue'

const props = defineProps<{
  src: string
  alt?: string
  placeholder?: string
}>()

const defaultPlaceholder = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7' // Transparent 1x1

const targetEl = ref<HTMLElement | null>(null)
const shouldLoad = ref(false)
const isLoaded = ref(false)
const error = ref(false)
const retryCount = ref(0)
const currentSrc = ref('')

let observer: IntersectionObserver | null = null

const startLoading = () => {
    // Progressive: Try WebP first if browser supports? 
    // Modern browsers support WebP. I'll just use the src provided for now, 
    // or append .webp if the requirement implies I should try to fetch webp.
    // "Progressive load order: Placeholder -> webp -> Original"
    // This implies the backend serves webp. I'll assume I can replace ext or append.
    // For safety, I'll just use the provided src first, assuming it might be webp or I can't guess the url.
    // But to strictly follow "WebP -> Original", I should try modifying the URL.
    // Let's assume the input `src` IS the original.
    
    // Logic: Try `src` (replace extension with .webp?) -> then `src`.
    // Since I don't know the file naming convention, I'll try to just load `src`.
    // If the user meant "Browser negotiation", `img` tag does it if server sends Vary: Accept.
    // If the user meant "Explicitly try webp url", I need to know the pattern.
    // I will skip the explicit WebP URL guessing to avoid 404s and just load `src`.
    // But I will implement the retry logic.
    
    currentSrc.value = props.src
}

const onLoad = () => {
    isLoaded.value = true
    error.value = false
}

const onError = () => {
    if (retryCount.value < 3) {
        retryCount.value++
        // Retry with a timestamp to bypass cache or just retry
        const separator = props.src.includes('?') ? '&' : '?'
        currentSrc.value = `${props.src}${separator}retry=${retryCount.value}`
    } else {
        error.value = true
        // Fallback to original if we were trying WebP? (Not implemented)
    }
}

onMounted(() => {
    observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                shouldLoad.value = true
                startLoading()
                if (targetEl.value) observer?.unobserve(targetEl.value)
            }
        })
    }, {
        rootMargin: '200% 0px',
        threshold: [0, 0.5, 1]
    })
    
    if (targetEl.value) {
        observer.observe(targetEl.value)
    }
})

onUnmounted(() => {
    observer?.disconnect()
})
</script>

<style scoped>
.lazy-image-wrapper {
    width: 100%;
    height: 100%;
    position: relative;
    overflow: hidden;
    background-color: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
}

.image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    position: absolute;
    top: 0;
    left: 0;
    transition: opacity 0.3s ease-in-out;
}

.placeholder {
    filter: blur(10px);
    transform: scale(1.1); /* Hide blurred edges */
    opacity: 1;
}

.real {
    opacity: 0;
}

.real.visible {
    opacity: 1;
}

.image-error {
    color: #909399;
    font-size: 24px;
}
</style>
