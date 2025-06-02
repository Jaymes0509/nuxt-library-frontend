<template>
  <section class="announcement">
    <h2>最新公告</h2>
    <ul>
      <li v-for="(a, i) in announcements" :key="i">{{ a }}</li>
    </ul>
  </section>
</template>

<script setup>
const announcements = [
  '6/1 系統維護公告',
  '5/25 開放假日借閱服務',
  '5/20 舉辦數位典藏講座'
]
</script>

<style scoped>
.announcement {
  padding: 1rem;
  background-color: #f9f9f9;
}
</style>
