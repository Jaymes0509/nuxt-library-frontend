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
              <h1>{{ title }}</h1>
              <p>{{ subtitle }}</p>
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
          <li v-for="(link, index) in links" :key="link.href || link.label" :title="link.label">
            <template v-if="link.label !== '無障礙專區'">
              <NuxtLink :to="link.href" class="top-link">
                {{ link.label }}
              </NuxtLink>
            </template>
            <template v-else>
              <button class="a11y-toggle" @click="toggleAccessibility" aria-label="切換視障友善模式">
                {{ isAccessible ? '標準模式' : '無障礙模式' }}
              </button>
            </template>

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
          <button @click="toggleDropdown" class="lang-btn" title="語言">🌐 語言</button>
          <ul v-if="showDropdown" class="lang-menu">
            <li v-for="lang in languages" :key="lang.code" :title="lang.label">
              <a href="#" class="dropdown-item" @click.prevent="selectLang(lang.code)">
                {{ lang.label }}
              </a>
            </li>
          </ul>
        </div>

        <div class="search">
          <input type="text" v-model="query" placeholder="站內搜尋" class="search-input" @keyup.enter="submitSearch" />
          <button class="search-icon" @click="submitSearch">
            🔍
          </button>
        </div>

        <!-- 登入狀態顯示器 -->
        <div class="login-status">
          <div v-if="isLoggedIn" class="user-info" @click="toggleUserMenu" :aria-expanded="showUserMenu">
            <img src="/images/zheng.jpg" alt="會員頭像" class="user-avatar-img" />
            <span class="user-name">{{ userInfo.name || '會員' }}</span>
            <span class="user-menu-arrow">{{ showUserMenu ? '▲' : '▼' }}</span>
            <div v-if="showUserMenu" class="user-menu" @click.stop>
              <div class="user-menu-header">
                <span class="user-role">{{ userInfo.role === 'admin' ? '管理員' : '一般會員' }}</span>
              </div>
              <button @click="logout" class="user-menu-item">
                🚪 登出
              </button>
            </div>
          </div>
          <NuxtLink v-else to="/login" class="login-btn">
            🔑 登入
          </NuxtLink>
        </div>
      </div>

      <!-- <div class="search">
      <input type="text" v-model="query" placeholder="站內搜尋" class="search-input" @keyup.enter="submitSearch" />
      <button class="search-icon" @click="submitSearch">
        🔍
      </button>
    </div> -->

      <!-- <NuxtLink to="/login" class="login-btn">登入</NuxtLink> -->
    </div>
  </template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const showDropdown = ref(false)
const isMenuOpen = ref(false)
const showUserMenu = ref(false)

// 登入狀態相關
const isLoggedIn = ref(false)
const userInfo = ref({})

// 自動登出
const idleTimer = ref(null)
const IDLE_TIMEOUT = 60 * 60 * 1000 // 1 小時

const resetIdleTimer = () => {
  if (idleTimer.value) clearTimeout(idleTimer.value)

  if (isLoggedIn.value) {
    idleTimer.value = setTimeout(() => {
      alert('您已閒置超過 1 小時，系統已自動將您登出。')
      logout()
    }, IDLE_TIMEOUT)
  }
}

const activityEvents = ['mousemove', 'mousedown', 'keypress', 'scroll', 'touchstart']

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
  // 關閉用戶選單
  showUserMenu.value = false
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
  // 關閉語言選單
  showDropdown.value = false
}

// 點擊外部區域關閉選單
const closeMenus = (event) => {
  const target = event.target
  const isUserInfo = target.closest('.user-info')
  const isLangMenu = target.closest('.lang-btn, .lang-menu')

  if (!isUserInfo) {
    showUserMenu.value = false
  }

  if (!isLangMenu) {
    showDropdown.value = false
  }
}

