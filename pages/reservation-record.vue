<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="history-bg">
        <h1 class="history-title">我的預約清單</h1>
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
                  <option value="addedDate">加入時間</option>
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

          <!-- 批量操作面板 -->
          <div v-if="reservationList.length > 0" class="batch-control-panel">
            <div class="batch-control-left">
              <label class="batch-checkbox-label">
                <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="batch-checkbox" />
                <span>全選</span>
              </label>
              <span class="batch-info">
                已選擇 {{ selectedCount }} 本書籍
              </span>
            </div>
            <div class="batch-control-right">
              <button @click="removeSelected" class="batch-btn batch-btn-remove" :disabled="selectedCount === 0">
                移除選取
              </button>
              <button @click="batchReserve" class="batch-btn batch-btn-reserve" :disabled="selectedCount === 0">
                批量預約 ({{ selectedCount }})
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
            <p>預約清單中沒有書籍</p>
            <p class="history-empty-subtitle">請先搜尋書籍並加入預約清單</p>
            <button @click="goToSearch" class="history-empty-btn">
              前往搜尋書籍
            </button>
          </div>

          <!-- 表格視圖 -->
          <div v-else
            :class="['history-table-scroll', itemsPerPage > 10 ? 'history-table-scrollable' : 'history-table-fill']">
            <div v-if="viewMode === 'table'" class="history-grid-table">
              <div class="history-grid-header">
                <div class="history-grid-checkbox">
                  <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="batch-checkbox" />
                </div>
                <div>書名</div>
                <div>作者</div>
                <div>ISBN</div>
                <div>加入時間</div>
                <div>操作</div>
              </div>
              <div class="history-grid-body">
                <div v-for="(book, index) in paginatedBooks" :key="index" class="history-grid-row">
                  <div class="history-grid-checkbox">
                    <input type="checkbox" :checked="selectedBooks.includes(book.id)"
                      @change="toggleSelectBook(book.id)" class="batch-checkbox" />
                  </div>
                  <div class="history-grid-title-cell">{{ book.title }}</div>
                  <div>{{ book.author }}</div>
                  <div>{{ book.isbn }}</div>
                  <div>{{ formatDateTime(book.addedDate) }}</div>
                  <div class="history-grid-actions">
                    <button @click="viewBookDetail(book)" class="history-detail-btn">詳情</button>
                    <button @click="removeFromList(book.id)" class="history-remove-btn">移除</button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="history-grid">
              <div v-for="(book, index) in paginatedBooks" :key="index" class="history-grid-card">
                <div class="history-grid-card-header">
                  <input type="checkbox" :checked="selectedBooks.includes(book.id)" @change="toggleSelectBook(book.id)"
                    class="batch-checkbox" />
                  <button @click="removeFromList(book.id)" class="history-remove-btn-card">×</button>
                </div>
                <div class="history-grid-img-wrap">
                  <img :src="getDefaultCoverUrl(index)" :alt="book.title" class="history-grid-img" />
                </div>
                <div class="history-grid-info">
                  <h3 class="history-grid-title reservation-record-book-title">{{ book.title }}</h3>
                  <p class="history-grid-author">作者：{{ book.author }}</p>
                  <p class="history-grid-isbn">ISBN：{{ book.isbn }}</p>
                  <p class="history-grid-date">加入時間：{{ formatDateTime(book.addedDate) }}</p>
                  <button class="history-detail-btn" @click="viewBookDetail(book)">詳情</button>
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
import { useHead } from '#imports'
import { reservationAPI } from '~/utils/api'

// 設置頁面標題
useHead({
  title: '預約清單'
})

// 獲取 router 實例
const router = useRouter()

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

// 響應式資料
const reservationList = ref([])
const selectedBooks = ref([])
const loading = ref(false)
const error = ref(null)

// 預設封面圖片
function getDefaultCoverUrl(index) {
  return `https://via.placeholder.com/300x400/4ECDC4/FFFFFF?text=${encodeURIComponent('書籍封面')}`
}

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

// 從後端 API 載入預約清單
async function loadReservationList() {
  loading.value = true
  error.value = null

  try {
    console.log('開始載入預約清單...')
    const response = await reservationAPI.getReservationList('current')
    console.log('API 回應：', response.data)

    if (response.data && Array.isArray(response.data)) {
      // 轉換後端資料格式為前端所需格式
      reservationList.value = response.data.map(item => {
        const converted = {
          id: item.reservation_id || item.reservationId || item.id,
          title: item.title,
          author: item.author,
          isbn: item.isbn,
          publisher: item.publisher || '',
          addedDate: item.created_at || item.createdAt || new Date().toISOString(),
          status: item.status || 'active'
        }
        console.log('轉換後的項目：', converted)
        return converted
      })

      console.log('載入的預約清單：', reservationList.value)
      console.log('清單長度：', reservationList.value.length)
    } else {
      console.log('API 回應不是陣列或為空')
      reservationList.value = []
    }
  } catch (err) {
    console.error('載入預約清單失敗：', err)
    error.value = '無法載入預約清單，請稍後再試'
    reservationList.value = []
  } finally {
    loading.value = false
  }
}

