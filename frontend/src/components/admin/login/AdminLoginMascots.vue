<template>
  <div class="mascot-stage">
    <div class="mascot-inner">
      <!-- Purple tall -->
      <div
        ref="purpleRef"
        class="char-purple"
        :style="purpleBodyStyle"
      >
        <div
          class="purple-eyes"
          :style="purpleEyesStyle"
        >
          <MascotEyeBall
            :size="18"
            :pupil-size="7"
            :max-distance="5"
            eye-color="#ffffff"
            pupil-color="#2D2D2D"
            :is-blinking="isPurpleBlinking"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="purpleForceX"
            :force-look-y="purpleForceY"
          />
          <MascotEyeBall
            :size="18"
            :pupil-size="7"
            :max-distance="5"
            eye-color="#ffffff"
            pupil-color="#2D2D2D"
            :is-blinking="isPurpleBlinking"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="purpleForceX"
            :force-look-y="purpleForceY"
          />
        </div>
      </div>

      <!-- Black tall -->
      <div
        ref="blackRef"
        class="char-black"
        :style="blackBodyStyle"
      >
        <div
          class="black-eyes"
          :style="blackEyesStyle"
        >
          <MascotEyeBall
            :size="16"
            :pupil-size="6"
            :max-distance="4"
            eye-color="#ffffff"
            pupil-color="#2D2D2D"
            :is-blinking="isBlackBlinking"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="blackForceX"
            :force-look-y="blackForceY"
          />
          <MascotEyeBall
            :size="16"
            :pupil-size="6"
            :max-distance="4"
            eye-color="#ffffff"
            pupil-color="#2D2D2D"
            :is-blinking="isBlackBlinking"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="blackForceX"
            :force-look-y="blackForceY"
          />
        </div>
      </div>

      <!-- Orange -->
      <div
        ref="orangeRef"
        class="char-orange"
        :style="orangeBodyStyle"
      >
        <div
          class="orange-eyes"
          :style="orangeEyesStyle"
        >
          <MascotPupil
            :size="12"
            :max-distance="5"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="plainForceX"
            :force-look-y="plainForceY"
          />
          <MascotPupil
            :size="12"
            :max-distance="5"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="plainForceX"
            :force-look-y="plainForceY"
          />
        </div>
      </div>

      <!-- Yellow -->
      <div
        ref="yellowRef"
        class="char-yellow"
        :style="yellowBodyStyle"
      >
        <div
          class="yellow-eyes"
          :style="yellowEyesStyle"
        >
          <MascotPupil
            :size="12"
            :max-distance="5"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="plainForceX"
            :force-look-y="plainForceY"
          />
          <MascotPupil
            :size="12"
            :max-distance="5"
            :mouse-x="mouseX"
            :mouse-y="mouseY"
            :force-look-x="plainForceX"
            :force-look-y="plainForceY"
          />
        </div>
        <div
          class="yellow-mouth"
          :style="yellowMouthStyle"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import MascotEyeBall from './MascotEyeBall.vue'
import MascotPupil from './MascotPupil.vue'

const props = defineProps<{
  isTyping: boolean
  passwordLength: number
  showPassword: boolean
}>()

const mouseX = ref(0)
const mouseY = ref(0)

const purpleRef = ref<HTMLElement | null>(null)
const blackRef = ref<HTMLElement | null>(null)
const yellowRef = ref<HTMLElement | null>(null)
const orangeRef = ref<HTMLElement | null>(null)

const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

let purpleBlinkTimer: ReturnType<typeof setTimeout> | undefined
let blackBlinkTimer: ReturnType<typeof setTimeout> | undefined
let lookTimer: ReturnType<typeof setTimeout> | undefined
let peekTimer: ReturnType<typeof setTimeout> | undefined
let peekScheduleTimer: ReturnType<typeof setTimeout> | undefined

function randBlinkDelay() {
  return Math.random() * 4000 + 3000
}

function schedulePurpleBlink() {
  purpleBlinkTimer = setTimeout(() => {
    isPurpleBlinking.value = true
    setTimeout(() => {
      isPurpleBlinking.value = false
      schedulePurpleBlink()
    }, 150)
  }, randBlinkDelay())
}

