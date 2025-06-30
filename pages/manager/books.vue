<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'

const books = ref([])
const loading = ref(true)
const error = ref(null)
const itemsPerPage = ref(20)
const currentPage = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)
const modalBook = ref(null)
const isAddMode = ref(false)
const maxBookId = ref(0)

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/manager/books/all', {
      params: {
        field: 'title',
        keyword: '',
        page: currentPage.value - 1, // 後端通常從0開始
        size: itemsPerPage.value
      }
    })
    books.value = res.data.content || []
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
    // 計算資料庫最大 bookId
    maxBookId.value = books.value.length > 0 ? Math.max(...books.value.map(b => b.bookId)) : 0

    // 查看books_id
    console.log('book_id：', res.data.book_id)

  } catch (e) {
    error.value = '載入書籍失敗'
  } finally {
    loading.value = false
  }
}

onMounted(fetchBooks)
watch([itemsPerPage, currentPage], fetchBooks)

const openModal = (book) => {
  modalBook.value = { ...book }
  isAddMode.value = false
}

const openAddBookModal = () => {
  isAddMode.value = true
  modalBook.value = {
    isbn: '',
    title: '',
    author: '',
    publisher: '',
    publishdate: '',
    version: '',
    classification: '',
    summary: '',
    imgUrl: '',
    is_available: true
  }
}

const closeModal = () => {
  modalBook.value = null
}

const saveModal = () => {
  // 實現保存邏輯
  console.log('Saving:', modalBook.value)
  closeModal()
}

const deleteBook = () => {
  // 實現刪除邏輯
  console.log('Deleting:', modalBook.value)
  closeModal()
}
</script>

<template>
  <div class="p-8">
    <h2 class="text-2xl font-bold mb-4">書籍管理</h2>
    <div class="mb-4 flex items-center justify-between">
      <div>
        每頁顯示：
        <select v-model="itemsPerPage">
          <option :value="20">20 筆</option>
          <option :value="50">50 筆</option>
        </select>
      </div>
      <button class="bg-green-600 text-white px-6 py-2 rounded shadow hover:bg-green-700"
        @click="openAddBookModal">新增書籍</button>
    </div>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">{{ error }}</div>
    <table v-else class="min-w-full border table-fixed">
      <thead>
        <tr>
          <th class="border px-2 py-1 w-16">書籍編號</th>
          <th class="border px-2 py-1 w-32">ISBN</th>
          <th class="border px-2 py-1 w-80">書名</th>
          <th class="border px-2 py-1 w-24">作者</th>
          <th class="border px-2 py-1 w-24">出版社</th>
          <th class="border px-2 py-1 w-20">出版日期</th>
          <th class="border px-2 py-1 w-16">狀態</th>
          <th class="border px-2 py-1 w-24">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.book_id">
          <td class="border px-2 py-1 w-16">{{ book.bookId }}</td>
          <td class="border px-2 py-1 w-32" :title="book.isbn">{{ book.isbn }}</td>
          <td class="border px-2 py-1 w-80">
            <div class="book-title-cell w-80" :title="book.title">{{ book.title }}</div>
          </td>
          <td class="border px-2 py-1 w-24 author-cell" :title="book.author">
            <div class="author-cell w-24" :title="book.author">{{ book.author }}</div>
          </td>
          <td class="border px-2 py-1 w-24 publisher-cell" :title="book.publisher">
            <div class="publisher-cell w-24" :title="book.publisher">{{ book.publisher }}</div>
          </td>
          <td class="border px-2 py-1 w-20">{{ book.publishdate }}</td>
          <td class="border px-2 py-1 w-16">
            <span v-if="book.is_available" class="text-green-600">在架</span>
            <span v-else class="text-red-600">借出</span>
          </td>
          <td class="border px-2 py-1 w-24">
            <button class="bg-blue-500 text-white px-4 py-2 rounded" @click="openModal(book)">修改</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="mt-4 flex items-center gap-4">
      <button :disabled="currentPage === 1" @click="currentPage--"
        class="px-3 py-1 border rounded disabled:opacity-50 disabled:cursor-not-allowed">
        上一頁
      </button>
      <span>第 {{ currentPage }} / {{ totalPages }} 頁</span>
      <button :disabled="currentPage === totalPages" @click="currentPage++"
        class="px-3 py-1 border rounded disabled:opacity-50 disabled:cursor-not-allowed">
        下一頁
      </button>
      <span>共 {{ totalElements }} 筆</span>
    </div>
    <div v-if="modalBook" class="modal-backdrop">
      <div class="modal-content modern-modal wide-modal">
        <div class="modal-header">
          <h3 class="modal-title">{{ isAddMode ? '新增書籍' : '編輯書籍' }}</h3>
          <div class="modal-id-status">
            <span class="modal-bookid">書籍編號：{{ modalBook.bookId }}</span>
            <div class="modal-status ml-6">
              <label class="toggle-switch">
                <input type="checkbox" v-model="modalBook.is_available">
                <span class="toggle-slider"></span>
              </label>
              <span class="ml-2 text-base font-medium"
                :class="modalBook.is_available ? 'text-green-600' : 'text-gray-500'">
                {{ modalBook.is_available ? '在架' : '借出' }}
              </span>
            </div>
          </div>
        </div>
        <form @submit.prevent="saveModal">
          <div class="modal-grid">
            <div class="modal-field modal-title-row">
              <label>書名</label>
              <input v-model="modalBook.title" class="modal-input" />
            </div>
            <div class="modal-left-wrap">
              <div class="modal-field">
                <label>ISBN</label>
                <input v-model="modalBook.isbn" class="modal-input" />
              </div>
              <div class="modal-field">
                <label>作者</label>
                <input v-model="modalBook.author" class="modal-input" />
              </div>
              <div class="modal-field">
                <label>出版社</label>
                <input v-model="modalBook.publisher" class="modal-input" />
              </div>
              <div class="modal-field">
                <label>出版年份</label>
                <input v-model="modalBook.publishdate" type="number" min="1900" :max="new Date().getFullYear()"
                  class="modal-input" placeholder="請輸入年份" />
              </div>
              <div class="modal-field">
                <label>版本</label>
                <input v-model="modalBook.version" class="modal-input" />
              </div>
              <div class="modal-field">
                <label>分類</label>
                <input v-model="modalBook.classification" class="modal-input" />
              </div>
            </div>
            <div class="modal-summary-cell">
              <label>摘要</label>
              <textarea v-model="modalBook.summary" class="modal-input modal-summary" placeholder="請輸入摘要內容"></textarea>
            </div>
            <div class="modal-field modal-imgurl">
              <label>圖片網址</label>
              <input v-model="modalBook.imgUrl" class="modal-input" />
            </div>
          </div>
          <div class="modal-actions">
            <button type="submit"
              class="bg-green-500 text-white px-6 py-2 rounded shadow hover:bg-green-600">確定</button>
            <button type="button" @click="closeModal"
              class="bg-gray-400 text-white px-6 py-2 rounded shadow hover:bg-gray-500 ml-4">取消</button>
            <button v-if="!isAddMode" type="button" @click="deleteBook"
              class="bg-red-500 text-white px-6 py-2 rounded shadow hover:bg-red-600 ml-4">刪除</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
