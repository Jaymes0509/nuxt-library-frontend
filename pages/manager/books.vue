<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const books = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    // 請根據你的後端實際 port 調整 baseURL
    const res = await axios.get('http://localhost:8080/api/manager/books/all', {
      params: {
        field: 'title',
        keyword: '',
        page: 0,
        size: 100
      }
    })

    console.log('後端回傳資料1：', res.data)

    books.value = res.data || []
    console.log('後端回傳資料2：', res.data)


  } catch (e) {
    error.value = '載入書籍失敗'
  } finally {
    loading.value = false
  }
})


// const books = [
//   {
//     bookID: 1,
//     isbn: '9789864791924',
//     title: '圖書館管理學',
//     author: '王小明',
//     publisher: '知識出版社',
//     publishdate: '2022-01-01',
//     version: '1',
//     type: '專業書籍',
//     language: '中文',
//     classification: '020',
//     c_id: 1,
//     is_available: 1,
//     created_at: '2022-01-01 10:00:00',
//     updated_at: '2022-01-01 10:00:00'
//   },
//   {
//     bookID: 2,
//     isbn: '9789573274597',
//     title: '現代圖書資訊',
//     author: '李大華',
//     publisher: '學術出版社',
//     publishdate: '2021-05-15',
//     version: '2',
//     type: '參考書',
//     language: '中文',
//     classification: '025',
//     c_id: 2,
//     is_available: 0,
//     created_at: '2021-05-15 09:00:00',
//     updated_at: '2021-05-15 09:00:00'
//   }
// ]

</script>

<template>
  <div class="p-8">
    <h2 class="text-2xl font-bold mb-4">書籍管理</h2>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">{{ error }}</div>
    <table v-else class="min-w-full border">
      <thead>
        <tr>
          <th class="border px-2 py-1">ID</th>
          <th class="border px-2 py-1">ISBN</th>
          <th class="border px-2 py-1">書名</th>
          <th class="border px-2 py-1">作者</th>
          <th class="border px-2 py-1">出版社</th>
          <th class="border px-2 py-1">出版日期</th>
          <th class="border px-2 py-1">狀態</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="book in books" :key="book.bookID">
          <td class="border px-2 py-1">{{ book.book_ID }}</td>
          <td class="border px-2 py-1">{{ book.isbn }}</td>
          <td class="border px-2 py-1">{{ book.title }}</td>
          <td class="border px-2 py-1">{{ book.author }}</td>
          <td class="border px-2 py-1">{{ book.publisher }}</td>
          <td class="border px-2 py-1">{{ book.publishdate }}</td>
          <td class="border px-2 py-1">
            <span v-if="book.is_available" class="text-green-600">在架</span>
            <span v-else class="text-red-600">借出</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
