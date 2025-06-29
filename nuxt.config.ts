// https://nuxt.com/docs/api/configuration/nuxt-config

export default defineNuxtConfig({

  devtools: { enabled: false },

  ssr: false,

  modules: [
    '@nuxtjs/google-fonts',
  ],

  css: ['@/assets/css/main.css',
    'leaflet/dist/leaflet.css'
  ],

  components: true,

  googleFonts: {
    families: {
      'Hachi Maru Pop': true, // 這裡指定你要的 Google Font
    },
    display: 'swap'
  },

  runtimeConfig: {
    apiSecret: process.env.API_SECRET,
    public: {
      apiBase: process.env.API_BASE_URL || 'http://localhost:8080',
      appName: '圖書館預約系統'
    }
  },

  compatibilityDate: '2025-05-15',

  app: {
    head: {
      title: '圖書館預約系統',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { name: 'description', content: '圖書館書籍預約系統' }
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
      ]
    }
  },

  devServer: {
    port: 3000,
    host: 'localhost'

  }

})