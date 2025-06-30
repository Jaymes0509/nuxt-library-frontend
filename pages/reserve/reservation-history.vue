<template>
    <div class="scroll-wrapper">
        <div class="intro">
            <div class="history-bg">
                <h1 class="history-title">預約記錄</h1>

                <!-- 登入檢查 -->
                <LoginRequiredPrompt v-if="!isLoggedIn" message="您需要登入才能查詢預約紀錄" />

                <!-- 預約記錄內容（只有登入後才顯示） -->
                <div v-else class="history-main">
                    <!-- 控制面板 -->
                    <div class="history-control-panel">
                        <div class="history-control-panel-left">
                            <div class="history-row">
                                <span class="history-label">每頁顯示：</span>
                                <select v-model="itemsPerPage" class="history-select pretty-select-page">
                                    <option v-for="size in pageSizes" :key="size" :value="size">{{ size }} 筆</option>
                                </select>
                                <span class="history-label">排序：</span>
                                <select v-model="sortConfig.field" class="history-select pretty-select">
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
                    <!-- 預約狀態面板 -->
                    <div class="reservation-stats-panel">
                        <div class="stats-header">
                            <h3 class="stats-title">預約狀態</h3>
                            <button class="stats-refresh-btn" @click="fetchReservations">🔄 重新整理</button>
                        </div>
                        <div class="stats-content">
                            <div class="stats-item">
                                <span class="stats-label">待領取：</span>
                                <span class="stats-value stats-pending">{{ stats.pending }} 本</span>
                            </div>
                            <div class="stats-item">
                                <span class="stats-label">已領取：</span>
                                <span class="stats-value stats-completed">{{ stats.completed }} 本</span>
                            </div>
                            <div class="stats-item">
                                <span class="stats-label">已取消：</span>
                                <span class="stats-value stats-cancelled">{{ stats.cancelled }} 本</span>
                            </div>
                            <div class="stats-item">
                                <span class="stats-label">剩餘可預約：</span>
                                <span class="stats-value stats-warning">{{ stats.remaining }} 本</span>
                            </div>
                        </div>
                        <div class="stats-progress">
                            <div class="progress-bar">
                                <div class="progress-fill" :style="{ width: progressPercentage + '%' }"></div>
                            </div>
                            <span class="progress-text">{{ stats.total }} / 10</span>
                        </div>
                        <div v-if="stats.total >= 10" class="stats-warning-message">
                            ⚠️ 您已達到預約上限，無法再進行新的預約
                        </div>
                    </div>

                    <!-- 批量操作面板 -->
                    <div v-if="reservationBooks.length > 0" class="batch-control-panel">
                        <div class="batch-control-left">
                            <label class="batch-checkbox-label">
                                <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll"
                                    class="batch-checkbox" />
                                <span>全選</span>
                            </label>
                            <span class="batch-info">
                                已選擇 {{ selectedBooks.length }} 筆記錄
                            </span>
                        </div>
                        <div class="batch-control-right">
                            <button @click="handleBatchCancel" class="batch-btn batch-btn-remove"
                                :disabled="selectedBooks.length === 0">
                                批量取消 ({{ selectedBooks.length }})
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
                                <div class="history-grid-checkbox">
                                    <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll"
                                        class="batch-checkbox" />
                                </div>
                                <div>書名</div>
                                <div>作者</div>
                                <div>取書地點</div>
                                <div>取書方式</div>
                                <div>取書時間</div>
                                <div>狀態</div>
                                <div>操作</div>
                            </div>
                            <div class="history-grid-body">
                                <div v-for="(reservation, index) in paginatedBooks" :key="index"
                                    :class="['history-grid-row', { 'is-cancelled': reservation.status === 'cancelled' }]"
                                    @click="handleRowClick(reservation.reservationId)">
                                    <div class="history-grid-checkbox">
                                        <input type="checkbox"
                                            :checked="selectedBooks.includes(reservation.reservationId)"
                                            @change="toggleSelectBook(reservation.reservationId)" class="batch-checkbox"
                                            :disabled="reservation.status === 'cancelled'" @click.stop />
                                    </div>
                                    <div class="history-grid-title-cell">{{ reservation.title }}</div>
                                    <div>{{ reservation.author }}</div>
                                    <div>{{ reservation.pickupLocation }}</div>
                                    <div>{{ reservation.pickupMethod }}</div>
                                    <div>{{ reservation.pickupTime }}</div>
                                    <div :class="['history-status', getStatusClass(reservation.status)]">
                                        {{ getStatusText(reservation.status) }}
                                    </div>
                                    <div class="history-grid-actions" @click.stop>
                                        <button @click="viewBookDetail(reservation)"
                                            class="history-detail-btn">詳情</button>
                                        <button @click="handleCancel(reservation.reservationId)"
                                            class="history-cancel-btn"
                                            :disabled="reservation.status === 'cancelled'">取消預約</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-else class="history-grid">
                            <div v-for="(reservation, index) in paginatedBooks" :key="index"
                                :class="['history-grid-card', { 'is-cancelled': reservation.status === 'cancelled' }]">
                                <div class="history-grid-card-header">
                                    <input type="checkbox" :checked="selectedBooks.includes(reservation.reservationId)"
                                        @change="toggleSelectBook(reservation.reservationId)" class="batch-checkbox"
                                        :disabled="reservation.status === 'cancelled'" />
                                </div>
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
                                        <p>取書方式：{{ reservation.pickupMethod }}</p>
                                        <p>取書時間：{{ reservation.pickupTime }}</p>
                                        <p>預約日期：{{ reservation.reservationDate }}</p>
                                    </div>
                                    <div :class="['history-status', getStatusClass(reservation.status)]">
                                        {{ getStatusText(reservation.status) }}
                                    </div>
                                    <div class="history-grid-actions">
                                        <button class="history-detail-btn"
                                            @click="viewBookDetail(reservation)">詳情</button>
                                        <button @click="handleCancel(reservation.reservationId)"
                                            class="history-cancel-btn"
                                            :disabled="reservation.status === 'cancelled'">取消預約</button>
                                    </div>
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
            :type="customAlert.type" @close="closeAlert" @confirm="confirmAction" />
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
    message: '',
    type: 'alert', // 'alert' or 'confirm'
})
const pendingAction = ref(null);

