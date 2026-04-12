import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getPortalConfig, getBrandConfig } from '@/api/config'
import {
  APP_NAME,
  APP_SHORT_NAME,
  APP_SHORT_NAME_EN,
  APP_SLOGAN,
  LAW_FIRM_NAME,
  LAW_FIRM_WEBSITE,
  LOGO_URL,
  ICP_LICENSE,
  PORTAL_ACCESS_NOTICE,
  PORTAL_EYEBROW_EN,
  STAFF_ENTRY_LABEL,
} from '@/config/app'
import logger from '@/utils/logger'

/**
 * 全局应用配置 Store
 * 
 * 配置优先级：后台配置 > 环境变量默认值
 */
export const useAppConfigStore = defineStore('appConfig', () => {
  // 配置值（使用环境变量作为默认值）
  const appName = ref(APP_NAME)
  const appShortName = ref(APP_SHORT_NAME)
  const appShortNameEn = ref(APP_SHORT_NAME_EN)
  const appSlogan = ref(APP_SLOGAN)
  const lawFirmName = ref(LAW_FIRM_NAME)
  const lawFirmWebsite = ref(LAW_FIRM_WEBSITE)
  const logoUrl = ref(LOGO_URL)
  const icpLicense = ref(ICP_LICENSE)
  const copyright = ref('')
  const portalEyebrowEn = ref(PORTAL_EYEBROW_EN)
  const portalAccessNotice = ref(PORTAL_ACCESS_NOTICE)
  const staffEntryLabel = ref(STAFF_ENTRY_LABEL)
  const displayName = computed(() => appName.value || lawFirmName.value || appShortName.value || APP_NAME)

  // 加载状态
  const loaded = ref(false)
  const loading = ref(false)

  /**
   * 从 API 加载配置
   */
  async function loadConfig() {
    if (loading.value) return
    loading.value = true
    
    try {
      // 并行获取门户配置和品牌配置
      const [portalRes, brandRes] = await Promise.all([
        getPortalConfig({ suppressErrorMessage: true }).catch(() => null),
        getBrandConfig({ suppressErrorMessage: true }).catch(() => null),
      ])
      
      // 应用门户配置
      if (portalRes?.success && portalRes.data) {
        const portal = portalRes.data
        if (portal.lawFirmName) lawFirmName.value = portal.lawFirmName
        if (portal.lawFirmWebsite) lawFirmWebsite.value = portal.lawFirmWebsite
        if (portal.appSlogan) appSlogan.value = portal.appSlogan
        if (portal.icpLicense) icpLicense.value = portal.icpLicense
        if (portal.copyright) copyright.value = portal.copyright
        if (portal.logoUrl) logoUrl.value = portal.logoUrl
        if (portal.portalEyebrowEn != null) {
          portalEyebrowEn.value = portal.portalEyebrowEn
        }
        if (portal.portalAccessNotice != null) {
          portalAccessNotice.value = portal.portalAccessNotice
        }
        if (portal.staffEntryLabel != null) {
          staffEntryLabel.value = portal.staffEntryLabel
        }
      }
      
      // 应用品牌配置
      if (brandRes?.success && brandRes.data) {
        const brand = brandRes.data
        if (brand.appName) appName.value = brand.appName
        if (brand.appShortName) appShortName.value = brand.appShortName
        if (brand.appShortNameEn) appShortNameEn.value = brand.appShortNameEn
        if (brand.logoUrl) logoUrl.value = brand.logoUrl
      }
      
      loaded.value = true
      logger.debug('应用配置加载完成')
    } catch (error) {
      logger.warn('加载应用配置失败，使用默认配置', error)
    } finally {
      loading.value = false
    }
  }

  return {
    // 配置值
    appName,
    appShortName,
    appShortNameEn,
    appSlogan,
    lawFirmName,
    displayName,
    lawFirmWebsite,
    logoUrl,
    icpLicense,
    copyright,
    portalEyebrowEn,
    portalAccessNotice,
    staffEntryLabel,
    // 状态
    loaded,
    loading,
    // 方法
    loadConfig,
  }
})
