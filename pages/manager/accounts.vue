<script setup>
// 假資料，根據 members 資料表
const members = [
  {
    user_id: 1,
    name: '王小明',
    gender: '男',
    id_number: 'A123456789',
    birth_date: '1990-01-01',
    nationality: '台灣',
    education: '大學',
    occupation: '學生',
    email: 'ming@example.com',
    phone: '0912345678',
    created_at: '2022-01-01 10:00:00'
  },
  {
    user_id: 2,
    name: '李小華',
    gender: '女',
    id_number: 'B987654321',
    birth_date: '1992-05-12',
    nationality: '台灣',
    education: '碩士',
    occupation: '工程師',
    email: 'hua@example.com',
    phone: '0987654321',
    created_at: '2022-02-15 09:30:00'
  }
]
</script>

<template>
  <div class="p-8">
    <h2 class="text-2xl font-bold mb-4">帳號管理</h2>
    <table class="min-w-full border">
      <thead>
        <tr>
          <th class="border px-2 py-1">ID</th>
          <th class="border px-2 py-1">姓名</th>
          <th class="border px-2 py-1">性別</th>
          <th class="border px-2 py-1">身分證字號</th>
          <th class="border px-2 py-1">生日</th>
          <th class="border px-2 py-1">國籍</th>
          <th class="border px-2 py-1">學歷</th>
          <th class="border px-2 py-1">職業</th>
          <th class="border px-2 py-1">Email</th>
          <th class="border px-2 py-1">電話</th>
          <th class="border px-2 py-1">註冊時間</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="member in members" :key="member.user_id">
          <td class="border px-2 py-1">{{ member.user_id }}</td>
          <td class="border px-2 py-1">{{ member.name }}</td>
          <td class="border px-2 py-1">{{ member.gender }}</td>
          <td class="border px-2 py-1">{{ member.id_number }}</td>
          <td class="border px-2 py-1">{{ member.birth_date }}</td>
          <td class="border px-2 py-1">{{ member.nationality }}</td>
          <td class="border px-2 py-1">{{ member.education }}</td>
          <td class="border px-2 py-1">{{ member.occupation }}</td>
          <td class="border px-2 py-1">{{ member.email }}</td>
          <td class="border px-2 py-1">{{ member.phone }}</td>
          <td class="border px-2 py-1">{{ member.created_at }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>