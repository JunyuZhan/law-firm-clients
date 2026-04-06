import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getJSON, removeItem, setJSON } from '@/utils/storage'

interface PortalVisitorProfile {
  clientId: number | null
  clientName: string
  lastMatterId: string
  lastMatterToken: string
}

const STORAGE_KEY = 'portal_visitor_profile'

function getDefaultProfile(): PortalVisitorProfile {
  return {
    clientId: null,
    clientName: '',
    lastMatterId: '',
    lastMatterToken: '',
  }
}

export const usePortalVisitorStore = defineStore('portalVisitor', () => {
  const profile = ref<PortalVisitorProfile>(getJSON<PortalVisitorProfile>(STORAGE_KEY, getDefaultProfile()) || getDefaultProfile())

  const hasProfile = computed(() => Boolean(profile.value.clientName))
  const displayName = computed(() => profile.value.clientName || '访客')
  const displayId = computed(() => profile.value.clientId)

  function saveProfile(payload: Partial<PortalVisitorProfile>) {
    profile.value = {
      ...profile.value,
      ...payload,
    }
    setJSON(STORAGE_KEY, profile.value)
  }

  function hydrateProfile() {
    profile.value = getJSON<PortalVisitorProfile>(STORAGE_KEY, getDefaultProfile()) || getDefaultProfile()
  }

  function clearProfile() {
    profile.value = getDefaultProfile()
    removeItem(STORAGE_KEY)
  }

  return {
    profile,
    hasProfile,
    displayName,
    displayId,
    saveProfile,
    hydrateProfile,
    clearProfile,
  }
})