const showAlert = (title, message) => {
    customAlert.value.title = title
    customAlert.value.message = message
    customAlert.value.type = 'alert'
    customAlert.value.show = true
}

const closeAlert = () => {
    customAlert.value.show = false
}

// 處理取消預約
function handleCancel(reservationId) {
    pendingAction.value = () => proceedWithCancellation(reservationId);
    customAlert.value = {
        show: true,
        title: '確認取消',
        message: '您確定要取消這筆預約嗎？此操作無法復原。',
        type: 'confirm',
    };
}

// 確認後執行取消
async function proceedWithCancellation(reservationId) {
    try {
        const response = await reservationAPI.updateReservationStatus(reservationId, 'cancelled');
        if (response.status === 200 || response.status === 204) {
            showAlert('取消成功', '您的預約已成功取消。');
            // 重新載入預約歷史
            await fetchReservations();
        } else {
            throw new Error('取消預約時發生錯誤');
        }
    } catch (err) {
        console.error('取消預約失敗：', err);
        const message = err.response?.data?.message || '取消預約失敗，請稍後再試。';
        showAlert('取消失敗', message);
    }
}

// 當確認事件觸發時調用
const confirmAction = async () => {
    closeAlert();
    if (typeof pendingAction.value === 'function') {
        await pendingAction.value();
        pendingAction.value = null; // Reset
    }
};

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

// 狀態處理函數
function getStatusText(status) {
    const statusMap = {
        'pending': '待領取',
        'completed': '已領取',
        'cancelled': '已取消',
        'expired': '已過期',
        'PENDING': '待領取',
        'COMPLETED': '已領取',
        'CANCELLED': '已取消',
        'EXPIRED': '已過期'
    }
    return statusMap[status] || status
}

function getStatusClass(status) {
    const statusClassMap = {
        'pending': 'history-status-pending',
        'completed': 'history-status-completed',
        'cancelled': 'history-status-cancelled',
        'expired': 'history-status-expired',
        'PENDING': 'history-status-pending',
        'COMPLETED': 'history-status-completed',
        'CANCELLED': 'history-status-cancelled',
        'EXPIRED': 'history-status-expired'
    }
    return statusClassMap[status] || 'history-status-pending'
}

// 預約記錄資料
const reservationBooks = ref([])
const selectedBooks = ref([])
const loading = ref(false)
const error = ref(null)