// 檢查登入狀態
const checkLoginStatus = () => {
  const storedUser = localStorage.getItem('user')
  const jwtToken = localStorage.getItem('jwt_token')
  const authToken = localStorage.getItem('authToken')

  if (storedUser) {
    try {
      userInfo.value = JSON.parse(storedUser)
      isLoggedIn.value = true
      console.log('✅ 用戶已登入：', userInfo.value)
    } catch (e) {
      console.error('❌ 解析用戶資訊失敗：', e)
      isLoggedIn.value = false
      userInfo.value = {}
    }
  } else if (jwtToken || authToken) {
    // 如果有 token 但沒有用戶資訊，也視為已登入
    isLoggedIn.value = true
    userInfo.value = { name: '會員', role: 'user' }
    console.log('✅ 檢測到登入 token')
  } else {
    isLoggedIn.value = false
    userInfo.value = {}
    console.log('❌ 用戶未登入')
  }
}

// 登出功能
const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('jwt_token')
  localStorage.removeItem('authToken')
  isLoggedIn.value = false
  userInfo.value = {}
  showUserMenu.value = false
  console.log('✅ 用戶已登出')

  // 清除計時器
  if (idleTimer.value) clearTimeout(idleTimer.value)

  // 重新導向到首頁
  router.push('/')
}

const languages = [
  { code: 'zh-tw', label: '繁體中文' },
  { code: 'en', label: 'English' },
  { code: 'ja', label: '日本語' }
]

function selectLang(code) {
  // 暫時移除 i18n 功能
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

  // 檢查登入狀態
  checkLoginStatus()

  // 監聽 localStorage 變化
  window.addEventListener('storage', checkLoginStatus)

  // 監聽點擊事件，關閉選單
  document.addEventListener('click', closeMenus)

  // 自動登出設定
  activityEvents.forEach(event => {
    window.addEventListener(event, resetIdleTimer, true)
  })
  resetIdleTimer()
})

onUnmounted(() => {
  window.removeEventListener('storage', checkLoginStatus)
  document.removeEventListener('click', closeMenus)

  // 自動登出清理
  if (idleTimer.value) clearTimeout(idleTimer.value)
  activityEvents.forEach(event => {
    window.removeEventListener(event, resetIdleTimer, true)
  })
})

// 監聽登入狀態變化以啟動/停止計時器
watch(isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    resetIdleTimer()
  } else {
    if (idleTimer.value) {
      clearTimeout(idleTimer.value)
    }
  }
})

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value
}

const closeMenu = () => {
  isMenuOpen.value = false
}

let links = [
  { label: '首頁', href: '/' },
  { label: '網站導覽', href: '' },
  { label: '開放時間', href: '/opening-hours' },
  { label: '意見信箱', href: '/feedback' },
  { label: '無障礙專區', href: '' }]

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
  border-bottom: 1px solid #444;
  padding: 4px;
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

  /* 手機版登入狀態樣式 */
  .user-info {
    padding: 6px 10px;
  }

  .user-name {
    font-size: 0.8rem;
  }

  .login-btn {
    padding: 6px 12px;
    font-size: 0.8rem;
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

  /* 超小螢幕登入狀態樣式 */
  .user-info {
    padding: 4px 8px;
  }

  .user-name {
    font-size: 0.75rem;
  }

  .login-btn {
    padding: 4px 8px;
    font-size: 0.75rem;
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

  .search-icon {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 1.1rem;
  }
}

/* 登入狀態顯示器樣式 */
.login-status {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 10px 4px 4px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info:hover {
  background: rgba(255, 255, 255, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar-img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #374151;
  white-space: nowrap;
}

.user-menu-arrow {
  font-size: 0.8rem;
  color: #6b7280;
  transition: transform 0.2s;
}

.user-info[aria-expanded="true"] .user-menu-arrow {
  transform: rotate(180deg);
}

.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 120px;
  z-index: 1000;
  overflow: hidden;
}

.user-menu-header {
  padding: 8px 12px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.user-role {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 500;
}

.user-menu-item {
  width: 100%;
  padding: 8px 12px;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 0.9rem;
  color: #374151;
  transition: background 0.2s;
}

.user-menu-item:hover {
  background: #f3f4f6;
}

.login-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #2563eb;
  color: white;
  text-decoration: none;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.2s ease;
  border: 1px solid #2563eb;
}

.login-btn:hover {
  background: #1d4ed8;
  border-color: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);
}
</style>