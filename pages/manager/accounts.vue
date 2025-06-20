<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const members = ref([])
const loading = ref(true)
const error = ref(null)

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/manager/accounts/all')
    members.value = res.data || []
  } catch (e) {
    error.value = '載入會員失敗'
  } finally {
    loading.value = false
  }
})


// const accounts = [
//   {
//     user_id: 1,
//     name: "王小明",
//     gender: "男",
//     id_number: "A123456789",
//     birth_date: "1995-05-10",
//     nationality: "中華民國",
//     education: "大學",
//     occupation: "工程師",
//     address_city: "台北市",
//     address_zip: "100",
//     address_dist: "中正區",
//     address_detail: "忠孝東路一段1號",
//     email: "ming@example.com",
//     phone: "0912345678",
//     password: "pass123",
//     created_at: "2025-06-16 14:00:00",
//     updated_at: "2025-06-16 14:00:00"
//   },
//   {
//     user_id: 2,
//     name: "陳美麗",
//     gender: "女",
//     id_number: "B987654321",
//     birth_date: "1990-08-25",
//     nationality: "中華民國",
//     education: "碩士",
//     occupation: "設計師",
//     address_city: "新北市",
//     address_zip: "220",
//     address_dist: "板橋區",
//     address_detail: "中山路二段88號",
//     email: "mei@example.com",
//     phone: "0922333444",
//     password: "12345678",
//     created_at: "2025-06-16 14:00:00",
//     updated_at: "2025-06-16 14:00:00"
//   },
//   {
//     user_id: 3,
//     name: "李志豪",
//     gender: "男",
//     id_number: "C112233445",
//     birth_date: "1988-03-03",
//     nationality: "中華民國",
//     education: "博士",
//     occupation: "教授",
//     address_city: "台中市",
//     address_zip: "400",
//     address_dist: "西區",
//     address_detail: "台灣大道三段2號",
//     email: "zhihao@example.com",
//     phone: "0933444555",
//     password: "admin999",
//     created_at: "2025-06-16 14:00:00",
//     updated_at: "2025-06-16 14:00:00"
//   }
// ]
</script>

<template>
  <div class="p-8">
    <h2 class="text-2xl font-bold mb-4">會員管理</h2>
    <div v-if="loading">載入中...</div>
    <div v-else-if="error">{{ error }}</div>
    <table v-else class="min-w-full border">
      <thead>
        <tr>
          <th class="border px-2 py-1">ID</th>
          <th class="border px-2 py-1">姓名</th>
          <th class="border px-2 py-1">性別</th>
          <th class="border px-2 py-1">信箱</th>
          <th class="border px-2 py-1">電話</th>
          <th class="border px-2 py-1">居住地</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="member in members" :key="member.id">
          <td class="border px-2 py-1">{{ member.id }}</td>
          <td class="border px-2 py-1">{{ member.name }}</td>
          <td class="border px-2 py-1">{{ member.gender }}</td>
          <td class="border px-2 py-1">{{ member.email }}</td>
          <td class="border px-2 py-1">{{ member.phone }}</td>
          <td class="border px-2 py-1">
            {{ member.addressCounty }}{{ member.addressTown }}{{ member.addressDetail }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
