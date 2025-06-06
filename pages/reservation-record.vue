<template>
  <div class="scroll-wrapper">
    <div class="intro">
  <div class="history-bg">
    <h1 class="history-title">預約查詢</h1>
    <div class="history-main">
      <!-- 控制面板 -->
      <div class="history-control-panel">
        <div class="history-control-panel-left">
          <div class="history-row">
            <span class="history-label">每頁顯示：</span>
            <select v-model="itemsPerPage" class="history-select">
              <option v-for="size in pageSizes" :key="size" :value="size">{{ size }} 筆</option>
            </select>
          </div>
          <div class="history-row">
            <span class="history-label">排序：</span>
            <select v-model="sortConfig.field" class="history-select">
              <option value="title">書名</option>
              <option value="author">作者</option>
              <option value="borrowDate">借閱日</option>
              <option value="dueDate">到期日</option>
            </select>
            <button @click="toggleSortOrder" class="history-sort-btn">
              <span v-if="sortConfig.ascending">↑ 升冪</span>
              <span v-else>↓ 降冪</span>
            </button>
          </div>
        </div>
        <div class="history-control-panel-right">
          <button
            @click="viewMode = 'table'"
            :class="['history-view-btn', viewMode === 'table' ? 'history-view-btn-active' : '']"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="history-view-icon"><path d="M3 12h.01"></path><path d="M3 18h.01"></path><path d="M3 6h.01"></path><path d="M8 12h13"></path><path d="M8 18h13"></path><path d="M8 6h13"></path></svg>
            表格
          </button>
          <button
            @click="viewMode = 'grid'"
            :class="['history-view-btn', viewMode === 'grid' ? 'history-view-btn-active' : '']"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="history-view-icon"><rect width="7" height="7" x="3" y="3" rx="1"/><rect width="7" height="7" x="14" y="3" rx="1"/><rect width="7" height="7" x="14" y="14" rx="1"/><rect width="7" height="7" x="3" y="14" rx="1"/></svg>
            網格
          </button>
        </div>
      </div>

      <!-- 表格視圖 -->
      <div :class="['history-table-scroll', itemsPerPage > 10 ? 'history-table-scrollable' : 'history-table-fill']">
        <div v-if="viewMode === 'table'" class="history-grid-table">
          <div class="history-grid-header">
            <div>書名</div>
            <div>作者</div>
            <div>借閱日</div>
            <div>到期日</div>
            <div>書本資訊</div>
          </div>
          <div class="history-grid-body">
            <div
              v-for="(book, index) in paginatedBooks"
              :key="index"
              class="history-grid-row"
            >
              <div>{{ book.title }}</div>
              <div>{{ book.author }}</div>
              <div>{{ book.borrowDate }}</div>
              <div>{{ book.dueDate }}</div>
              <div>
                <button @click="viewBookDetail(book)" class="history-detail-btn">詳情</button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="history-grid">
          <div v-for="(book, index) in paginatedBooks" :key="index" class="history-grid-card">
            <div class="history-grid-img-wrap">
              <img :src="book.coverUrl || getDefaultCoverUrl(index)" :alt="book.title" class="history-grid-img" />
            </div>
            <div class="history-grid-info">
              <h3 class="history-grid-title">{{ book.title }}</h3>
              <p class="history-grid-author">{{ book.author }}</p>
              <div class="history-grid-dates">
                <p>借閱日：{{ book.borrowDate }}</p>
                <p>到期日：{{ book.dueDate }}</p>
              </div>
              <button class="history-detail-btn" @click="viewBookDetail(book)">詳情</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分頁控制 -->
      <div class="history-pagination">
        <div class="history-pagination-controls">
          <button 
            class="history-pagination-btn"
            :disabled="currentPage === 1"
            @click="currentPage--"
          >
            <span class="sr-only">上一頁</span>
            <span style="display:inline-block;width:1em;">&#8592;</span>
          </button>
          <span>共{{ totalPages }}頁</span>
          <input
            type="number"
            :value="currentPage"
            class="history-pagination-input"
            min="1"
            :max="totalPages"
            @change="e => goToPage(parseInt(e.target.value))"
          />
          <span>/{{ totalPages }}頁</span>
          <button 
            class="history-pagination-btn"
            :disabled="currentPage >= totalPages"
            @click="currentPage++"
          >
            <span class="sr-only">下一頁</span>
            <span style="display:inline-block;width:1em;">&#8594;</span>
          </button>
        </div>
        <div class="history-pagination-info">
          顯示第 {{ (currentPage - 1) * itemsPerPage + 1 }} 到 {{ Math.min(currentPage * itemsPerPage, sortedBooks.length) }} 筆，共 {{ sortedBooks.length }} 筆
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'

