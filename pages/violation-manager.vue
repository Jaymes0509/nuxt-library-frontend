<template>
    <div class="violation-page">
        <h2>📚 違規紀錄查詢</h2>

        <!-- 查詢區 -->
        <div class="search-section">
            <label>會員 ID：</label>
            <input v-model="userId" placeholder="輸入會員 ID" />
            <button @click="fetchViolations">查詢</button>
        </div>

        <!-- 停權狀態顯示 -->
        <div v-if="suspended !== null" class="status">
            <span v-if="suspended" style="color: red;">⚠️ 該會員目前停權中</span>
            <span v-else style="color: green;">✅ 該會員目前未停權</span>
        </div>

        <!-- 違規紀錄列表 -->
        <div v-if="records.length > 0" class="record-list">
            <h3>違規紀錄列表</h3>
            <table>
                <thead>
                    <tr>
                        <th>日期</th>
                        <th>違規類型</th>
                        <th>停權結束日</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="r in records" :key="r.violationId">
                        <td>{{ formatDate(r.violationDate) }}</td>
                        <td>{{ r.violationType }}</td>
                        <td>{{ formatDate(r.penaltyEndDate) }}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const userId = ref('')
const records = ref([])
const suspended = ref(null)

const fetchViolations = async () => {
    if (!userId.value) {
        alert('請輸入會員 ID')
        return
    }

    try {
        const res = await axios.get(`http://localhost:8080/api/violations/user/${userId.value}`)
        records.value = res.data

        const s = await axios.get(`http://localhost:8080/api/violations/user/${userId.value}/suspended`)
        suspended.value = s.data
    } catch (err) {
        alert('查詢失敗：' + err)
        records.value = []
        suspended.value = null
    }
}

const formatDate = (datetime) => {
    return datetime ? new Date(datetime).toLocaleString() : '-'
}
</script>

<style scoped>
.violation-page {
    padding: 20px;
    max-width: 800px;
    margin: auto;
}

.search-section {
    margin-bottom: 20px;
}

input {
    margin-left: 10px;
    padding: 4px;
    width: 200px;
}

button {
    margin-left: 10px;
    padding: 5px 10px;
}

.record-list table {
    width: 100%;
    border-collapse: collapse;
}

.record-list th,
.record-list td {
    border: 1px solid #ccc;
    padding: 8px;
    text-align: center;
}

.status {
    margin-bottom: 20px;
}
</style>