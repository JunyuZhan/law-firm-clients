<template>
  <div
    ref="eyeRef"
    class="mascot-eye-outer"
    :style="eyeOuterStyle"
  >
    <div
      v-if="!isBlinking"
      class="mascot-eye-inner"
      :style="pupilStyle"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = withDefaults(
  defineProps<{
    size?: number
    pupilSize?: number
    maxDistance?: number
    eyeColor?: string
    pupilColor?: string
    isBlinking?: boolean
    mouseX: number
    mouseY: number
    forceLookX?: number
    forceLookY?: number
  }>(),
  {
    size: 48,
    pupilSize: 16,
    maxDistance: 10,
    eyeColor: '#ffffff',
    pupilColor: '#2D2D2D',
    isBlinking: false,
    forceLookX: undefined,
    forceLookY: undefined,
  },
)

const eyeRef = ref<HTMLElement | null>(null)

const pupilOffset = computed(() => {
  const el = eyeRef.value
  if (!el) return { x: 0, y: 0 }

  if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
    return { x: props.forceLookX, y: props.forceLookY }
  }

  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 2
  const deltaX = props.mouseX - cx
  const deltaY = props.mouseY - cy
  const dist = Math.min(Math.sqrt(deltaX ** 2 + deltaY ** 2), props.maxDistance)
  const angle = Math.atan2(deltaY, deltaX)
  return {
    x: Math.cos(angle) * dist,
    y: Math.sin(angle) * dist,
  }
})

const eyeOuterStyle = computed(() => ({
  width: `${props.size}px`,
  height: props.isBlinking ? '2px' : `${props.size}px`,
  backgroundColor: props.eyeColor,
  overflow: 'hidden',
}))

const pupilStyle = computed(() => {
  const off = pupilOffset.value
  return {
    width: `${props.pupilSize}px`,
    height: `${props.pupilSize}px`,
    backgroundColor: props.pupilColor,
    transform: `translate(${off.x}px, ${off.y}px)`,
    transition: 'transform 0.1s ease-out',
  }
})

</script>

<style scoped>
.mascot-eye-outer {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: height 0.15s ease-out, width 0.15s ease-out;
}

.mascot-eye-inner {
  border-radius: 50%;
}
</style>
