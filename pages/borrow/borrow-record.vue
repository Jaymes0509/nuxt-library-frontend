<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="history-bg">
        <h1 class="history-title">借書清單</h1>

        <!-- 登入檢查 -->
        <LoginRequiredPrompt v-if="!isLoggedIn" message="您需要登入才能使用借書功能" />

        <!-- 借書清單內容（只有登入後才顯示） -->
        <div v-else class="history-main">
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
          <div v-if="borrowList.length > 0" class="batch-control-panel">
            <div class="batch-control-left">
              <label class="batch-checkbox-label">
                <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="batch-checkbox" />
                <span>全選</span>
              </label>
              <span class="batch-info">
                已選擇 {{ selectedCount }} 本書籍
              </span>
              <span v-if="selectedCount > 10" class="batch-warning">
                (一次最多只能借閱10本書)
              </span>
            </div>
            <div class="batch-control-right">
              <button @click="removeSelected" class="batch-btn batch-btn-remove" :disabled="selectedCount === 0">
                移除選取
              </button>
              <button @click="batchBorrow" class="batch-btn batch-btn-borrow"
                :disabled="selectedCount === 0 || selectedCount > 10">
                確認借閱 ({{ selectedCount }})
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
            <p>借書清單中沒有書籍</p>
            <p class="history-empty-subtitle">請先搜尋書籍並加入借書清單</p>
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
                  <img :src="getDefaultCoverUrl(book)" :alt="book.title" class="history-grid-img" />
                </div>
                <div class="history-grid-info">
                  <h3 class="history-grid-title borrow-record-book-title">{{ book.title }}</h3>
                  <p class="history-grid-author">作者：{{ book.author }}</p>
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
    <CustomAlert :show="customAlert.show" :title="customAlert.title" :message="customAlert.message"
      @close="closeAlert" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '#imports'
import CustomAlert from '~/components/CustomAlert.vue'
import { borrowApi } from '~/utils/borrowApi'

// ===== 頁面設定 =====
useHead({ title: '借書清單' })

// ===== 響應式資料 =====
const router = useRouter()
const viewMode = ref('table')
const borrowList = ref([])
const selectedBooks = ref([])
const loading = ref(false)
const error = ref(null)
const loginError = ref(false)

// 登入狀態檢查
const isLoggedIn = ref(false)

// 自訂提示視窗
const customAlert = ref({
  show: false,
  title: '',
  message: ''
})

const showAlert = (title, message) => {
  customAlert.value.title = title
  customAlert.value.message = message
  customAlert.value.show = true
}

const closeAlert = () => {
  customAlert.value.show = false
}

// 分頁設定
const pageSizes = [10, 20, 30, 50, 100]
const itemsPerPage = ref(10)
const currentPage = ref(1)

// 排序設定
const sortConfig = ref({
  field: 'title',
  ascending: true
})

// ===== 工具函數 =====
const getDefaultCoverUrl = (book) => {
  // 如果有書籍的 imgUrl，使用它
  if (book.imgUrl) {
    return book.imgUrl
  }

  // 如果有 ISBN，嘗試從 OpenLibrary 獲取封面
  if (book.isbn) {
    return `https://covers.openlibrary.org/b/isbn/${book.isbn}-M.jpg`
  }

  // 否則使用預設封面
  return 'https://cdn-icons-png.flaticon.com/512/2232/2232688.png'
}

