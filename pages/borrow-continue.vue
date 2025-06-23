<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="history-bg">
        <h1 class="history-title">借閱續借</h1>

        <!-- 登入檢查 -->
        <div v-if="!isLoggedIn" class="login-required">
          <div class="login-required-icon">🔒</div>
          <h2>需要登入會員</h2>
          <p>您需要登入會員才能使用借閱續借功能</p>
          <button @click="goToLogin" class="login-required-btn">
            前往登入
          </button>
        </div>

        <!-- 借閱續借內容（只有登入後才顯示） -->
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
              <button @click="viewMode = 'table'"
                :class="['history-view-btn', viewMode === 'table' ? 'history-view-btn-active' : '']">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                  class="history-view-icon">
                  <path d="M3 12h.01"></path>
                  <path d="M3 18h.01"></path>
                  <path d="M3 6h.01"></path>
                  <path d="M8 12h13"></path>
                  <path d="M8 18h13"></path>
                  <path d="M8 6h13"></path>
                </svg>
                表格
              </button>
              <button @click="viewMode = 'grid'"
                :class="['history-view-btn', viewMode === 'grid' ? 'history-view-btn-active' : '']">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                  class="history-view-icon">
                  <rect width="7" height="7" x="3" y="3" rx="1" />
                  <rect width="7" height="7" x="14" y="3" rx="1" />
                  <rect width="7" height="7" x="14" y="14" rx="1" />
                  <rect width="7" height="7" x="3" y="14" rx="1" />
                </svg>
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
                <div>續借次數</div>
                <div>功能</div>
              </div>
              <div class="history-grid-body">
                <div v-for="(book, index) in paginatedBooks" :key="index" class="history-grid-row">
                  <div>{{ book.title }}</div>
                  <div>{{ book.author }}</div>
                  <div>{{ book.borrowDate }}</div>
                  <div :class="{
                    'text-red-600 font-medium': isOverdue(book.dueDate) && !book.isReturned,
                    'text-green-600 font-medium': book.isReturned
                  }">
                    {{ formatDueDate(book.dueDate, book.isReturned) }}
                  </div>
                  <div>{{ book.renewCount }}/2</div>
                  <div class="history-grid-actions">
                    <button @click="renewBook(book)" class="history-detail-btn" :class="{
                      'history-detail-btn-disabled': !canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned,
                      'history-detail-btn-overdue': isOverdue(book.dueDate) && !book.isReturned,
                      'history-detail-btn-returned': book.isReturned
                    }"
                      :disabled="!canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned || isOverdue(book.dueDate)">
                      {{ getButtonText(book) }}
                    </button>
                    <button @click="returnBook(book)" class="history-return-btn" :disabled="book.isReturned">
                      還書
                    </button>
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
                    <p :class="{
                      'text-red-600 font-medium': isOverdue(book.dueDate) && !book.isReturned,
                      'text-green-600 font-medium': book.isReturned
                    }">
                      到期日：{{ formatDueDate(book.dueDate, book.isReturned) }}
                    </p>
                    <p>續借次數：{{ book.renewCount }}/2</p>
                  </div>
                  <div class="history-grid-actions">
                    <button class="history-detail-btn" :class="{
                      'history-detail-btn-disabled': !canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned,
                      'history-detail-btn-overdue': isOverdue(book.dueDate) && !book.isReturned,
                      'history-detail-btn-returned': book.isReturned
                    }" @click="renewBook(book)"
                      :disabled="!canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned || isOverdue(book.dueDate)">
                      {{ getButtonText(book) }}
                    </button>
                    <button @click="returnBook(book)" class="history-return-btn" :disabled="book.isReturned">
                      還書
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分頁控制 -->
          <div class="history-pagination">
            <div class="history-pagination-controls">
              <button class="history-pagination-btn" :disabled="currentPage === 1" @click="currentPage--">
                <span class="sr-only">上一頁</span>
              </button>
              <span>共{{ totalPages }}頁</span>
              <input type="number" :value="currentPage" class="history-pagination-input" min="1" :max="totalPages"
                @change="e => goToPage(parseInt(e.target.value))" />
              <span>/{{ totalPages }}頁</span>
              <button class="history-pagination-btn" :disabled="currentPage >= totalPages" @click="currentPage++">
                <span class="sr-only">下一頁</span>
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
  <CustomAlert :show="customAlert.show" :title="customAlert.title" :message="customAlert.message" @close="closeAlert" />
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { borrowApi, borrowUtils } from '~/utils/borrowApi'
import CustomAlert from '~/components/CustomAlert.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const viewMode = ref('table')
const pageSizes = [10, 20, 30, 50, 100]
const itemsPerPage = ref(10)
const currentPage = ref(1)
const sortConfig = ref({ field: 'title', ascending: true })
const borrowedBooks = ref([])
const loading = ref(false)
const renewing = ref(null)

