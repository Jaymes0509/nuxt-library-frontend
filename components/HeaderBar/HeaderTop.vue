<template>
  <div class="top-bar">
    <slot name="logo">
      <!-- 本動畫來源：https://www.aigei.com/s?type=gif_moving_graph&q=看書 -->
      <div class="cat"><img src="/public/images/book-reading.gif"></div>
      <!-- 預設 logo -->
      <div class="logo">
        <img src="/images/library-logo.png" alt="Logo" />
        <div class="title">
          <h1>{{ title }}</h1>
          <p>{{ subtitle }}</p>
        </div>
      </div>
    </slot>

    <div class="top-links">
      <ul>
        <li v-for="link in links" :key="link.href">
          <a :href="link.href">{{ link.label }}</a>
        </li>
      </ul>
      <!-- <div class="icons">
        <img src="/yt.png" alt="YouTube" />
        <img src="/line.png" alt="Line" />
        <img src="/ig.png" alt="Instagram" />
        <img src="/fb.png" alt="Facebook" />
      </div> -->
      <div class="search">
        <input type="text" placeholder="站內搜尋" />
        <span>🔍</span>
      </div>
    </div>
  </div>
</template>

<script setup>
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
  { label: '網站導覽', href: '/guide' },
  { label: '無障礙專區', href: '/accessibility' },
  { label: '開放時間', href: '/hours' },
  { label: '意見信箱', href: '/contact' },
  { label: 'EN', href: '/en' }]
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
  gap: 0.5rem;
  margin: 0;
  padding: 0;
}

.top-links a {
  text-decoration: none;
  color: #333;
  font-size: 0.85rem;
}

.icons img {
  height: 24px;
  margin-left: 0.5rem;
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