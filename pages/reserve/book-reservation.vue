<template>
    <div class="scroll-wrapper">
        <div class="intro">
            <div class="reservation-bg">
                <div class="reservation-container">
                    <!-- 頁面標題區 -->
                    <div class="reservation-title-area">
                        <h1 class="reservation-title">{{ isBatchMode ? '批量預約' : '我要預約' }}</h1>
                        <p class="reservation-subtitle">{{ isBatchMode ? '請為選取的書籍統一設定預約資訊' : '請填寫以下預約信息' }}</p>
                    </div>

                    <div v-if="!books.length" class="reservation-notfound">
                        <div class="reservation-notfound-inner">
                            <div class="reservation-notfound-icon">
                                <svg class="reservation-notfound-svg" fill="none" stroke="currentColor"
                                    viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                        d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                                </svg>
                            </div>
                            <p class="reservation-notfound-text">找不到該書籍資訊，請從書籍查詢頁面重新進入。</p>
                        </div>
                    </div>

                    <div v-else class="reservation-card">
                        <!-- 書籍信息區 -->
                        <div class="reservation-bookinfo">
                            <h2 class="reservation-bookinfo-title">{{ isBatchMode ? '預約書籍清單' : '預約書籍' }}</h2>
                            <div v-if="isBatchMode" class="batch-books-list">
                                <div v-for="(book, index) in books" :key="index" class="batch-book-item">
                                    <span class="batch-book-number">{{ index + 1 }}.</span>
                                    <span class="batch-book-title">{{ book.title }}</span>
                                    <span class="batch-book-author">作者：{{ book.author }}</span>
                                </div>
                            </div>
                            <div v-else>
                                <p class="reservation-bookinfo-book">{{ books[0].title }}</p>
                                <p class="reservation-bookinfo-author">作者：{{ books[0].author }}</p>
                            </div>
                        </div>

                        <!-- 預約表單區 -->
                        <div class="reservation-form">
                            <!-- 取書時間 -->
                            <div class="reservation-form-group">
                                <label class="reservation-label">
                                    <svg class="reservation-label-icon" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                                    </svg>
                                    取書時間
                                </label>
                                <input type="datetime-local" v-model="form.time"
                                    :class="['reservation-input', { 'shake': isShaking.time }]" :max="maxDateTime" />
                                <span v-if="errors.time" class="error-message">{{ errors.time }}</span>
                            </div>

                            <!-- 取書地點 -->
                            <div class="reservation-form-group">
                                <label class="reservation-label">
                                    <svg class="reservation-label-icon" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                                    </svg>
                                    取書地點
                                </label>
                                <select v-model="form.location"
                                    :class="['reservation-input', { 'shake': isShaking.location }]">
                                    <option disabled value="">請選擇取書地點</option>
                                    <option>一樓服務台</option>
                                    <option>二樓自助區</option>
                                </select>
                                <span v-if="errors.location" class="error-message">{{ errors.location }}</span>
                            </div>

                            <!-- 取書方式 -->
                            <div class="reservation-form-group">
                                <label class="reservation-label">
                                    <svg class="reservation-label-icon" fill="none" stroke="currentColor"
                                        viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                            d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                                    </svg>
                                    取書方式
                                </label>
                                <select v-model="form.method"
                                    :class="['reservation-input', { 'shake': isShaking.method }]">
                                    <option disabled value="">請選擇取書方式</option>
                                    <option>親自取書</option>
                                    <option>他人代領</option>
                                </select>
                                <span v-if="errors.method" class="error-message">{{ errors.method }}</span>
                            </div>

                            <!-- 預約須知 -->
                            <div class="reservation-notice">
                                <h3 class="reservation-notice-title">預約須知</h3>
                                <ul class="reservation-notice-list">
                                    <li><strong>違規情況：</strong></li>
                                    <li class="violation-item"> 預約成立後7天內未完成借閱將自動取消預約</li>
                                    <li>您目前已預約 {{ userReservedCount }} 本書，本次將預約 {{ books.length }} 本書籍</li>
                                </ul>
                            </div>

                            <!-- 按鈕區域 -->
                            <div class="reservation-btn-area">
                                <button type="button" @click="goBack" class="reservation-btn reservation-btn-back">
                                    返回
                                </button>
                                <button type="button" @click="handleReserve"
                                    class="reservation-btn reservation-btn-confirm">
                                    {{ isBatchMode ? `確認預約 ${books.length} 本書` : '確認預約' }}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <CustomAlert :show="customAlert.show" :title="customAlert.title" :message="customAlert.message"
        :items="customAlert.items" @close="closeAlert" />
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
import { useHead } from '#imports'
import { reservationAPI } from '~/utils/api'
import CustomAlert from '~/components/CustomAlert.vue'

