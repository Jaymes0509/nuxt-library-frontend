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

      <div class="top-links">
        <ul>
          <li v-for="(link, index) in links" :key="link.href" :title="link.label">
            <a v-if="link.label !== '無障礙專區'" :href="link.href">{{ link.label }}</a>
            <button v-else class="a11y-toggle" @click="toggleAccessibility" aria-label="切換視障友善模式">
              {{ isAccessible ? '標準模式' : '無障礙模式' }}
            </button>
            <span v-if="index !== links.length - 1" class="separator">＊</span>
          </li>
        </ul>
        <!-- <div class="icons">
        <img src="/yt.png" alt="YouTube" />
        <img src="/line.png" alt="Line" />
        <img src="/ig.png" alt="Instagram" />
        <img src="/fb.png" alt="Facebook" />
      </div> -->
        <!-- 語言切換選單 -->
        <!-- <div>
          <button @click="toggleDropdown" class="lang-btn" title="語言">🌐 語言</button>
          <ul v-if="showDropdown" class="lang-menu">
            <li v-for="lang in languages" :key="lang.code" :title="lang.label">
              <a href="#" class="dropdown-item" @click.prevent="selectLang(lang.code)">
                {{ lang.label }}
              </a>
            </li>
          </ul>
        </div> -->

        <div class="search">
          <input type="text" placeholder="站內搜尋" />
          <span>🔍</span>
        </div>
      </div>
    </div>
  </template>

<script setup>
import { ref, onMounted } from 'vue'
// import { useI18n } from 'vue-i18n'

// const { locale, t } = useI18n()
// const showDropdown = ref(false)

// const toggleDropdown = () => {
//   showDropdown.value = !showDropdown.value
// }

// const selectLang = (code) => {
//   locale.value = code
//   showDropdown.value = false
// }

// const languages = [
//   { code: 'zh-tw', label: '繁體中文' },
//   { code: 'en', label: 'English' },
//   { code: 'ja', label: '日本語' }
// ]

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


let links = [
  { label: '首頁', href: '/' },
  { label: '網站導覽', href: '' },
  { label: '開放時間', href: '/opening-hours' },
  { label: '意見信箱', href: '/feedback' },
  { label: '無障礙專區', href: '' }]

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
  flex-wrap: wrap;
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
  font-size: 1rem;
}

.top-links a:hover {
  color: skyblue;
}

.a11y-toggle {
  white-space: nowrap;
  font-weight: bold;
  font-size: large;
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
  /* margin: 0.25rem; */
  /* background-color: red; */
  color: #999;
  margin-left: 0.7rem;
}

/* .icons img {
  height: 24px;
  margin-left: 0.5rem;
} */

/* .lang-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  padding: 4px 8px;
}

.lang-menu {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 6px;
  flex-direction: column;
  background: white;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  z-index: 20;
  min-width: 95px;
  list-style: none;
  padding: 0;
}

.dropdown-item {
  display: grid;
  padding: 8px 12px;
  font-size: 0.85rem;
  text-decoration: none;
}

.dropdown-item:hover {
  background-color: skyblue;
}

.dropdown-item:active {
  background-color: tomato;
} */

.search {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #333;
  margin-left: 1rem;
}

.search input {
  border: none;
  outline: none;
  background: transparent;
}

.search span {
  margin-left: 0.5rem;
}

.member-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-shrink: 0;
  /* 防止被壓縮消失 */
  min-width: 0;
  max-width: 100%;
  flex-wrap: wrap;
}

.member-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.logout-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 4px 12px;
  font-size: 0.9rem;
  cursor: pointer;
  white-space: nowrap;
}

.logout-btn:hover {
  background: #1d4ed8;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
  min-width: 0;
  max-width: 100%;
}

.user-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

@media (max-width: 1024px) {
  .top-bar {
    flex-wrap: wrap;
    row-gap: 0.5rem;
  }

  .member-bar {
    margin-top: 0.5rem;
    flex-direction: row;
    align-items: center;
    justify-content: flex-end;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .top-bar {
    flex-direction: column;
    align-items: stretch;
    padding: 0.5rem 0.5rem;
  }

  .logo-link {
    justify-content: center;
    margin-bottom: 0.5rem;
  }

  .logo {
    margin-left: 0;
    flex-direction: column;
    align-items: center;
  }

  .top-links {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }

  .top-links ul {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }

  .search {
    margin-left: 0;
    margin-top: 0.5rem;
  }

  .member-bar {
    margin-top: 0.5rem;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 0.5rem;
    width: 100%;
  }
}
</style>