function scheduleBlackBlink() {
  blackBlinkTimer = setTimeout(() => {
    isBlackBlinking.value = true
    setTimeout(() => {
      isBlackBlinking.value = false
      scheduleBlackBlink()
    }, 150)
  }, randBlinkDelay())
}

function onMouseMove(e: MouseEvent) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}

function calcPose(el: HTMLElement | null) {
  if (!el) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const rect = el.getBoundingClientRect()
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3
  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY
  const faceX = Math.max(-15, Math.min(15, deltaX / 20))
  const faceY = Math.max(-10, Math.min(10, deltaY / 30))
  const bodySkew = Math.max(-6, Math.min(6, -deltaX / 120))
  return { faceX, faceY, bodySkew }
}

const purplePose = computed(() => {
  void mouseX.value
  void mouseY.value
  return calcPose(purpleRef.value)
})
const blackPose = computed(() => {
  void mouseX.value
  void mouseY.value
  return calcPose(blackRef.value)
})
const yellowPose = computed(() => {
  void mouseX.value
  void mouseY.value
  return calcPose(yellowRef.value)
})
const orangePose = computed(() => {
  void mouseX.value
  void mouseY.value
  return calcPose(orangeRef.value)
})

const pwdVisible = computed(() => props.passwordLength > 0 && props.showPassword)
const pwdHiddenTyping = computed(
  () => props.isTyping || (props.passwordLength > 0 && !props.showPassword),
)

const purpleForceX = computed(() => {
  if (pwdVisible.value) return isPurplePeeking.value ? 4 : -4
  if (isLookingAtEachOther.value) return 3
  return undefined
})

const purpleForceY = computed(() => {
  if (pwdVisible.value) return isPurplePeeking.value ? 5 : -4
  if (isLookingAtEachOther.value) return 4
  return undefined
})

const blackForceX = computed(() => {
  if (pwdVisible.value) return -4
  if (isLookingAtEachOther.value) return 0
  return undefined
})

const blackForceY = computed(() => {
  if (pwdVisible.value) return -4
  if (isLookingAtEachOther.value) return -4
  return undefined
})

const plainForceX = computed(() => (pwdVisible.value ? -5 : undefined))
const plainForceY = computed(() => (pwdVisible.value ? -4 : undefined))

const purpleBodyStyle = computed(() => ({
  height: pwdHiddenTyping.value ? '440px' : '400px',
  transform: pwdVisible.value
    ? 'skewX(0deg)'
    : pwdHiddenTyping.value
      ? `skewX(${(purplePose.value.bodySkew || 0) - 12}deg) translateX(40px)`
      : `skewX(${purplePose.value.bodySkew || 0}deg)`,
  transformOrigin: 'bottom center',
}))

const purpleEyesStyle = computed(() => ({
  left: pwdVisible.value ? '20px' : isLookingAtEachOther.value ? '55px' : `${45 + purplePose.value.faceX}px`,
  top: pwdVisible.value ? '35px' : isLookingAtEachOther.value ? '65px' : `${40 + purplePose.value.faceY}px`,
}))

const blackBodyStyle = computed(() => ({
  transform: pwdVisible.value
    ? 'skewX(0deg)'
    : isLookingAtEachOther.value
      ? `skewX(${(blackPose.value.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`
      : pwdHiddenTyping.value
        ? `skewX(${(blackPose.value.bodySkew || 0) * 1.5}deg)`
        : `skewX(${blackPose.value.bodySkew || 0}deg)`,
  transformOrigin: 'bottom center',
}))

const blackEyesStyle = computed(() => ({
  left: pwdVisible.value ? '10px' : isLookingAtEachOther.value ? '32px' : `${26 + blackPose.value.faceX}px`,
  top: pwdVisible.value ? '28px' : isLookingAtEachOther.value ? '12px' : `${32 + blackPose.value.faceY}px`,
}))

const orangeBodyStyle = computed(() => ({
  transform: pwdVisible.value ? 'skewX(0deg)' : `skewX(${orangePose.value.bodySkew || 0}deg)`,
  transformOrigin: 'bottom center',
}))

const orangeEyesStyle = computed(() => ({
  left: pwdVisible.value ? '50px' : `${82 + orangePose.value.faceX}px`,
  top: pwdVisible.value ? '85px' : `${90 + orangePose.value.faceY}px`,
}))

