<template>
  <div class="book-detail-wrapper">
    <!-- 左半邊：封面圖片 -->
    <div class="cover-area">
      <img
        class="cover-image"
        :src="bookCoverUrl"
        :alt="`書籍《${book.title}》封面`"
      />
    </div>

    <!-- 右半邊：文字內容區 -->
    <div class="info-area">
      <!-- 書名 -->
      <h1 class="book-title">{{ book.title }}</h1>

      <!-- 內容簡介 -->
      <section class="section-block" v-if="book.description">
        <h2>內容簡介</h2>
        <p>{{ book.description }}</p>
      </section>

      <!-- 作者介紹 -->
      <section class="section-block">
        <h2>作者</h2>
        <p>{{ book.author }}</p>
      </section>

      <!-- 詳細資料 -->
      <section class="section-block">
        <h2>詳細資料</h2>
        <ul class="details-list">
          <li><strong>ISBN：</strong>{{ book.isbn }}</li>
          <li><strong>出版社：</strong>{{ book.publisher }}</li>
          <li><strong>出版日期：</strong>{{ book.publishdate }}</li>
          <li><strong>分類號：</strong>{{ book.classification }}</li>
          <li><strong>語言：</strong>{{ book.language }}</li>
          <li><strong>總藏書量：</strong>{{ book.total_copies }}</li>
          <li><strong>可借閱數量：</strong>{{ book.available_copies }}</li>
          <li>
            <strong>在架狀態：</strong>
            <span :class="book.is_available === '1' ? 'available' : 'unavailable'">
              {{ book.is_available === '1' ? '可借閱' : '不可借閱' }}
            </span>
          </li>
        </ul>
      </section>

      <!-- 動作區域 -->
      <div class="action-area">
        <button 
          class="reserve-btn" 
          @click="reserveBook"
          :disabled="book.is_available !== '1'"
          :class="{ 'disabled': book.is_available !== '1' }"
        >
          📖 {{ book.is_available === '1' ? '預約此書' : '無法預約' }}
        </button>
        <button class="back-btn" @click="goBack">
          ← 返回搜尋結果
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// 從 URL query 參數中獲取書籍資訊
const book = ref({
  id: route.params.id || route.query.id || '',
  title: route.query.title || '未知書名',
  author: route.query.author || '未知作者',
  isbn: route.query.isbn || '',
  publisher: route.query.publisher || '未知出版社',
  publishdate: route.query.publishdate || '',
  classification: route.query.classification || '',
  language: route.query.language || '',
  description: route.query.description || '',
  is_available: route.query.is_available || '0',
  total_copies: route.query.total_copies || '1',
  available_copies: route.query.available_copies || '0'
})

// 計算封面圖片 URL
const bookCoverUrl = computed(() => {
  if (book.value.isbn) {
    return `https://covers.openlibrary.org/b/isbn/${book.value.isbn}-L.jpg`
  }
  // 如果沒有 ISBN，使用預設圖片
  return 'https://via.placeholder.com/300x400?text=No+Cover'
})

// 預約書籍功能
const reserveBook = () => {
  if (book.value.is_available === '1') {
    alert(`已成功預約：《${book.value.title}》`)
  } else {
    alert('此書籍目前無法預約')
  }
}

// 改進的返回功能
const goBack = () => {
  // 方法1: 檢查是否有返回的搜尋參數
  if (route.query.returnQuery) {
    router.push({
      path: '/', // 搜尋頁面路徑，請根據您的實際路徑調整
      query: {
        q: route.query.returnQuery,
        page: route.query.returnPage || '1'
      }
    })
    return
  }
  
  // 方法2: 檢查瀏覽器歷史記錄
  if (window.history.length > 1) {
    router.back()
  } else {
    // 方法3: 如果沒有歷史記錄，導航到搜尋頁面
    router.push('/')
  }
}

// 組件掛載時的處理
onMounted(() => {
  // 如果沒有必要的書籍資訊，可能需要重新獲取
  if (!book.value.title || book.value.title === '未知書名') {
    console.warn('書籍資訊不完整，可能需要從 API 重新獲取')
  }
})
</script>

<style scoped>
/* 容器最外層：flex 佈局，手機先上下堆疊 */
.book-detail-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin: 1rem;
}

/* 左側封面圖片區 */
.cover-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.cover-image {
  max-width: 300px;
  max-height: 400px;
  width: 100%;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 右側文字資訊區 */
.info-area {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 書名樣式 */
.book-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: bold;
  color: #333;
}

/* 每個區段標題與內容 */
.section-block h2 {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: #555;
}

.section-block p {
  margin: 0;
  line-height: 1.6;
  color: #444;
}

/* 詳細資料列表 */
.details-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.details-list li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
  color: #444;
}

/* 可用性狀態樣式 */
.available {
  color: #28a745;
  font-weight: bold;
}

.unavailable {
  color: #dc3545;
  font-weight: bold;
}

/* 動作區域：放置按鈕 */
.action-area {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.reserve-btn {
  background-color: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0.6rem 1.2rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.reserve-btn:hover:not(.disabled) {
  background-color: #155a9c;
}

.reserve-btn.disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.back-btn {
  background-color: #6c757d;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0.6rem 1.2rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.back-btn:hover {
  background-color: #5a6268;
}

/* RWD：寬度 ≥ 768px 時左右並列 */
@media screen and (min-width: 768px) {
  .book-detail-wrapper {
    flex-direction: row;
  }
  .cover-area,
  .info-area {
    align-items: flex-start;
  }
  .cover-area {
    max-width: 40%;
  }
  .info-area {
    max-width: 60%;
  }
}

/* 手機版響應式調整 */
@media screen and (max-width: 767px) {
  .action-area {
    flex-direction: column;
  }
  
  .reserve-btn,
  .back-btn {
    width: 100%;
  }
}
</style>