// 預約狀態統計
const stats = computed(() => {
    const pending = reservationBooks.value.filter(book =>
        book.status === 'pending' || book.status === 'PENDING'
    ).length
    const completed = reservationBooks.value.filter(book =>
        book.status === 'completed' || book.status === 'COMPLETED'
    ).length
    const cancelled = reservationBooks.value.filter(book =>
        book.status === 'cancelled' || book.status === 'CANCELLED'
    ).length
    const total = pending + completed + cancelled
    const remaining = Math.max(0, 10 - total)

    return {
        pending,
        completed,
        cancelled,
        total,
        remaining
    }
})

// 進度條百分比
const progressPercentage = computed(() => {
    return Math.min(100, (stats.value.total / 10) * 100)
})

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
            // 不傳 userId，讓後端自動從 token 解析
            response = await reservationAPI.getReservations()
        } catch (firstError) {
            console.log('不傳 userId 失敗:', firstError)
            throw firstError
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
                    pickupLocation: reservation.pickup_location || '一樓服務台', // 預設取書地點
                    pickupMethod: reservation.pickup_method || '親自取書', // 新增
                    pickupTime: formatDateTime(reservation.reserve_time || reservation.pickup_time || ''),
                    reservationDate: formatDateTime(reservation.created_at || reservation.reservation_date || ''),
                    expiryDate: formatDateTime(reservation.expiry_date || reservation.updated_at || ''),
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

// 新增：處理批量取消
async function handleBatchCancel() {
    const activeSelection = selectedBooks.value.filter(id => {
        const book = reservationBooks.value.find(b => b.reservationId === id);
        return book && book.status !== 'cancelled';
    });

    if (activeSelection.length === 0) {
        showAlert('提示', '請先選擇有效的預約記錄進行取消。');
        return;
    }

    pendingAction.value = () => proceedWithBatchCancellation(activeSelection);
    customAlert.value = {
        show: true,
        title: '確認批量取消',
        message: `您確定要取消選取的 ${activeSelection.length} 筆有效預約嗎？`,
        type: 'confirm',
    };
}

async function proceedWithBatchCancellation(idsToCancel) {
    try {
        await reservationAPI.batchCancelReservations(idsToCancel);
        showAlert('操作完成', '批量取消操作已完成。');
        await fetchReservations();
        selectedBooks.value = [];
    } catch (err) {
        console.error('批量取消失敗：', err);
        showAlert('錯誤', '批量取消時發生錯誤，請稍後再試。');
    }
}

// ===== 選取操作 =====
const toggleSelectBook = (reservationId) => {
    const index = selectedBooks.value.indexOf(reservationId);
    if (index === -1) {
        selectedBooks.value.push(reservationId);
    } else {
        selectedBooks.value.splice(index, 1);
    }
};

const toggleSelectAll = () => {
    if (isAllSelected.value) {
        selectedBooks.value = [];
    } else {
        selectedBooks.value = paginatedBooks.value.map(book => book.reservationId);
    }
};

// ===== 計算屬性 =====
const isAllSelected = computed(() => {
    if (paginatedBooks.value.length === 0) return false;
    return paginatedBooks.value.every(book => selectedBooks.value.includes(book.reservationId));
});

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

// 新增：處理行點擊事件
function handleRowClick(reservationId) {
    // 檢查該預約是否已取消
    const reservation = reservationBooks.value.find(r => r.reservationId === reservationId);
    if (reservation && reservation.status === 'cancelled') {
        return; // 已取消的預約不能選取
    }

    // 切換該行的選取狀態
    const index = selectedBooks.value.indexOf(reservationId);
    if (index === -1) {
        selectedBooks.value.push(reservationId);
    } else {
        selectedBooks.value.splice(index, 1);
    }
}
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
    grid-template-columns: 50px 2fr 1fr 1fr 1fr 1fr 1fr 200px;
    gap: 16px;
    padding: 16px 20px;
    background: rgba(243, 244, 246, 0.6);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(229, 231, 235, 0.4);
    font-weight: 600;
    color: #222;
    font-size: 0.95rem;
}

.history-grid-header>div:nth-child(n+2),
.history-grid-row>div:nth-child(n+2) {
    text-align: center;
    justify-content: center;
}

.history-grid-body {
    max-height: 500px;
    overflow-y: auto;
}

