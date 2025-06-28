<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="result-bg">
        <div class="result-container">
          <!-- 頁面標題區 -->
          <div class="result-title-area">
            <h1 class="result-title">借書結果</h1>
            <p class="result-subtitle">您的借書申請已處理完成</p>
          </div>

          <div class="result-card">
            <!-- 結果摘要 -->
            <div class="result-summary">
              <div v-if="success" class="success-icon">
                <svg class="success-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div v-else class="error-icon">
                <svg class="error-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h2 class="result-status">{{ success ? '借書成功！' : '借書失敗' }}</h2>
              <p class="result-message">{{ success ? '您的書籍已成功借閱' : '部分或全部書籍借閱失敗' }}</p>
            </div>

            <!-- 借書資訊 -->
            <div class="borrow-info">
              <h3 class="borrow-info-title">借書資訊</h3>
              <div class="borrow-info-grid">
                <div class="info-item">
                  <span class="info-label">借書期限：</span>
                  <span class="info-value">{{ duration }}天</span>
                </div>
                <div class="info-item">
                  <span class="info-label">取書地點：</span>
                  <span class="info-value">{{ location }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">借書方式：</span>
                  <span class="info-value">{{ method }}</span>
                </div>
              </div>
            </div>

            <!-- 書籍結果列表 -->
            <div class="books-results">
              <h3 class="books-results-title">書籍借閱結果</h3>
              <div class="books-list">
                <div v-for="(result, index) in results" :key="index" class="book-result-item">
                  <div class="book-info">
                    <h4 class="book-title">{{ getBookTitle(result.bookId) }}</h4>
                    <p class="book-author">{{ getBookAuthor(result.bookId) }}</p>
                  </div>
                  <div class="book-status">
                    <span :class="['status-badge', result.status === 'success' ? 'status-success' : 'status-failed']">
                      {{ result.status === 'success' ? '成功' : '失敗' }}
                    </span>
                    <p v-if="result.status === 'failed'" class="error-message">{{ result.message }}</p>
                    <p v-else class="borrow-id">借閱編號：{{ result.borrowId }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 統計資訊 -->
            <div class="statistics">
              <div class="stat-item">
                <span class="stat-label">成功借閱：</span>
                <span class="stat-value success">{{ successCount }}本</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">失敗借閱：</span>
                <span class="stat-value failed">{{ failedCount }}本</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">總計：</span>
                <span class="stat-value">{{ totalCount }}本</span>
              </div>
            </div>

            <!-- 按鈕區域 -->
            <div class="result-btn-area">
              <button type="button" @click="goToBorrowSearch" class="result-btn result-btn-secondary">
                繼續借書
              </button>
              <button type="button" @click="goToBorrowManagement" class="result-btn result-btn-primary">
                查看借閱記錄
              </button>
              <button type="button" @click="goHome" class="result-btn result-btn-back">
                返回首頁
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

// 設置頁面標題
useHead({
  title: '借書結果'
})

const route = useRoute()
const router = useRouter()

// 響應式資料
const success = ref(false)
const duration = ref('')
const location = ref('')
const method = ref('')
const books = ref([])
const results = ref([])

// 計算屬性
const successCount = computed(() => {
  return results.value.filter(result => result.status === 'success').length
})

const failedCount = computed(() => {
  return results.value.filter(result => result.status === 'failed').length
})

const totalCount = computed(() => {
  return results.value.length
})

// 根據書籍ID獲取書籍標題
const getBookTitle = (bookId) => {
  const book = books.value.find(b => b.id === bookId)
  return book ? book.title : '未知書籍'
}

// 根據書籍ID獲取書籍作者
const getBookAuthor = (bookId) => {
  const book = books.value.find(b => b.id === bookId)
  return book ? book.author : '未知作者'
}

// 導航函數
const goToBorrowSearch = () => {
  router.push('/borrow-search')
}

const goToBorrowManagement = () => {
  router.push('/borrow-management')
}

const goHome = () => {
  router.push('/')
}

// 初始化
onMounted(() => {
  // 從URL參數獲取資料
  if (route.query.success) {
    success.value = route.query.success === 'true'
  }
  
  if (route.query.duration) {
    duration.value = route.query.duration
  }
  
  if (route.query.location) {
    location.value = route.query.location
  }
  
  if (route.query.method) {
    method.value = route.query.method
  }
  
  if (route.query.books) {
    try {
      books.value = JSON.parse(route.query.books)
    } catch (error) {
      console.error('解析書籍資料失敗:', error)
    }
  }
  
  if (route.query.results) {
    try {
      results.value = JSON.parse(route.query.results)
    } catch (error) {
      console.error('解析結果資料失敗:', error)
    }
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

/* 滾動條預設為透明 */
.intro::-webkit-scrollbar {
  width: 8px;
}

.intro::-webkit-scrollbar-thumb {
  background-color: transparent;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .intro::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .intro {
  scrollbar-color: rgba(0, 0, 0, 0.4) transparent;
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

.result-title-area {
  text-align: center;
  margin-bottom: 32px;
}

.result-title {
  font-size: 2rem;
  font-weight: bold;
  color: #18181b;
}

.result-subtitle {
  margin-top: 8px;
  color: #4b5563;
}

.result-card {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(229, 231, 235, 0.4);
  padding: 24px;
}

.result-summary {
  text-align: center;
  margin-bottom: 32px;
  padding: 24px;
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.success-icon {
  width: 64px;
  height: 64px;
  background: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px auto;
}

.success-svg {
  width: 32px;
  height: 32px;
  color: white;
}

.error-icon {
  width: 64px;
  height: 64px;
  background: #ef4444;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px auto;
}

.error-svg {
  width: 32px;
  height: 32px;
  color: white;
}

.result-status {
  font-size: 1.5rem;
  font-weight: bold;
  color: #18181b;
  margin-bottom: 8px;
}

.result-message {
  color: #4b5563;
  font-size: 1rem;
}

.borrow-info {
  margin-bottom: 32px;
  padding: 20px;
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.borrow-info-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 16px;
}

.borrow-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-label {
  font-weight: 500;
  color: #4b5563;
}

.info-value {
  color: #18181b;
  font-weight: 500;
}

.books-results {
  margin-bottom: 32px;
}

.books-results-title {
  font-size: 1.1rem;
  font-weight: 500;
  color: #222;
  margin-bottom: 16px;
}

.books-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.book-result-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 1rem;
  font-weight: 500;
  color: #18181b;
  margin-bottom: 4px;
}

.book-author {
  font-size: 0.9rem;
  color: #4b5563;
}

.book-status {
  text-align: right;
  min-width: 120px;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  margin-bottom: 8px;
}

.status-success {
  background: #10b981;
  color: white;
}

.status-failed {
  background: #ef4444;
  color: white;
}

.error-message {
  font-size: 0.8rem;
  color: #ef4444;
  margin: 0;
}

.borrow-id {
  font-size: 0.8rem;
  color: #4b5563;
  margin: 0;
}

.statistics {
  display: flex;
  justify-content: space-around;
  margin-bottom: 32px;
  padding: 20px;
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 0.9rem;
  color: #4b5563;
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 1.5rem;
  font-weight: bold;
}

.stat-value.success {
  color: #10b981;
}

.stat-value.failed {
  color: #ef4444;
}

.result-btn-area {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.result-btn {
  padding: 12px 24px;
  border: 1px solid #2563eb;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  min-width: 120px;
}

.result-btn-primary {
  background: #2563eb;
  color: #fff;
}

.result-btn-primary:hover {
  background: #1d4ed8;
}

.result-btn-secondary {
  background: #10b981;
  color: #fff;
  border-color: #10b981;
}

.result-btn-secondary:hover {
  background: #059669;
}

.result-btn-back {
  background: #fff;
  color: #2563eb;
}

.result-btn-back:hover {
  background: #f3f4f6;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .borrow-info-grid {
    grid-template-columns: 1fr;
  }
  
  .book-result-item {
    flex-direction: column;
    gap: 12px;
  }
  
  .book-status {
    text-align: left;
  }
  
  .statistics {
    flex-direction: column;
    gap: 16px;
  }
  
  .result-btn-area {
    flex-direction: column;
  }
  
  .result-btn {
    width: 100%;
  }
}
</style> 