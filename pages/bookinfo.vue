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
          <li><strong>出版年：</strong>{{ book.publishdate }}</li>
          <li><strong>分類號：</strong>{{ book.classification }}</li>
          <li><strong>分類系統：</strong>{{ book.categorysystem }}</li>
          <li><strong>語言：</strong>{{ book.language }}</li>
          <li>
            <strong>在架狀態：</strong>
            <span :class="book.is_available === 1 ? 'available' : 'unavailable'">
              {{ book.is_available === 1 ? '可借閱' : '不可借閱' }}
            </span>
          </li>
        </ul>
      </section>

      <!-- 動作區域 -->
      <div class="action-area">
        <button 
          class="reserve-btn" 
          @click="reserveBook"
          :disabled="!book.is_available"
          :class="{ 'disabled': !book.is_available }"
        >
          {{ book.is_available === 1 ? '預約此書' : '無法預約' }}
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
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const book = ref({})

onMounted(async () => {
  const isbn = route.query.isbn
  if (!isbn) return

  try {
    const res = await axios.get(`/api/books/isbn/${isbn}`)
    const data = res.data

    // 統一處理 is_available（支援後端回傳為 boolean 或 int）
    data.is_available = (data.is_available === 1 || data.is_available === '1' || data.is_available === true) ? 1 : 0

    book.value = data
  } catch (error) {
    console.error('無法取得書籍資料', error)
  }
  console.log('book 資料：', book.value)

})

const bookCoverUrl = computed(() => {
  if (route.query.coverUrl) {
    return route.query.coverUrl
  }
  if (book.value.isbn) {
    return `https://covers.openlibrary.org/b/isbn/${book.value.isbn}-L.jpg`
  }
  return 'https://dummyimage.com/300X400/d1d1d1/030303&text=%E6%9A%AB%E7%84%A1%E5%B0%81%E9%9D%A2'
})

const goBack = () => {
  const query = {
    q: route.query.returnQuery,
    page: route.query.returnPage,
    from: 'bookinfo',
    returnType: route.query.returnType
  }
  
  if (route.query.returnQuery) query.q = route.query.returnQuery
  if (route.query.returnPage) query.page = route.query.returnPage
  if (route.query.returnType) query.returnType = route.query.returnType
  query.from = 'bookinfo'

  // 可選擴充：如果有分類、語言、年份也帶回
  if (route.query.categorysystem) {
    query.categorysystem = route.query.categorysystem
  }
  if (route.query.language) {
    query.language = route.query.language
  }
  if (route.query.yearFrom) {
    query.yearFrom = route.query.yearFrom
  }
  if (route.query.yearTo) {
    query.yearTo = route.query.yearTo
  }

  router.push({
    path: '/catalogue-search',
    query
  })
}
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