.history-grid-row {
    display: grid;
    grid-template-columns: 50px 2fr 1fr 1fr 1fr 1fr 1fr 200px;
    gap: 16px;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(229, 231, 235, 0.2);
    align-items: center;
    transition: background 0.2s;
    cursor: pointer;
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

/* 狀態樣式 */
.history-status {
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 0.85rem;
    font-weight: 500;
    text-align: center;
    min-width: 80px;
}

.history-status-pending {
    background: #fef3c7;
    color: #d97706;
    border: 1px solid #fbbf24;
}

.history-status-completed {
    background: #d1fae5;
    color: #059669;
    border: 1px solid #34d399;
}

.history-status-cancelled {
    background: #fee2e2;
    color: #dc2626;
    border: 1px solid #f87171;
}

.history-status-expired {
    background: #f3f4f6;
    color: #6b7280;
    border: 1px solid #d1d5db;
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

    .batch-control-panel {
        flex-direction: column;
        gap: 16px;
    }

    .batch-control-left,
    .batch-control-right {
        justify-content: center;
    }

    .history-grid-header,
    .history-grid-row {
        grid-template-columns: 1fr 170px;
        font-size: 0.9rem;
    }

    .history-grid-header>div:nth-child(2),
    .history-grid-header>div:nth-child(3),
    .history-grid-header>div:nth-child(4),
    .history-grid-header>div:nth-child(5),
    .history-grid-header>div:nth-child(6),
    .history-grid-row>div:nth-child(2),
    .history-grid-row>div:nth-child(3),
    .history-grid-row>div:nth-child(4),
    .history-grid-row>div:nth-child(5),
    .history-grid-row>div:nth-child(6) {
        display: none;
    }

    .history-grid {
        grid-template-columns: 1fr;
    }
}

/* === 控制面板美化（與 reservation-record.vue 完全一致）=== */
.history-row .pretty-select,
.history-row .pretty-select-page {
    appearance: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    background: #fff url('data:image/svg+xml;utf8,<svg fill="%23256be9" height="20" viewBox="0 0 20 20" width="20" xmlns="http://www.w3.org/2000/svg"><path d="M7.293 7.293a1 003 0 011.414 0L10 8.586l1.293-1.293a1 1 0 111.414 1.414l-2 2a1 1 0 01-1.414 0l-2-2a1 1 0 010-1.414z"/></svg>') no-repeat right 0.75rem center/1.2em auto;
    border: 1.5px solid #2563eb;
    border-radius: 8px;
    padding: 8px 36px 8px 16px;
    font-size: 1.05rem;
    color: #222;
    min-width: 110px;
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.07);
    transition: border 0.2s, box-shadow 0.2s;
    text-align: center;
    cursor: pointer;
}

.history-row .pretty-select:focus,
.history-row .pretty-select-page:focus {
    border-color: #1976d2;
    outline: none;
    box-shadow: 0 0 0 2px #2563eb33;
}

.history-row .pretty-select option,
.history-row .pretty-select-page option {
    background: #fff;
    color: #222;
    text-align: left;
}

.history-sort-btn {
    border: 1.5px solid #2563eb;
    border-radius: 8px;
    background: #fff;
    color: #2563eb;
    font-size: 1.05rem;
    font-weight: 500;
    padding: 8px 20px;
    min-width: 90px;
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.07);
    transition: background 0.2s, color 0.2s, border 0.2s;
    cursor: pointer;
}

.history-sort-btn:hover,
.history-sort-btn:focus {
    background: #2563eb;
    color: #fff;
    border-color: #1976d2;
}

.history-view-btn {
    border: 1.5px solid #2563eb;
    border-radius: 8px;
    background: #fff;
    color: #2563eb;
    font-size: 1.05rem;
    font-weight: 500;
    padding: 8px 20px;
    min-width: 90px;
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.07);
    transition: background 0.2s, color 0.2s, border 0.2s;
    cursor: pointer;
    margin-right: 4px;
}

.history-view-btn:last-child {
    margin-right: 0;
}

.history-view-btn-active,
.history-view-btn:hover,
.history-view-btn:focus {
    background: #2563eb;
    color: #fff;
    border-color: #1976d2;
}

.history-grid-actions {
    display: flex;
    flex-wrap: nowrap;
    gap: 8px;
    justify-content: center;
    align-items: center;
}

