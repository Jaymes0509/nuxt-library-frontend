<template>
    <div class="scroll-wrapper">
        <div class="intro">
            <div class="history-bg">
                <h1 class="history-title">預約記錄</h1>

                <!-- 登入檢查 -->
                <div v-if="!isLoggedIn" class="login-required">
                    <div class="login-required-icon">🔒</div>
                    <h2>需要登入會員</h2>
                    <p>您需要登入會員才能查看預約記錄</p>
                    <button @click="goToLogin" class="login-required-btn">
                        前往登入
                    </button>
                </div>

                <!-- 預約記錄內容（只有登入後才顯示） -->
                <div v-else class="history-main">
                    <!-- 控制面板 -->
                    <div class="history-control-panel">
                        <div class="history-control-panel-left">
                            <div class="history-row">
                                <span class="history-label">每頁顯示：</span>
                                <select v-model="itemsPerPage" class="history-select">
                                    <option v-for="size in pageSizes" :key="size" :value="size">{{ size }} 筆</option>
                                </select>
                            </div>
                            <div class="history-row">
                                <span class="history-label">排序：</span>
                                <select v-model="sortConfig.field" class="history-select">
                                    <option value="title">書名</option>
                                    <option value="author">作者</option>
                                    <option value="pickupTime">取書時間</option>
                                </select>
                                <button @click="toggleSortOrder" class="history-sort-btn">
                                    {{ sortConfig.ascending ? '↑ 升冪' : '↓ 降冪' }}
                                </button>
                            </div>
                        </div>
                        <div class="history-control-panel-right">
                            <button @click="viewMode = 'table'"
                                :class="['history-view-btn', viewMode === 'table' ? 'history-view-btn-active' : '']">
                                表格
                            </button>
                            <button @click="viewMode = 'grid'"
                                :class="['history-view-btn', viewMode === 'grid' ? 'history-view-btn-active' : '']">
                                網格
                            </button>
                        </div>
                    </div>

                    <!-- 載入中狀態 -->
                    <div v-if="loading" class="history-loading">
                        <div class="history-loading-spinner"></div>
                        <p>載入中...</p>
                    </div>

                    <!-- 錯誤信息 -->
                    <div v-else-if="error" class="history-error">
                        <div class="history-error-icon">!</div>
                        <p>{{ error }}</p>
                        <pre class="history-error-details">{{ error }}</pre>
                    </div>

                    <!-- 無資料狀態 -->
                    <div v-else-if="!paginatedBooks.length" class="history-empty">
                        <div class="history-empty-icon">📚</div>
                        <p>目前沒有預約記錄</p>
                        <p class="history-empty-subtitle">您還沒有任何預約記錄</p>
                    </div>

                    <!-- 表格視圖 -->
                    <div v-else
                        :class="['history-table-scroll', itemsPerPage > 10 ? 'history-table-scrollable' : 'history-table-fill']">
                        <div v-if="viewMode === 'table'" class="history-grid-table">
                            <div class="history-grid-header">
                                <div>書名</div>
                                <div>作者</div>
                                <div>取書地點</div>
                                <div>取書時間</div>
                                <div>操作</div>
                            </div>
                            <div class="history-grid-body">
                                <div v-for="(reservation, index) in paginatedBooks" :key="index"
                                    class="history-grid-row">
                                    <div class="history-grid-title-cell">{{ reservation.title }}</div>
                                    <div>{{ reservation.author }}</div>
                                    <div>{{ reservation.pickupLocation }}</div>
                                    <div>{{ reservation.pickupTime }}</div>
                                    <div>
                                        <button @click="viewBookDetail(reservation)"
                                            class="history-detail-btn">詳情</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-else class="history-grid">
                            <div v-for="(reservation, index) in paginatedBooks" :key="index" class="history-grid-card">
                                <div class="history-grid-img-wrap">
                                    <img :src="getDefaultCoverUrl(index)" :alt="reservation.title"
                                        class="history-grid-img" />
                                </div>
                                <div class="history-grid-info">
                                    <h3 class="history-grid-title reservation-record-book-title">{{ reservation.title }}
                                    </h3>
                                    <p class="history-grid-author">作者：{{ reservation.author }}</p>
                                    <div class="history-grid-dates">
                                        <p>取書地點：{{ reservation.pickupLocation }}</p>
                                        <p>取書時間：{{ reservation.pickupTime }}</p>
                                        <p>預約日期：{{ reservation.reservationDate }}</p>
                                    </div>
                                    <button class="history-detail-btn" @click="viewBookDetail(reservation)">詳情</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 分頁控制 -->
                    <div v-if="paginatedBooks.length" class="history-pagination">
                        <div class="history-pagination-controls">
                            <button class="history-pagination-btn" :disabled="currentPage === 1" @click="currentPage--">
                                <span aria-hidden="true">←</span>
                            </button>
                            <span>共{{ totalPages }}頁</span>
                            <input type="number" :value="currentPage" class="history-pagination-input" min="1"
                                :max="totalPages" @change="e => goToPage(parseInt(e.target.value))" />
                            <span>/{{ totalPages }}頁</span>
                            <button class="history-pagination-btn" :disabled="currentPage >= totalPages"
                                @click="currentPage++">
                                <span aria-hidden="true">→</span>
                            </button>
                        </div>
                        <div class="history-pagination-info">
                            顯示第 {{ (currentPage - 1) * itemsPerPage + 1 }} 到 {{ Math.min(currentPage * itemsPerPage,
                                sortedBooks.length) }} 筆，共 {{ sortedBooks.length }} 筆
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <CustomAlert :show="customAlert.show" :title="customAlert.title" :message="customAlert.message"
            @close="closeAlert" />
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '#imports'
import { reservationAPI } from '~/utils/api'
import CustomAlert from '~/components/CustomAlert.vue'