const formatDateTime = (dateTimeStr) => {
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

// ===== 核心操作 =====
const loadBorrowList = async () => {
  loading.value = true
  error.value = null

  try {
    console.log('開始載入借書清單...')
    const savedList = localStorage.getItem('borrowList')

    if (savedList) {
      const parsedList = JSON.parse(savedList)
      // 為每本書添加加入時間（如果沒有）
      borrowList.value = parsedList.map(book => ({
        ...book,
        addedDate: book.addedDate || new Date().toISOString()
      }))
    } else {
      borrowList.value = []
    }

    console.log('載入的借書清單：', borrowList.value)
  } catch (err) {
    console.error('載入借書清單失敗', err)
    error.value = '載入借書清單失敗'
    borrowList.value = []
  } finally {
    loading.value = false
  }
}

const removeFromList = async (bookId) => {
  try {
    console.log('嘗試移除書籍，ID：', bookId)

    // 從 localStorage 中移除
    const updatedList = borrowList.value.filter(book => book.id !== bookId)
    borrowList.value = updatedList
    localStorage.setItem('borrowList', JSON.stringify(updatedList))

    removeFromSelection(bookId)
    showAlert('成功', '移除成功！')
  } catch (err) {
    console.error('移除書籍失敗', err)
    showAlert('移除失敗', '移除書籍失敗')
  }
}

const removeSelected = async () => {
  if (selectedBooks.value.length === 0) {
    showAlert('提示', '請先選擇要移除的書籍')
    return
  }

  try {
    const updatedList = borrowList.value.filter(book => !selectedBooks.value.includes(book.id))
    borrowList.value = updatedList
    localStorage.setItem('borrowList', JSON.stringify(updatedList))
    selectedBooks.value = []

    showAlert('成功', `成功移除 ${selectedBooks.value.length} 本書籍`)
  } catch (err) {
    console.error('批量移除書籍失敗：', err)
    showAlert('錯誤', '批量移除失敗，請稍後再試')
  }
}

// ===== 輔助函數 =====
const removeFromSelection = (bookId) => {
  const selectedIndex = selectedBooks.value.indexOf(bookId)
  if (selectedIndex !== -1) {
    selectedBooks.value.splice(selectedIndex, 1)
    console.log('從選取清單移除成功')
  }
}

// ===== 選取操作 =====
const toggleSelectBook = (bookId) => {
  const index = selectedBooks.value.indexOf(bookId)
  if (index === -1) {
    selectedBooks.value.push(bookId)
  } else {
    selectedBooks.value.splice(index, 1)
  }
}

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedBooks.value = []
  } else {
    selectedBooks.value = paginatedBooks.value.map(book => book.id)
  }
}

// ===== 導航函數 =====
const viewBookDetail = (book) => {
  router.push({
    path: '/borrow-bookinfo',
    query: {
      id: book.id,
      isbn: book.isbn,
      title: book.title,
      author: book.author,
      publisher: book.publisher,
      publishdate: book.publishdate,
      imgUrl: book.imgUrl,
      returnQuery: '',
      returnPage: '1',
      from: 'borrow-list',
      returnType: 'list'
    }
  })
}

const goToSearch = () => {
  router.push('/borrow-search')
}

const batchBorrow = async () => {
  if (selectedBooks.value.length === 0) {
    showAlert('提示', '請至少選擇一本書')
    return
  }

  // 使用預設值構造請求體
  const borrowRequest = {
    books: selectedBooks.value.map(bookId => ({
      bookId: bookId,
      duration: 30, // 預設 30 天
      location: '一樓服務台', // 預設地點
      method: '親自借書' // 預設方式
    }))
  }

  try {
    const response = await borrowApi.borrowBooks(borrowRequest)
    showAlert('成功', '借閱成功！')

    // 從 borrowList 和 localStorage 中移除已借閱的書籍
    const borrowedIds = new Set(selectedBooks.value);
    borrowList.value = borrowList.value.filter(b => !borrowedIds.has(b.id));
    localStorage.setItem('borrowList', JSON.stringify(borrowList.value));

    // 清空選取
    selectedBooks.value = [];

  } catch (err) {
    console.error('借書失敗:', err)
    showAlert('失敗', err.message || '借書失敗，請稍後再試')
  }
}

const fetchBorrowList = async () => {
  loading.value = true
  error.value = null
  loginError.value = false // 重置登入錯誤狀態

  try {
    await loadBorrowList()
  } catch (err) {
    console.error('載入借書清單失敗', err)
    // 假設 API 對於未登入或 token 失效會回傳 401 或 403
    if (err.response && (err.response.status === 401 || err.response.status === 403)) {
      isLoggedIn.value = false
      loginError.value = true
    } else {
      error.value = '載入借書清單失敗'
    }
  } finally {
    loading.value = false
  }
}

// ===== 排序和分頁 =====
const toggleSortOrder = () => {
  sortConfig.value.ascending = !sortConfig.value.ascending
}

const goToPage = (page) => {
  const pageNum = parseInt(page)
  if (pageNum && !isNaN(pageNum) && pageNum >= 1 && pageNum <= totalPages.value) {
    currentPage.value = pageNum
  }
}

