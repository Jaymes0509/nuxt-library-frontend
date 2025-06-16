// https://nuxt.com/docs/api/configuration/nuxt-config

export default defineNuxtConfig({

  devtools: { enabled: true },

  ssr: false,

  modules: [
    // '@nuxtjs/tailwindcss',
    '@nuxtjs/google-fonts'
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
    public: {
      apiBase: '/api'
    }
  },

  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080/api', // 你的 Spring Boot 後端
        changeOrigin: true,
        prependPath: true
      }
    }
  },

  compatibilityDate: '2025-05-15',

})