// 設置頁面標題
useHead({
    title: '書籍預約'
})

// 模擬登入與預約數量限制
const isLoggedIn = true
const userReservedCount = ref(2)
const maxReservation = 10

const route = useRoute()
const router = useRouter()

interface Book {
    title: string
    author: string
    isbn: string
    is_available: number
    id: number
}

const books = computed<Book[]>(() => {
    const booksParam = route.query.books
    if (!booksParam || typeof booksParam !== 'string') return []

    try {
        return JSON.parse(booksParam)
    } catch (error) {
        console.error('解析書籍資料失敗：', error)
        return []
    }
})

const form = ref({
    time: '',
    location: '一樓服務台',
    method: '親自取書',
})

const errors = ref({
    time: '',
    location: '',
    method: ''
})

const isShaking = ref({
    time: false,
    location: false,
    method: false
})

// 自訂提示視窗
const customAlert = ref({
    show: false,
    title: '',
    message: '',
    items: [] as string[],
})

const showAlert = (title: string, message: string, items: string[] = []) => {
    customAlert.value.title = title
    customAlert.value.message = message
    customAlert.value.items = items
    customAlert.value.show = true
}

const closeAlert = () => {
    customAlert.value.show = false
}

function shakeField(field: keyof typeof isShaking.value) {
    isShaking.value[field] = true
    setTimeout(() => {
        isShaking.value[field] = false
    }, 500)
}

function validateForm() {
    console.log('開始驗證表單...')
    let isValid = true
    errors.value = {
        time: '',
        location: '',
        method: ''
    }

    // 驗證取書時間
    if (!form.value.time) {
        console.log('時間欄位為空')
        errors.value.time = '請選擇取書時間'
        shakeField('time')
        isValid = false
    } else {
        // 檢查取書時間是否小於現在時間
        const selectedTime = new Date(form.value.time)
        const currentTime = new Date()

        if (selectedTime < currentTime) {
            console.log('取書時間小於現在時間')
            errors.value.time = '取書時間不能小於當前時間，請重新選擇'
            shakeField('time')
            isValid = false
        }

        // 檢查是否超過7天
        const maxTime = new Date(currentTime.getTime() + 7 * 24 * 60 * 60 * 1000) // 7天後
        if (selectedTime > maxTime) {
            console.log('取書時間超過7天')
            errors.value.time = '取書時間不能超過7天後'
            shakeField('time')
            isValid = false
        }
    }

    // 取書地點和方式有預設值，不需要驗證
    console.log('表單驗證結果：', isValid)
    console.log('錯誤訊息：', errors.value)
    return isValid
}

const reservationList = ref([])

onMounted(async () => {
    // 取得目前用戶的預約清單
    try {
        // 不傳入 userId，讓後端從 token 中獲取用戶資訊
        const response = await reservationAPI.getReservationList()
        reservationList.value = response.data || []
    } catch (e) {
        console.log('載入預約清單失敗，使用空清單：', e)
        reservationList.value = []
    }
})

