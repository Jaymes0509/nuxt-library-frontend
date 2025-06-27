<template>
  <div ref="topContainer" class="rankings-container">
    <!-- 固定的返回按鈕（只在第二層顯示） -->
    <div v-if="step !== 'summary'" style="position: fixed; bottom: 10rem; right: 2.5rem; z-index: 20;">
      <button @click="goBackToSummary" class="back-button">上一頁</button>
    </div>

    <h1 class="title center">圖書排行榜</h1>

    <!-- 總覽排行榜（三卡片） -->
    <div v-if="step === 'summary'" class="summary-cards">
      <div v-for="type in ['reservation', 'borrow', 'rating']" :key="type" class="card grouped-card">
        <div class="card-header-wrapper">
          <button class="card-header" @click="step = type">
            {{
              type === 'reservation' ? '預約次數' :
                type === 'borrow' ? '借閱次數' :
                  '評分高低'
            }}
          </button>
        </div>
        <h2 class="card-title">
          {{
            type === 'reservation' ? '預約次數排行榜(總和)' :
              type === 'borrow' ? '借閱次數排行榜(總和)' :
                '評分高低排行榜(總和)'
          }}
        </h2>
        <ol class="ranking-list">
          <li v-for="(book, index) in topBooks(type, true)"
            :key="book.bookId ? book.bookId + '-' + type : 'empty-' + index" class="ranking-item consistent-height">
            <template v-if="book.bookId">
              <!-- 正常書卡 -->
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
                        {{ book.averageRating !== undefined && book.averageRating !== null ?
                          book.averageRating.toFixed(2)
                          : '無評分' }}
                      </span>
                    </div>
                  </template>
                  <template v-else>
                    {{ type === 'reservation' ? '預約次數：' : '借閱次數：' }}
                    <span class="stat-count">{{ book.statCount || '' }}</span>
                  </template>
                </div>
              </div>
            </template>

            <template v-else>
              <!-- 空卡片 -->
              <div class="left-part">
                <div class="ranking-index">NO.{{ index + 1 }}</div>
                <div class="cover empty-cover">無資料</div>
              </div>
              <div class="book-info empty-info">
                <div class="book-title">（空）</div>
              </div>
            </template>
          </li>

        </ol>
      </div>
    </div>

    <!-- 詳細排行榜畫面（第二層） -->
    <div v-else class="detail-container">
      <h2 class="subtitle center">
        {{
          step === 'borrow' ? '📘 借閱排行榜詳細' :
            step === 'reservation' ? '📗 預約排行榜詳細' :
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
                {{ step === 'borrow' ? '借閱次數：' : step === 'reservation' ? '預約次數：' : '' }}
                <span class="stat-count">{{ book.statCount || 0 }}</span>
              </template>
              <!-- ✅ 書籍簡介（適用於三種排行榜） -->
              <div class="book-description" v-if="book.description">
                <strong>簡介：</strong>
                <p :class="{ truncated: !expandedIndexSet.has(index) }">
                  {{ book.description }}
                </p>
                <button class="toggle-button" @click="toggleExpand(index)">
                  {{ expandedIndexSet.has(index) ? '收起' : '顯示更多' }}
                </button>
              </div>
            </div>
          </div>
        </li>
      </ol>


      <!-- 分頁與每頁顯示設定 -->
      <div class="pagination">
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
.rankings-container {
  padding: 0 2rem;
}

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

.center {
  text-align: center;
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
  flex: 1 1 20%;
  min-width: 240px;
  max-width: 320px;
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
  min-height: 160px;
  padding: 0.5rem 0;
  border-bottom: 1px solid #ccc;
}

.ranking-item.consistent-height.detail-card {
  margin-top: 0.5rem;
}

.ranking-item.detail-card {
  max-width: 100%;
  box-sizing: border-box;
}

.left-part {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
}

.ranking-index {
  font-weight: bold;
  width: 3rem;
}

.cover {
  width: 80px;
  height: 120px;
  object-fit: cover;
  margin-top: 0.25rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.cover.empty-cover {
  width: 60px;
  height: 90px;
  background-color: #f5f5f5;
  border: 1px dashed #ccc;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 0.8rem;
  color: #888;
}

.book-info {
  flex: 1;
  overflow: hidden;
  padding-left: 1rem;
  line-height: 1.6;
}

.book-info.empty-info {
  opacity: 0.6;
  font-style: italic;
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

.stat-count {
  font-weight: bold;
  margin-left: 0.2rem;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

.subtitle {
  font-size: 1.4rem;
  font-weight: bold;
  margin-bottom: 1rem;
}

.filters {
  margin-bottom: 1.5rem;
}

.inline {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.label {
  font-size: 1rem;
  line-height: 2.2rem;
  height: 2.2rem;
  display: inline-block;
  vertical-align: middle;
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

.search-bar {
  text-align: center;
  margin-bottom: 1rem;
}

.detailed-list {
  padding: 0;
  list-style: none;
}

.detail-card {
  background-color: #f5f8ff;
  border: 1px solid #d0d0d0;
  border-radius: 10px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.08);
  transition: box-shadow 0.3s ease;
  min-height: 240px;
  display: flex;
  align-items: flex-start;
  max-width: 100%;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.detail-card .book-title {
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 0.4rem;
  white-space: normal;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.detail-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.book-description {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #333;
  line-height: 1.5;
}

.book-description p.truncated {
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.book-description p {
  white-space: normal;
  overflow-wrap: break-word;
  word-wrap: break-word;
  line-height: 1.6;
}

.toggle-button {
  background: none;
  border: none;
  color: #007bff;
  cursor: pointer;
  padding: 0;
  font-size: 0.9rem;
  margin-top: 0.2rem;
}

.toggle-button:hover {
  text-decoration: underline;
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
const defaultCover = 'https://cdn-icons-png.flaticon.com/512/2232/2232688.png'
const selectedPeriod = ref('all')
const selectedCategory = ref('')
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 20
const totalItems = ref(0)
const totalPages = ref(1)
const expandedIndexSet = ref(new Set())

function toggleExpand(index) {
  if (expandedIndexSet.value.has(index)) {
    expandedIndexSet.value.delete(index)
  } else {
    expandedIndexSet.value.add(index)
  }
}

const bookCategories = [
  '總類', '哲學類', '宗教類', '科學類', '應用科學類', '社會科學類',
  '史地類：中國史地', '史地類：世界史地', '語言文學類', '藝術類'
]

const years = Array.from({ length: new Date().getFullYear() - 2020 + 1 }, (_, i) => 2020 + i)
const months = Array.from({ length: 12 }, (_, i) => i + 1)

async function fetchRankings() {
  try {
    if (step.value === 'summary') {
      const res = await api.get('/api/rankings/all')
      rankedReserveBooks.value = res.data.reservationRanking || []
      rankedBorrowBooks.value = res.data.borrowRanking || []
      rankedRatingBooks.value = res.data.ratingRanking || []
      rankedBooks.value = []

      // ✅ 歸零分頁狀態（放在 summary 裡）
      totalItems.value = 0
      totalPages.value = 1

      return
    }

    const params = {
      type: step.value,
      page: currentPage.value - 1, // 後端從 0 開始
      size: pageSize
    }

    if (selectedCategory.value) {
      params.category = selectedCategory.value
    }
    if (selectedPeriod.value === 'year' && selectedYear.value) {
      params.year = selectedYear.value
    }
    if (selectedPeriod.value === 'month' && selectedYear.value && selectedMonth.value) {
      params.year = selectedYear.value
      params.month = selectedMonth.value
    }
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    } else {
      params.keyword = null // 一定要補這行
    }

    const res = await api.get('/api/rankings/detail', { params })

    // 後端回傳的是 Page 格式
    const content = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 1
    rankedBooks.value = content
  } catch (error) {
    console.error('❌ 載入排行榜失敗', error)
  }
}

function topBooks(type, isSummary = false) {
  let list = isSummary
    ? type === 'reservation' ? rankedReserveBooks.value
      : type === 'borrow' ? rankedBorrowBooks.value
        : rankedRatingBooks.value
    : rankedBooks.value;

  list.forEach(book => book.statCount = Number(book.statCount));

  const filtered = list.filter(book => {
    if (!isSummary && step.value !== type) return false;

    // 總覽與詳細都過濾掉統計值為 0 的書
    if (type === 'reservation' || type === 'borrow') {
      return book.statCount > 0;
    } else if (type === 'rating') {
      return book.averageRating > 0;
    }
    return true;
  });

  // 補空白卡片（僅限總覽）
  if (isSummary) {
    const padded = [...filtered];
    while (padded.length < 10) {
      padded.push({}); // 用 {} 當作空卡片，前端會自動判斷無 bookId
    }
    return padded;
  }

  return filtered;
}


function goBackToSummary() {
  selectedCategory.value = ''
  selectedPeriod.value = 'all'
  selectedYear.value = new Date().getFullYear()
  selectedMonth.value = new Date().getMonth() + 1
  searchKeyword.value = ''
  rankedBooks.value = []
  currentPage.value = 1
  step.value = 'summary'
}


// const totalPages = computed(() => {
//   return Math.ceil(rankedBooks.value.length / pageSize.value) || 1
// })

watch([selectedPeriod, selectedCategory, selectedYear, selectedMonth, searchKeyword], () => {
  currentPage.value = 1
  fetchRankings()
})


watch(step, () => {
  fetchRankings()
})

watch(currentPage, () => {
  fetchRankings()
})

onMounted(async () => {
  await fetchRankings()
})


</script>