.history-cancel-btn {
    padding: 6px 12px;
    background: #ef4444;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 0.9rem;
    cursor: pointer;
    transition: background 0.2s;
}

.history-cancel-btn:hover {
    background: #dc2626;
}

.history-grid-checkbox {
    display: flex;
    align-items: center;
    justify-content: center;
}

.batch-checkbox {
    width: 18px;
    height: 18px;
    cursor: pointer;
    accent-color: #2563eb;
}

.history-grid-card-header {
    position: relative;
    padding: 12px;
    background: rgba(243, 244, 246, 0.6);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(229, 231, 235, 0.4);
    display: flex;
    justify-content: flex-start;
    align-items: center;
    gap: 12px;
}

.is-cancelled {
    color: #9ca3af;
    background-color: #f9fafb;
    text-decoration: line-through;
    cursor: not-allowed;
}

.is-cancelled .history-grid-title-cell,
.is-cancelled .history-grid-title {
    color: #9ca3af;
}

.history-cancel-btn:disabled {
    background: #9ca3af;
    cursor: not-allowed;
}

/* 批量操作面板樣式 */
.batch-control-panel {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(229, 231, 235, 0.4);
    border-radius: 8px;
    padding: 16px 20px;
    margin-bottom: 16px;
}

.batch-control-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.batch-control-right {
    display: flex;
    align-items: center;
    gap: 12px;
}

.batch-checkbox-label {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 1rem;
    color: #222;
    cursor: pointer;
}

.batch-info {
    font-size: 0.95rem;
    color: #4b5563;
}

.batch-warning {
    font-size: 0.9rem;
    color: #dc2626;
    font-weight: 500;
}

.batch-btn {
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 0.95rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid;
}

.batch-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.batch-btn-remove {
    background: #fff;
    color: #dc2626;
    border-color: #dc2626;
}

.batch-btn-remove:hover:not(:disabled) {
    background: #dc2626;
    color: #fff;
}

.batch-btn-reserve {
    background: #2563eb;
    color: #fff;
    border-color: #2563eb;
}

.batch-btn-reserve:hover:not(:disabled) {
    background: #1d4ed8;
    border-color: #1d4ed8;
}

/* 預約狀態面板樣式 */
.reservation-stats-panel {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(229, 231, 235, 0.4);
    border-radius: 12px;
    padding: 16px;
    margin: 16px 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    width: 100%;
}

.stats-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.stats-title {
    font-size: 1.1rem;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
}

.stats-refresh-btn {
    background: #2563eb;
    color: white;
    border: none;
    border-radius: 6px;
    padding: 6px 12px;
    font-size: 0.85rem;
    cursor: pointer;
    transition: background 0.2s;
}

.stats-refresh-btn:hover {
    background: #1d4ed8;
}

.stats-content {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 12px;
    margin-bottom: 12px;
}

.stats-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background: rgba(249, 250, 251, 0.8);
    border-radius: 6px;
    border: 1px solid rgba(229, 231, 235, 0.4);
}

.stats-label {
    font-size: 0.9rem;
    color: #6b7280;
    font-weight: 500;
}

.stats-value {
    font-size: 1rem;
    font-weight: 600;
}

.stats-pending {
    color: #f59e0b;
}

.stats-completed {
    color: #10b981;
}

.stats-cancelled {
    color: #ef4444;
}

.stats-warning {
    color: #dc2626;
}

.stats-progress {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
}

.progress-bar {
    flex: 1;
    height: 6px;
    background: rgba(229, 231, 235, 0.6);
    border-radius: 3px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #10b981, #059669);
    border-radius: 3px;
    transition: width 0.3s ease;
}

.progress-text {
    font-size: 0.85rem;
    color: #6b7280;
    font-weight: 500;
    min-width: 50px;
    text-align: right;
}

.stats-warning-message {
    background: rgba(254, 226, 226, 0.8);
    border: 1px solid rgba(239, 68, 68, 0.3);
    border-radius: 6px;
    padding: 10px 12px;
    color: #dc2626;
    font-size: 0.85rem;
    font-weight: 500;
    text-align: center;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .stats-content {
        grid-template-columns: 1fr;
        gap: 12px;
    }

    .stats-header {
        flex-direction: column;
        gap: 12px;
        align-items: flex-start;
    }

    .stats-refresh-btn {
        align-self: flex-end;
    }
}
</style>