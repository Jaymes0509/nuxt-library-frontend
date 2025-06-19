<script setup>
const accounts = [
  {
    user_id: 1,
    name: "王小明",
    gender: "男",
    id_number: "A123456789",
    birth_date: "1995-05-10",
    nationality: "中華民國",
    education: "大學",
    occupation: "工程師",
    address_city: "台北市",
    address_zip: "100",
    address_dist: "中正區",
    address_detail: "忠孝東路一段1號",
    email: "ming@example.com",
    phone: "0912345678",
    password: "pass123",
    created_at: "2025-06-16 14:00:00",
    updated_at: "2025-06-16 14:00:00"
  },
  {
    user_id: 2,
    name: "陳美麗",
    gender: "女",
    id_number: "B987654321",
    birth_date: "1990-08-25",
    nationality: "中華民國",
    education: "碩士",
    occupation: "設計師",
    address_city: "新北市",
    address_zip: "220",
    address_dist: "板橋區",
    address_detail: "中山路二段88號",
    email: "mei@example.com",
    phone: "0922333444",
    password: "12345678",
    created_at: "2025-06-16 14:00:00",
    updated_at: "2025-06-16 14:00:00"
  },
  {
    user_id: 3,
    name: "李志豪",
    gender: "男",
    id_number: "C112233445",
    birth_date: "1988-03-03",
    nationality: "中華民國",
    education: "博士",
    occupation: "教授",
    address_city: "台中市",
    address_zip: "400",
    address_dist: "西區",
    address_detail: "台灣大道三段2號",
    email: "zhihao@example.com",
    phone: "0933444555",
    password: "admin999",
    created_at: "2025-06-16 14:00:00",
    updated_at: "2025-06-16 14:00:00"
  }
]
</script>

<template>
  <div class="p-8">
    <h2 class="text-2xl font-bold mb-4">會員管理</h2>
    <table class="min-w-full border">
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
        <tr v-for="account in accounts" :key="account.user_id">
          <td class="border px-2 py-1">{{ account.user_id }}</td>
          <td class="border px-2 py-1">{{ account.name }}</td>
          <td class="border px-2 py-1">{{ account.gender }}</td>
          <td class="border px-2 py-1">{{ account.email }}</td>
          <td class="border px-2 py-1">{{ account.phone }}</td>
          <td class="border px-2 py-1">{{ account.address_city }}{{ account.address_dist }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