// 設置頁面標題
useHead({
    title: '預約記錄'
})

// 獲取 router 實例
const router = useRouter()

// 視圖模式
const viewMode = ref('table')

// 自訂提示視窗
const customAlert = ref({
    show: false,
    title: '',
    message: ''
})

const showAlert = (title, message) => {
    customAlert.value.title = title
    customAlert.value.message = message
    customAlert.value.show = true
}

const closeAlert = () => {
    customAlert.value.show = false
}

// 登入狀態檢查
const isLoggedIn = ref(false)
const user = ref(null)

// 分頁設定
const pageSizes = [10, 20, 30, 50, 100]
const itemsPerPage = ref(10)
const currentPage = ref(1)

// 排序設定
const sortConfig = ref({
    field: 'title',
    ascending: true
})

// 預設封面圖片
function getDefaultCoverUrl(index) {
    return `https://via.placeholder.com/300x400/4ECDC4/FFFFFF?text=${encodeURIComponent('書籍封面')}`
}

// 預約記錄資料
const reservationBooks = ref([])
const loading = ref(false)
const error = ref(null)

// 檢查登入狀態
const checkLoginStatus = () => {
    // 檢查 localStorage 中的用戶資訊
    const storedUser = localStorage.getItem('user')
    const jwtToken = localStorage.getItem('jwt_token')
    const authToken = localStorage.getItem('authToken')

    console.log('=== 登入狀態檢查 ===')
    console.log('storedUser:', storedUser)
    console.log('jwtToken:', jwtToken)
    console.log('authToken:', authToken)

    if (storedUser) {
        try {
            user.value = JSON.parse(storedUser)
            isLoggedIn.value = true
            console.log('✅ 用戶已登入：', user.value)
        } catch (e) {
            console.error('❌ 解析用戶資訊失敗：', e)
            isLoggedIn.value = false
        }
    } else if (jwtToken || authToken) {
        // 如果有 token 但沒有用戶資訊，也視為已登入
        isLoggedIn.value = true
        console.log('✅ 檢測到登入 token')
    } else {
        isLoggedIn.value = false
        console.log('❌ 用戶未登入')
    }

    console.log('最終登入狀態：', isLoggedIn.value)
    console.log('==================')
}

// 跳轉到登入頁面
const goToLogin = () => {
    router.push('/login')
}

// 格式化日期時間
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return ''
    const date = new Date(dateTimeStr)
    return date.toLocaleString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    })
}

