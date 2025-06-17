  <template>
    <div class="top-bar">
      <slot name="logo">
        <NuxtLink to="/" class="logo-link" aria-label="回到首頁">
          <!-- 本動畫來源：https://www.aigei.com/s?type=gif_moving_graph&q=看書 -->
          <div class="cat"><img src="/public/images/book-reading.gif"></div>
          <!-- 預設 logo -->
          <div class="logo">
            <img src="/public/images/libraryLogo.png" alt="Logo" />
            <div class="title">
              <h1>{{ $t('header.title') }}</h1>
              <p>{{ $t('header.subtitle') }}</p>
            </div>
          </div>
        </NuxtLink>
      </slot>

      <!-- 漢堡選單按鈕 -->
      <button class="menu-toggle" @click="toggleMenu" aria-label="選單">
        <span></span>
        <span></span>
        <span></span>
      </button>

      <!-- 遮罩，選單展開時顯示，點擊可關閉選單 -->
      <div v-if="isMenuOpen" class="menu-backdrop" @click="closeMenu"></div>

      <div class="top-links" :class="{ 'menu-open': isMenuOpen }">
        <ul>

          <li v-for="(link, index) in links" :key="link.href" :title="$t(link.key)">
            <a v-if="link.label !== '無障礙專區'" :href="link.href">{{ $t(link.key) }}</a>
            <button v-else class="a11y-toggle" @click="toggleAccessibility" aria-label="切換視障友善模式">
              {{ isAccessible ? $t('header.normalMode') : $t('header.accessibleMode') }}
            </button>

            <span v-if="index !== links.length - 1" class="separator">＊</span>
          </li>
        </ul>
      </div>

      <!-- <div class="icons">
        <img src="/yt.png" alt="YouTube" />
        <img src="/line.png" alt="Line" />
        <img src="/ig.png" alt="Instagram" />
        <img src="/fb.png" alt="Facebook" />
      </div> -->

      <!-- 語言切換選單 -->
      <div class="top-right">
        <div>
          <button @click="toggleDropdown" class="lang-btn" title="語言">🌐 {{ $t('header.language') }}</button>
          <ul v-if="showDropdown" class="lang-menu">
            <li v-for="lang in languages" :key="lang.code" :title="lang.label">
              <a href="#" class="dropdown-item" @click.prevent="selectLang(lang.code)">
                {{ lang.label }}
              </a>
            </li>
          </ul>
        </div>

        <div class="search">
          <input type="text" v-model="query" :placeholder="$t('header.search')" class="search-input"
            @keyup.enter="submitSearch" />
          <button class="search-icon" @click="submitSearch">
            🔍
          </button>
        </div>
        <!-- <NuxtLink to="/login" class="login-btn">{{ $t('login') }}</NuxtLink> -->
      </div>

    </div>

    <!-- <div class="search">
      <input type="text" v-model="query" placeholder="站內搜尋" class="search-input" @keyup.enter="submitSearch" />
      <button class="search-icon" @click="submitSearch">
        🔍
      </button>
    </div> -->

    <!-- <NuxtLink to="/login" class="login-btn">登入</NuxtLink> -->
  </template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { useI18n } from 'vue-i18n'


const { locale, t } = useI18n()
const showDropdown = ref(false)
const isMenuOpen = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const languages = [
  { code: 'zh-tw', label: '繁體中文' },
  { code: 'en', label: 'English' },
  { code: 'ja', label: '日本語' }
]

function selectLang(code) {
  locale.value = code
  showDropdown.value = false
}

defineProps({
  title: {
    type: String,
    default: '圖書館'
  },
  subtitle: {
    type: String,
    default: 'LIBRARY'
  }
})

const isAccessible = ref(false)

const toggleAccessibility = () => {
  isAccessible.value = !isAccessible.value
  const html = document.documentElement

  if (isAccessible.value) {
    html.classList.add('accessible-mode')
    localStorage.setItem('accessibleMode', 'true')
  } else {
    html.classList.remove('accessible-mode')
    localStorage.removeItem('accessibleMode')
  }
}

