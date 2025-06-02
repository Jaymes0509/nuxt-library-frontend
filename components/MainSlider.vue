<template>
  <!-- <div class="slider-container">
    <button class="arrow left" @click="prev">‹</button> -->

    <div class="slider">
      <img :src="images[current]" alt="slide image" />
    </div>

    <!-- <button class="arrow right" @click="next">›</button>
  </div> -->

  <div class="dots">
  <span
    v-for="(img, index) in images"
    :key="index"
    :class="{ active: index === current }"
    @click="current = index"
  />
</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const images = [
  'https://static.vecteezy.com/system/resources/thumbnails/000/525/908/small_2x/gde7_tf5t_180914.jpg',
  'https://static.vecteezy.com/system/resources/thumbnails/000/087/792/small_2x/playground-slide-vector.jpg',
  'https://cdn11.bigcommerce.com/s-tddaa/images/stencil/1280x1280/products/1898/8598/7_Slide_Retouched_902-315__72528.1469218489.jpg?c=2',
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
  max-width: 1500px;
  margin: 0 auto;
  padding: 20px 0;
}

.slider {
  width: 100%;
  /* height: 400px; */
  display: flex;
  justify-content: center;
  align-items: center;
}

.slider img {
  max-width: 100%;
  max-height: 300px;
  object-fit: fill;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
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
