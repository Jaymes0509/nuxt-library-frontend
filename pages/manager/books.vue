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
</script>

<template>
  <div class="p-8">
    <h2 class="text-2xl font-bold mb-4">書籍管理</h2>
    <div class="mb-4">
      每頁顯示：
      <select v-model="itemsPerPage">
        <option :value="20">20 筆</option>
        <option :value="50">50 筆</option>
      </select>
    </div>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">{{ error }}</div>
    <table v-else class="min-w-full border">
      <thead>
        <tr>
          <th class="border px-2 py-1">書籍編號</th>
          <th class="border px-2 py-1">ISBN</th>
          <th class="border px-2 py-1">書名</th>
          <th class="border px-2 py-1">作者</th>
          <th class="border px-2 py-1">出版社</th>
          <th class="border px-2 py-1">出版日期</th>
          <th class="border px-2 py-1">狀態</th>
          <th class="border px-2 py-1">詳細資料</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.book_id">
          <td class="border px-2 py-1">{{ book.book_id }}</td>
          <td class="border px-2 py-1">{{ book.isbn }}</td>
          <td class="border px-2 py-1">{{ book.title }}</td>
          <td class="border px-2 py-1">{{ book.author }}</td>
          <td class="border px-2 py-1">{{ book.publisher }}</td>
          <td class="border px-2 py-1">{{ book.publishdate }}</td>
          <td class="border px-2 py-1">
            <span v-if="book.is_available" class="text-green-600">在架</span>
            <span v-else class="text-red-600">借出</span>
          </td>
          <td class="border px-2 py-1">
            <button class="bg-blue-500 text-white px-4 py-2 rounded">詳細資料</button>
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
</style>