// 獲取預約記錄
async function fetchReservations() {
    if (loading.value) return

    loading.value = true
    error.value = null

    try {
        console.log('開始載入預約歷史記錄...')

        let response
        try {
            // 使用正確的 API 查詢 reservations 表 (預約歷史)
            response = await reservationAPI.getReservations(1)
        } catch (firstError) {
            console.log('使用 userId=1 失敗，嘗試不傳參數:', firstError)
            try {
                // 備用方案：不傳參數
                response = await reservationAPI.getReservations()
            } catch (secondError) {
                console.log('所有方案都失敗:', secondError)
                throw secondError
            }
        }

        console.log('API 回傳資料：', response.data)

        if (response.data && Array.isArray(response.data)) {
            reservationBooks.value = response.data.map((reservation, index) => {
                // 處理 reservations 表的資料結構
                const processedReservation = {
                    reservationId: reservation.reservation_id || reservation.id || `res_${index}`,
                    title: reservation.book_title || reservation.title || '未知書名',
                    author: reservation.book_author || reservation.author || '未知作者',
                    isbn: reservation.book_isbn || reservation.isbn || '未知ISBN',
                    publisher: reservation.book_publisher || reservation.publisher || '未知出版社',
                    classification: reservation.classification || '',
                    categoryName: reservation.category_name || '',
                    pickupLocation: reservation.pickup_location || '未指定地點',
                    pickupTime: reservation.pickup_time || '',
                    reservationDate: reservation.created_at || '',
                    expiryDate: reservation.updated_at || '',
                    status: reservation.status || 'pending',
                    userId: reservation.user_id,
                    // 保存完整的原始資料，以便詳情頁使用
                    bookInfo: {
                        bookId: reservation.book_id,
                        title: reservation.book_title || reservation.title,
                        author: reservation.book_author || reservation.author,
                        isbn: reservation.book_isbn || reservation.isbn,
                        publisher: reservation.book_publisher || reservation.publisher,
                        classification: reservation.classification,
                        category: {
                            cName: reservation.category_name
                        }
                    }
                }

                // 輸出第一筆資料的處理結果
                if (index === 0) {
                    console.log('第一筆處理後的預約記錄：', JSON.stringify(processedReservation, null, 2))
                }

                return processedReservation
            })

            console.log('總筆數：', reservationBooks.value.length)
            console.log('前端處理後的 reservationBooks：', reservationBooks.value)
        } else {
            console.log('API 回應不是陣列或為空')
            reservationBooks.value = []
        }
    } catch (err) {
        console.error('獲取預約記錄失敗：', err)
        console.log('錯誤詳情:', {
            status: err.response?.status,
            statusText: err.response?.statusText,
            data: err.response?.data
        })
        error.value = '無法載入預約記錄，請稍後再試'
        reservationBooks.value = []
    } finally {
        loading.value = false
    }
}

// 查看書籍詳情
function viewBookDetail(reservation) {
    console.log('點擊詳情按鈕，預約資料：', reservation)

    if (reservation.bookInfo && reservation.bookInfo.isbn) {
        // 使用 query 參數跳轉到 bookinfo 頁面
        const isbn = reservation.bookInfo.isbn
        console.log('跳轉到 bookinfo 頁面，ISBN：', isbn)

        router.push({
            path: '/bookinfo',
            query: {
                isbn: isbn,
                returnQuery: '',
                returnPage: '1',
                from: 'reservation-history',
                returnType: 'list'
            }
        })
    } else {
        console.warn('缺少書籍資訊或 ISBN，無法跳轉')
        // 可以顯示錯誤訊息或使用預設值
        showAlert('提示', '無法獲取書籍詳情，請稍後再試')
    }
}

// 排序功能
function toggleSortOrder() {
    sortConfig.value.ascending = !sortConfig.value.ascending
}

// 排序後的資料
const sortedBooks = computed(() => {
    const books = [...reservationBooks.value]
    const field = sortConfig.value.field
    const ascending = sortConfig.value.ascending

    return books.sort((a, b) => {
        let valueA, valueB

        // 根據不同欄位類型進行排序
        if (field === 'reservationDate') {
            valueA = new Date(a[field] || 0).getTime()
            valueB = new Date(b[field] || 0).getTime()
        } else {
            valueA = (a[field] || '').toString().toLowerCase()
            valueB = (b[field] || '').toString().toLowerCase()
        }

        // 進行排序比較
        if (valueA < valueB) return ascending ? -1 : 1
        if (valueA > valueB) return ascending ? 1 : -1
        return 0
    })
})

