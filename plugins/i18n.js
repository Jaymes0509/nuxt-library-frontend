// vue-i18n 註冊插件，讓 Nuxt 3 支援多國語系
import { createI18n } from 'vue-i18n'
import zhTW from '../locales/zh-tw'
import en from '../locales/en'
import ja from '../locales/ja'

export default defineNuxtPlugin(nuxtApp => {
  const i18n = createI18n({
    legacy: false,
    locale: 'zh-tw',
    fallbackLocale: 'zh-tw',
    messages: {
      'zh-tw': zhTW,
      en,
      ja
    }
  })
  nuxtApp.vueApp.use(i18n)
}) 