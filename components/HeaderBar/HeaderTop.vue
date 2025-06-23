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
        <img src="/images/TSUNG-YAN.jpg" alt="選單" class="menu-toggle-img" />
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
        <div v-if="isMobile" class="mobile-extra">
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
            <button class="search-icon" @click="submitSearch">🔍</button>
          </div>
          <div class="login-status">
            <div v-if="isLoggedIn" class="user-info" @click="toggleUserMenu" :aria-expanded="showUserMenu">
              <img src="/images/zheng.jpg" alt="會員頭像" class="user-avatar-img" />
              <span class="user-name">{{ userInfo.name || '會員' }}</span>
              <span class="user-menu-arrow">{{ showUserMenu ? '▲' : '▼' }}</span>
              <div v-if="showUserMenu" class="user-menu" @click.stop>
                <div class="user-menu-header">
                  <span class="user-role">{{ userInfo.role === 'admin' ? '管理員' : '一般會員' }}</span>
                </div>
                <button @click="logout" class="user-menu-item">🚪 登出</button>
              </div>
            </div>
            <NuxtLink v-else to="/login" class="login-btn">🔑 登入</NuxtLink>
          </div>
        </div>
      </div>

      <!-- <div class="icons">
        <img src="/yt.png" alt="YouTube" />
        <img src="/line.png" alt="Line" />
        <img src="/ig.png" alt="Instagram" />
        <img src="/fb.png" alt="Facebook" />
      </div> -->
      <!-- 語言切換選單 -->
      <div v-if="!isMobile" class="top-right">
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
  console.log('showUserMenu:', showUserMenu.value)
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

const isMobile = ref(false)
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
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

  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('storage', checkLoginStatus)
  document.removeEventListener('click', closeMenus)

  // 自動登出清理
  if (idleTimer.value) clearTimeout(idleTimer.value)
  activityEvents.forEach(event => {
    window.removeEventListener(event, resetIdleTimer, true)
  })

  window.removeEventListener('resize', checkMobile)
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
  font-size: 1rem;
  width: 100%;
}

.search span {
  margin-left: 0.5rem;
  font-size: 1.25rem;
}

.login-btn,
.user-menu-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 4px 16px;
  font-size: 1rem;
  background: #fff;
  color: #222;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  font-weight: 500;
  text-decoration: none;
  outline: none;
  min-height: 36px;
  min-width: 36px;
  justify-content: center;
}

.login-btn:hover,
.user-menu-item:hover {
  background: #f3f4f6;
  color: #1976d2;
}

.top-right {
  margin-left: auto;
  justify-content: flex-end;
  display: flex;
  align-items: center;
  gap: 1rem;
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
    display: none !important;
  }

  .menu-toggle {
    display: flex !important;
    position: fixed;
    top: 16px;
    right: 16px;
    z-index: 10000;
    background: transparent;
    border: none;
    padding: 0;
    width: 40px;
    height: 40px;
    align-items: center;
    justify-content: center;
  }

  .menu-toggle-img {
    width: 32px;
    height: 32px;
    object-fit: cover;
    border-radius: 8px;
    display: block;
  }

  .top-links {
    position: fixed;
    top: 0;
    right: -100%;
    width: 220px;
    max-width: 90vw;
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
    display: none !important;
  }

  .search {
    width: 100%;
    margin: 0.5rem 0;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
  }

  .search-input {
    width: 80%;
    min-width: 0;
    font-size: 1rem;
  }

  .search-icon {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    margin-left: 0.5rem;
    font-size: 1.1rem;
    border-radius: 6px;
    border: 1px solid #d1d5db;
    background: #f3f4f6;
    transition: background 0.2s;
  }

  .search-icon:hover {
    background: #e0e7ef;
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

  .mobile-extra {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    margin-top: 1rem;
    gap: 2rem;
  }

  .mobile-extra>div {
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1.2rem;
  }

  .mobile-extra .search {
    width: 90%;
    margin: 0.5rem auto;
    align-items: center;
    justify-content: center;
    display: flex;
  }

  .mobile-extra .login-status {
    width: 100%;
    margin: 0.5rem 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1.2rem;
  }
}

@media screen and (min-width: 768px) and (max-width: 1240px) {
  .logo {
    margin-left: 1rem !important;
  }

  .top-links {
    margin-left: 0 !important;
    flex-shrink: 1 !important;
    min-width: 0 !important;
  }

  .top-bar {
    flex-wrap: nowrap !important;
  }

  .top-right {
    margin-left: auto !important;
    justify-content: flex-end !important;
    width: auto !important;
    display: flex !important;
    align-items: center !important;
    flex-shrink: 0 !important;
  }
}

@media screen and (min-width: 768px) {
  .mobile-extra {
    display: none !important;
  }
}

.menu-toggle-img,
.user-avatar-img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
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

.user-avatar-img {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  position: relative;
}

.user-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 140px;
  z-index: 1000;
  overflow: hidden;
  padding: 0;
}

.user-menu-header {
  padding: 10px 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  font-size: 0.95rem;
  color: #374151;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.user-role {
  font-size: 0.95rem;
  color: #6b7280;
  font-weight: 500;
}

.user-menu-item {
  width: 100%;
  padding: 6px 12px;
  background: #2563eb;
  border: none;
  text-align: center;
  cursor: pointer;
  font-size: 0.8rem;
  color: #fff;
  border-radius: 6px;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.user-menu-item:hover {
  background: #1d4ed8;
}
</style>