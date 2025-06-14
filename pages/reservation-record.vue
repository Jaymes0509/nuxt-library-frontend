<template>
  <Suspense>
    <template #default>
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
                      <option value="reservationDate">預約日期</option>
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
                    表格
                  </button>
                  <button
                    @click="viewMode = 'grid'"
                    :class="['history-view-btn', viewMode === 'grid' ? 'history-view-btn-active' : '']"
                  >
                    網格
                  </button>
                </div>
              </div>

              <!-- 載入中狀態 -->
              <div v-if="loading" class="history-loading">
                載入中...
              </div>

              <!-- 錯誤信息 -->
              <div v-else-if="error" class="history-error">
                {{ error }}
              </div>

              <!-- 無資料狀態 -->
              <div v-else-if="!paginatedBooks.length" class="history-empty">
                目前沒有預約記錄
              </div>

              <!-- 表格視圖 -->
              <div v-else :class="['history-table-scroll', itemsPerPage > 10 ? 'history-table-scrollable' : 'history-table-fill']">
                <div v-if="viewMode === 'table'" class="history-grid-table">
                  <div class="history-grid-header">
                    <div>書名</div>
                    <div>作者</div>
                    <div>取書地點</div>
                    <div>取書時間</div>
                    <div>預約狀態</div>
                    <div>操作</div>
                  </div>
                  <div class="history-grid-body">
                    <div
                      v-for="(reservation, index) in paginatedBooks"
                      :key="index"
                      class="history-grid-row"
                    >
                      <div>{{ reservation.bookTitle }}</div>
                      <div>{{ reservation.author }}</div>
                      <div>{{ reservation.pickupLocation }}</div>
                      <div>{{ reservation.pickupTime }}</div>
                      <div>{{ reservation.status || '待確認' }}</div>
                      <div>
                        <button @click="viewBookDetail(reservation)" class="history-detail-btn">詳情</button>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="history-grid">
                  <div v-for="(reservation, index) in paginatedBooks" :key="index" class="history-grid-card">
                    <div class="history-grid-img-wrap">
                      <img :src="getDefaultCoverUrl(index)" :alt="reservation.bookTitle" class="history-grid-img" />
                    </div>
                    <div class="history-grid-info">
                      <h3 class="history-grid-title">{{ reservation.bookTitle }}</h3>
                      <p class="history-grid-author">作者：{{ reservation.author }}</p>
                      <div class="history-grid-dates">
                        <p>取書地點：{{ reservation.pickupLocation }}</p>
                        <p>取書時間：{{ reservation.pickupTime }}</p>
                        <p>預約日期：{{ reservation.reservationDate }}</p>
                        <p>預約狀態：{{ reservation.status || '待確認' }}</p>
                      </div>
                      <button class="history-detail-btn" @click="viewBookDetail(reservation)">詳情</button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 分頁控制 -->
              <div v-if="paginatedBooks.length" class="history-pagination">
                <div class="history-pagination-controls">
                  <button 
                    class="history-pagination-btn"
                    :disabled="currentPage === 1"
                    @click="currentPage--"
                  >
                    <span class="sr-only">上一頁</span>
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
    <template #fallback>
      <div class="history-loading">
        載入中...
      </div>
    </template>
  </Suspense>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 視圖模式
const viewMode = ref('table')

// 分頁設定
const pageSizes = [10, 20, 30, 50, 100]
const itemsPerPage = ref(10)
const currentPage = ref(1)

// 排序設定
const sortConfig = ref({
  field: 'title',
  ascending: true
})

// 預設封面圖片
function getDefaultCoverUrl(index) {
  return `https://via.placeholder.com/300x400/4ECDC4/FFFFFF?text=${encodeURIComponent('書籍封面')}`
}

// 預約記錄資料
const reservationBooks = ref([])
const loading = ref(false)
const error = ref(null)

// 獲取預約記錄
async function fetchReservations() {
  if (loading.value) return
  
  loading.value = true
  error.value = null
  
  try {
    const response = await axios.get('/api/reservations')  // API 路徑，確保正確

    console.log('預約記錄資料：', response.data)
    
    if (response.data && Array.isArray(response.data)) {
      reservationBooks.value = response.data.map(reservation => ({
        reservationId: reservation.reservationId,
        bookTitle: reservation.book?.title || '',
        author: reservation.book?.author || '',
        pickupLocation: reservation.pickupLocation || '',
        pickupTime: reservation.pickupTime ? new Date(reservation.pickupTime).toLocaleString('zh-TW') : '',
        status: reservation.status,
        reservationDate: reservation.reservationDate
          ? new Date(reservation.reservationDate).toLocaleString('zh-TW')
          : ''
      }))
    } else {
      console.warn('API 返回格式不符合預期：', response.data)
      reservationBooks.value = []
      error.value = '資料格式錯誤'
    }
  } catch (err) {
    console.error('獲取預約記錄失敗：', err)
    error.value = '無法載入預約記錄，請稍後再試。錯誤詳情：' + (err.response?.data?.statusMessage || err.message)
    reservationBooks.value = []
  } finally {
    loading.value = false
  }
}

