<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">預約紀錄</h1>
    <div class="space-y-4">
      <!-- 控制面板 -->
      <div class="control-panel">
        <div class="control-panel-left">
          <div class="flex items-center gap-2">
            <span class="text-sm">每頁顯示：</span>
            <select v-model="itemsPerPage" class="w-[120px] border rounded px-2 py-1">
              <option v-for="size in pageSizes" :key="size" :value="size">{{ size }} 筆</option>
            </select>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm">排序：</span>
            <select v-model="sortConfig.field" class="w-[120px] border rounded px-2 py-1">
              <option value="title">書名</option>
              <option value="author">作者</option>
              <option value="borrowDate">借閱日</option>
              <option value="dueDate">到期日</option>
            </select>
            <button @click="toggleSortOrder" class="border rounded px-2 py-1">
              <span v-if="sortConfig.ascending">↑ 升冪</span>
              <span v-else>↓ 降冪</span>
            </button>
          </div>
        </div>
        <div class="control-panel-right">
          <button
            @click="viewMode = 'table'"
            :class="[
              'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 border border-input shadow-sm hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2',
              viewMode === 'table' ? 'bg-primary text-primary-foreground' : ''
            ]"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-list-icon h-4 w-4 mr-2"><path d="M3 12h.01"></path><path d="M3 18h.01"></path><path d="M3 6h.01"></path><path d="M8 12h13"></path><path d="M8 18h13"></path><path d="M8 6h13"></path></svg>
            表格
          </button>
          <button
            @click="viewMode = 'grid'"
            :class="[
              'inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 border border-input shadow-sm hover:bg-accent hover:text-accent-foreground h-9 px-4 py-2',
              viewMode === 'grid' ? 'bg-primary text-primary-foreground' : ''
            ]"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-grid h-4 w-4 mr-2"><rect width="7" height="7" x="3" y="3" rx="1"/><rect width="7" height="7" x="14" y="3" rx="1"/><rect width="7" height="7" x="14" y="14" rx="1"/><rect width="7" height="7" x="3" y="14" rx="1"/></svg>
            網格
          </button>
        </div>
      </div>

      <!-- 表格視圖 -->
      <div
        :class="['table-scroll-wrapper', itemsPerPage > 10 ? 'scrollable' : 'fill-screen']"
      >
        <table v-if="viewMode === 'table'" class="w-full border-separate border-spacing-0 rounded-lg overflow-hidden bg-gray-800 text-gray-100">
          <thead class="bg-gray-700 text-gray-200">
            <tr>
              <th class="cursor-pointer px-4 py-3 text-left font-semibold" @click="updateSort('title')">書名 {{ getSortIcon('title') }}</th>
              <th class="cursor-pointer px-4 py-3 text-left font-semibold" @click="updateSort('author')">作者 {{ getSortIcon('author') }}</th>
              <th class="cursor-pointer px-4 py-3 text-left font-semibold" @click="updateSort('borrowDate')">借閱日 {{ getSortIcon('borrowDate') }}</th>
              <th class="cursor-pointer px-4 py-3 text-left font-semibold" @click="updateSort('dueDate')">到期日 {{ getSortIcon('dueDate') }}</th>
              <th class="px-4 py-3 text-left font-semibold">書本資訊</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(book, index) in paginatedBooks" :key="index" class="border-b border-gray-700 hover:bg-gray-700/60 transition">
              <td class="px-4 py-3 align-middle">{{ book.title }}</td>
              <td class="px-4 py-3 align-middle">{{ book.author }}</td>
              <td class="px-4 py-3 align-middle">{{ book.borrowDate }}</td>
              <td class="px-4 py-3 align-middle">{{ book.dueDate }}</td>
              <td class="px-4 py-3 align-middle">
                <button @click="viewBookDetail(book)" class="rounded-md px-4 py-1 bg-blue-500 hover:bg-blue-600 text-white font-medium shadow-sm transition">詳情</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          <div v-for="(book, index) in paginatedBooks" 
               :key="index"
               class="border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow">
            <div class="aspect-[3/4] relative bg-gray-100">
              <img :src="book.coverUrl || getDefaultCoverUrl(index)" 
                   :alt="book.title"
                   class="object-cover w-full h-full" />
            </div>
            <div class="p-4 space-y-2">
              <h3 class="font-semibold truncate">{{ book.title }}</h3>
              <p class="text-sm text-gray-600 truncate">{{ book.author }}</p>
              <div class="text-xs text-gray-500">
                <p>借閱日：{{ book.borrowDate }}</p>
                <p>到期日：{{ book.dueDate }}</p>
              </div>
              <button class="w-full border rounded px-2 py-1 bg-blue-600 text-white" @click="viewBookDetail(book)">詳情</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分頁控制 -->
      <div class="pagination-center mt-4">
        <div class="pagination-controls">
          <button 
            class="h-8 w-8 p-0 border rounded flex items-center justify-center"
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
            class="h-8 w-12 text-center border rounded"
            min="1"
            :max="totalPages"
            @change="e => goToPage(parseInt(e.target.value))"
          />
          <span>/{{ totalPages }}頁</span>
          <button 
            class="h-8 w-8 p-0 border rounded flex items-center justify-center"
            :disabled="currentPage >= totalPages"
            @click="currentPage++"
          >
            <span class="sr-only">下一頁</span>
            <span style="display:inline-block;width:1em;">&#8594;</span>
          </button>
        </div>
        <div class="text-sm text-gray-500 text-center">
          顯示第 {{ (currentPage - 1) * itemsPerPage + 1 }} 到 {{ Math.min(currentPage * itemsPerPage, sortedBooks.length) }} 筆，共 {{ sortedBooks.length }} 筆
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
    path: `/book/${book.id}`,
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
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}

@media (min-width: 640px) {
  .grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}

@media (min-width: 768px) {
  .grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  }
}

@media (min-width: 1024px) {
  .grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  }
}

/* 隱藏數字輸入框的上下箭頭 */
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
  appearance: textfield;
}

/* 表格美化 */
table {
  width: 100%;
  table-layout: fixed;
  border-radius: 0.5rem;
  overflow: hidden;
}
th, td {
  border: none;
  min-width: 100px;
  padding: 0.75rem 1rem;
  word-break: break-all;
  text-align: center;
}
th:first-child, td:first-child {
  text-align: left;
}
th {
  background: #374151;
  color: #e5e7eb;
  font-weight: 600;
  text-align: center;
}
tbody tr {
  transition: background 0.2s;
}
tbody tr:hover {
  background: #374151cc;
}
button {
  outline: none;
}


/* 分頁控制置中 */
.pagination-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.control-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
}
.control-panel-left {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}
.control-panel-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.table-scroll-wrapper {
  width: 100%;
}

.table-scroll-wrapper.fill-screen {
  min-height: 30vh; /* 讓表格至少有 60vh 高，但不會強制填滿 */
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.table-scroll-wrapper.scrollable {
  max-height: 60vh; /* 跟 fill-screen 一樣高 */
  overflow-y: auto;
}

select option {
  text-align: center;
}
</style>