async function handleReserve() {
    console.log('開始處理預約請求...')
    console.log('表單資料：', form.value)
    console.log('書籍資料：', books.value)
    console.log('第一本書籍的詳細資料：', books.value[0])
    console.log('is_available 值：', books.value[0]?.is_available)
    console.log('is_available 類型：', typeof books.value[0]?.is_available)

    if (!validateForm()) {
        console.log('表單驗證失敗')
        return
    }

    if (books.value.length === 0) {
        console.log('沒有書籍資料')
        return
    }

    // 檢查書籍可用性，支援多種格式
    // 對於來自預約清單的書籍，跳過可用性檢查，因為已經通過了加入清單的驗證
    const availability = (books.value[0] as any).is_available
    let isAvailable = true // 預設為可用

    // 只有在有可用性資訊時才進行檢查
    if (availability !== undefined && availability !== null) {
        isAvailable = availability === 1 ||
            availability === true ||
            String(availability) === '1' ||
            String(availability) === 'true' ||
            String(availability) === 'available'
    }

    console.log('書籍可用性檢查結果：', isAvailable)
    console.log('可用性資訊：', availability)

    if (!isAvailable) {
        console.log('書籍不可用')
        showAlert('預約失敗', '此書籍目前不可預約')
        return
    }

    // 檢查是否有重複預約
    const reservedBookIds = (reservationList.value as any[]).map(item => item.book_id || item.id)
    const duplicateBooks = books.value.filter(book => reservedBookIds.includes((book as any).book_id))
    if (duplicateBooks.length > 0) {
        showAlert(
            '重複預約',
            '您已預約過以下書籍，無法重複預約！',
            duplicateBooks.map(b => b.title)
        )
        return
    }

    try {
        // 獲取當前登入用戶的 ID
        const storedUser = localStorage.getItem('user')
        let currentUserId = 1 // 預設值

        if (storedUser) {
            try {
                const userData = JSON.parse(storedUser)
                currentUserId = userData.user_id || userData.id || 1
            } catch (e) {
                console.error('解析用戶資訊失敗：', e)
            }
        }

        // 準備批量預約資料，完全符合後端 ReservationBatchRequestDTO 格式
        const reservationData = {
            userId: currentUserId, // 使用當前用戶的 ID
            books: books.value.map(book => ({
                bookId: (book as any).book_id, // 直接使用 book_id
                reserveTime: form.value.time
            })),
            pickupLocation: form.value.location,
            pickupMethod: form.value.method
        }

        console.log('發送批量預約請求：', reservationData)

        // 調用批量預約 API
        const response = await reservationAPI.batchReservation(reservationData)
        console.log('API 原始回傳：', response.data)

        let reservationIds: string[] = []

        // 如果預約有結果，則處理後續移除操作
        if (response.data?.success && response.data.results) {
            const successfulResults = response.data.results.filter((result: any) => result.status === 'success');
            const successfulBookIds = successfulResults.map((result: any) => result.bookId);

            // 根據成功預約的 book_id 找出對應的 logId
            const logIdsToDelete = books.value
                .filter(book => successfulBookIds.includes((book as any).book_id))
                .map(book => (book as any).logId)
                .filter(logId => logId); // 確保 logId 存在

            if (logIdsToDelete.length > 0) {
                console.log('預約成功，將從預約清單中移除 Log IDs:', logIdsToDelete);
                // 平行發送刪除請求
                await Promise.allSettled(
                    logIdsToDelete.map(logId => reservationAPI.deleteReservationLog(logId))
                );
            }

            // 提取預約 ID
            reservationIds = successfulResults.map((result: any) => result.reservationId)
        }

        console.log('提取的預約 ID：', reservationIds)

        // 無論成功或失敗，都跳轉到結果頁面，並帶上完整的結果資訊
        router.push({
            path: '/reserve/book-reserveresult',
            query: {
                books: JSON.stringify(books.value.map(book => ({
                    title: book.title,
                    author: book.author,
                    isbn: book.isbn,
                    time: form.value.time,
                    location: form.value.location,
                    method: form.value.method
                }))),
                reservationIds: JSON.stringify(reservationIds),
                time: form.value.time,
                location: form.value.location,
                method: form.value.method,
                results: JSON.stringify(response.data.results || []),
                success: response.data.success || false
            }
        })
    } catch (error: any) {
        console.error('預約失敗：', error)
        const message = error.response?.data?.message || error.response?.data?.error || error.message || '預約失敗，請稍後再試';
        showAlert('預約失敗', message);
    }
}

const isBatchMode = computed(() => route.query.batch === 'true')

// 計算最大時間（7天後）
const maxDateTime = computed(() => {
    const now = new Date()
    const maxTime = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000) // 7天後
    return maxTime.toISOString().slice(0, 16) // 格式：YYYY-MM-DDTHH:MM
})

function goBack() {
    router.back()
}
</script>

<style>
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

.reservation-bg {
    padding: 32px 0 100px 0;
    background: transparent;
}

.reservation-container {
    max-width: 700px;
    margin: 0 auto;
    padding: 0 16px;
}

.reservation-title-area {
    text-align: center;
    margin-bottom: 32px;
}

.reservation-title {
    font-size: 2rem;
    font-weight: bold;
    color: #18181b;
}

.reservation-subtitle {
    margin-top: 8px;
    color: #4b5563;
}

.reservation-notfound {
    background: rgba(243, 244, 246, 0.6);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    padding: 24px;
    text-align: center;
    border: 1px solid rgba(229, 231, 235, 0.4);
}

.reservation-notfound-inner {
    padding: 24px;
}

.reservation-notfound-icon {
    width: 64px;
    height: 64px;
    background: #e5e7eb;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px auto;
}

.reservation-notfound-svg {
    width: 32px;
    height: 32px;
    color: #6b7280;
}

.reservation-notfound-text {
    color: #6b7280;
}

.reservation-card {
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    border: 1px solid rgba(229, 231, 235, 0.4);
}

