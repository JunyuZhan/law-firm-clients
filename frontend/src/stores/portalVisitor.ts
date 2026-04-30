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
  const hasAccessContext = computed(() => Boolean(profile.value.lastMatterId && profile.value.lastMatterToken))

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

  function clearAccessContext() {
    profile.value = {
      ...profile.value,
      lastMatterId: '',
      lastMatterToken: '',
    }
    setJSON(STORAGE_KEY, profile.value)
  }

  function applyMatterAccessContext(matterId: string, accessToken: string) {
    saveProfile({
      lastMatterId: matterId,
      lastMatterToken: accessToken,
    })
  }

  function resolveAccessContext(routeQuery?: Record<string, unknown>) {
    const routeMatterId = typeof routeQuery?.matterId === 'string' ? routeQuery.matterId : ''
    const routeToken = typeof routeQuery?.token === 'string' ? routeQuery.token : ''

    if (routeMatterId && routeToken) {
      return {
        matterId: routeMatterId,
        token: routeToken,
        source: 'route' as const,
      }
    }

    if (profile.value.lastMatterId && profile.value.lastMatterToken) {
      return {
        matterId: profile.value.lastMatterId,
        token: profile.value.lastMatterToken,
        source: 'stored' as const,
      }
    }

    return {
      matterId: '',
      token: '',
      source: 'none' as const,
    }
  }

  return {
    profile,
    hasProfile,
    displayName,
    displayId,
    hasAccessContext,
    saveProfile,
    hydrateProfile,
    clearProfile,
    clearAccessContext,
    applyMatterAccessContext,
    resolveAccessContext,
  }
})