onMounted(() => {
  if (localStorage.getItem('accessibleMode') === 'true') {
    document.documentElement.classList.add('accessible-mode')
    isAccessible.value = true
  }
})

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

let links = [

  { label: '首頁', href: '/', key: 'header.home' },
  { label: '網站導覽', href: '', key: 'header.sitemap' },
  { label: '開放時間', href: '/opening-hours', key: 'header.openingHours' },
  { label: '意見信箱', href: '/feedback', key: 'header.feedback' },
  { label: '無障礙專區', href: '', key: 'header.accessible' }
]
// async function submitSearch() {
//   if (!query.value.trim()) return
//   const { data, error } = await useFetch(`/api/search?q=${encodeURIComponent(query.value)}`)

//   if (error.value) {
//     console.error('搜尋失敗:', error.value)
//   } else {
//     results.value = data.value // 將資料儲存顯示
//   }
// }
const query = ref('')
const router = useRouter()

const submitSearch = () => {
  if (query.value.trim()) {
    router.push(`/search?q=${encodeURIComponent(query.value.trim())}`)
  }
}
</script>

<style scoped>
.header {
  background: #f8f8f8;
  border-bottom: 1px solid #ccc;
  font-family: Arial, sans-serif;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  padding: 0.2rem 1rem;
  align-items: center;
}

.logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}

.cat {
  position: absolute;
  /* 讓它脫離正常排版，可以自由定位 */
  padding: 10px 0;
}

.cat img {
  width: 100px;
  /* 可以依需要調整大小 */
  height: auto;
}

.logo {
  margin-left: 10rem;
  display: flex;
  align-items: center;
}

.logo img {
  height: 100px;
  margin-right: 1rem;
}

.title h1 {
  margin: 0;
  font-size: 2rem;
  font-family: "Hachi Maru Pop", cursive;
}

.title p {
  margin: 0;
  font-size: 1rem;
  font-family: Verdana, Geneva, Tahoma, sans-serif;
}

.top-links {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-left: 10rem;
}

.top-links ul {
  display: flex;
  list-style: none;
  gap: 1rem;
  margin: 0;
  padding: 0;
}

.top-links a {
  text-decoration: none;
  color: #333;
  font-size: 1.25rem;
}

.top-links a:hover {
  color: skyblue;
}

.a11y-toggle {
  white-space: nowrap;
  font-weight: bold;
  font-size: 1.25rem;
  color: white;
  background-color: #111;
  border-radius: 5rem;
}

.a11y-toggle:hover {
  color: yellow;
  /* 黃色高對比 */
}

.a11y-toggle:focus {
  outline: 2px solid red;
  outline-offset: 2px;
}

.separator {
  color: #999;
  margin-left: 0.7rem;
  font-size: 1.25rem;
}

.lang-btn {
  /* position: relative; */
  /* display: inline-block; */
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.25rem;
  padding: 4px 8px;
}

.lang-menu {
  list-style: none;
  position: absolute;
  margin-top: 6px;
  /* left: 70%;
   */
  transform: translateX(-10%);
  flex-direction: column;
  background: white;
  border: 1px solid #ccc;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  z-index: 20;
  min-width: 95px;
  padding: 0;
}

.lang-btn:hover {
  background-color: lightgray;
  border-radius: 3rem;
}

.dropdown-item {
  display: block;
  /* width: 100%; */
  text-align: center;
  left: 0;
  /* ✅ 文字置中 */
  /* margin: 0 auto; */
  /* ✅ 居中用於防止預設 left-align */
  color: black;
  /* 依你的設計調整顏色 */
  padding: 8px 12px;
  font-size: 1.25rem;
  text-decoration: none;
  border: none;
}

.dropdown-item:hover {
  background-color: skyblue;
}

