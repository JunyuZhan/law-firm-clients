import { DefineComponent } from 'vue'

const EyeBall: DefineComponent<{
  size?: number
  pupilSize?: number
  maxDistance?: number
  eyeColor?: string
  pupilColor?: string
  isBlinking?: boolean
  forceLookX?: number
  forceLookY?: number
}, any, any, any, any, any>

export default EyeBall