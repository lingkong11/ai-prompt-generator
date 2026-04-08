import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import zhCN from '@/locale/zh-CN'
import zhTW from '@/locale/zh-TW'
import en from '@/locale/en'

export type Locale = 'zh-CN' | 'zh-TW' | 'en'
export type Theme = 'light' | 'dark' | 'purple'

const localeMap: Record<Locale, any> = { 'zh-CN': zhCN, 'zh-TW': zhTW, 'en': en }

const localeNames: Record<Locale, string> = {
  'zh-CN': '简体中文',
  'zh-TW': '繁體中文',
  'en': 'English',
}

const themeNames: Record<Theme, string> = {
  light: '☀️ Light',
  dark: '🌙 Dark',
  purple: '💜 Purple',
}

/**
 * Detect browser language from navigator.language.
 * Maps browser locale tags to supported locales.
 * Fallback: 'en'
 */
function detectBrowserLocale(): Locale {
  const nav = navigator.language || ''
  if (nav.startsWith('zh-TW') || nav.startsWith('zh-HK') || nav.startsWith('zh-Hant')) return 'zh-TW'
  if (nav.startsWith('zh')) return 'zh-CN'
  return 'en'
}

export const useAppStore = defineStore('app', () => {
  // Per-client persisted preference, auto-detect on first visit
  const savedLocale = localStorage.getItem('app_locale') as Locale | null
  const savedTheme = localStorage.getItem('app_theme') as Theme | null

  const locale = ref<Locale>(savedLocale || detectBrowserLocale())
  const theme = ref<Theme>(savedTheme || 'purple')

  const t = computed(() => localeMap[locale.value])

  const setLocale = (l: Locale) => {
    locale.value = l
    localStorage.setItem('app_locale', l)
    document.documentElement.setAttribute('lang', l)
  }

  const setTheme = (t: Theme) => {
    theme.value = t
    localStorage.setItem('app_theme', t)
    applyTheme(t)
  }

  const applyTheme = (t: Theme) => {
    const html = document.documentElement
    html.classList.remove('theme-light', 'theme-dark', 'theme-purple')
    html.classList.add(`theme-${t}`)
  }

  // Init (only runs once per session)
  setLocale(locale.value)
  applyTheme(theme.value)

  return { locale, theme, t, setLocale, setTheme, localeNames, themeNames }
})