// ===== 計算屬性 =====
const isAllSelected = computed(() => {
  return paginatedBooks.value.length > 0 &&
    paginatedBooks.value.every(book => selectedBooks.value.includes(book.id))
})

const selectedCount = computed(() => selectedBooks.value.length)

const sortedBooks = computed(() => {
  const books = [...borrowList.value]
  const field = sortConfig.value.field
  const ascending = sortConfig.value.ascending

  return books.sort((a, b) => {
    let valueA, valueB

    if (field === 'addedDate') {
      valueA = new Date(a[field] || 0).getTime()
      valueB = new Date(b[field] || 0).getTime()
    } else {
      valueA = (a[field] || '').toString().toLowerCase()
      valueB = (b[field] || '').toString().toLowerCase()
    }

    if (valueA < valueB) return ascending ? -1 : 1
    if (valueA > valueB) return ascending ? 1 : -1
    return 0
  })
})

const totalPages = computed(() => Math.ceil(sortedBooks.value.length / itemsPerPage.value))

const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return sortedBooks.value.slice(start, end)
})

// ===== 監聽器 =====
watch(itemsPerPage, () => {
  currentPage.value = 1
})

watch([() => sortConfig.value.field, () => sortConfig.value.ascending], () => {
  currentPage.value = 1
})

// ===== 生命週期 =====
onMounted(async () => {
  // 檢查登入狀態
  checkLoginStatus()

  // 只有登入後才載入借書清單
  if (isLoggedIn.value) {
    await fetchBorrowList()
  }
})

// ===== 登入狀態檢查 =====
const checkLoginStatus = () => {
  // 檢查 localStorage 中的用戶資訊
  const storedUser = localStorage.getItem('user')
  const jwtToken = localStorage.getItem('jwt_token')
  const authToken = localStorage.getItem('authToken')

  console.log('=== 登入狀態檢查 ===')
  console.log('storedUser:', storedUser)
  console.log('jwtToken:', jwtToken)
  console.log('authToken:', authToken)

  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      isLoggedIn.value = true
      console.log('✅ 用戶已登入：', user)
    } catch (e) {
      console.error('❌ 解析用戶資訊失敗：', e)
      isLoggedIn.value = false
    }
  } else if (jwtToken || authToken) {
    // 如果有 token 但沒有用戶資訊，也視為已登入
    isLoggedIn.value = true
    console.log('✅ 檢測到登入 token')
  } else {
    isLoggedIn.value = false
    console.log('❌ 用戶未登入')
  }

  console.log('最終登入狀態：', isLoggedIn.value)
  console.log('==================')
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

.history-sort-btn:hover {
  background: #f3f4f6;
}

.history-view-btn {
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
  min-width: 80px;
}

.history-view-btn:hover {
  background: #f3f4f6;
}

.history-view-btn-active {
  background: #1976d2;
  color: #fff;
  border-color: #1976d2;
}

.history-view-btn-active:hover {
  background: #1565c0;
}

/* 批量操作面板 */
.batch-control-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  gap: 16px;
  flex-wrap: wrap;
}

.batch-control-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.batch-control-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.batch-checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 1rem;
  color: #18181b;
}

.batch-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.batch-info {
  font-size: 1rem;
  color: #18181b;
}

.batch-warning {
  font-size: 0.9rem;
  color: #dc3545;
  font-weight: 500;
}

.batch-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
}

.batch-btn-remove {
  background: #dc3545;
  color: #fff;
}

.batch-btn-remove:hover:not(:disabled) {
  background: #c82333;
}

.batch-btn-borrow {
  background: #28a745;
  color: #fff;
  margin-left: auto;
  /* 將按鈕推到最右邊 */
}

.batch-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 載入中狀態 */
.history-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  gap: 16px;
}

.history-loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #1976d2;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* 錯誤狀態 */
.history-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  gap: 16px;
  text-align: center;
}

.history-error-icon {
  width: 48px;
  height: 48px;
  background: #dc3545;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

.history-error-details {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  font-family: monospace;
  font-size: 0.9rem;
  color: #6c757d;
  max-width: 100%;
  overflow-x: auto;
  white-space: pre-wrap;
}

/* 無資料狀態 */
.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  gap: 16px;
  text-align: center;
}

