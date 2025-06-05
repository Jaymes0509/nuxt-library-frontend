<template>
  <div class="top-bar">
    <slot name="logo">
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
    </slot>

    <div class="top-links">
      <ul>
        <li v-for="(link, index) in links" :key="link.href" :title="link.label">
          <a :href="link.href">{{ link.label }}</a>
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
        <input type="text" placeholder="站內搜尋" />
        <span>🔍</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
// import { useI18n } from 'vue-i18n'

// const { locale, t } = useI18n()
const showDropdown = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

// const selectLang = (code) => {
//   locale.value = code
//   showDropdown.value = false
// }

const languages = [
  { code: 'zh-tw', label: '繁體中文' },
  { code: 'en', label: 'English' },
  { code: 'ja', label: '日本語' }
]
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
let links = [
<<<<<<< HEAD
  { label: '網站導覽', href: '/guide' },
  { label: '無障礙專區', href: '/accessibility' },
  { label: '開放時間', href: '/hours' },
  { label: '意見信箱', href: '/contact' },
  { label: 'EN', href: '/en' }]
=======
  { label: '首頁', href: '/' },
  { label: '網站導覽', href: '' },
  { label: '無障礙專區', href: '' },
  { label: '開放時間', href: '/opening-hours' },
  { label: '意見信箱', href: '' },]
>>>>>>> f17f4bba10f1c94f3d94df040354434b1a871b6d
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

.lang-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  padding: 4px 8px;
}

.lang-menu {
  position: absolute;
  margin-top: 6px;
  /* top: 100%; */
  flex-direction: column;
  background: white;
  border: 1px solid #ccc;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  z-index: 20;
  min-width: 95px;
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
}

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
</style>