// 視圖模式
const viewMode = ref('table') // 'table' 或 'grid'

// 分頁設定
const pageSizes = [10, 20, 30, 50, 100]
const itemsPerPage = ref(10)
const currentPage = ref(1)

// 排序設定
const sortConfig = ref({
  field: 'title',
  ascending: true
})

// 生成大量測試資料
const books = [
  '哈利波特：神秘的魔法石', '哈利波特：消失的密室', '哈利波特：阿茲卡班的逃犯',
  '魔戒首部曲：魔戒現身', '魔戒二部曲：雙城奇謀', '魔戒三部曲：王者再臨',
  '三體', '三體Ⅱ黑暗森林', '三體Ⅲ死神永生', '挪威的森林',
  '1984', '美麗新世界', '動物農莊', '華氏451度',
  '追風箏的孩子', '群山回唱', '燦爛千陽', '小王子',
  '百年孤寂', '霍爾的移動城堡', '神隱少女', '天空之城'
]

const authors = [
  'J.K. 羅琳', '托爾金', '劉慈欣', '村上春樹',
  '喬治·歐威爾', '赫胥黎', '卡勒德·胡賽尼', '安東尼·聖修伯里',
  '加西亞·馬奎斯', '宮崎駿'
]

// 預設封面圖片（這裡使用隨機顏色作為示例）
function getDefaultCoverUrl(index) {
  const colors = ['FF6B6B', 'FF8787', '4ECDC4', '45B7D1', '96CEB4', 'FFEEAD', 'D4A5A5', 'FFE3E3']
  const color = colors[index % colors.length]
  return `https://via.placeholder.com/300x400/${color}/FFFFFF?text=${encodeURIComponent('書籍封面')}`
}

const borrowedBooks = ref(Array.from({ length: 50 }, (_, i) => ({
  id: String(i + 1),  // 添加 id 字段
  title: books[i % books.length],
  author: authors[i % authors.length],
  borrowDate: new Date(2025, 4, i % 28 + 1).toISOString().split('T')[0],
  dueDate: new Date(2025, 5, i % 28 + 1).toISOString().split('T')[0],
  coverUrl: null // 這裡之後可以放資料庫的圖片URL
})))

// 排序功能
function toggleSortOrder() {
  sortConfig.value.ascending = !sortConfig.value.ascending
}

function updateSort(field) {
  if (sortConfig.value.field === field) {
    sortConfig.value.ascending = !sortConfig.value.ascending
  } else {
    sortConfig.value.field = field
    sortConfig.value.ascending = true
  }
}

function getSortIcon(field) {
  if (sortConfig.value.field !== field) return ''
  return sortConfig.value.ascending ? '↑' : '↓'
}

// 排序後的資料
const sortedBooks = computed(() => {
  return [...borrowedBooks.value].sort((a, b) => {
    const field = sortConfig.value.field
    const modifier = sortConfig.value.ascending ? 1 : -1
    
    if (a[field] < b[field]) return -1 * modifier
    if (a[field] > b[field]) return 1 * modifier
    return 0
  })
})

// 計算總頁數
const totalPages = computed(() => Math.ceil(sortedBooks.value.length / itemsPerPage.value))

// 監聽每頁顯示數量變更
watch(itemsPerPage, () => {
  currentPage.value = 1
})

