<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="result-bg">
        <div class="result-container">
          <!-- 成功訊息區 -->
          <div class="result-success-area">
            <div class="result-success-icon">
              <svg class="result-success-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h1 class="result-success-title">預約成功！</h1>
            <p class="result-success-subtitle">您的書籍已成功預約，請在指定時間內取書</p>
          </div>

          <!-- 預約詳情卡片 -->
          <div class="result-card">
            <!-- 書籍資訊 -->
            <div class="result-bookinfo">
              <h2 class="result-bookinfo-title">預約書籍資訊</h2>
              <div class="result-bookinfo-content">
                <p class="result-bookinfo-book">{{ book.title }}</p>
                <p class="result-bookinfo-author">作者：{{ book.author }}</p>
                <p class="result-bookinfo-isbn">ISBN：{{ book.isbn }}</p>
              </div>
            </div>

            <!-- 預約資訊 -->
            <div class="result-reservation-info">
              <h2 class="result-reservation-title">預約資訊</h2>
              <div class="result-reservation-content">
                <div class="result-info-item">
                  <span class="result-info-label">取書時間：</span>
                  <span class="result-info-value">{{ formatDateTime(form.time) }}</span>
                </div>
                <div class="result-info-item">
                  <span class="result-info-label">取書地點：</span>
                  <span class="result-info-value">{{ form.location }}</span>
                </div>
                <div class="result-info-item">
                  <span class="result-info-label">取書方式：</span>
                  <span class="result-info-value">{{ form.method }}</span>
                </div>
                <div class="result-info-item">
                  <span class="result-info-label">預約編號：</span>
                  <span class="result-info-value">{{ reservationId }}</span>
                </div>
              </div>
            </div>

            <!-- 注意事項 -->
            <div class="result-notice">
              <h3 class="result-notice-title">注意事項</h3>
              <ul class="result-notice-list">
                <li>請在預約時間內完成取書</li>
                <li>超過預約時間未取書將自動取消預約</li>
                <li>如需取消預約，請至「我的預約」頁面操作</li>
                <li>如有任何問題，請聯繫圖書館服務台</li>
              </ul>
            </div>

            <!-- 按鈕區域 -->
            <div class="result-btn-area">
              <button type="button" @click="goToReservationRecord" class="result-btn result-btn-record">
                查看預約記錄
              </button>
              <button type="button" @click="goToHome" class="result-btn result-btn-home">
                返回首頁
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useHead } from '#imports'

// 設置頁面標題
useHead({
  title: '預約成功'
})

const route = useRoute()
const router = useRouter()

// 從路由參數獲取書籍資訊
const book = computed(() => ({
  title: String(route.query.title || '未知書名'),
  author: String(route.query.author || '未知作者'),
  isbn: String(route.query.isbn || '未知ISBN')
}))

// 從路由參數獲取預約資訊
const form = computed(() => ({
  time: String(route.query.time || ''),
  location: String(route.query.location || ''),
  method: String(route.query.method || '')
}))

// 生成預約編號（實際應用中應該由後端生成）
const reservationId = computed(() => {
  const timestamp = new Date().getTime()
  const random = Math.floor(Math.random() * 1000)
  return `RES${timestamp}${random}`
})

// 格式化日期時間
function formatDateTime(dateTimeStr: string) {
  if (!dateTimeStr) return '未指定'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}

// 跳轉到預約記錄頁面
function goToReservationRecord() {
  router.push('/reservation-record')
}

// 返回首頁
function goToHome() {
  router.push('/')
}

// 檢查必要參數
onMounted(() => {
  if (!route.query.title || !route.query.time || !route.query.location || !route.query.method) {
    router.push('/book-reservation')
  }
})
</script>

<style>
.scroll-wrapper {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.intro {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  background: transparent;
}

.result-bg {
  padding: 32px 0 100px 0;
  background: transparent;
}

.result-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 成功訊息區樣式 */
.result-success-area {
  text-align: center;
  margin-bottom: 32px;
}

.result-success-icon {
  width: 80px;
  height: 80px;
  background: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.result-success-svg {
  width: 40px;
  height: 40px;
  color: white;
}

.result-success-title {
  font-size: 2rem;
  font-weight: bold;
  color: #18181b;
  margin-bottom: 8px;
}

.result-success-subtitle {
  color: #4b5563;
  font-size: 1.1rem;
}

/* 預約詳情卡片樣式 */
.result-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.result-bookinfo {
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
}

.result-bookinfo-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 16px;
}

.result-bookinfo-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-bookinfo-book {
  font-size: 1.5rem;
  font-weight: bold;
  color: #18181b;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.result-bookinfo-author,
.result-bookinfo-isbn {
  font-size: 0.95rem;
  color: #4b5563;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* 預約資訊樣式 */
.result-reservation-info {
  padding: 24px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
}

.result-reservation-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 16px;
}

.result-reservation-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-info-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.result-info-label {
  color: #4b5563;
  min-width: 80px;
  flex-shrink: 0;
}

.result-info-value {
  color: #18181b;
  font-weight: 500;
  word-wrap: break-word;
  overflow-wrap: break-word;
  flex: 1;
}

/* 注意事項樣式 */
.result-notice {
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  padding: 24px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
}

.result-notice-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 12px;
}

.result-notice-list {
  color: #4b5563;
  font-size: 0.95rem;
  margin: 0;
  padding-left: 20px;
}

.result-notice-list li {
  margin-bottom: 8px;
}

/* 按鈕區域樣式 */
.result-btn-area {
  padding: 24px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.result-btn {
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.result-btn-record {
  background: #2563eb;
  color: white;
  border: none;
}

.result-btn-record:hover {
  background: #1d4ed8;
}

.result-btn-home {
  background: white;
  color: #2563eb;
  border: 1px solid #2563eb;
}

.result-btn-home:hover {
  background: #f3f4f6;
}

/* 響應式設計 */
@media screen and (max-width: 640px) {
  .result-btn-area {
    flex-direction: column;
  }

  .result-btn {
    width: 100%;
  }
}
</style>