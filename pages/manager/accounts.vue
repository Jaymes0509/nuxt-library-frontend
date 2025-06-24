<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'

const members = ref([])
const loading = ref(true)
const error = ref(null)
const itemsPerPage = ref(20)
const currentPage = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)

// Modal 狀態
const showModal = ref(false)
const selectedMember = ref(null)
const editAddressCounty = ref('')
const editAddressTown = ref('')
const editAddressDetail = ref('')

const fetchMembers = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/manager/accounts/all', {
      params: {
        page: currentPage.value - 1, // 後端通常從0開始
        size: itemsPerPage.value
      }
    })
    members.value = res.data.content || []
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (e) {
    error.value = '載入會員失敗'
  } finally {
    loading.value = false
  }
}

onMounted(fetchMembers)
watch([itemsPerPage, currentPage], fetchMembers)

// 開啟 modal 並帶入會員資料
function openDetailModal(member) {
  selectedMember.value = { ...member }
  editAddressCounty.value = member.addressCounty
  editAddressTown.value = member.addressTown
  editAddressDetail.value = member.addressDetail
  showModal.value = true
  console.log('showModal', showModal.value)
}

// 關閉 modal
function closeModal() {
  showModal.value = false
  selectedMember.value = null
}

// 儲存編輯
async function saveMemberDetail() {
  try {
    // 這裡假設有一個 PATCH API
    await axios.patch(`http://localhost:8080/api/manager/accounts/${selectedMember.value.id}`, {
      addressCounty: editAddressCounty.value,
      addressTown: editAddressTown.value,
      addressDetail: editAddressDetail.value
    })
    // 更新本地資料
    selectedMember.value.addressCounty = editAddressCounty.value
    selectedMember.value.addressTown = editAddressTown.value
    selectedMember.value.addressDetail = editAddressDetail.value
    // 重新抓取會員列表
    await fetchMembers()
    closeModal()
    alert('更新成功')
  } catch (e) {
    alert('更新失敗')
  }
}
</script>

<template>
  <div class="p-8">
    <h2 class="text-2xl font-bold mb-4">會員管理</h2>
    <div>
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
          <th class="border px-2 py-1">ID</th>
          <th class="border px-2 py-1">姓名</th>
          <th class="border px-2 py-1">性別</th>
          <th class="border px-2 py-1">信箱</th>
          <th class="border px-2 py-1">電話</th>
          <th class="border px-2 py-1">居住地</th>
          <th class="border px-2 py-1">詳細資料</th>
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
          <td class="border px-2 py-1">
            <NuxtLink :to="`/member/${member.id}`" class="bg-blue-500 text-white px-4 py-2 rounded">詳細資料</NuxtLink>
          </td>
        </tr>
      </tbody>
    </table>
    <div>
      <button :disabled="currentPage === 1" @click="currentPage--">上一頁</button>
      <span>第 {{ currentPage }} / {{ totalPages }} 頁</span>
      <button :disabled="currentPage === totalPages" @click="currentPage++">下一頁</button>
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