// 計算總頁數
const totalPages = computed(() => Math.ceil(sortedBooks.value.length / itemsPerPage.value))

// 計算當前頁面應該顯示的資料
const paginatedBooks = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage.value
    const end = start + itemsPerPage.value
    return sortedBooks.value.slice(start, end)
})

// 頁面跳轉
function goToPage(page) {
    const pageNum = parseInt(page)
    if (pageNum && !isNaN(pageNum) && pageNum >= 1 && pageNum <= totalPages.value) {
        currentPage.value = pageNum
    }
}

// 監聽每頁顯示筆數變動，自動跳回第一頁
watch(itemsPerPage, () => {
    currentPage.value = 1
})

// 監聽排序設定變化
watch([() => sortConfig.value.field, () => sortConfig.value.ascending], () => {
    // 當排序設定改變時，重置到第一頁
    currentPage.value = 1
})

// 初始化載入資料
onMounted(async () => {
    checkLoginStatus()
    if (isLoggedIn.value) {
        try {
            await fetchReservations()
        } catch (err) {
            console.error('初始化載入失敗：', err)
        }
    }
})
</script>

<style scoped>
.scroll-wrapper {
    position: relative;
    height: 100%;
    display: flex;
    flex-direction: column;
}

.intro {
    flex: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    scrollbar-width: thin;
    scrollbar-color: transparent transparent;
    background: transparent;
}

/* 滾動條預設為透明 */
.intro::-webkit-scrollbar {
    width: 8px;
}

.intro::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .intro::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .intro {
    scrollbar-color: rgba(0, 0, 0, 0.4) transparent;
}

.history-bg {
    padding: 24px 24px 100px 24px;
    background: transparent;
}

.history-title {
    font-size: 2rem;
    font-weight: bold;
    margin-bottom: 16px;
    color: #18181b;
}

.history-main {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.history-control-panel {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    gap: 16px;
    flex-wrap: wrap;
}

.history-control-panel-left {
    display: flex;
    align-items: center;
    gap: 32px;
    flex-wrap: wrap;
}

.history-control-panel-right {
    display: flex;
    align-items: center;
    gap: 8px;
}

.history-row {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.history-label {
    font-size: 1rem;
    color: #222;
}

.history-select {
    min-width: 120px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    padding: 8px 16px;
    font-size: 1rem;
    background: #fff;
    color: #18181b;
    cursor: pointer;
    transition: background 0.2s;
    height: 40px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    text-align-last: center;
}

.history-select:hover {
    background: #f3f4f6;
}

.history-sort-btn {
    border: 1px solid #d1d5db;
    border-radius: 6px;
    padding: 8px 16px;
    background: #fff;
    color: #18181b;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.3s ease;
    height: 40px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 100px;
}

.history-view-btn {
    display: inline-flex;
    align-items: center;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    background: #fff;
    color: #18181b;
    font-size: 1rem;
    padding: 8px 16px;
    cursor: pointer;
    transition: background 0.2s, color 0.2s;
    margin-right: 4px;
}

.history-view-btn:last-child {
    margin-right: 0;
}

.history-view-btn-active {
    background: #2563eb;
    color: #fff;
}

.history-table-scroll {
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);
    border-radius: 8px;
    border: 1px solid rgba(229, 231, 235, 0.4);
    overflow: hidden;
}

.history-table-scrollable {
    max-height: 600px;
    overflow-y: auto;
}

.history-table-fill {
    min-height: 400px;
}

.history-grid-table {
    width: 100%;
}

.history-grid-header {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr 120px;
    gap: 16px;
    padding: 16px 20px;
    background: rgba(243, 244, 246, 0.6);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(229, 231, 235, 0.4);
    font-weight: 600;
    color: #222;
    font-size: 0.95rem;
}

.history-grid-body {
    max-height: 500px;
    overflow-y: auto;
}

.history-grid-row {
    display: grid;
    grid-template-columns: 2fr 1fr 1fr 1fr 120px;
    gap: 16px;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(229, 231, 235, 0.2);
    align-items: center;
    transition: background 0.2s;
}

.history-grid-row:hover {
    background: rgba(243, 244, 246, 0.3);
}

.history-grid-row:last-child {
    border-bottom: none;
}

.history-grid-title-cell {
    font-weight: 500;
    color: #18181b;
    word-wrap: break-word;
    overflow-wrap: break-word;
}

.history-detail-btn {
    padding: 6px 12px;
    background: #2563eb;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 0.9rem;
    cursor: pointer;
    transition: background 0.2s;
}

.history-detail-btn:hover {
    background: #1d4ed8;
}

.history-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    padding: 20px;
}

