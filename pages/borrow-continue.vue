<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="history-bg">
        <h1 class="history-title">借閱續借</h1>
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
                <div>續借次數</div>
                <div>功能</div>
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
                  <div :class="{ 
                    'text-red-600 font-medium': isOverdue(book.dueDate) && !book.isReturned,
                    'text-green-600 font-medium': book.isReturned
                  }">
                    {{ formatDueDate(book.dueDate, book.isReturned) }}
                  </div>
                  <div>{{ book.renewCount }}/2</div>
                  <div>
                    <button 
                      @click="renewBook(book)" 
                      class="history-detail-btn"
                      :class="{ 
                        'history-detail-btn-disabled': !canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned,
                        'history-detail-btn-overdue': isOverdue(book.dueDate) && !book.isReturned,
                        'history-detail-btn-returned': book.isReturned
                      }"
                      :disabled="!canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned || isOverdue(book.dueDate)"
                    >
                      {{ getButtonText(book) }}
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
                  <button 
                    class="history-detail-btn"
                    :class="{ 
                      'history-detail-btn-disabled': !canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned,
                      'history-detail-btn-overdue': isOverdue(book.dueDate) && !book.isReturned,
                      'history-detail-btn-returned': book.isReturned
                    }"
                    @click="renewBook(book)"
                    :disabled="!canRenew(book.dueDate) || book.renewCount >= 2 || book.isReturned || isOverdue(book.dueDate)"
                  >
                    {{ getButtonText(book) }}
                  </button>
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

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'

// 檢查是否可以續借（到期日前3天）
function canRenew(dueDate) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const dueDateObj = new Date(dueDate)
  const diffTime = dueDateObj.getTime() - today.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 3 && diffDays > 0
}

// 檢查是否逾期
function isOverdue(dueDate) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const dueDateObj = new Date(dueDate)
  return today > dueDateObj
}

// 格式化到期日顯示
function formatDueDate(dueDate, isReturned) {
  if (isReturned) {
    return '已歸還'
  }
  if (isOverdue(dueDate)) {
    return `${dueDate} (逾期)`
  }
  return dueDate
}

// 取得按鈕文字
function getButtonText(book) {
  if (book.isReturned) {
    return '已歸還'
  }
  if (isOverdue(book.dueDate)) {
    return '已逾期'
  }
  if (book.renewCount >= 2) {
    return '已達上限'
  }
  if (!canRenew(book.dueDate)) {
    return '尚未到續借時間'
  }
  return '續借'
}

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

// 串接API取得1號會員的歷史紀錄
const borrowedBooks = ref([])

// 預設封面圖片
function getDefaultCoverUrl(index) {
  const colors = ['#f87171', '#fb923c', '#fbbf24', '#34d399', '#60a5fa', '#818cf8', '#a78bfa', '#f472b6']
  const colorIndex = index % colors.length
  return `data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100"><rect width="100" height="100" fill="${colors[colorIndex]}"/><text x="50" y="50" font-family="Arial" font-size="14" fill="white" text-anchor="middle" dominant-baseline="middle">無封面</text></svg>`
}

// 重試機制
async function retryRequest(url, options, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      console.log(`嘗試第 ${i + 1} 次請求...`);
      const response = await axios.get(url, options);
      return response;
    } catch (err) {
      if (i === maxRetries - 1) throw err;
      console.log(`第 ${i + 1} 次請求失敗，等待重試...`);
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}