const yellowBodyStyle = computed(() => ({
  transform: pwdVisible.value ? 'skewX(0deg)' : `skewX(${yellowPose.value.bodySkew || 0}deg)`,
  transformOrigin: 'bottom center',
}))

const yellowEyesStyle = computed(() => ({
  left: pwdVisible.value ? '20px' : `${52 + yellowPose.value.faceX}px`,
  top: pwdVisible.value ? '35px' : `${40 + yellowPose.value.faceY}px`,
}))

const yellowMouthStyle = computed(() => ({
  left: pwdVisible.value ? '10px' : `${40 + yellowPose.value.faceX}px`,
  top: pwdVisible.value ? '88px' : `${88 + yellowPose.value.faceY}px`,
}))

watch(
  () => props.isTyping,
  (typing) => {
    if (lookTimer) clearTimeout(lookTimer)
    if (typing) {
      isLookingAtEachOther.value = true
      lookTimer = setTimeout(() => {
        isLookingAtEachOther.value = false
      }, 800)
    } else {
      isLookingAtEachOther.value = false
    }
  },
)

watch(
  () => [props.passwordLength, props.showPassword] as const,
  () => {
    if (peekTimer) clearTimeout(peekTimer)
    if (peekScheduleTimer) clearTimeout(peekScheduleTimer)
    isPurplePeeking.value = false

    if (props.passwordLength > 0 && props.showPassword) {
      const schedulePeek = () => {
        peekScheduleTimer = setTimeout(() => {
          isPurplePeeking.value = true
          peekTimer = setTimeout(() => {
            isPurplePeeking.value = false
            schedulePeek()
          }, 800)
        }, Math.random() * 3000 + 2000)
      }
      schedulePeek()
    }
  },
)

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
  schedulePurpleBlink()
  scheduleBlackBlink()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  if (purpleBlinkTimer) clearTimeout(purpleBlinkTimer)
  if (blackBlinkTimer) clearTimeout(blackBlinkTimer)
  if (lookTimer) clearTimeout(lookTimer)
  if (peekTimer) clearTimeout(peekTimer)
  if (peekScheduleTimer) clearTimeout(peekScheduleTimer)
})
</script>

<style scoped>
.mascot-stage {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 400px;
  width: 100%;
}

.mascot-inner {
  position: relative;
  width: 550px;
  height: 400px;
}

.char-purple {
  position: absolute;
  bottom: 0;
  left: 70px;
  width: 180px;
  height: 400px;
  background-color: #6c3ff5;
  border-radius: 10px 10px 0 0;
  z-index: 1;
  transition: transform 0.7s ease-in-out, height 0.7s ease-in-out;
}

.purple-eyes {
  position: absolute;
  display: flex;
  gap: 32px;
  transition: left 0.7s ease-in-out, top 0.7s ease-in-out;
}

.char-black {
  position: absolute;
  bottom: 0;
  left: 240px;
  width: 120px;
  height: 310px;
  background-color: #2d2d2d;
  border-radius: 8px 8px 0 0;
  z-index: 2;
  transition: transform 0.7s ease-in-out;
}

.black-eyes {
  position: absolute;
  display: flex;
  gap: 24px;
  transition: left 0.7s ease-in-out, top 0.7s ease-in-out;
}

.char-orange {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 240px;
  height: 200px;
  z-index: 3;
  background-color: #ff9b6b;
  border-radius: 120px 120px 0 0;
  transition: transform 0.7s ease-in-out;
}

.orange-eyes {
  position: absolute;
  display: flex;
  gap: 32px;
  transition: left 0.2s ease-out, top 0.2s ease-out;
}

.char-yellow {
  position: absolute;
  bottom: 0;
  left: 310px;
  width: 140px;
  height: 230px;
  background-color: #e8d754;
  border-radius: 70px 70px 0 0;
  z-index: 4;
  transition: transform 0.7s ease-in-out;
}

.yellow-eyes {
  position: absolute;
  display: flex;
  gap: 24px;
  transition: left 0.2s ease-out, top 0.2s ease-out;
}

.yellow-mouth {
  position: absolute;
  width: 80px;
  height: 4px;
  background-color: #2d2d2d;
  border-radius: 999px;
  transition: left 0.2s ease-out, top 0.2s ease-out;
}
</style>