.history-grid-card {
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 8px;
    border: 1px solid rgba(229, 231, 235, 0.4);
    overflow: hidden;
    transition: transform 0.2s, box-shadow 0.2s;
    position: relative;
}

.history-grid-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.history-grid-img-wrap {
    width: 100%;
    height: 200px;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f3f4f6;
}

.history-grid-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.history-grid-info {
    padding: 16px;
}

.history-grid-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #18181b;
    margin-bottom: 8px;
    line-height: 1.4;
    word-wrap: break-word;
    overflow-wrap: break-word;
}

.history-grid-author {
    font-size: 0.9rem;
    color: #4b5563;
    margin-bottom: 8px;
}

.history-grid-dates {
    font-size: 0.9rem;
    color: #4b5563;
    margin-bottom: 12px;
}

.history-grid-dates p {
    margin-bottom: 4px;
}

.history-loading {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #4b5563;
}

.history-loading-spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #e5e7eb;
    border-top: 4px solid #2563eb;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}

.history-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #dc2626;
    text-align: center;
}

.history-error-icon {
    width: 48px;
    height: 48px;
    background: #dc2626;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    font-weight: bold;
    margin-bottom: 16px;
}

.history-error-details {
    background: #fef2f2;
    border: 1px solid #fecaca;
    border-radius: 4px;
    padding: 12px;
    margin-top: 12px;
    font-size: 0.9rem;
    color: #991b1b;
    max-width: 100%;
    overflow-x: auto;
}

.history-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    text-align: center;
    color: #6b7280;
}

.history-empty-icon {
    font-size: 4rem;
    margin-bottom: 16px;
}

.history-empty-subtitle {
    font-size: 1rem;
    color: #9ca3af;
    margin-bottom: 24px;
}

.history-pagination {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);
    border-radius: 8px;
    border: 1px solid rgba(229, 231, 235, 0.4);
}

.history-pagination-controls {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
}

.history-pagination-btn {
    padding: 8px 12px;
    background: #fff;
    border: 1px solid #d1d5db;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 0.9rem;
}

.history-pagination-btn:hover:not(:disabled) {
    background: #f3f4f6;
}

.history-pagination-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.history-pagination-input {
    width: 60px;
    padding: 8px;
    border: 1px solid #d1d5db;
    border-radius: 4px;
    text-align: center;
    font-size: 0.9rem;
}

.history-pagination-info {
    font-size: 0.9rem;
    color: #6b7280;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .history-control-panel {
        flex-direction: column;
        align-items: stretch;
    }

    .history-control-panel-left,
    .history-control-panel-right {
        justify-content: center;
    }

    .history-grid-header,
    .history-grid-row {
        grid-template-columns: 1.5fr 1fr 80px;
        font-size: 0.9rem;
    }

    .history-grid-header>div:nth-child(3),
    .history-grid-header>div:nth-child(4),
    .history-grid-row>div:nth-child(3),
    .history-grid-row>div:nth-child(4) {
        display: none;
    }

    .history-grid {
        grid-template-columns: 1fr;
    }
}

/* 登入提示樣式 */
.login-required {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    text-align: center;
    color: #6b7280;
    background: transparent;
    border-radius: 12px;
    margin: 20px;
}

.login-required-icon {
    font-size: 4rem;
    margin-bottom: 16px;
}

.login-required h2 {
    font-size: 1.5rem;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
}

.login-required p {
    font-size: 1rem;
    color: #6b7280;
    margin-bottom: 24px;
    max-width: 400px;
}

.login-required-btn {
    padding: 12px 32px;
    background: #2563eb;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.login-required-btn:hover {
    background: #1d4ed8;
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
}

.login-required-btn:active {
    transform: translateY(0);
}
</style>