async function fetchBorrowHistory() {
  try {
    console.log('開始獲取借閱記錄...');
    const res = await retryRequest('http://localhost:8080/api/borrows/member/1/history', {
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      timeout: 15000
    });
    
    console.log('API 響應狀態:', res.status);
    console.log('API 響應數據:', res.data);
    
    if (!Array.isArray(res.data)) {
      console.error('API 響應不是數組:', res.data);
      borrowedBooks.value = [];
      return;
    }
    
    // 確保所有記錄都被處理
    borrowedBooks.value = res.data.map(item => {
      console.log('處理借閱記錄:', item);
      
      // 檢查並記錄關聯資料
      if (!item.book) {
        console.warn('借閱記錄缺少書籍資訊:', item.borrowId);
      }
      if (!item.member) {
        console.warn('借閱記錄缺少會員資訊:', item.borrowId);
      }
      
      // 建立處理後的記錄
      const processedItem = {
        id: item.borrowId,
        title: item.book?.title || '無標題',
        author: item.book?.author || '無作者',
        borrowDate: item.borrowDate?.split('T')[0] || '',
        dueDate: item.dueDate?.split('T')[0] || '',
        renewCount: item.renewCount || 0,
        isReturned: item.status === 'RETURNED',
        status: item.status,
        book: item.book,
        member: item.member,
        coverUrl: item.book?.coverUrl || null
      };
      
      console.log('處理後的記錄:', processedItem);
      return processedItem;
    });
    
    // 檢查處理後的資料
    if (borrowedBooks.value.length === 0) {
      console.warn('沒有找到任何借閱記錄');
    } else {
      console.log('處理完成，共', borrowedBooks.value.length, '筆記錄');
      borrowedBooks.value.forEach((book, index) => {
        console.log(`第 ${index + 1} 筆記錄:`, book);
      });
    }
  } catch (err) {
    console.error('獲取借閱記錄失敗:');
    console.error('錯誤類型:', err.name);
    console.error('錯誤訊息:', err.message);
    
    if (err.response) {
      // 伺服器回應了錯誤狀態碼
      console.error('錯誤狀態碼:', err.response.status);
      console.error('錯誤資料:', err.response.data);
      console.error('錯誤標頭:', err.response.headers);
    } else if (err.request) {
      // 請求已發送但沒有收到回應
      console.error('請求已發送但沒有收到回應');
      console.error('請求配置:', err.config);
      
      // 如果是超時錯誤，嘗試重新連接
      if (err.code === 'ECONNABORTED') {
        console.log('嘗試重新連接...');
        try {
          const retryRes = await retryRequest('http://localhost:8080/api/borrows/test', {
            timeout: 5000
          });
          console.log('後端服務測試回應:', retryRes.data);
        } catch (retryErr) {
          console.error('後端服務測試失敗:', retryErr.message);
        }
      }
    } else {
      // 請求設定時發生錯誤
      console.error('請求設定錯誤:', err.message);
    }
    
    if (err.code) {
      console.error('錯誤代碼:', err.code);
    }
    
    borrowedBooks.value = [];
  }
}

// 重新整理資料
async function refreshData() {
  try {
    await fetchBorrowHistory();
  } catch (err) {
    console.error('重新整理資料失敗:', err);
  }
}

onMounted(async () => {
  try {
    await fetchBorrowHistory();
  } catch (err) {
    console.error('初始化資料失敗:', err);
  }
})

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

// 續借功能（前端假動作，實際應串接後端續借API）
function renewBook(book) {
  if (book.isReturned) {
    alert('此書已歸還')
    return
  }
  if (book.renewCount >= 2) {
    alert('此書已達到續借上限，無法再次續借')
    return
  }
  if (isOverdue(book.dueDate)) {
    alert('此書已逾期，無法續借')
    return
  }
  if (!canRenew(book.dueDate)) {
    alert('尚未到續借時間（到期前3天內才能續借）')
    return
  }

  // 這裡可串接後端續借API
  alert('請串接後端續借API')


  // 取得當前到期日
  const currentDueDate = new Date(book.dueDate)
  

  // 計算新的到期日（當前到期日 + 30天）
  const newDueDate = new Date(currentDueDate)
  newDueDate.setDate(newDueDate.getDate() + 30)
  
  // 更新書籍的到期日
  book.dueDate = newDueDate.toISOString().split('T')[0]
  // 增加續借次數
  book.renewCount++

}

// 添加 goToPage 函數
function goToPage(page) {
  const pageNum = parseInt(page)
  if (pageNum && !isNaN(pageNum) && pageNum >= 1 && pageNum <= totalPages.value) {
    currentPage.value = pageNum

  }
}

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
  grid-template-columns: 2fr 1.5fr 1fr 1fr 0.8fr 0.8fr;
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
  border-bottom: 1px solid rgba(229, 231, 235, 0.4);
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

.history-grid-dates {
  font-size: 0.9rem;
  color: #4b5563;
  margin-bottom: 8px;
  flex-grow: 1;
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
  width: 100%;
  margin-top: auto;
}

.history-detail-btn:hover {
  background: #1d4ed8;
}

.history-detail-btn-disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.history-detail-btn-disabled:hover {
  background: #9ca3af;
}

.history-detail-btn-overdue {
  background: #dc2626;
}

.history-detail-btn-overdue:hover {
  background: #b91c1c;
}

.history-detail-btn-returned {
  background: #16a34a;
  cursor: not-allowed;
}

.history-detail-btn-returned:hover {
  background: #16a34a;
}

.text-red-600 {
  color: #dc2626;
}

.text-green-600 {
  color: #16a34a;
}

.font-medium {
  font-weight: 500;
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

.history-pagination-input::-webkit-outer-spin-button,
.history-pagination-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.history-pagination-input[type=number] {
  appearance: textfield;
  -moz-appearance: textfield;
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
    grid-template-columns: 1.5fr 1fr 1fr 1fr 0.8fr 0.8fr;
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
</style>