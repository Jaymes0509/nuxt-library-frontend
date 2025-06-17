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
                  <option value="pickupTime">取書時間</option>
                </select>
                <button @click="toggleSortOrder" class="history-sort-btn">
                  {{ sortConfig.ascending ? '↑ 升冪' : '↓ 降冪' }}
                </button>
              </div>
            </div>
            <div class="history-control-panel-right">
              <button @click="viewMode = 'table'"
                :class="['history-view-btn', viewMode === 'table' ? 'history-view-btn-active' : '']">
                表格
              </button>
              <button @click="viewMode = 'grid'"
                :class="['history-view-btn', viewMode === 'grid' ? 'history-view-btn-active' : '']">
                網格
              </button>
            </div>
          </div>

          <!-- 載入中狀態 -->
          <div v-if="loading" class="history-loading">
            <div class="history-loading-spinner"></div>
            <p>載入中...</p>
          </div>

          <!-- 錯誤信息 -->
          <div v-else-if="error" class="history-error">
            <div class="history-error-icon">!</div>
            <p>{{ error }}</p>
            <pre class="history-error-details">{{ error }}</pre>
          </div>

          <!-- 無資料狀態 -->
          <div v-else-if="!paginatedBooks.length" class="history-empty">
            <div class="history-empty-icon">📚</div>
            <p>目前沒有預約記錄</p>
          </div>

          <!-- 表格視圖 -->
          <div v-else
            :class="['history-table-scroll', itemsPerPage > 10 ? 'history-table-scrollable' : 'history-table-fill']">
            <div v-if="viewMode === 'table'" class="history-grid-table">
              <div class="history-grid-header">
                <div>書名</div>
                <div>作者</div>
                <div>取書地點</div>
                <div>取書時間</div>
                <div>操作</div>
              </div>
              <div class="history-grid-body">
                <div v-for="(reservation, index) in paginatedBooks" :key="index" class="history-grid-row">
                  <div class="history-grid-title-cell">{{ reservation.title }}</div>
                  <div>{{ reservation.author }}</div>
                  <div>{{ reservation.pickupLocation }}</div>
                  <div>{{ reservation.pickupTime }}</div>
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
                  <h3 class="history-grid-title reservation-record-book-title">{{ reservation.bookTitle }}</h3>
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
              <button class="history-pagination-btn" :disabled="currentPage === 1" @click="currentPage--">
                <span aria-hidden="true">←</span>
              </button>
              <span>共{{ totalPages }}頁</span>
              <input type="number" :value="currentPage" class="history-pagination-input" min="1" :max="totalPages"
                @change="e => goToPage(parseInt(e.target.value))" />
              <span>/{{ totalPages }}頁</span>
              <button class="history-pagination-btn" :disabled="currentPage >= totalPages" @click="currentPage++">
                <span aria-hidden="true">→</span>
              </button>
            </div>
            <div class="history-pagination-info">
              顯示第 {{ (currentPage - 1) * itemsPerPage + 1 }} 到 {{ Math.min(currentPage * itemsPerPage,
                sortedBooks.length) }} 筆，共 {{ sortedBooks.length }} 筆
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
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