// 計算當前頁面應該顯示的資料
const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return sortedBooks.value.slice(start, end)
})

const router = useRouter()
function viewBookDetail(book) {
  // 跳轉到書本詳情頁面
  router.push({
    path: `/${book.id}`,
    query: {
      title: book.title,
      author: book.author,
      coverUrl: book.coverUrl || getDefaultCoverUrl(book.id)
    }
  })
}

// 添加 goToPage 函數
function goToPage(page) {
  const pageNum = parseInt(page)
  if (pageNum && !isNaN(pageNum) && pageNum >= 1 && pageNum <= totalPages.value) {
    currentPage.value = pageNum
  }
}
</script>

<style scoped>
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
  background: #fff;
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

.history-bg {
  padding: 24px 24px 100px 24px;
  background: #fff;
}
.history-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 16px;
  color: #18181b;
}
.history-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.history-control-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}
.history-control-panel-left {
  display: flex;
  align-items: center;
  gap: 32px;
}
.history-control-panel-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.history-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.history-label {
  font-size: 1rem;
  color: #222;
}
.history-select {
  width: 120px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 1rem;
  background: #fff;
  color: #18181b;
}
.history-sort-btn {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 4px 8px;
  background: #fff;
  color: #18181b;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.history-sort-btn:hover {
  background: #f3f4f6;
}
.history-view-btn {
  display: inline-flex;
  align-items: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #18181b;
  font-size: 1rem;
  padding: 8px 16px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  margin-right: 4px;
}
.history-view-btn:last-child {
  margin-right: 0;
}
.history-view-btn-active {
  background: #2563eb;
  color: #fff;
}
.history-view-icon {
  width: 20px;
  height: 20px;
  margin-right: 6px;
}
.history-table-scroll {
  width: 100%;
}
.history-table-fill {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.history-table-scrollable {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.history-grid-table {
  display: flex;
  flex-direction: column;
  width: 100%;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}
.history-grid-header,
.history-grid-row {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1.5fr 1.5fr 1fr;
  align-items: center;
}
.history-grid-header {
  background: #f3f4f6;
  color: #222;
  font-weight: 600;
  padding: 12px 0;
}
.history-grid-header > div {
  padding: 12px 16px;
  text-align: center;
}
.history-grid-header > div:first-child {
  text-align: left;
}
.history-grid-body {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.history-grid-row {
  min-height: 0;
  flex: 1;
  border-bottom: 1px solid #e5e7eb;
  transition: background 0.2s;
}
.history-grid-row:last-child {
  border-bottom: none;
}
.history-grid-row > div {
  padding: 12px 16px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}
.history-grid-row > div:first-child {
  text-align: left;
  justify-content: flex-start;
}
.history-grid-row:hover {
  background: #f3f4f6;
}
.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}
.history-grid-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s;
}
.history-grid-card:hover {
  box-shadow: 0 4px 16px #0002;
  background: #f3f4f6;
}
.history-grid-img-wrap {
  aspect-ratio: 3/4;
  background: #f3f4f6;
  position: relative;
}
.history-grid-img {
  object-fit: cover;
  width: 100%;
  height: 100%;
}
.history-grid-info {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.history-grid-title {
  font-weight: 600;
  font-size: 1.1rem;
  color: #18181b;
  margin-bottom: 2px;
}
.history-grid-author {
  font-size: 0.95rem;
  color: #6b7280;
  margin-bottom: 4px;
}
.history-grid-dates {
  font-size: 0.9rem;
  color: #4b5563;
  margin-bottom: 8px;
}
.history-detail-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s;
}
.history-detail-btn:hover {
  background: #1d4ed8;
}
.history-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}
.history-pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}
.history-pagination-btn {
  height: 32px;
  width: 32px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #18181b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}
.history-pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.history-pagination-btn:hover {
  background: #f3f4f6;
}
.history-pagination-input {
  height: 32px;
  width: 48px;
  text-align: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  color: #18181b;
  background: #fff;
}
.history-pagination-info {
  font-size: 0.95rem;
  color: #4b5563;
  text-align: center;
}
</style>