<template>
    <div class="violation-page">
        <h2 class="page-title">📚 所有違規紀錄</h2>

        <!-- 載入中狀態 -->
        <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <p>載入紀錄中...</p>
        </div>

        <!-- 錯誤訊息 -->
        <div v-else-if="error" class="error-state">
            <p>❌ {{ error }}</p>
        </div>

        <!-- 無資料狀態 -->
        <div v-else-if="records.length === 0" class="empty-state">
            <p>🎉 太棒了！目前沒有任何違規紀錄。</p>
        </div>

        <!-- 違規紀錄列表 -->
        <div v-else class="record-list">
            <p class="record-count">總共有 {{ records.length }} 筆違規紀錄</p>
            <table>
                <thead>
                    <tr>
                        <th>會員 ID</th>
                        <th>違規類型</th>
                        <th>違規日期</th>
                        <th>停權結束日</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="r in records" :key="r.violationId">
                        <td>{{ r.userId }}</td>
                        <td>{{ r.violationType }}</td>
                        <td>{{ formatDate(r.violationDate) }}</td>
                        <td>{{ formatDate(r.penaltyEndDate) }}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <CustomAlert :show="customAlert.show" :title="customAlert.title" :message="customAlert.message"
            @close="closeAlert" />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import CustomAlert from '~/components/CustomAlert.vue'

const records = ref([])
const loading = ref(true)
const error = ref(null)

// Custom Alert State
const customAlert = ref({
    show: false,
    title: '',
    message: ''
})

const showAlert = (title, message) => {
    customAlert.value = { show: true, title, message }
}

const closeAlert = () => {
    customAlert.value.show = false
}

const fetchAllViolations = async () => {
    loading.value = true
    error.value = null
    try {
        const res = await axios.get(`http://localhost:8080/api/violations`)
        // 假設 API 回傳的資料是按日期降冪排序的
        records.value = res.data
    } catch (err) {
        console.error('查詢失敗:', err)
        error.value = '無法載入違規紀錄，請稍後再試。'
        showAlert('查詢失敗', '無法載入違規紀錄，請檢查後端服務是否正常。')
        records.value = []
    } finally {
        loading.value = false
    }
}

const formatDate = (datetime) => {
    if (!datetime) return '—'
    return new Date(datetime).toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    })
}

onMounted(() => {
    fetchAllViolations()
})
</script>

<style scoped>
.violation-page {
    padding: 24px;
    max-width: 900px;
    margin: auto;
    background-color: #f9fafb;
    border-radius: 8px;
}

.page-title {
    font-size: 1.75rem;
    font-weight: bold;
    color: #1f2937;
    margin-bottom: 24px;
    text-align: center;
}

.loading-state,
.error-state,
.empty-state {
    text-align: center;
    padding: 40px;
    color: #6b7280;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #e5e7eb;
    border-top-color: #3b82f6;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin: 0 auto 16px;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.record-list {
    background-color: white;
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.record-count {
    padding: 12px 16px;
    font-size: 0.9rem;
    color: #4b5563;
    border-bottom: 1px solid #e5e7eb;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th,
td {
    border-bottom: 1px solid #e5e7eb;
    padding: 12px 16px;
    text-align: left;
}

th {
    background-color: #f9fafb;
    font-weight: 600;
    font-size: 0.875rem;
    color: #374151;
    text-align: center;
}

td {
    font-size: 0.9rem;
    color: #4b5563;
    text-align: center;
}

tbody tr:last-child td {
    border-bottom: none;
}

tbody tr:hover {
    background-color: #f3f4f6;
}
</style>