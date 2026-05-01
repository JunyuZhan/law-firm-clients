<template>
  <div
    ref="pupilRef"
    class="mascot-pupil"
    :style="pupilStyle"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'

const props = withDefaults(
  defineProps<{
    size?: number
    maxDistance?: number
    pupilColor?: string
    mouseX: number
    mouseY: number
    forceLookX?: number
    forceLookY?: number
  }>(),
  {
    size: 12,
    maxDistance: 5,
    pupilColor: '#2D2D2D',
    forceLookX: undefined,
    forceLookY: undefined,
  },
)

const pupilRef = ref<HTMLElement | null>(null)

const pupilStyle = computed(() => {
  let x = 0
  let y = 0
  const el = pupilRef.value
  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    x = props.forceLookX
    y = props.forceLookY
  } else if (el) {
    const rect = el.getBoundingClientRect()
    const cx = rect.left + rect.width / 2
    const cy = rect.top + rect.height / 2
    const deltaX = props.mouseX - cx
    const deltaY = props.mouseY - cy
    const dist = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), props.maxDistance)
    const angle = Math.atan2(deltaY, deltaX)
    x = Math.cos(angle) * dist
    y = Math.sin(angle) * dist
  }

  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    backgroundColor: props.pupilColor,
    transform: `translate(${x}px, ${y}px)`,
    transition: 'transform 0.1s ease-out',
  }
})

function onResize() {
  pupilRef.value?.getBoundingClientRect()
}

onMounted(() => {
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.mascot-pupil {
  border-radius: 50%;
}
</style>
