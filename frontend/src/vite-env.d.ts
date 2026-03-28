/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_NAME?: string
  readonly VITE_APP_SHORT_NAME?: string
  readonly VITE_APP_NAME_EN?: string
  readonly VITE_APP_SHORT_NAME_EN?: string
  readonly VITE_PORTAL_TITLE?: string
  readonly VITE_APP_SLOGAN?: string
  readonly VITE_LAW_FIRM_NAME?: string
  readonly VITE_LAW_FIRM_WEBSITE?: string
  readonly VITE_LOGO_URL?: string
  readonly VITE_LOGO_COLLAPSED_URL?: string
  readonly VITE_ICP_LICENSE?: string
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare global {
  const global: typeof globalThis
}

export {}