.history-empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.history-empty-subtitle {
  color: #6c757d;
  margin-bottom: 24px;
}

.history-empty-btn {
  padding: 12px 24px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.3s ease;
}

.history-empty-btn:hover {
  background: #1565c0;
}

/* 表格視圖 */
.history-table-scroll {
  border: 1px solid #e9ecef;
  border-radius: 8px;
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
  background: #fff;
}

.history-grid-header {
  display: grid;
  grid-template-columns: 50px 1fr 1fr 1fr 120px;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
  font-weight: 600;
  color: #18181b;
  align-items: center;
}

.history-grid-body {
  max-height: 500px;
  overflow-y: auto;
}

.history-grid-row {
  display: grid;
  grid-template-columns: 50px 1fr 1fr 1fr 120px;
  gap: 16px;
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
  align-items: center;
  transition: background 0.2s ease;
}

.history-grid-row:hover {
  background: #f8f9fa;
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
}

.history-grid-actions {
  display: flex;
  gap: 8px;
}

.history-detail-btn {
  padding: 6px 12px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.history-detail-btn:hover {
  background: #1565c0;
}

.history-remove-btn {
  padding: 6px 12px;
  background: #dc3545;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.history-remove-btn:hover {
  background: #c82333;
}

/* 網格視圖 */
.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  padding: 24px;
  background: #fff;
}

.history-grid-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}

.history-grid-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.history-grid-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.history-remove-btn-card {
  width: 24px;
  height: 24px;
  background: #dc3545;
  color: #fff;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.history-remove-btn-card:hover {
  background: #c82333;
}

.history-grid-img-wrap {
  width: 100%;
  height: 200px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
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
  margin: 0 0 8px 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #18181b;
  line-height: 1.4;
}

.borrow-record-book-title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-grid-author {
  margin: 0 0 8px 0;
  color: #6c757d;
  font-size: 0.9rem;
}

.history-grid-date {
  margin: 0 0 16px 0;
  color: #6c757d;
  font-size: 0.9rem;
}

/* 分頁控制 */
.history-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px;
  background: #fff;
  border-top: 1px solid #e9ecef;
}

.history-pagination-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.history-pagination-btn {
  width: 40px;
  height: 40px;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #18181b;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 1rem;
}

.history-pagination-btn:hover:not(:disabled) {
  background: #f3f4f6;
  border-color: #9ca3af;
}

.history-pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.history-pagination-input {
  width: 60px;
  height: 40px;
  text-align: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  color: #18181b;
  background: #fff;
}

.history-pagination-input:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
}

.history-pagination-info {
  font-size: 0.9rem;
  color: #6c757d;
  text-align: center;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .history-bg {
    padding: 16px 16px 80px 16px;
  }

  .history-control-panel {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .history-control-panel-left {
    gap: 16px;
  }

  .history-control-panel-right {
    justify-content: center;
  }

  .batch-control-panel {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .batch-control-left {
    justify-content: center;
  }

  .batch-control-right {
    justify-content: center;
  }

  .history-grid-header {
    grid-template-columns: 40px 1fr 1fr 1fr 100px;
    gap: 8px;
    padding: 12px;
    font-size: 0.9rem;
  }

  .history-grid-row {
    grid-template-columns: 40px 1fr 1fr 1fr 100px;
    gap: 8px;
    padding: 12px;
    font-size: 0.9rem;
  }

  .history-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 16px;
  }

  .history-pagination-controls {
    gap: 8px;
  }

  .history-pagination-btn {
    width: 36px;
    height: 36px;
  }

  .history-pagination-input {
    width: 50px;
    height: 36px;
  }
}

@media (max-width: 480px) {
  .history-grid-header {
    grid-template-columns: 30px 1fr 1fr 1fr 80px;
    gap: 4px;
    padding: 8px;
    font-size: 0.8rem;
  }

  .history-grid-row {
    grid-template-columns: 30px 1fr 1fr 1fr 80px;
    gap: 4px;
    padding: 8px;
    font-size: 0.8rem;
  }

  .history-grid-actions {
    flex-direction: column;
    gap: 4px;
  }

  .history-detail-btn,
  .history-remove-btn {
    padding: 4px 8px;
    font-size: 0.8rem;
  }
}
</style>