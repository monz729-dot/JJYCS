/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE: string
  readonly VITE_DEV_ACCOUNTS_INFO: string
  readonly VITE_SHOW_DEBUG_INFO: string
  readonly VITE_RECAPTCHA_SITE_KEY: string
  readonly VITE_GOOGLE_MAPS_API_KEY: string
  readonly VITE_PWA_NAME: string
  readonly VITE_PWA_SHORT_NAME: string
  readonly VITE_PWA_DESCRIPTION: string
  readonly VITE_ENABLE_2FA: string
  readonly VITE_ENABLE_QR_SCANNER: string
  readonly VITE_ENABLE_CBM_CALCULATOR: string
  readonly VITE_ENABLE_MOCK_API: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}