// 排序功能
function toggleSortOrder() {
  sortConfig.value.ascending = !sortConfig.value.ascending
}

// 排序後的資料
const sortedBooks = computed(() => {
  const books = [...reservationBooks.value]
  const field = sortConfig.value.field
  const ascending = sortConfig.value.ascending

  return books.sort((a, b) => {
    if (a[field] < b[field]) return ascending ? -1 : 1
    if (a[field] > b[field]) return ascending ? 1 : -1
    return 0
  })
})

// 計算總頁數
const totalPages = computed(() => Math.ceil(sortedBooks.value.length / itemsPerPage.value))

// 計算當前頁面應該顯示的資料
const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return sortedBooks.value.slice(start, end)
})

// 頁面跳轉
function goToPage(page) {
  const pageNum = parseInt(page)
  if (pageNum && !isNaN(pageNum) && pageNum >= 1 && pageNum <= totalPages.value) {
    currentPage.value = pageNum
  }
}

// 監聽每頁顯示筆數變動，自動跳回第一頁
watch(itemsPerPage, () => {
  currentPage.value = 1
})

// 初始化載入資料
onMounted(() => {
  fetchReservations()
})
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

.history-bg {
  padding: 24px 24px 100px 24px;
  background: transparent;
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
  flex-wrap: wrap;
}
.history-control-panel-left {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
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
  flex-wrap: wrap;
}
.history-label {
  font-size: 1rem;
  color: #222;
}
.history-select {
  min-width: 120px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 1rem;
  background: #fff;
  color: #18181b;
  cursor: pointer;
  transition: background 0.2s;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  text-align-last: center;
}

.history-select:hover {
  background: #f3f4f6;
}

.history-sort-btn {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 16px;
  background: #fff;
  color: #18181b;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  text-align: center;
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
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}
.history-grid-header,
.history-grid-row {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1.5fr 1.5fr 1.2fr 1fr;
  align-items: center;
}
.history-grid-header {
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
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
  border: 1px solid rgba(229, 231, 235, 0.4);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.2s;
}
.history-grid-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  background: rgba(243, 244, 246, 0.8);
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
  min-width: 32px;
  padding: 0 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #18181b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 1rem;
  line-height: 1;
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

/* 隱藏 Chrome, Safari, Edge, Opera 的箭頭 */
.history-pagination-input::-webkit-outer-spin-button,
.history-pagination-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 隱藏 Firefox 的箭頭 */
.history-pagination-input[type=number] {
  appearance: none;
-webkit-appearance: none;
}

.history-pagination-info {
  font-size: 0.95rem;
  color: #4b5563;
  text-align: center;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .history-control-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .history-control-panel-left {
    flex-direction: column;
    gap: 16px;
  }

  .history-control-panel-right {
    justify-content: center;
  }

  .history-grid-header,
  .history-grid-row {
    grid-template-columns: 1.5fr 1fr 1fr 1fr 0.8fr;
    font-size: 0.9rem;
  }

  .history-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (max-width: 640px) {
  .history-bg {
    padding: 16px 16px 80px 16px;
  }

  .history-grid-header,
  .history-grid-row {
    grid-template-columns: 1.2fr 1fr 0.8fr;
    font-size: 0.85rem;
  }

  .history-grid-header > div,
  .history-grid-row > div {
    padding: 8px;
  }

  .history-grid {
    grid-template-columns: 1fr;
  }

  .history-pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .history-select,
  .history-sort-btn,
  .history-view-btn {
    width: 100%;
    justify-content: center;
  }

  .history-row {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
  }

  .history-label {
    text-align: center;
  }
}

/* 載入中狀態 */
.history-loading {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
  font-size: 1.1rem;
}

/* 錯誤信息 */
.history-error {
  text-align: center;
  padding: 2rem;
  color: #dc2626;
  font-size: 1.1rem;
}

/* 無資料狀態 */
.history-empty {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
  font-size: 1.1rem;
}
</style>