.dropdown-item:active {
  background-color: tomato;
}

.search {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #333;
  margin-left: 1rem;
  width: 150px;

}

.search-input {
  border: none;
  outline: none;
  font-size: 16px;
  flex: 1;
  background: transparent;
  font-size: 1.25rem;
  width: 100%;
}


.search span {
  margin-left: 0.5rem;
  font-size: 1.25rem;
}

.login-btn {
  font-size: 1.25rem;
}

.top-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: nowrap;
  /* 防止換行 */
}

/* 響應式設計 */
@media screen and (max-width: 1199px) {
  .logo {
    margin-left: 8rem;
  }

  .logo img {
    height: 80px;
  }

  .title h1 {
    font-size: 1.8rem;
  }

  .title p {
    font-size: 0.9rem;
  }

  .cat img {
    width: 80px;
  }
}

@media screen and (max-width: 991px) {
  .logo {
    margin-left: 6rem;
  }

  .logo img {
    height: 70px;
  }

  .title h1 {
    font-size: 1.6rem;
  }

  .cat img {
    width: 70px;
  }

  .search {
    width: 180px;
  }
}

@media screen and (max-width: 767px) {
  .menu-toggle {
    display: flex !important;
  }

  .top-links {
    position: fixed;
    top: 0;
    right: -100%;
    width: 30%;
    max-width: 100px;
    height: 100vh;
    background: white;
    flex-direction: column;
    padding: 80px 20px 20px;
    transition: right 0.3s ease;
    box-shadow: -2px 0 5px rgba(0, 0, 0, 0.1);
    z-index: 99;
    align-items: center;
  }

  .top-links.menu-open {
    right: 0;
  }

  .top-links ul {
    width: 100%;
    margin-bottom: 1rem;
    flex-direction: column;
    text-align: center;
    align-items: center;
    padding: 0;
  }

  .top-links li {
    width: 100%;
    padding: 10px 0;
  }

  .top-right {
    flex-direction: column;
    width: 100%;
    gap: 1.2rem;
    margin-top: 1rem;
    align-items: center;
    text-align: center;
  }

  .search {
    width: 50%;
    margin-left: 0;
  }

  .logo {
    margin-left: 4rem;
  }

  .logo img {
    height: 60px;
  }

  .title h1 {
    font-size: 1.4rem;
  }

  .title p {
    font-size: 0.8rem;
  }

  .cat img {
    width: 60px;
  }

  .lang-menu {
    position: static;
    margin-top: 0.5rem;
    width: 100%;
  }

  .dropdown-item {
    padding: 12px;
  }

  .separator {
    display: none !important;
  }
}

@media screen and (max-width: 480px) {
  .logo {
    margin-left: 3rem;
  }

  .logo img {
    height: 50px;
  }

  .title h1 {
    font-size: 1.2rem;
  }

  .title p {
    font-size: 0.7rem;
  }

  .cat img {
    width: 50px;
  }

  .top-bar {
    padding: 0.2rem 0.5rem;
  }

  .top-right {
    gap: 0.5rem;
    /* 在小螢幕上減少間距 */
  }

  .search {
    width: 120px;
    /* 在小螢幕上進一步縮小搜尋框 */
  }

  .lang-btn,
  .login-btn {
    padding: 4px 8px;
    /* 調整按鈕內邊距 */
    font-size: 1rem;
    /* 稍微縮小字體 */
  }
}

.menu-toggle {
  display: none !important;
  background: red !important;
  border: 2px solid black !important;
  width: 48px !important;
  height: 48px !important;
  z-index: 9999 !important;
}

.menu-toggle span {
  background: #fff !important;
  height: 6px !important;
  margin: 6px 0 !important;
  border-radius: 3px !important;
}

.menu-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.2);
  z-index: 98;
  display: block;
}

/* 一定要放在最後 */
@media screen and (max-width: 767px) {
  .menu-toggle {
    display: flex !important;
  }

}
</style>