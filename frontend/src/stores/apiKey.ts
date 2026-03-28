import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

const API_KEY_STORAGE_KEY = 'admin_api_key'

export const useApiKeyStore = defineStore('apiKey', () => {
  // 状态
  const apiKey = ref<string | null>(localStorage.getItem(API_KEY_STORAGE_KEY))

  // 计算属性
  const isAuthenticated = computed(() => !!apiKey.value)

  // 方法
  function setApiKey(key: string) {
    apiKey.value = key
    localStorage.setItem(API_KEY_STORAGE_KEY, key)
  }

  function clearApiKey() {
    apiKey.value = null
    localStorage.removeItem(API_KEY_STORAGE_KEY)
  }

  function getApiKey(): string | null {
    return apiKey.value
  }

  return {
    apiKey,
    isAuthenticated,
    setApiKey,
    clearApiKey,
    getApiKey,
  }
})