.reservation-bookinfo {
    background: rgba(243, 244, 246, 0.6);
    backdrop-filter: blur(10px);
    padding: 24px;
    border-bottom: 1px solid rgba(229, 231, 235, 0.4);
}

.reservation-bookinfo-title {
    font-size: 1.1rem;
    font-weight: 500;
    color: #222;
    margin-bottom: 8px;
}

.reservation-bookinfo-book {
    font-size: 1.5rem;
    font-weight: bold;
    color: #18181b;
    margin-bottom: 4px;
}

.reservation-bookinfo-author {
    font-size: 0.95rem;
    color: #4b5563;
}

.reservation-form {
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.reservation-form-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.reservation-label {
    display: flex;
    align-items: center;
    font-size: 1rem;
    font-weight: 500;
    color: #222;
    margin-bottom: 4px;
}

.reservation-label-icon {
    width: 20px;
    height: 20px;
    margin-right: 8px;
    color: #6b7280;
}

.reservation-input {
    width: 100%;
    background: #fff;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    padding: 8px 12px;
    font-size: 1rem;
    color: #18181b;
    outline: none;
    box-sizing: border-box;
}

.reservation-input:focus {
    border-color: #2563eb;
    box-shadow: 0 0 0 2px #2563eb22;
}

.reservation-notice {
    background: rgba(243, 244, 246, 0.6);
    backdrop-filter: blur(10px);
    border-radius: 8px;
    padding: 16px;
    border: 1px solid rgba(229, 231, 235, 0.4);
}

.reservation-notice-title {
    font-size: 1rem;
    font-weight: 500;
    color: #222;
    margin-bottom: 8px;
}

.reservation-notice-list {
    color: #4b5563;
    font-size: 0.95rem;
    margin: 0;
    padding-left: 20px;
}

.reservation-notice-list li {
    margin-bottom: 4px;
}

.violation-item {
    color: #18181b;
    font-weight: 500;
    margin-left: 16px;
}

.violation-text {
    color: #dc2626;
    font-weight: 500;
}

.consequence-text {
    color: #18181b;
    font-weight: 400;
}

.reservation-notice-list strong {
    color: #dc2626;
    font-weight: 600;
}

.reservation-btn-area {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    padding-top: 16px;
    border-top: 1px solid #e5e7eb;
}

.reservation-btn {
    border: 1px solid #2563eb;
    border-radius: 6px;
    padding: 8px 20px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s, color 0.2s;
}

.reservation-btn-back {
    background: #fff;
    color: #2563eb;
}

.reservation-btn-back:hover {
    background: #f3f4f6;
}

.reservation-btn-confirm {
    background: #2563eb;
    color: #fff;
}

.reservation-btn-confirm:hover {
    background: #1d4ed8;
}

.history-grid-title {
    max-height: 2.8em;
    /* 兩行文字的高度 */
    line-height: 1.4;
    overflow: hidden;
    position: relative;
}

.history-grid-title::after {
    content: '...';
    position: absolute;
    bottom: 0;
    right: 0;
    padding-left: 40px;
    background: linear-gradient(to right, transparent, white 50%);
}

.error-message {
    color: #dc2626;
    font-size: 0.875rem;
    margin-top: 4px;
}

@keyframes shake {

    0%,
    100% {
        transform: translateX(0);
    }

    10%,
    30%,
    50%,
    70%,
    90% {
        transform: translateX(-5px);
    }

    20%,
    40%,
    60%,
    80% {
        transform: translateX(5px);
    }
}

.shake {
    animation: shake 0.5s cubic-bezier(.36, .07, .19, .97) both;
    border-color: #dc2626 !important;
}

.reservation-input.shake:focus {
    border-color: #dc2626;
    box-shadow: 0 0 0 2px rgba(220, 38, 38, 0.2);
}

/* 批量預約相關樣式 */
.batch-books-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    max-height: 200px;
    overflow-y: auto;
    padding: 8px;
    background: rgba(243, 244, 246, 0.3);
    border-radius: 6px;
    border: 1px solid rgba(229, 231, 235, 0.4);
}

.batch-book-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 4px;
    font-size: 0.95rem;
}

.batch-book-number {
    font-weight: 600;
    color: #2563eb;
    min-width: 20px;
}

.batch-book-title {
    font-weight: 500;
    color: #18181b;
    flex: 1;
    word-wrap: break-word;
    overflow-wrap: break-word;
}

.batch-book-author {
    color: #4b5563;
    font-size: 0.9rem;
    white-space: nowrap;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .batch-book-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
    }

    .batch-book-author {
        white-space: normal;
    }
}
</style>