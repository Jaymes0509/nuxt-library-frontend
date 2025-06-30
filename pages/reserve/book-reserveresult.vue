<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="result-bg">
        <div class="result-container">
          <!-- 成功訊息區 -->
          <div class="result-success-area">
            <div class="result-success-icon" :class="{ 'result-error-icon': !isAllSuccess }">
              <svg v-if="isAllSuccess" class="result-success-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              <svg v-else class="result-error-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h1 class="result-success-title">
              {{ isAllSuccess ? (isBatchMode ? '批量預約成功！' : '預約成功！') : '書籍已預約過，無法重複預約' }}
            </h1>
            <p class="result-success-subtitle">
              {{ isAllSuccess
                ? (isBatchMode ? `您已成功預約 ${reservationBooks.length} 本書籍` : '您的書籍已成功預約，請在指定時間內取書')
                : `成功預約 ${successResults.length} 本，失敗 ${failedResults.length} 本書籍`
              }}
            </p>
          </div>

          <!-- 失敗結果詳情 -->
          <div v-if="!isAllSuccess && failedResults.length > 0" class="result-error-detail">
            <h3 class="result-error-title">預約失敗詳情</h3>
            <div class="result-error-list">
              <div v-for="(item, idx) in failedResults" :key="item.bookId" class="result-error-item">
                <span class="result-error-bookid">書籍ID {{ item.bookId }}</span>
                <span class="result-error-reason">{{ item.reason || '未知錯誤' }}</span>
              </div>
            </div>
          </div>

          <!-- 預約詳情卡片 -->
          <div class="result-card">
            <!-- 書籍資訊 -->
            <div class="result-bookinfo">
              <h2 class="result-bookinfo-title">{{ isBatchMode ? '預約書籍清單' : '預約書籍資訊' }}</h2>
              <div v-if="isBatchMode" class="result-books-list">
                <div v-for="(book, index) in reservationBooks" :key="index" class="result-book-item">
                  <span class="result-book-number">{{ index + 1 }}.</span>
                  <div class="result-book-info">
                    <p class="result-book-title">{{ book.title }}</p>
                    <p class="result-book-author">作者：{{ book.author }}</p>
                    <p class="result-book-isbn">ISBN：{{ book.isbn }}</p>
                  </div>
                </div>
              </div>
              <div v-else class="result-bookinfo-content">
                <p class="result-bookinfo-book">{{ reservationBooks[0].title }}</p>
                <p class="result-bookinfo-author">作者：{{ reservationBooks[0].author }}</p>
                <p class="result-bookinfo-isbn">ISBN：{{ reservationBooks[0].isbn }}</p>
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
                  <span class="result-info-value">{{ reservationIds[0] || '生成中...' }}</span>
                </div>
              </div>
            </div>

            <!-- 注意事項 -->
            <div class="result-notice">
              <h3 class="result-notice-title">注意事項</h3>
              <ul class="result-notice-list">
                <li>請在預約時間內完成取書</li>
                <li>超過預約時間未取書將自動取消預約</li>
                <li>預約成功通知郵件已自動發送至您的信箱</li>
                <li>如需取消預約，請至「我的預約」頁面操作</li>
                <li>如有任何問題，請聯繫圖書館服務台</li>
              </ul>
            </div>

            <!-- 按鈕區域 -->
            <div class="result-btn-area">
              <button type="button" @click="goToReservationList" class="result-btn result-btn-record">
                查看預約清單
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
// @ts-nocheck
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useHead } from '#imports'

// 設置頁面標題
useHead({
  title: '預約成功'
})

const route = useRoute()
const router = useRouter()

// 獲取預約結果資料
const reservationBooks = computed(() => {
  const booksParam = route.query.books
  if (!booksParam || typeof booksParam !== 'string') return []

  try {
    return JSON.parse(booksParam)
  } catch (error) {
    console.error('解析書籍資料失敗：', error)
    return []
  }
})

// 獲取預約 ID 列表
const reservationIds = computed(() => {
  const idsParam = route.query.reservationIds
  if (!idsParam || typeof idsParam !== 'string') return []

  try {
    return JSON.parse(idsParam)
  } catch (error) {
    console.error('解析預約 ID 失敗：', error)
    return []
  }
})

// 獲取預約結果
const results = computed(() => {
  const resultsParam = route.query.results
  if (!resultsParam || typeof resultsParam !== 'string') return []

  try {
    return JSON.parse(resultsParam)
  } catch (error) {
    console.error('解析預約結果失敗：', error)
    return []
  }
})

// 檢查預約是否全部成功
const isAllSuccess = computed(() => route.query.success === 'true')

