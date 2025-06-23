<template>
  <div ref="topContainer" class="rankings-container">
    <!-- 固定的返回按鈕（只在第二層顯示） -->
    <div v-if="step !== 'summary'" style="position: fixed; bottom: 10rem; right: 2.5rem; z-index: 20;">
      <button @click="goBackToSummary" class="back-button">上一頁</button>
    </div>

    <h1 class="title center">圖書排行榜</h1>

    <!-- 總覽排行榜（三卡片） -->
    <div v-if="step === 'summary'" class="summary-cards">
      <div v-for="type in ['reserve', 'borrow', 'rating']" :key="type" class="card grouped-card">
        <div class="card-header-wrapper">
          <button class="card-header" @click="step = type">
            {{
              type === 'reserve' ? '預約次數' :
                type === 'borrow' ? '借閱次數' :
                  '評分高低'
            }}
          </button>
        </div>
        <h2 class="card-title">
          {{
            type === 'reserve' ? '預約次數排行榜(總和)' :
              type === 'borrow' ? '借閱次數排行榜(總和)' :
                '評分高低排行榜(總和)'
          }}
        </h2>
        <ol class="ranking-list">
          <li v-for="(book, index) in topBooks(type, true)" :key="book.bookId + '-' + type"
            class="ranking-item consistent-height">
            <div class="left-part">
              <div class="ranking-index">NO.{{ index + 1 }}</div>
              <img :src="book.cover || defaultCover" alt="封面" class="cover" />
            </div>
            <div class="book-info">
              <div class="book-title" :title="book.title">{{ book.title || '' }}</div>
              <div class="book-author">作者：{{ book.author || '' }}</div>
              <div class="book-stat">
                <template v-if="type === 'rating'">
                  <div>
                    平均評分：<span>
                      {{ book.averageRating !== undefined && book.averageRating !== null ? book.averageRating.toFixed(2)
                        : '無評分' }}
                    </span>
                  </div>
                </template>
                <template v-else>
                  {{ type === 'reserve' ? '預約次數：' : '借閱次數：' }}
                  <span class="stat-count">{{ book.statCount || '' }}</span>
                </template>
              </div>
            </div>
          </li>
        </ol>
      </div>
    </div>

    <!-- 詳細排行榜畫面（第二層） -->
    <div v-else class="detail-container">
      <h2 class="subtitle center">
        {{
          step === 'borrow' ? '📘 借閱排行榜詳細' :
            step === 'reserve' ? '📗 預約排行榜詳細' :
              '📙 評分排行榜詳細'
        }}
      </h2>

      <!-- 查詢篩選區塊 -->
      <div class="filters center">
        <div class="inline">
          <label class="label">書籍分類：</label>
          <select v-model="selectedCategory" class="select">
            <option value="">全部分類</option>
            <option v-for="cat in bookCategories" :key="cat" :value="cat">{{ cat }}</option>
          </select>

          <label class="label">時間篩選：</label>
          <select v-model="selectedPeriod" class="select">
            <option value="all">總和</option>
            <option value="year">年份</option>
            <option value="month">月份</option>
          </select>

          <select v-if="['year', 'month'].includes(selectedPeriod)" v-model="selectedYear" class="select">
            <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
          </select>

          <select v-if="selectedPeriod === 'month' && selectedYear" v-model="selectedMonth" class="select">
            <option v-for="month in months" :key="month" :value="month">{{ month }} 月</option>
          </select>
        </div>

        <!-- 搜尋輸入框移至下方獨立列 -->
        <div class="search-bar">
          <input type="text" v-model="searchKeyword" placeholder="輸入書名搜尋" class="select" style="width: 440px" />
        </div>
      </div>

      <!-- 書籍清單 -->
      <ol class="detailed-list">
        <li v-for="(book, index) in topBooks(step)" :key="book.bookId"
          class="ranking-item consistent-height detail-card">
          <div class="left-part">
            <div class="ranking-index">{{ index + 1 }}</div>
            <img :src="book.cover || defaultCover" class="cover" />
          </div>
          <div class="book-info" v-if="book && book.title">
            <div class="book-title">{{ book.title }}</div>
            <div class="book-author">作者：{{ book.author || '未知' }}</div>
            <div class="book-stat">
              <template v-if="step === 'rating'">
                <div>平均評分：
                  <span>{{ book.averageRating !== undefined && book.averageRating !== null ?
                    book.averageRating.toFixed(1) : '無評分' }}</span>
                </div>
                <div>評論數：<span>{{ book.statCount ?? 0 }}</span></div>
              </template>
              <template v-else>
                {{ step === 'borrow' ? '借閱次數：' : '預約次數：' }}
                <span class="stat-count">{{ book.statCount || 0 }}</span>
              </template>
            </div>
          </div>
        </li>
      </ol>

      <!-- 分頁與每頁顯示設定 -->
      <div class="pagination">
        <div>
          每頁顯示：
          <select v-model="pageSize" class="select">
            <option v-for="size in [10, 20, 50, 100]" :key="size" :value="size">{{ size }}</option>
          </select> 本
        </div>
        <div class="page-buttons">
          <button v-for="page in totalPages" :key="page" @click="currentPage = page"
            :class="['page-button', currentPage === page ? 'active' : '']">
            {{ page }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.back-button {
  padding: 0.2rem 0.6rem;
  font-size: 1.1rem;
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 6px;
  cursor: pointer;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1);
}