// 加入預約清單
async function addToReservationList(book) {
  try {
    console.log('開始加入預約清單，書籍：', book)

    const response = await reservationAPI.addReservation({
      bookId: parseInt(book.isbn) || 1, // 使用 ISBN 作為 bookId
      userId: 1, // 使用數字 ID
      status: 'PENDING',
      reservationDate: new Date().toISOString().slice(0, 19).replace('T', ' ') // 格式化為後端期望的格式
    })

    console.log('加入預約 API 回應：', response.data)

    if (response.data && response.data.success) {
      console.log('預約加入成功，重新載入清單...')
      // 重新載入清單以獲取最新資料
      await loadReservationList()
      alert('已成功加入預約清單！')
      return true
    } else {
      console.log('預約加入失敗：', response.data)
      throw new Error('加入失敗')
    }
  } catch (err) {
    console.error('加入預約清單失敗：', err)
    if (err.response?.status === 409) {
      alert('此書籍已在預約清單中')
    } else if (err.response?.data?.message) {
      alert(`加入失敗：${err.response.data.message}`)
    } else {
      alert('加入預約清單失敗，請稍後再試')
    }
    return false
  }
}

// 從預約清單移除
async function removeFromList(bookId) {
  try {
    const response = await reservationAPI.deleteReservation(bookId)

    if (response.data) {
      // 從本地清單中移除
      const index = reservationList.value.findIndex(item => item.id === bookId)
      if (index !== -1) {
        reservationList.value.splice(index, 1)
      }

      // 同時從選取清單中移除
      const selectedIndex = selectedBooks.value.indexOf(bookId)
      if (selectedIndex !== -1) {
        selectedBooks.value.splice(selectedIndex, 1)
      }
    } else {
      throw new Error('移除失敗')
    }
  } catch (err) {
    console.error('移除書籍失敗：', err)
    alert('移除書籍失敗，請稍後再試')
  }
}

// 移除選取的書籍
async function removeSelected() {
  if (selectedBooks.value.length === 0) {
    alert('請先選擇要移除的書籍')
    return
  }

  try {
    const response = await reservationAPI.batchDeleteReservation(selectedBooks.value)

    if (response.data) {
      // 重新載入清單以獲取最新資料
      await loadReservationList()
      selectedBooks.value = []
      alert(`成功移除 ${selectedBooks.value.length} 本書籍`)
    } else {
      throw new Error('批量移除失敗')
    }
  } catch (err) {
    console.error('批量移除書籍失敗：', err)
    alert('批量移除失敗，請稍後再試')
  }
}

// 切換選取單本書籍
function toggleSelectBook(bookId) {
  const index = selectedBooks.value.indexOf(bookId)
  if (index === -1) {
    selectedBooks.value.push(bookId)
  } else {
    selectedBooks.value.splice(index, 1)
  }
}

// 切換全選
function toggleSelectAll() {
  if (isAllSelected.value) {
    selectedBooks.value = []
  } else {
    selectedBooks.value = paginatedBooks.value.map(book => book.id)
  }
}

// 批量預約
function batchReserve() {
  if (selectedBooks.value.length === 0) {
    alert('請先選擇要預約的書籍')
    return
  }

  const selectedBookData = reservationList.value.filter(book =>
    selectedBooks.value.includes(book.id)
  )

  // 將選取的書籍資料傳遞到預約頁面
  router.push({
    path: '/book-reservation',
    query: {
      batch: 'true',
      books: JSON.stringify(selectedBookData)
    }
  })
}

// 查看書籍詳情
function viewBookDetail(book) {
  router.push({
    path: '/bookinfo',
    query: {
      isbn: book.isbn,
      returnQuery: '',
      returnPage: '1',
      from: 'reservation-list',
      returnType: 'list'
    }
  })
}

// 前往搜尋頁面
function goToSearch() {
  router.push('/catalogue-search')
}

// 計算屬性
const isAllSelected = computed(() => {
  return paginatedBooks.value.length > 0 &&
    paginatedBooks.value.every(book => selectedBooks.value.includes(book.id))
})

const selectedCount = computed(() => selectedBooks.value.length)

