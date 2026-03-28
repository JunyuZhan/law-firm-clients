<template>
  <div
    ref="containerRef"
    class="pull-refresh-container"
    :style="{ transform: `translateY(${translateY}px)` }"
    @touchstart="handleTouchStart"
    @touchmove="handleTouchMove"
    @touchend="handleTouchEnd"
  >
    <div
      class="pull-refresh-indicator"
      :style="{ height: `${Math.min(translateY, maxDistance)}px` }"
    >
      <div class="pull-refresh-icon">
        <loading-outlined v-if="loading" />
        <sync-outlined
          v-else
          :spin="translateY >= pullThreshold"
        />
      </div>
      <div class="pull-refresh-text">
        {{ statusText }}
      </div>
    </div>
    <div class="pull-refresh-content">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { LoadingOutlined, SyncOutlined } from '@ant-design/icons-vue'

interface Props {
  threshold?: number
  maxDistance?: number
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  threshold: 60,
  maxDistance: 120,
  loading: false,
})

const emit = defineEmits<{
  (e: 'refresh'): void
}>()

const containerRef = ref<HTMLElement>()
const startY = ref(0)
const translateY = ref(0)
const isDragging = ref(false)

const pullThreshold = computed(() => props.threshold)
const canRefresh = computed(() => translateY.value >= pullThreshold.value)
const statusText = computed(() => {
  if (props.loading) return '加载中...'
  if (canRefresh.value) return '释放立即刷新'
  return '下拉刷新'
})

function handleTouchStart(e: TouchEvent) {
  if (props.loading) return

  const scrollableElement = containerRef.value?.querySelector('.pull-refresh-content')

  if (scrollableElement && scrollableElement.scrollTop > 0) {
    return
  }

  startY.value = e.touches[0].clientY
  isDragging.value = true
}

function handleTouchMove(e: TouchEvent) {
  if (!isDragging.value || props.loading) return

  const currentY = e.touches[0].clientY
  const diff = currentY - startY.value

  if (diff > 0) {
    e.preventDefault()

    const resistance = 0.4
    translateY.value = Math.min(diff * resistance, props.maxDistance)
  }
}

function handleTouchEnd() {
  if (!isDragging.value) return

  isDragging.value = false

  if (canRefresh.value && !props.loading) {
    translateY.value = pullThreshold.value
    emit('refresh')
  } else {
    translateY.value = 0
  }
}

function reset() {
  translateY.value = 0
}

defineExpose({
  reset,
})
</script>

<style scoped>
.pull-refresh-container {
  position: relative;
  width: 100%;
  overflow: hidden;
  -webkit-overflow-scrolling: touch;
  transition: transform 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.pull-refresh-container.is-dragging {
  transition: none;
}

.pull-refresh-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: var(--primary-color);
}

.pull-refresh-icon {
  font-size: 24px;
  margin-bottom: 8px;
  color: var(--primary-color);
  transition: transform 0.3s ease;
}

.pull-refresh-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.pull-refresh-content {
  overflow-y: auto;
  overflow-x: hidden;
  -webkit-overflow-scrolling: touch;
  height: 100%;
  position: relative;
}
</style>