table {
  width: 100%;
  background: #fff;
  border-collapse: collapse;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

th,
td {
  border: 1px solid #d1d5db;
  /* 淡灰色邊框 */
  padding: 8px 12px;
  text-align: left;
}

th {
  background: #f3f4f6;
  /* 表頭淡灰色 */
  font-weight: bold;
  color: #374151;
}

tbody tr:nth-child(even) {
  background: #fafbfc;
  /* 斑馬紋效果 */
}

tbody tr:hover {
  background: #f1f5f9;
  /* 滑鼠移過時高亮 */
}

.text-green-600 {
  color: #16a34a;
  font-weight: bold;
}

.text-red-600 {
  color: #dc2626;
  font-weight: bold;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modern-modal {
  background: #fff;
  border-radius: 22px;
  padding: 40px 48px 32px 48px;
  min-width: 800px;
  max-width: 96vw;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  position: relative;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.modal-title {
  font-size: 2rem;
  font-weight: bold;
  letter-spacing: 1px;
  color: #222;
  margin: 0;
}

.modal-id-status {
  display: flex;
  align-items: center;
  gap: 18px;
}

.modal-bookid {
  font-weight: 600;
  color: #374151;
  font-size: 1.08rem;
  background: #f3f4f6;
  border-radius: 6px;
  padding: 4px 16px;
}

.modal-title-row,
.modal-imgurl {
  grid-column: 1 / -1;
  width: 100%;
}

.modal-left-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 0.2rem;
}

.modal-left-wrap .modal-field {
  margin-bottom: 0;
  gap: 0px;
}

.modal-title-row .modal-input,
.modal-imgurl .modal-input {
  width: 100%;
}

.modal-summary-cell {
  display: flex;
  flex-direction: column;
  height: 100%;
  margin-left: 8px;
}

.modal-summary {
  height: 100%;
  min-height: 180px;
  max-height: 420px;
  resize: vertical;
  font-size: 1rem;
  align-self: stretch;
}

.modal-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px 32px;
  margin-bottom: 32px;
  align-items: stretch;
}

.modal-field {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.modal-field label {
  font-weight: 700;
  color: #374151;
  font-size: 1.08rem;
  margin-bottom: 2px;
}

.modal-input {
  padding: 4px 8px;
  font-size: 1rem;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  transition: border 0.2s;
}

.modal-input:focus {
  outline: none;
  border-color: #60a5fa;
  background: #fff;
}

.modal-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  gap: 24px;
}

.toggle-switch {
  position: relative;
  width: 36px;
  height: 20px;
  display: inline-block;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .2s;
  border-radius: 999px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 16px;
  width: 16px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: .2s;
  border-radius: 50%;
}

input:checked+.toggle-slider {
  background-color: #4ade80;
}

input:checked+.toggle-slider:before {
  transform: translateX(16px);
}

@media (max-width: 900px) {
  .modern-modal {
    min-width: 98vw;
    padding: 18px 2vw 18px 2vw;
  }

  .modal-grid {
    grid-template-columns: 1fr;
    gap: 16px 0;
  }
}

.wide-modal {
  min-width: 0;
  max-width: 98vw;
  width: 900px;
  padding: 32px 28px 24px 28px;
  box-sizing: border-box;
  overflow-x: auto;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-flex {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.modal-left {
  flex: 1 1 200px;
  min-width: 120px;
  max-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.modal-left .modal-input {
  padding: 6px 10px;
  font-size: 0.98rem;
  min-height: 28px;
}

.modal-right {
  flex: 2 1 500px;
  min-width: 260px;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (max-width: 950px) {
  .wide-modal {
    width: 98vw;
    min-width: 0;
    padding: 18px 2vw 18px 2vw;
  }

  .modal-flex {
    flex-direction: column;
    gap: 12px;
  }

  .modal-right,
  .modal-left {
    max-width: 100%;
    min-width: 0;
  }
}

.book-title-cell {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 320px;
}

.author-cell {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 96px;
}

.publisher-cell {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 96px;
}
</style>