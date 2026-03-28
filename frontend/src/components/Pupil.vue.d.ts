import { DefineComponent } from 'vue'

const Pupil: DefineComponent<{
  size?: number
  maxDistance?: number
  pupilColor?: string
  forceLookX?: number
  forceLookY?: number
}, any, any, any, any, any>

export default Pupil