// 排序後的資料
const sortedBooks = computed(() => {
  const books = [...reservationList.value]
  const field = sortConfig.value.field
  const ascending = sortConfig.value.ascending

  return books.sort((a, b) => {
    let valueA, valueB

    // 根據不同欄位類型進行排序
    if (field === 'addedDate') {
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

// 排序功能
function toggleSortOrder() {
  sortConfig.value.ascending = !sortConfig.value.ascending
}

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
onMounted(() => {
  loadReservationList()
})

// 暴露方法給其他組件使用
defineExpose({
  addToReservationList
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

/* 批量操作面板樣式 */
.batch-control-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(229, 231, 235, 0.4);
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
}

.batch-control-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.batch-control-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1rem;
  color: #222;
  cursor: pointer;
}

.batch-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #2563eb;
}

.batch-info {
  font-size: 0.95rem;
  color: #4b5563;
}

.batch-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid;
}

.batch-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.batch-btn-remove {
  background: #fff;
  color: #dc2626;
  border-color: #dc2626;
}

.batch-btn-remove:hover:not(:disabled) {
  background: #dc2626;
  color: #fff;
}

.batch-btn-reserve {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}

.batch-btn-reserve:hover:not(:disabled) {
  background: #1d4ed8;
  border-color: #1d4ed8;
}

.history-table-scroll {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
  overflow: hidden;
}

.history-table-scrollable {
  max-height: 600px;
  overflow-y: auto;
}

.history-table-fill {
  min-height: 400px;
}

.history-grid-table {
  width: 100%;
}

.history-grid-header {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 120px;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
  font-weight: 600;
  color: #222;
  font-size: 0.95rem;
}

.history-grid-body {
  max-height: 500px;
  overflow-y: auto;
}

.history-grid-row {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 120px;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(229, 231, 235, 0.2);
  align-items: center;
  transition: background 0.2s;
}

.history-grid-row:hover {
  background: rgba(243, 244, 246, 0.3);
}

.history-grid-row:last-child {
  border-bottom: none;
}

.history-grid-checkbox {
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-grid-title-cell {
  font-weight: 500;
  color: #18181b;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.history-grid-actions {
  display: flex;
  gap: 8px;
}

.history-detail-btn {
  padding: 6px 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
}

.history-detail-btn:hover {
  background: #1d4ed8;
}

.history-remove-btn {
  padding: 6px 12px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
}

.history-remove-btn:hover {
  background: #b91c1c;
}

.history-remove-btn-card {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.history-remove-btn-card:hover {
  background: #b91c1c;
}

.history-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  padding: 20px;
}

.history-grid-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}

.history-grid-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.history-grid-card-header {
  position: relative;
  padding: 12px;
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-grid-img-wrap {
  width: 100%;
  height: 200px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
}

.history-grid-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.history-grid-info {
  padding: 16px;
}

.history-grid-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #18181b;
  margin-bottom: 8px;
  line-height: 1.4;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.history-grid-author,
.history-grid-isbn,
.history-grid-date {
  font-size: 0.9rem;
  color: #4b5563;
  margin-bottom: 4px;
}

.history-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #4b5563;
}

.history-loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.history-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #dc2626;
  text-align: center;
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
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 16px;
}

.history-error-details {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 4px;
  padding: 12px;
  margin-top: 12px;
  font-size: 0.9rem;
  color: #991b1b;
  max-width: 100%;
  overflow-x: auto;
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: #6b7280;
}

.history-empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.history-empty-subtitle {
  font-size: 1rem;
  color: #9ca3af;
  margin-bottom: 24px;
}

.history-empty-btn {
  padding: 12px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.history-empty-btn:hover {
  background: #1d4ed8;
}

.history-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  border: 1px solid rgba(229, 231, 235, 0.4);
}

.history-pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.history-pagination-btn {
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9rem;
}

.history-pagination-btn:hover:not(:disabled) {
  background: #f3f4f6;
}

.history-pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.history-pagination-input {
  width: 60px;
  padding: 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  text-align: center;
  font-size: 0.9rem;
}

.history-pagination-info {
  font-size: 0.9rem;
  color: #6b7280;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .history-control-panel {
    flex-direction: column;
    align-items: stretch;
  }

  .history-control-panel-left,
  .history-control-panel-right {
    justify-content: center;
  }

  .batch-control-panel {
    flex-direction: column;
    gap: 16px;
  }

  .batch-control-left,
  .batch-control-right {
    justify-content: center;
  }

  .history-grid-header,
  .history-grid-row {
    grid-template-columns: 40px 1.5fr 1fr 80px;
    font-size: 0.9rem;
  }

  .history-grid-header>div:nth-child(4),
  .history-grid-header>div:nth-child(5),
  .history-grid-row>div:nth-child(4),
  .history-grid-row>div:nth-child(5) {
    display: none;
  }

  .history-grid {
    grid-template-columns: 1fr;
  }
}
</style>