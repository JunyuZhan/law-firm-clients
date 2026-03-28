import { DefineComponent } from 'vue'

interface PupilProps {
  size?: number
  maxDistance?: number
  pupilColor?: string
  forceLookX?: number
  forceLookY?: number
}

const Pupil: DefineComponent<PupilProps>

export default Pupil
