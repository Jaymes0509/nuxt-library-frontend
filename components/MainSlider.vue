<template>
  <div class="slider-container">

    <div class="slider">
      <img :src="images[current]" alt="slide image" />
    </div>
  </div>

  <div class="dots">
    <span v-for="(img, index) in images" :key="index" :class="{ active: index === current }" @click="current = index" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const images = [
  'https://images.unsplash.com/photo-1534774592507-488885376ad3?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
  'https://images.unsplash.com/photo-1600431521340-491eca880813?q=80&w=2069&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
  'https://images.unsplash.com/photo-1522211988038-6fcbb8c12c7e?q=80&w=2070&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
]

const current = ref(0)

// const next = () => {
//   current.value = (current.value + 1) % images.length
// }
// const prev = () => {
//   current.value = (current.value - 1 + images.length) % images.length
// }

onMounted(() => {
  setInterval(() => {
    current.value = (current.value + 1) % images.length
  }, 6000)
})

</script>

<style scoped>
.slider-container {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  height: 300px;
  /* ✅ 明確高度 */
  margin: 0 auto;
  padding: 20px 0;
}

.slider {
  flex: 1;
  /* ✅ 自動撐滿父層寬度 */
  width: 100vh;
  height: 100%;
  /* ✅ 撐滿容器高度 */
  display: flex;
  justify-content: center;
  align-items: center;
}

.slider img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 0 40px rgba(0, 0, 0, 0.3);
  /* box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); */
}

/* .arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  font-size: 2rem;
  padding: 8px 16px;
  cursor: pointer;
  z-index: 10;
  border-radius: 4px;
}

.arrow.left {
  left: 10px;
}

.arrow.right {
  right: 10px;
} */

.dots {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 12px;
}

.dots span {
  width: 12px;
  height: 12px;
  background-color: #ccc;
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.3s;
}

.dots span.active {
  background-color: #333;
}
</style>