// 格式化日期時間
function formatDateTime(dateTimeStr) {
  if (!dateTimeStr) return ''
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

// 獲取預約記錄
async function fetchReservations() {
  if (loading.value) return

  loading.value = true
  error.value = null

  try {
    const response = await axios.get('/api/reservations')

    console.log('API 回傳資料：', response.data);

    if (response.data && Array.isArray(response.data)) {
      reservationBooks.value = response.data.map((reservation, index) => {
        // 直接使用扁平化的資料結構
        const processedReservation = {
          reservationId: reservation.reservation_id || `res_${index}`,
          title: reservation.title || '未知書名',
          author: reservation.author || '未知作者',
          isbn: reservation.isbn || '未知ISBN',
          publisher: reservation.publisher || '未知出版社',
          classification: reservation.classification || '',
          categoryName: reservation.category_name || '',
          pickupLocation: reservation.pickup_location || '未指定地點',
          pickupTime: reservation.pickup_time || '',
          reservationDate: reservation.reservation_date || '',
          expiryDate: reservation.expiry_date || '',
          status: reservation.status || 'pending',
          // 保存完整的原始資料，以便詳情頁使用
          bookInfo: {
            bookId: reservation.book_id,
            title: reservation.title,
            author: reservation.author,
            isbn: reservation.isbn,
            publisher: reservation.publisher,
            classification: reservation.classification,
            category: {
              cName: reservation.category_name
            }
          }
        }

        // 輸出第一筆資料的處理結果
        if (index === 0) {
          console.log('第一筆處理後的預約記錄：', JSON.stringify(processedReservation, null, 2))
        }

        return processedReservation
      })

      console.log('總筆數：', reservationBooks.value.length)
      console.log('資料範例：', {
        title: reservationBooks.value[0]?.title,
        author: reservationBooks.value[0]?.author,
        pickupLocation: reservationBooks.value[0]?.pickupLocation,
        pickupTime: reservationBooks.value[0]?.pickupTime,
        status: reservationBooks.value[0]?.status
      })

      console.log('前端處理後的 reservationBooks：', reservationBooks.value);
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

// 查看書籍詳情
function viewBookDetail(reservation) {
  if (reservation.bookInfo) {
    // 使用 router 導航到詳情頁，並傳遞書籍資訊
    router.push({
      path: `/book/${reservation.bookInfo.bookId}`,
      state: { bookInfo: reservation.bookInfo }
    })
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
    let valueA, valueB

    // 根據不同欄位類型進行排序
    if (field === 'reservationDate') {
      valueA = new Date(a[field] || 0).getTime()
      valueB = new Date(b[field] || 0).getTime()
    } else {
      valueA = (a[field] || '').toString().toLowerCase()
      valueB = (b[field] || '').toString().toLowerCase()
    }

    // 進行排序比較
    if (valueA < valueB) return ascending ? -1 : 1
    if (valueA > valueB) return ascending ? 1 : -1
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

// 監聽排序設定變化
watch([() => sortConfig.value.field, () => sortConfig.value.ascending], () => {
  // 當排序設定改變時，重置到第一頁
  currentPage.value = 1
})

// 初始化載入資料
onMounted(async () => {
  try {
    await fetchReservations()
  } catch (err) {
    console.error('初始化載入失敗：', err)
  }
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
  transition: all 0.3s ease;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
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
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
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
  width: 100%;
  overflow-x: auto;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.history-grid-header,
.history-grid-row {
  display: grid;
  grid-template-columns: minmax(300px, 3fr) minmax(120px, 1.5fr) minmax(120px, 1.5fr) minmax(150px, 1.5fr) minmax(80px, 0.8fr);
  align-items: center;
  min-width: 900px;
}

.history-grid-header {
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  color: #222;
  font-weight: 600;
  padding: 12px 0;
}

.history-grid-header>div,
.history-grid-row>div {
  padding: 12px 16px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-grid-header>div:first-child,
.history-grid-row>div:first-child {
  text-align: left;
  justify-content: flex-start;
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

.history-grid-row>div {
  padding: 12px 16px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-grid-row>div:first-child {
  text-align: left;
  justify-content: flex-start;
}

.history-grid-row:hover {
  background: #f3f4f6;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.history-grid-card {
  border: 1px solid rgba(229, 231, 235, 0.4);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
  height: 480px;
  width: 100%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.history-grid-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.1), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.history-grid-card:hover {
  transform: translateY(-4px);
  border-color: rgba(37, 99, 235, 0.3);
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.1),
    0 2px 4px -1px rgba(0, 0, 0, 0.06),
    0 0 0 2px rgba(37, 99, 235, 0.1);
}

.history-grid-card:hover::before {
  opacity: 1;
}

.history-grid-img-wrap {
  height: 240px;
  width: 100%;
  overflow: hidden;
  position: relative;
  background: #f3f4f6;
}

.history-grid-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.history-grid-card:hover .history-grid-img {
  transform: scale(1.05);
}

.history-grid-info {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.history-grid-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.history-grid-title {
  font-weight: 600;
  font-size: 1.1rem;
  color: #18181b;
  margin: 0;
  max-height: 2.8em;
  line-height: 1.4;
  overflow: hidden;
  position: relative;
}

.history-grid-title::after {
  content: '...';
  position: absolute;
  bottom: 0;
  right: 0;
  padding-left: 40px;
  background: linear-gradient(to right, transparent, white 50%);
}

.history-grid-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.9rem;
  color: #4b5563;
}

.history-grid-pickup {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.9rem;
  color: #4b5563;
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid rgba(229, 231, 235, 0.6);
}

.history-grid-label {
  color: #6b7280;
  font-weight: 500;
  margin-right: 4px;
}

.history-grid-author,
.history-grid-isbn,
.history-grid-publisher,
.history-grid-location,
.history-grid-time {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.history-detail-btn {
  margin-top: auto;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  width: 100%;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.history-detail-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.history-detail-btn:active {
  transform: translateY(0);
  box-shadow: none;
}

.history-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding: 16px;
  background: transparent;
  border-radius: 8px;
}

.history-pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.history-pagination-btn {
  height: 36px;
  min-width: 36px;
  padding: 0 12px;
  border: 1px solid #2563eb;
  border-radius: 6px;
  background: #fff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
  font-size: 1.2rem;
  line-height: 1;
}

.history-pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.history-pagination-btn:hover:not(:disabled) {
  background: #2563eb;
  color: #fff;
}

.history-pagination-input {
  height: 36px;
  width: 60px;
  text-align: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  color: #18181b;
  background: #fff;
  padding: 0 8px;
}

.history-pagination-info {
  font-size: 0.95rem;
  color: #4b5563;
  text-align: center;
  white-space: nowrap;
  padding: 4px 8px;
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

@media (max-width: 768px) {
  .scroll-wrapper {
    padding: 0;
  }

  .intro {
    padding: 0;
  }

  .history-bg {
    padding: 8px 4px 60px 4px;
  }

  .history-main {
    gap: 12px;
  }

  .history-title {
    font-size: 1.3rem;
    margin-bottom: 8px;
  }

  .history-grid-header,
  .history-grid-row {
    grid-template-columns: minmax(120px, 2fr) minmax(80px, 1fr) minmax(60px, 0.8fr);
    font-size: 0.8rem;
    min-width: 0;
  }

  .history-grid-header>div,
  .history-grid-row>div {
    padding: 6px 8px;
  }

  .history-grid-title-cell {
    font-size: 0.8rem;
  }

  .history-detail-btn {
    padding: 4px 6px;
    font-size: 0.75rem;
    min-width: 50px;
  }

  .history-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .history-grid-card {
    height: 320px;
    min-width: 0;
  }

  .history-grid-img-wrap {
    height: 100px;
  }

  .history-grid-info {
    padding: 8px;
    gap: 6px;
  }

  .history-pagination {
    margin-top: 8px;
    padding: 6px;
    width: 100%;
  }

  .history-pagination-controls {
    gap: 4px;
    width: 100%;
  }

  .history-pagination-info {
    font-size: 0.8rem;
    white-space: normal;
    line-height: 1.2;
  }

  .history-pagination-input {
    width: 40px;
  }
}

@media (max-width: 480px) {
  .history-bg {
    padding: 4px 2px 40px 2px;
  }

  .history-title {
    font-size: 1.1rem;
  }

  .history-control-panel {
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
  }

  .history-control-panel-left {
    flex-direction: column;
    gap: 4px;
  }

  .history-control-panel-right {
    justify-content: center;
    gap: 4px;
  }

  .history-row {
    flex-direction: column;
    align-items: stretch;
    width: 100%;
    gap: 2px;
  }

  .history-label {
    text-align: center;
    font-size: 0.9rem;
  }

  .history-select {
    min-width: 80px;
    font-size: 0.9rem;
    height: 32px;
    padding: 4px 8px;
  }

  .history-sort-btn,
  .history-view-btn {
    font-size: 0.9rem;
    height: 32px;
    padding: 4px 8px;
  }

  .history-grid-header,
  .history-grid-row {
    grid-template-columns: minmax(80px, 2fr) minmax(60px, 1fr) minmax(40px, 0.8fr);
    font-size: 0.7rem;
    min-width: 0;
  }

  .history-grid-header>div,
  .history-grid-row>div {
    padding: 4px 4px;
  }

  .history-grid-title-cell {
    font-size: 0.7rem;
  }

  .history-detail-btn {
    padding: 2px 4px;
    font-size: 0.7rem;
    min-width: 40px;
  }

  .history-grid-card {
    height: 220px;
    min-width: 0;
  }

  .history-grid-img-wrap {
    height: 60px;
  }

  .history-grid-info {
    padding: 4px;
    gap: 2px;
  }

  .history-pagination {
    margin-top: 4px;
    padding: 2px;
    width: 100%;
  }

  .history-pagination-controls {
    gap: 2px;
    width: 100%;
  }

  .history-pagination-info {
    font-size: 0.7rem;
    white-space: normal;
    line-height: 1.1;
  }

  .history-pagination-input {
    width: 30px;
  }
}

/* 載入中狀態 */
.history-loading {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
  font-size: 1.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.history-loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 錯誤信息 */
.history-error {
  text-align: center;
  padding: 3rem;
  color: #dc2626;
  font-size: 1.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  background: rgba(220, 38, 38, 0.1);
  border-radius: 8px;
  margin: 1rem 0;
}

.history-error-icon {
  width: 48px;
  height: 48px;
  background: #dc2626;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
}

/* 無資料狀態 */
.history-empty {
  text-align: center;
  padding: 3rem;
  color: #6b7280;
  font-size: 1.1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  background: rgba(243, 244, 246, 0.6);
  border-radius: 8px;
  margin: 1rem 0;
}

.history-empty-icon {
  font-size: 2.5rem;
}

.history-error-details {
  background: rgba(0, 0, 0, 0.05);
  padding: 1rem;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.9rem;
  white-space: pre-wrap;
  word-break: break-all;
  max-width: 100%;
  overflow-x: auto;
}

.history-grid-status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}

.status-pending {
  background: rgba(234, 179, 8, 0.1);
  color: #854d0e;
}

.status-completed {
  background: rgba(34, 197, 94, 0.1);
  color: #166534;
}

.status-cancelled {
  background: rgba(239, 68, 68, 0.1);
  color: #991b1b;
}

.reservation-record-book-title {
  max-height: 2.8em;
  line-height: 1.4;
  overflow: hidden;
  position: relative;
}

.reservation-record-book-title::after {
  content: '...';
  position: absolute;
  bottom: 0;
  right: 0;
  padding-left: 40px;
  background: linear-gradient(to right, transparent, white 50%);
}

.history-grid-title-cell {
  font-weight: 600;
  color: #18181b;
  text-align: left;
  padding: 12px 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-table-scroll::-webkit-scrollbar {
  height: 6px;
}

.history-table-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.history-table-scroll::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.history-table-scroll::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.3);
}
</style>