// 登入狀態檢查
const isLoggedIn = ref(false)

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

// 檢查登入狀態
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

// 跳轉到登入頁面
const goToLogin = () => {
  router.push('/login')
}

// 取得借閱歷史
async function fetchBorrowHistory() {
  loading.value = true
  try {
    const result = await borrowApi.getBorrowHistory()
    if (result.success) {
      borrowedBooks.value = result.data.map(item => ({
        id: item.borrowId,
        title: item.bookTitle || '無標題',
        author: item.bookAuthor || '無作者',
        borrowDate: item.borrowDate?.split('T')[0] || '',
        dueDate: item.dueDate?.split('T')[0] || '',
        renewCount: item.renewCount || 0,
        isReturned: item.status === 'RETURNED',
        status: item.status,
        coverUrl: null
      }))
    } else {
      borrowedBooks.value = []
      showAlert('錯誤', result.message || '取得借閱紀錄失敗')
    }
  } catch (err) {
    borrowedBooks.value = []
    showAlert('錯誤', '取得借閱紀錄失敗')
  } finally {
    loading.value = false
  }
}

// 續借功能
async function renewBook(book) {
  if (book.isReturned) {
    showAlert('提示', '此書已歸還')
    return
  }
  if (book.renewCount >= 2) {
    showAlert('提示', '此書已達到續借上限，無法再次續借')
    return
  }
  if (borrowUtils.isOverdue(book.dueDate)) {
    showAlert('提示', '此書已逾期，無法續借')
    return
  }
  if (!borrowUtils.canRenew(book.dueDate)) {
    showAlert('提示', '尚未到續借時間（到期前3天內才能續借）')
    return
  }
  try {
    renewing.value = book.id
    const checkResponse = await borrowApi.checkRenewable(book.id)
    if (!checkResponse.success || !checkResponse.data.canRenew) {
      showAlert('提示', '此書目前無法續借')
      return
    }
    const response = await borrowApi.renewBook(book.id)
    if (response.success) {
      showAlert('成功', '續借成功！')
      await fetchBorrowHistory()
    } else {
      showAlert('失敗', '續借失敗: ' + response.message)
    }
  } catch (error) {
    showAlert('失敗', '續借失敗: ' + error.message)
  } finally {
    renewing.value = null
  }
}

// 還書功能
async function returnBook(book) {
  if (book.isReturned) {
    showAlert('提示', '此書已歸還');
    return;
  }

  try {
    const response = await borrowApi.returnBook(book.id);
    if (response.success) {
      showAlert('成功', '還書成功！');
      await fetchBorrowHistory();
    } else {
      showAlert('失敗', response.message || '還書失敗');
    }
  } catch (error) {
    showAlert('失敗', error.message || '還書失敗，請稍後再試');
  }
}

// 工具函數直接用 borrowUtils
const canRenew = borrowUtils.canRenew
const isOverdue = borrowUtils.isOverdue
const formatDueDate = borrowUtils.formatDueDate
const getButtonText = borrowUtils.getButtonText