.back-button:hover {
  background-color: #e0e0e0;
  border-color: #999;
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.2);
}

.rankings-container {
  padding: 0 2rem;
}

.center {
  text-align: center;
}

.inline {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.search-bar {
  text-align: center;
  margin-bottom: 1rem;
}

.summary-cards {
  display: flex;
  justify-content: space-between;
  gap: 1.5rem;
  margin-bottom: 3rem;
  flex-wrap: wrap;
}

.grouped-card {
  background-color: #f9f9f9;
  border: 2px solid #ddd;
  padding: 1.5rem;
  border-radius: 0.75rem;
  flex: 1 1 30%;
  min-width: 280px;
  max-width: 360px;
  box-sizing: border-box;
}

.card-header-wrapper {
  display: flex;
  justify-content: center;
}

.card-header {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  text-align: center;
  background: none;
  border: none;
  cursor: pointer;
  color: #2c3e50;
}

.card-header:hover {
  text-decoration: underline;
}

.card-title {
  font-size: 1.2rem;
  font-weight: bold;
  margin-bottom: 0.8rem;
  text-align: center;
  color: #333;
}

.ranking-list {
  padding: 0;
  list-style: none;
}

.ranking-item.consistent-height {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  min-height: 140px;
  padding: 0.5rem 0;
  border-bottom: 1px solid #ccc;
}

.ranking-item.consistent-height.detail-card {
  margin-top: 0.5rem;
}


.ranking-index {
  font-weight: bold;
  width: 3rem;
}

.left-part {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
}

.cover {
  width: 80px;
  height: 120px;
  object-fit: cover;
  margin-top: 0.25rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.book-info {
  flex: 1;
  overflow: hidden;
  padding-left: 1rem;
  line-height: 1.6;
}


.book-title {
  font-weight: bold;
  font-size: 1rem;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 0.4rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  display: inline-block;
  box-sizing: border-box;
  color: #222;
}


.book-title:hover {
  position: relative;
}

.book-title:hover::after {
  content: attr(title);
  position: absolute;
  white-space: normal;
  background-color: #fff;
  border: 1px solid #ccc;
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
  top: 100%;
  left: 0;
  z-index: 10;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  width: max-content;
  max-width: 300px;
}

.book-author,
.book-stat {
  font-size: 0.9rem;
  color: #444;
}

.select {
  padding: 0.4rem 0.6rem;
  font-size: 1rem;
  height: 2.2rem;
  line-height: 1.2;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  vertical-align: middle;
}

.label {
  font-size: 1rem;
  line-height: 2.2rem;
  height: 2.2rem;
  display: inline-block;
  vertical-align: middle;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
  margin-top: 2rem;
}

.page-buttons {
  display: flex;
  gap: 0.5rem;
}

.page-button {
  padding: 0.3rem 0.7rem;
  font-size: 1rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: white;
  cursor: pointer;
}

.page-button.active {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.detail-card {
  background-color: #f5f8ff;
  border: 1px solid #d0d0d0;
  border-radius: 10px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
}

.detail-card .book-title {
  font-size: 1.5rem;
  /* 想更大就調整這個值，例如 1.5rem */
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 0.4rem;
}


.detail-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080' // 🔧 請改成你的後端位置
})

const step = ref('summary')
const rankedReserveBooks = ref([])
const rankedBorrowBooks = ref([])
const rankedRatingBooks = ref([])
const rankedBooks = ref([])
const defaultCover = 'https://via.placeholder.com/80x120.png?text=No+Cover'

const sortOptions = [
  { label: '預約次數', value: 'reserve' },
  { label: '借閱次數', value: 'borrow' },
  { label: '評分高低', value: 'rating' }
]

const selectedPeriod = ref('all')
const selectedCategory = ref('')
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const currentPage = ref(1)
const pageSize = ref(10)

const bookCategories = [
  '總類', '哲學類', '宗教類', '科學類', '應用科學類', '社會科學類',
  '史地類：中國史地', '史地類：世界史地', '語言文學類', '藝術類'
]

const years = Array.from({ length: new Date().getFullYear() - 2020 + 1 }, (_, i) => 2020 + i)
const months = Array.from({ length: 12 }, (_, i) => i + 1)

async function fetchRankings() {
  try {
    const res = await api.get('/api/rankings/all')
    console.log('✅ 後端回傳資料', res.data)

    rankedReserveBooks.value = res.data.reservationRanking || []
    rankedBorrowBooks.value = res.data.borrowRanking || []
    rankedRatingBooks.value = res.data.ratingRanking || []

    if (step.value === 'reserve' || step.value === 'summary') {
      rankedBooks.value = rankedReserveBooks.value
    } else if (step.value === 'borrow') {
      rankedBooks.value = rankedBorrowBooks.value
    } else if (step.value === 'rating') {
      rankedBooks.value = rankedRatingBooks.value
    }
  } catch (error) {
    console.error('❌ 讀取排行榜失敗', error)
  }
}

function topBooks(type, isSummary = false) {
  let list = isSummary
    ? type === 'reserve' ? rankedReserveBooks.value
      : type === 'borrow' ? rankedBorrowBooks.value
        : rankedRatingBooks.value
    : rankedBooks.value

  if (selectedCategory.value) {
    list = list.filter(book => book.categoryName === selectedCategory.value)
  }

  return list
    .filter(book => isSummary || step.value === type)
    .sort((a, b) => {
      if (type === 'rating') {
        const diff = (b.averageRating || 0) - (a.averageRating || 0)
        if (diff !== 0) return diff
        const diff2 = (b.statCount || 0) - (a.statCount || 0)
        if (diff2 !== 0) return diff2
        return (a.bookId || 0) - (b.bookId || 0)
      } else {
        const diff = (b.statCount || 0) - (a.statCount || 0)
        if (diff !== 0) return diff
        return (a.bookId || 0) - (b.bookId || 0)
      }
    })
    .slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value)
}

function goBackToSummary() {
  selectedCategory.value = ''
  selectedPeriod.value = 'all'
  currentPage.value = 1
  step.value = 'summary'
}

const totalPages = computed(() => {
  return Math.ceil(rankedBooks.value.length / pageSize.value) || 1
})

watch([selectedPeriod, selectedCategory], () => {
  currentPage.value = 1
})

watch(step, () => {
  fetchRankings()
})

onMounted(() => {
  fetchRankings()
})
</script>