// 獲取成功和失敗的結果
const successResults = computed(() => results.value.filter(item => item.status === 'success'))
const failedResults = computed(() => results.value.filter(item => item.status === 'fail'))

// 檢查是否為批量預約模式
const isBatchMode = computed(() => reservationBooks.value.length > 1)

// 從路由參數獲取預約資訊
const form = computed(() => {
  // 如果是批量預約，從第一本書籍中獲取預約資訊
  if (isBatchMode.value && reservationBooks.value.length > 0) {
    return {
      time: reservationBooks.value[0].reserve_time || reservationBooks.value[0].time || '',
      location: reservationBooks.value[0].location || '一樓服務台',
      method: reservationBooks.value[0].method || '親自取書'
    }
  }

  // 單本書籍預約
  return {
    time: String(route.query.reserve_time || route.query.time || ''),
    location: String(route.query.location || '一樓服務台'),
    method: String(route.query.method || '親自取書')
  }
})

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

// 跳轉到預約清單頁面
function goToReservationList() {
  router.push('/reserve/reservation-record')
}

// 返回首頁
function goToHome() {
  router.push('/')
}

// 自動發送預約成功通知郵件
const sendNotificationEmail = async () => {
  try {
    // 檢查是否有預約 ID
    if (!reservationIds.value || reservationIds.value.length === 0) {
      console.log('沒有預約 ID，跳過郵件通知')
      return
    }

    // 獲取當前登入用戶的 ID
    const storedUser = localStorage.getItem('user')
    let currentUserId = null

    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser)
        currentUserId = userData.user_id || userData.id
      } catch (e) {
        console.error('解析用戶資訊失敗：', e)
      }
    }

    // 如果沒有用戶 ID，跳過郵件通知
    if (!currentUserId) {
      console.log('無法獲取用戶 ID，跳過郵件通知')
      return
    }

    // 準備郵件通知資料
    const emailData = {
      userId: currentUserId,
      reservationIds: reservationIds.value.map(id => id.toString())
    }

    console.log('嘗試發送郵件通知，資料：', emailData)

    // 調用後端郵件通知 API（如果存在的話）
    const response = await fetch('http://localhost:8080/api/reservation-notification/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('jwt_token') || localStorage.getItem('authToken') || ''}`
      },
      body: JSON.stringify(emailData)
    })

    if (response.ok) {
      const result = await response.json()
      if (result.success) {
        console.log('✅ 預約成功通知郵件已自動發送！')
      } else {
        console.log('⚠️ 郵件通知 API 返回失敗：', result.message || '郵件發送失敗')
      }
    } else {
      console.log('⚠️ 郵件通知 API 不存在或發生錯誤，狀態碼：', response.status)
    }
  } catch (error) {
    console.log('⚠️ 郵件通知功能暫時不可用：', error.message)
  }
}

// 檢查必要參數
onMounted(() => {
  // 檢查是否有書籍資料
  if (reservationBooks.value.length === 0) {
    router.push('/reserve/book-reservation')
    return
  }

  // 檢查是否有預約時間（地點和方式有預設值）
  if (!form.value.time) {
    router.push('/reserve/book-reservation')
    return
  }

  // 預約成功時自動發送通知郵件
  if (isAllSuccess.value) {
    sendNotificationEmail()
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

/* 批量預約結果樣式 */
.result-books-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding: 12px;
  background: rgba(243, 244, 246, 0.3);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.result-book-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 6px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.result-book-number {
  font-weight: 600;
  color: #2563eb;
  font-size: 1.1rem;
  min-width: 24px;
  padding-top: 2px;
}

.result-book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.result-book-title {
  font-weight: 600;
  color: #18181b;
  font-size: 1rem;
  margin: 0;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.result-book-author,
.result-book-isbn {
  color: #4b5563;
  font-size: 0.9rem;
  margin: 0;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .result-book-item {
    flex-direction: column;
    gap: 8px;
  }

  .result-book-number {
    align-self: flex-start;
  }
}

.result-error-icon {
  background: #dc2626 !important;
}

.result-error-svg {
  width: 40px;
  height: 40px;
  color: white;
}

/* 失敗結果詳情樣式 */
.result-error-detail {
  background: rgba(254, 242, 242, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(239, 68, 68, 0.3);
  padding: 24px;
  margin-bottom: 32px;
}

.result-error-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #dc2626;
  margin-bottom: 16px;
}

.result-error-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-error-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border-left: 4px solid #dc2626;
}

.result-error-bookid {
  font-weight: 500;
  color: #374151;
}

.result-error-reason {
  color: #dc2626;
  font-size: 0.9rem;
}
</style>