const sortedBooks = computed(() => {
  const books = [...borrowedBooks.value]
  if (sortConfig.value.field) {
    books.sort((a, b) => {
      const fieldA = a[sortConfig.value.field]
      const fieldB = b[sortConfig.value.field]
      let comparison = 0
      if (fieldA > fieldB) {
        comparison = 1
      } else if (fieldA < fieldB) {
        comparison = -1
      }
      return sortConfig.value.ascending ? comparison : -comparison
    })
  }
  return books
})

const totalPages = computed(() => {
  return Math.ceil(sortedBooks.value.length / itemsPerPage.value)
})

const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return sortedBooks.value.slice(start, end)
})

const goToPage = (page) => {
  currentPage.value = page
}

const toggleSortOrder = () => {
  sortConfig.value.ascending = !sortConfig.value.ascending
}

watch(itemsPerPage, () => {
  currentPage.value = 1
})

onMounted(() => {
  // 檢查登入狀態
  checkLoginStatus()

  // 只有登入後才載入借閱歷史
  if (isLoggedIn.value) {
    fetchBorrowHistory()
  }
})

// 封面預設
function getDefaultCoverUrl(index) {
  const colors = ['#4A90E2', '#50E3C2', '#B8E986', '#F8E71C', '#F5A623', '#BD10E0', '#9013FE', '#4A4A4A'];
  const colorIndex = index % colors.length;
  return `data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><rect width="100" height="100" fill="${colors[colorIndex]}"/><text x="50" y="50" font-family="Arial" font-size="14" fill="white" text-anchor="middle" dominant-baseline="middle">無封面</text></svg>`
}
</script>

<style scoped>
.scroll-wrapper {
  height: 100vh;
  overflow-y: auto;
  scrollbar-width: thin;
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
  grid-template-columns: 2fr 1.5fr 1fr 1fr 0.8fr 1.2fr;
  align-items: center;
}

.history-grid-header {
  background: rgba(243, 244, 246, 0.6);
  backdrop-filter: blur(10px);
  color: #222;
  font-weight: 600;
  padding: 12px 0;
}

.history-grid-header>div {
  padding: 12px 16px;
  text-align: center;
}

.history-grid-header>div:first-child {
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
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
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
  background: rgba(243, 244, 246, 0.6);
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
  height: 100%;
  position: relative;
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

.history-detail-btn {
  padding: 6px 14px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-detail-btn:hover:not(:disabled) {
  background-color: #2b6cb0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.history-return-btn {
  padding: 6px 14px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.history-return-btn:hover:not(:disabled) {
  background-color: #c82333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.history-return-btn:disabled {
  background-color: #a0aec0;
  cursor: not-allowed;
  opacity: 0.6;
}

.history-detail-btn-disabled {
  background-color: #a0aec0;
  cursor: not-allowed;
  opacity: 0.6;
}

.history-detail-btn-overdue {
  background: #e53e3e;
}

.history-detail-btn-returned {
  background: #38a169;
}

.text-red-600 {
  color: #e53e3e;
}

.text-green-600 {
  color: #38a169;
}

.font-medium {
  font-weight: 500;
}

.history-grid-dates {
  margin-top: 8px;
  font-size: 0.9rem;
  color: #4a5568;
}

.history-grid-actions {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* Pagination Styles */
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

.history-pagination-info {
  font-size: 0.95rem;
  color: #4b5563;
  text-align: center;
}

/* 登入提示樣式 */
.login-required {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: #6b7280;
  background: transparent;
  border-radius: 12px;
  margin: 20px;
}

.login-required-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.login-required h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.login-required p {
  font-size: 1rem;
  color: #6b7280;
  margin-bottom: 24px;
  max-width: 400px;
}

.login-required-btn {
  padding: 12px 32px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.login-required-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
}

.login-required-btn:active {
  transform: translateY(0);
}
</style>