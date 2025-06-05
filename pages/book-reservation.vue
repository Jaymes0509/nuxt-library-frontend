<template>
  <div class="reservation-bg">
    <div class="reservation-container">
      <!-- 頁面標題區 -->
      <div class="reservation-title-area">
        <h1 class="reservation-title">我要預約</h1>
        <p class="reservation-subtitle">請填寫以下預約信息</p>
      </div>

      <div v-if="!book" class="reservation-notfound">
        <div class="reservation-notfound-inner">
          <div class="reservation-notfound-icon">
            <svg class="reservation-notfound-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <p class="reservation-notfound-text">找不到該書籍資訊，請從書籍頁面重新進入。</p>
        </div>
      </div>

      <div v-else class="reservation-card">
        <!-- 書籍信息區 -->
        <div class="reservation-bookinfo">
          <h2 class="reservation-bookinfo-title">預約書籍</h2>
          <p class="reservation-bookinfo-book">{{ book.title }}</p>
          <p class="reservation-bookinfo-author">作者：{{ book.author }}</p>
        </div>

        <!-- 預約表單區 -->
        <div class="reservation-form">
          <!-- 取書時間 -->
          <div class="reservation-form-group">
            <label class="reservation-label">
              <svg class="reservation-label-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              取書時間
            </label>
            <input 
              type="datetime-local" 
              v-model="form.time" 
              class="reservation-input" 
            />
          </div>

          <!-- 取書地點 -->
          <div class="reservation-form-group">
            <label class="reservation-label">
              <svg class="reservation-label-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              取書地點
            </label>
            <select 
              v-model="form.location" 
              class="reservation-input"
            >
              <option disabled value="">請選擇取書地點</option>
              <option>一樓服務台</option>
              <option>二樓自助區</option>
            </select>
          </div>

          <!-- 取書方式 -->
          <div class="reservation-form-group">
            <label class="reservation-label">
              <svg class="reservation-label-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              取書方式
            </label>
            <select 
              v-model="form.method" 
              class="reservation-input"
            >
              <option disabled value="">請選擇取書方式</option>
              <option>親自取書</option>
              <option>他人代領</option>
            </select>
          </div>

          <!-- 預約須知 -->
          <div class="reservation-notice">
            <h3 class="reservation-notice-title">預約須知</h3>
            <ul class="reservation-notice-list">
              <li>請在預約時間內完成取書</li>
              <li>超過預約時間未取書將自動取消預約</li>
              <li>每人最多可預約 {{ maxReservation }} 本書</li>
              <li>您目前已預約 {{ userReservedCount }} 本書</li>
            </ul>
          </div>

          <!-- 按鈕區域 -->
          <div class="reservation-btn-area">
            <button 
              type="button"
              @click="router.back()"
              class="reservation-btn reservation-btn-back"
            >
              返回
            </button>
            <button 
              type="button"
              @click="handleReserve"
              class="reservation-btn reservation-btn-confirm"
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
  router.push({
    path: '/reservation',
    query: {
      bookId: book.value?.id,
      title: book.value?.title,
      author: book.value?.author
    }
  })
}
</script>

<style>
.reservation-bg {
  background: #fff;
  padding: 32px 0 100px 0;
}
.reservation-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 0 16px;
}
.reservation-title-area {
  text-align: center;
  margin-bottom: 32px;
}
.reservation-title {
  font-size: 2rem;
  font-weight: bold;
  color: #18181b;
}
.reservation-subtitle {
  margin-top: 8px;
  color: #4b5563;
}
.reservation-notfound {
  background: #f3f4f6;
  border-radius: 12px;
  box-shadow: 0 2px 8px #0001;
  padding: 24px;
  text-align: center;
  border: 1px solid #e5e7eb;
}
.reservation-notfound-inner {
  padding: 24px;
}
.reservation-notfound-icon {
  width: 64px;
  height: 64px;
  background: #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px auto;
}
.reservation-notfound-svg {
  width: 32px;
  height: 32px;
  color: #6b7280;
}
.reservation-notfound-text {
  color: #6b7280;
}
.reservation-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px #0001;
  border: 1px solid #e5e7eb;
}
.reservation-bookinfo {
  background: #f3f4f6;
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}
.reservation-bookinfo-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 8px;
}
.reservation-bookinfo-book {
  font-size: 1.5rem;
  font-weight: bold;
  color: #18181b;
  margin-bottom: 4px;
}
.reservation-bookinfo-author {
  font-size: 0.95rem;
  color: #4b5563;
}
.reservation-form {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.reservation-form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.reservation-label {
  display: flex;
  align-items: center;
  font-size: 1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 4px;
}
.reservation-label-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
  color: #6b7280;
}
.reservation-input {
  width: 100%;
  background: #fff;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 1rem;
  color: #18181b;
  outline: none;
  box-sizing: border-box;
}
.reservation-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px #2563eb22;
}
.reservation-notice {
  background: #f3f4f6;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
}
.reservation-notice-title {
  font-size: 1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 8px;
}
.reservation-notice-list {
  color: #4b5563;
  font-size: 0.95rem;
  margin: 0;
  padding-left: 20px;
}
.reservation-notice-list li {
  margin-bottom: 4px;
}
.reservation-btn-area {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}
.reservation-btn {
  border: 1px solid #2563eb;
  border-radius: 6px;
  padding: 8px 20px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.reservation-btn-back {
  background: #fff;
  color: #2563eb;
}
.reservation-btn-back:hover {
  background: #f3f4f6;
}
.reservation-btn-confirm {
  background: #2563eb;
  color: #fff;
}
.reservation-btn-confirm:hover {
  background: #1d4ed8;
}
</style>
  