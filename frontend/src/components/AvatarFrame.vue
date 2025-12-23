<template>
  <div class="avatar-frame-container" :style="{ width: size + 'px', height: size + 'px' }">
    <canvas ref="canvas" :width="size * dpr" :height="size * dpr" :style="{ width: size + 'px', height: size + 'px' }"></canvas>
    <!-- Hidden video element for MP4 frames -->
    <video 
      ref="videoRef" 
      v-if="isVideo"
      muted 
      loop 
      playsinline 
      style="display: none;"
      @play="startAnimation"
      @pause="stopAnimation"
    ></video>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, computed } from 'vue'
import { imageCache } from '@/utils/resourceCache'

const props = defineProps({
  avatarUrl: { type: String, required: true },
  frameUrl: { type: String, default: '' },
  size: { type: Number, default: 100 }
})

const canvas = ref<HTMLCanvasElement | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)
const dpr = window.devicePixelRatio || 1
let ctx: CanvasRenderingContext2D | null = null
let animationId: number | null = null
let avatarImg: HTMLImageElement | null = null
let frameImg: HTMLImageElement | null = null

const isVideo = computed(() => {
  return props.frameUrl.endsWith('.mp4') || props.frameUrl.endsWith('.webm')
})

const loadResource = (url: string): Promise<HTMLImageElement> => {
  return new Promise((resolve, reject) => {
    const cached = imageCache.get(url)
    if (cached) {
      resolve(cached)
      return
    }
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      imageCache.put(url, img)
      resolve(img)
    }
    img.onerror = reject
    img.src = url
  })
}

const draw = () => {
  if (!canvas.value || !ctx) return
  
  ctx.clearRect(0, 0, canvas.value.width, canvas.value.height)
  
  // Draw Avatar (Circle)
  if (avatarImg) {
    ctx.save()
    ctx.beginPath()
    const center = canvas.value.width / 2
    const radius = (canvas.value.width / 2) * 0.8 // Avatar is slightly smaller than frame
    ctx.arc(center, center, radius, 0, Math.PI * 2)
    ctx.clip()
    ctx.drawImage(avatarImg, center - radius, center - radius, radius * 2, radius * 2)
    ctx.restore()
  }

  // Draw Frame
  if (isVideo.value && videoRef.value) {
    // Draw video frame
    ctx.drawImage(videoRef.value, 0, 0, canvas.value.width, canvas.value.height)
    animationId = requestAnimationFrame(draw)
  } else if (frameImg) {
    // Draw static image frame
    ctx.drawImage(frameImg, 0, 0, canvas.value.width, canvas.value.height)
  }
}

const startAnimation = () => {
  if (!animationId) {
    draw()
  }
}

const stopAnimation = () => {
  if (animationId) {
    cancelAnimationFrame(animationId)
    animationId = null
  }
}

const init = async () => {
  if (!canvas.value) return
  ctx = canvas.value.getContext('2d')
  if (!ctx) return

  // Load Avatar
  try {
    avatarImg = await loadResource(props.avatarUrl)
  } catch (e) {
    console.error('Failed to load avatar', e)
  }

  // Load Frame
  if (props.frameUrl) {
    if (isVideo.value) {
      if (videoRef.value) {
        videoRef.value.src = props.frameUrl
        videoRef.value.play().catch(e => console.error('Auto-play failed', e))
      }
    } else {
      try {
        frameImg = await loadResource(props.frameUrl)
      } catch (e) {
        console.error('Failed to load frame', e)
        // Fallback logic could go here
      }
    }
  }

  if (!isVideo.value) {
    draw()
  }
}

watch(() => [props.avatarUrl, props.frameUrl], init)

onMounted(init)

onUnmounted(() => {
  stopAnimation()
})
</script>

<style scoped>
.avatar-frame-container {
  position: relative;
  display: inline-block;
}
</style>
