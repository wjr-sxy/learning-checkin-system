<template>
  <div class="scrollable-tabs-container" ref="containerRef">
    <div class="tabs-wrapper">
      <div 
        v-for="(item, index) in items" 
        :key="item.value"
        class="tab-item"
        :class="{ active: modelValue === item.value }"
        ref="tabRefs"
        @click="selectTab(item.value, index)"
      >
        {{ item.label }}
      </div>
      <div class="tab-indicator" :style="indicatorStyle"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'

const props = defineProps<{
  items: { label: string; value: string }[]
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const containerRef = ref<HTMLElement | null>(null)
// const wrapperRef = ref<HTMLElement | null>(null)
const tabRefs = ref<HTMLElement[]>([])
const indicatorStyle = ref({})

const selectTab = (value: string, index: number) => {
  emit('update:modelValue', value)
  updateIndicator(index)
  scrollToTab(index)
}

const updateIndicator = (index: number) => {
  const tab = tabRefs.value[index]
  if (!tab) return

  // Width dynamic matching current Tab content width
  // The indicator should probably be relative to the wrapper
  const width = tab.offsetWidth
  const left = tab.offsetLeft

  indicatorStyle.value = {
    width: `${width}px`,
    transform: `translateX(${left}px)`,
    transition: 'all 300ms cubic-bezier(0.4, 0, 0.2, 1)' // Specified easing
  }
}

const scrollToTab = (index: number) => {
  const tab = tabRefs.value[index]
  const container = containerRef.value
  if (!tab || !container) return

  const center = tab.offsetLeft + tab.offsetWidth / 2
  const containerCenter = container.offsetWidth / 2
  const scrollLeft = center - containerCenter

  container.scrollTo({
    left: scrollLeft,
    behavior: 'smooth'
  })
}

watch(() => props.modelValue, (newVal) => {
  const index = props.items.findIndex(item => item.value === newVal)
  if (index !== -1) {
    nextTick(() => {
        updateIndicator(index)
        scrollToTab(index)
    })
  }
})

onMounted(() => {
    nextTick(() => {
        const index = props.items.findIndex(item => item.value === props.modelValue)
        if (index !== -1) {
            updateIndicator(index)
            // Initial scroll might not want smooth behavior, but it's fine
            scrollToTab(index)
        }
    })
})
</script>

<style scoped>
.scrollable-tabs-container {
  width: 100%;
  overflow-x: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
  position: relative;
  border-bottom: 1px solid #eee;
}

.scrollable-tabs-container::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}

.tabs-wrapper {
  display: flex;
  position: relative;
  min-width: 100%;
  padding: 0 10px; /* Optional padding */
}

.tab-item {
  /* Content adaptive width algorithm */
  flex: 0 0 auto;
  /* Min width 80px (including padding), Max 120px */
  min-width: 60px; /* 80 - 20 padding */
  max-width: 100px; /* 120 - 20 padding */
  padding: 10px; /* 10px internal padding */
  box-sizing: content-box; /* padding adds to width? Or border-box? */
  /* Requirement: "Min width 80px (including 10px padding)" 
     If box-sizing is content-box (default), width is content. 
     If we want total width 80, and padding is 10px each side (20px total), content is 60.
     Let's use border-box for easier calculation.
  */
  
  height: 44px;
  line-height: 24px;
  text-align: center;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  user-select: none;
  position: relative;
  z-index: 1;
}

/* Override box-sizing to match requirements easier */
.tab-item {
    box-sizing: border-box;
    min-width: 80px;
    max-width: 120px;
    padding: 0 10px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.tab-item.active {
  color: #FF5722; /* Highlight color */
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  background-color: #FF5722;
  z-index: 0;
  /* width and transform set by JS */
}
</style>
