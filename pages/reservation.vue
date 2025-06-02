<template>
  <div class="min-h-screen bg-black py-8">
    <div class="max-w-3xl mx-auto px-4">
      <!-- 頁面標題區 -->
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-100">書籍預約</h1>
        <p class="mt-2 text-gray-400">請填寫以下預約信息</p>
      </div>

      <div v-if="!book" class="bg-gray-800 rounded-lg shadow-xl p-6 text-center border border-gray-700">
        <div class="p-6">
          <div class="mx-auto w-16 h-16 bg-gray-700 rounded-full flex items-center justify-center mb-4">
            <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p class="text-gray-400">找不到該書籍資訊，請從書籍頁面重新進入。</p>
        </div>
      </div>

      <div v-else class="bg-gray-800 rounded-lg shadow-xl overflow-hidden border border-gray-700">
        <!-- 書籍信息區 -->
        <div class="bg-gradient-to-r from-gray-800 to-gray-900 p-6 border-b border-gray-700">
          <h2 class="text-xl font-semibold mb-2 text-gray-300">預約書籍</h2>
          <p class="text-2xl font-bold mb-1 text-white">{{ book.title }}</p>
          <p class="text-sm text-gray-400">作者：{{ book.author }}</p>
        </div>

        <!-- 預約表單區 -->
        <div class="p-6 space-y-6">
          <!-- 取書時間 -->
          <div class="space-y-2">
            <label class="flex items-center text-sm font-medium text-gray-300">
              <svg class="w-5 h-5 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              取書時間
            </label>
            <input 
              type="datetime-local" 
              v-model="form.time" 
              class="w-full bg-white border-gray-600 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-500 text-gray-900 placeholder-gray-500" 
            />
          </div>

          <!-- 取書地點 -->
          <div class="space-y-2">
            <label class="flex items-center text-sm font-medium text-gray-300">
              <svg class="w-5 h-5 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              取書地點
            </label>
            <select 
              v-model="form.location" 
              class="w-full bg-white border-gray-600 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-500 text-gray-900 [&>option]:text-gray-900 [&>option]:bg-white"
            >
              <option disabled value="">請選擇取書地點</option>
              <option>一樓服務台</option>
              <option>二樓自助區</option>
            </select>
          </div>

          <!-- 取書方式 -->
          <div class="space-y-2">
            <label class="flex items-center text-sm font-medium text-gray-300">
              <svg class="w-5 h-5 mr-2 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              取書方式
            </label>
            <select 
              v-model="form.method" 
              class="w-full bg-white border-gray-600 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-gray-500 text-gray-900 [&>option]:text-gray-900 [&>option]:bg-white"
            >
              <option disabled value="">請選擇取書方式</option>
              <option>親自取書</option>
              <option>他人代領</option>
            </select>
          </div>

          <!-- 預約須知 -->
          <div class="bg-gray-700/50 rounded-lg p-4 border border-gray-600">
            <h3 class="text-sm font-medium text-gray-200 mb-2">預約須知</h3>
            <ul class="text-sm text-gray-400 space-y-1 list-disc list-inside">
              <li>請在預約時間內完成取書</li>
              <li>超過預約時間未取書將自動取消預約</li>
              <li>每人最多可預約 {{ maxReservation }} 本書</li>
              <li>您目前已預約 {{ userReservedCount }} 本書</li>
            </ul>
          </div>

          <!-- 按鈕區域 -->
          <div class="flex justify-end space-x-4 pt-4 border-t border-gray-700">
            <button 
              type="button"
              @click="router.back()"
              class="border border-gray-600 text-gray-300 hover:bg-gray-700 rounded px-4 py-2"
            >
              返回
            </button>
            <button 
              type="button"
              @click="handleReserve"
              class="bg-gradient-to-r from-gray-700 to-gray-800 text-white hover:from-gray-600 hover:to-gray-700 border border-gray-600 rounded px-4 py-2"
            >
              確認預約
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ref, computed } from 'vue'
import { useHead } from '#imports'

// 設置頁面標題
useHead({
  title: '書籍預約'
})

// 模擬登入與預約數量限制
const isLoggedIn = true
const userReservedCount = ref(2)
const maxReservation = 3

const route = useRoute()
const router = useRouter()

const book = computed(() => {
  if (!route.query.bookId) return null
  
  return {
    id: route.query.bookId,
    title: route.query.title,
    author: route.query.author
  }
})

const form = ref({
  time: '',
  location: '',
  method: '',
})

function handleReserve() {
  if (!isLoggedIn) {
    alert('請先登入才能預約')
    router.push('/login')
    return
  }

  if (userReservedCount.value >= maxReservation) {
    alert('您已達到每人最多預約 3 本書的限制')
    return
  }

  if (!form.value.time || !form.value.location || !form.value.method) {
    alert('請完整填寫所有欄位')
    return
  }

  // 模擬預約成功
  userReservedCount.value++
  alert(`成功預約《${book.value?.title}》`)
  router.push('/history')
}
</script>

<style>
/* 添加漸變背景動畫 */
.bg-gradient-to-r {
  background-size: 200% 200%;
  animation: gradient 15s ease infinite;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 自定義滾動條 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background-color: rgb(31, 41, 55); /* bg-gray-800 */
}

::-webkit-scrollbar-thumb {
  background-color: rgb(75, 85, 99); /* bg-gray-600 */
  border-radius: 9999px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: rgb(107, 114, 128); /* bg-gray-500 */
}
</style>
  