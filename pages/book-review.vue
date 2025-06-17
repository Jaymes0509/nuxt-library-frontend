<template>
    <div class="bg-gray-100 min-h-screen py-10">

        <!-- 最大寬度容器，置中並有白色背景與圓角陰影 -->
        <div class="max-w-4xl mx-auto bg-white p-8 rounded-lg shadow-lg relative">

            <!-- 統一的返回按鈕 -->
            <div v-if="step" class="mb-6 text-left">
                <button class="fixed bg-white text-blue-600 px-4 py-2 rounded shadow-lg hover:bg-blue-100 z-20"
                    style="right: 1rem; bottom: 10rem; width: max-content;" @click="step = null">
                    ← 返回功能總覽
                </button>
            </div>

            <!-- 第一層：功能總覽 -->
            <div v-if="!step">
                <div class="mb-10 border-b pb-4">
                    <div class="flex justify-center items-center mb-2">
                        <span class="text-4xl mr-2">📚</span>
                        <h1 class="text-3xl font-bold">讀者書評</h1>
                    </div>
                    <div class="flex justify-center">
                        <div class="text-blue-800 text-lg font-semibold border-l-4 border-blue-600 pl-3 inline-block">
                            功能總覽
                        </div>
                    </div>
                </div>

                <!-- 功能選單 -->
                <div class="flex justify-center gap-12 mb-12">
                    <div class="bg-blue-100 hover:bg-blue-200 transition-all p-6 rounded-2xl shadow w-72 cursor-pointer text-center"
                        @click="goToWrite">
                        <div class="text-4xl mb-2">📝</div>
                        <div class="text-xl font-bold mb-1">撰寫心得</div>
                        <div class="text-sm text-gray-700">針對您借閱的書籍，留下寶貴評論與評分</div>
                    </div>

                    <div class="bg-green-100 hover:bg-green-200 transition-all p-6 rounded-2xl shadow w-72 cursor-pointer text-center"
                        @click="step = 'read'">
                        <div class="text-4xl mb-2">📖</div>
                        <div class="text-xl font-bold mb-1">閱讀心得</div>
                        <div class="text-sm text-gray-700">查看其他讀者對書籍的評價與心得內容</div>
                    </div>
                </div>

                <!-- 模擬登入登出 -->
                <div class="mt-4 flex flex-col gap-2 items-center">
                    <button class="bg-green-600 text-white px-4 py-2 rounded" @click="simulateLogin">模擬登入會員</button>
                    <button class="bg-gray-600 text-white px-4 py-2 rounded" @click="simulateLogout">模擬登出會員</button>
                </div>
            </div>

            <!-- 第二層：操作選單 -->
            <div v-if="step === 'write' && actionMode === null" class="max-w-full mx-auto">
                <h2 class="text-xl font-bold mb-4">請選擇操作類型</h2>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div class="border rounded p-6 shadow hover:shadow-md transition cursor-pointer"
                        @click="actionMode = 'new'">
                        <h3 class="text-lg font-semibold mb-2">✍ 撰寫新的書評</h3>
                        <p class="text-sm text-gray-600">針對您尚未評論過的借閱書籍新增書評</p>
                    </div>
                    <div class="border rounded p-6 shadow hover:shadow-md transition cursor-pointer"
                        @click="actionMode = 'edit'">
                        <h3 class="text-lg font-semibold mb-2">🛠 修改或查看我的書評</h3>
                        <p class="text-sm text-gray-600">檢視您已發表過的書評內容，進行修改或刪除</p>
                    </div>
                </div>
            </div>

            <!-- 第二層：撰寫新書評 → 選書 -->
            <div v-if="step === 'write' && actionMode === 'new' && !selectedBookForReview"
                class="max-w-3xl mx-auto text-left">
                <h2 class="text-2xl font-bold mb-6">請選擇您想撰寫書評的書籍</h2>

                <div v-if="borrowedBooks.length === 0" class="text-gray-600">您目前沒有可以撰寫書評的書籍。</div>

                <div v-for="book in borrowedBooks" :key="book.id"
                    class="border rounded p-4 mb-4 shadow hover:shadow-md transition cursor-pointer"
                    @click="startWritingReview(book)">
                    <h3 class="text-lg font-semibold">{{ book.title }}</h3>
                    <p class="text-sm text-gray-600">作者：{{ book.author }}</p>
                    <p class="text-sm text-blue-600 mt-2 hover:underline">點擊撰寫書評</p>
                </div>
            </div>

            <!-- 第二層：修改書評 → 顯示我的書評清單 -->
            <div v-if="step === 'write' && actionMode === 'edit'" class="max-w-4xl mx-auto text-left">
                <button class="text-blue-600 underline mb-4" @click="actionMode = null">← 返回操作選單</button>
                <h2 class="text-2xl font-bold mb-6">我的書評列表</h2>

                <div v-if="myReviews.length === 0" class="text-gray-600">您尚未撰寫任何書評。</div>

                <div v-for="review in myReviews" :key="review.id"
                    class="border rounded p-4 mb-4 shadow hover:shadow-md transition cursor-pointer">
                    <h3 class="text-lg font-semibold mb-1">{{ review.bookTitle }}</h3>
                    <p class="text-sm text-gray-600 mb-2">⭐ {{ review.rating }} 分</p>
                    <p class="text-gray-700">{{ review.comment }}</p>

                    <div class="flex gap-4 mt-2">
                        <button @click="editReview(review)" class="text-blue-600 hover:underline">修改</button>
                        <button @click="deleteReview(review)" class="text-red-600 hover:underline">刪除</button>
                    </div>
                </div>
            </div>

            <!-- 第三層：實際撰寫書評表單 -->
            <div v-if="step === 'write' && selectedBookForReview" class="max-w-3xl mx-auto text-left">
                <button class="text-blue-600 underline mb-4" @click="selectedBookForReview = null">← 返回書籍列表</button>
                <h2 class="text-2xl font-bold mb-4">撰寫《{{ selectedBookForReview.title }}》的書評</h2>

                <div class="mb-4">
                    <label class="block font-medium mb-1">評分（1~5 分）：</label>
                    <select v-model="newReview.rating" class="border rounded px-3 py-2 w-full">
                        <option disabled value="">請選擇評分</option>
                        <option v-for="n in 5" :key="n" :value="n">{{ n }} 分</option>
                    </select>
                </div>

                <div class="mb-4">
                    <label class="block font-medium mb-1">書評內容：</label>
                    <textarea v-model="newReview.comment" rows="6" class="border rounded px-3 py-2 w-full"></textarea>
                </div>

                <div class="flex gap-4 mt-4">
                    <button @click="submitReview" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                        提交
                    </button>
                    <button @click="selectedBookForReview = null" class="text-gray-600 hover:underline">取消</button>
                </div>
            </div>

            <!-- 第三層：修改書評表單 -->
            <div v-if="step === 'editReview'" class="max-w-3xl mx-auto text-left">
                <button class="text-blue-600 underline mb-4"
                    @click="() => { step = 'write'; actionMode = 'edit'; editingReview = null }">
                    ← 返回書評列表
                </button>
                <h2 class="text-2xl font-bold mb-4">修改書評：{{ editingReview.bookTitle }}</h2>

                <div class="mb-4">
                    <label class="block font-medium mb-1">評分（1~5 分）：</label>
                    <select v-model="reviewRating" class="border rounded px-3 py-2 w-full">
                        <option disabled value="">請選擇評分</option>
                        <option v-for="n in 5" :key="n" :value="n">{{ n }} 分</option>
                    </select>
                </div>

                <div class="mb-4">
                    <label class="block font-medium mb-1">書評內容：</label>
                    <textarea v-model="reviewText" rows="6" class="border rounded px-3 py-2 w-full"></textarea>
                </div>

                <div class="flex gap-4 mt-4">
                    <button @click="updateReview" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">
                        儲存修改
                    </button>
                    <button @click="() => { step = 'write'; actionMode = 'edit'; editingReview = null }"
                        class="text-gray-600 hover:underline">
                        取消
                    </button>
                </div>
            </div>

            <!-- 閱讀心得：篩選與搜尋 -->
            <div v-if="step === 'read'" class="max-w-5xl mx-auto mb-6 text-left">
                <div class="flex flex-wrap items-center gap-4 mb-6">
                    <label class="font-medium">分類：</label>
                    <select v-model="categoryFilter" class="border rounded px-3 py-1">
                        <option value="">全部</option>
                        <option value="總類">總類</option>
                        <option value="哲學類">哲學類</option>
                        <option value="宗教類">宗教類</option>
                        <option value="科學類">科學類</option>
                        <option value="應用科學類">應用科學類</option>
                        <option value="社會科學類">社會科學類</option>
                        <option value="歷史與地理類">歷史與地理類</option>
                        <option value="語言類">語言類</option>
                        <option value="藝術類">藝術類</option>
                        <option value="文學">文學</option>
                        <option value="心理">心理</option>
                        <option value="科幻">科幻</option>
                        <option value="歷史">歷史</option>
                    </select>

                    <label class="font-medium">搜尋書名：</label>
                    <input v-model="searchKeyword" type="text" placeholder="輸入書名關鍵字" class="border rounded px-3 py-1" />
                </div>
            </div>

            <!-- 閱讀心得：搜尋結果 -->
            <div v-if="step === 'read'" class="max-w-4xl mx-auto mb-12">
                <h2 class="text-xl font-bold mb-4">搜尋結果</h2>
                <div v-if="searchedBooks.length === 0" class="text-gray-600">找不到符合條件的書籍</div>
                <div v-for="book in searchedBooks" :key="book.id"
                    class="border rounded p-4 shadow hover:shadow-md transition mb-4">
                    <h3 class="text-lg font-semibold mb-1">{{ book.title }}</h3>
                    <p class="text-sm text-gray-600">作者：{{ book.author }}</p>
                    <button @click="viewBookReviews(book)" class="text-blue-600 hover:underline">查看書評</button>
                </div>

                <!-- 分頁按鈕 -->
                <div class="flex justify-center items-center gap-4 mt-4">
                    <button :disabled="currentPage === 1" @click="() => { if (currentPage > 1) currentPage--; }"
                        class="px-3 py-1 border rounded disabled:opacity-50">
                        上一頁
                    </button>

                    <span>第 {{ currentPage }} 頁，共 {{ searchPageInfo.totalPages }} 頁</span>

                    <button :disabled="currentPage === searchPageInfo.totalPages"
                        @click="() => { if (currentPage < searchPageInfo.totalPages) currentPage++; }"
                        class="px-3 py-1 border rounded disabled:opacity-50">
                        下一頁
                    </button>
                </div>
            </div>

            <!-- 閱讀心得：隨機書籍 -->
            <div v-if="step === 'read'" class="max-w-4xl mx-auto">
                <h2 class="text-xl font-bold mb-4">隨機推薦書籍</h2>
                <div v-if="randomBooks.length === 0" class="text-gray-600">目前沒有隨機書籍</div>
                <div v-for="book in randomBooks" :key="book.id"
                    class="border rounded p-4 shadow hover:shadow-md transition mb-4">
                    <h3 class="text-lg font-semibold mb-1">{{ book.title }}</h3>
                    <p class="text-sm text-gray-600">作者：{{ book.author }}</p>
                    <button @click="viewBookReviews(book)" class="text-blue-600 hover:underline">查看書評</button>
                </div>
            </div>

            <!-- 第三層：單一本書的所有書評 -->
            <div v-if="step === 'bookReviews'" class="max-w-3xl mx-auto text-left">
                <h2 class="text-2xl font-bold mb-4">{{ selectedBook.title }} 的書評</h2>

                <div class="flex justify-end mb-4">
                    <label class="mr-2">排序：</label>
                    <select v-model="sortReviewOption" class="border rounded px-2 py-1">
                        <option value="latest">最新時間</option>
                        <option value="likes">點讚數</option>
                    </select>
                </div>

                <div v-for="review in sortedBookReviews" :key="review.comment_id" class="border rounded p-4 mb-4">
                    <div class="flex justify-between items-center mb-2">
                        <h3 class="font-semibold">{{ review.reviewer }}</h3>
                        <span class="text-sm text-gray-500">{{ review.date }}</span>
                    </div>
                    <p class="mb-2">⭐ {{ review.rating }} 分</p>
                    <p class="text-gray-700">{{ review.comment }}</p>
                    <p class="text-sm text-gray-500 mt-2">
                        👍 點讚數：{{ review.likes }}
                        <button @click="toggleLike(review)" :disabled="review.cooldown"
                            class="text-blue-600 hover:underline ml-2">
                            {{ review.liked ? '取消讚' : '點讚' }}
                        </button>
                        <span v-if="review.cooldown" class="text-red-500 ml-2 text-sm">冷卻中...</span>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import { ref, computed, watch } from 'vue'
import { useCookie } from '#app'

const step = ref(null)
const selectedBook = ref(null)
const editingReview = ref(null)
const selectedBookForReview = ref(null)
const actionMode = ref(null)
const newReview = ref({ rating: '', comment: '' })
const searchedBooks = ref([])
const allReviews = ref([])

const user = useCookie('user')

const categoryFilter = ref('')
const searchKeyword = ref('')
const sortReviewOption = ref('latest')
const bookReviews = ref([])
const reviewText = ref('')
const reviewRating = ref(5)

// 分頁用變數
const searchPageInfo = ref({ totalPages: 1, number: 0 }) // 分頁資訊
const currentPage = ref(1) // 當前頁碼，1 起算

// 取消點讚冷卻秒數
const cooldownSeconds = 10

const randomBooks = ref([])
const borrowedBooks = ref([
    { id: 1, title: '活著', author: '余華' },
    { id: 3, title: '追風箏的人', author: '卡勒德胡賽尼' },
    { id: 6, title: '房思琪的初戀樂園', author: '林奕含' }
])

const goToWrite = () => {
    if (!isLoggedIn.value) {
        alert('請先登入會員後才能撰寫書評')
        return
    }
    // 進入撰寫流程前先清空所有狀態
    selectedBook.value = null
    selectedBookForReview.value = null
    editingReview.value = null
    actionMode.value = null
    newReview.value = { rating: '', comment: '' }
    step.value = 'write'
}

const simulateLogin = () => {
    document.cookie = 'user=123; path=/'
    user.value = '123' // 同步更新 Nuxt3 composable
    location.reload()
}

const simulateLogout = () => {
    document.cookie = 'user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/'
    location.reload()
}

const isLoggedIn = computed(() => {
    return user.value && !isNaN(Number(user.value))
})

const fetchRandomBooks = async () => {
    try {
        const res = await fetch('http://localhost:8080/api/random-books', { credentials: 'include' })
        if (!res.ok) throw new Error('取得隨機書籍失敗')
        randomBooks.value = await res.json()
    } catch (error) {
        console.error(error)
        randomBooks.value = []
    }
}

const myReviews = computed(() => {
    return allReviews.value.filter(r => r.reviewer === '您')
})

const viewBookReviews = async book => {
    selectedBook.value = book
    step.value = 'bookReviews'

    try {
        const res = await fetch(`http://localhost:8080/api/book-comments/book/${book.bookId}`, {
            credentials: 'include'
        })
        if (!res.ok) throw new Error('取得書評失敗')
        const reviews = await res.json()
        initReviews(reviews)
    } catch (error) {
        console.error(error)
        bookReviews.value = []
    }
}

function initReviews(reviews) {
    bookReviews.value = reviews.map(r => ({
        ...r,
        likes: Number(r.likes) || 0, // 確保 likes 是數字，沒有就用 0
        liked: false, // 若後端有資料可直接帶入
        cooldown: false
    }))
}

const fetchSearchedBooks = async () => {
    try {
        const params = new URLSearchParams()
        if (categoryFilter.value && categoryFilter.value.trim() !== '') params.append('category', categoryFilter.value.trim())
        if (searchKeyword.value && searchKeyword.value.trim() !== '') params.append('keyword', searchKeyword.value.trim())
        params.append('page', currentPage.value)
        params.append('pageSize', 10)

        const res = await fetch(`http://localhost:8080/api/search-books?${params.toString()}`, { credentials: 'include' })
        if (!res.ok) throw new Error('取得書籍失敗')
        const data = await res.json()

        searchedBooks.value = data.content // 取分頁物件裡的資料陣列
        searchPageInfo.value = data // 整個分頁資訊物件
    } catch (error) {
        console.error(error)
        searchedBooks.value = []
        searchPageInfo.value = { totalPages: 1, number: 0 }
    }
}

watch([categoryFilter, searchKeyword, currentPage], () => {
    if (categoryFilter.value.trim() !== '' || searchKeyword.value.trim() !== '') {
        fetchSearchedBooks()
    } else {
        searchedBooks.value = []
        searchPageInfo.value = { totalPages: 1, number: 0 }
    }
})

const sortedBookReviews = computed(() => {
    if (sortReviewOption.value === 'likes') {
        return [...bookReviews.value].sort((a, b) => b.likes - a.likes)
    } else {
        return [...bookReviews.value].sort((a, b) => new Date(b.date) - new Date(a.date))
    }
})

const toggleLike = async review => {
    const commentId = review.commentId || review.id

    if (!user.value) {
        alert('請先登入才能點讚書評')
        return
    }

    if (review.liked) {
        if (review.cooldown) {
            alert(`取消點讚請等待 ${cooldownSeconds} 秒冷卻時間`)
            return
        }

        try {
            const res = await fetch(`http://localhost:8080/api/comment/${commentId}/like?memberId=${user.value}`, {
                method: 'DELETE',
                credentials: 'include'
            })
            if (!res.ok) throw new Error('取消點讚失敗')
            review.liked = false
            review.likes--
            review.cooldown = true
            setTimeout(() => (review.cooldown = false), cooldownSeconds * 1000)
        } catch (error) {
            alert(error.message)
        }
    } else {
        try {
            const res = await fetch(`http://localhost:8080/api/comment/${commentId}/like?memberId=${user.value}`, {
                method: 'POST',
                credentials: 'include'
            })
            if (!res.ok) throw new Error('點讚失敗')
            review.liked = true
            review.likes++
        } catch (error) {
            alert(error.message)
        }
    }
}

const submitReview = async () => {
    if (!newReview.value.rating || !newReview.value.comment.trim()) {
        alert('請完整填寫評分與評論內容')
        return
    }

    const userId = Number(user.value)
    if (!userId) {
        alert('登入資訊失效或尚未登入，請重新登入後再提交書評')
        return
    }

    const payload = {
        bookId: selectedBookForReview.value.id,
        userId,
        rating: Number(newReview.value.rating),
        comment: newReview.value.comment
    }

    try {
        const res = await fetch('http://localhost:8080/api/book-comments', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
            credentials: 'include'
        })

        if (!res.ok) throw new Error('提交失敗')

        const data = await res.json()

        allReviews.value.push({
            id: data.commentId,
            reviewer: '您',
            rating: data.rating,
            comment: data.comment,
            date: data.createdAt,
            likes: 0,
            likedBy: [],
            bookId: data.bookId,
            bookTitle: selectedBookForReview.value.title
        })

        borrowedBooks.value = borrowedBooks.value.filter(book => book.id !== selectedBookForReview.value.id)

        selectedBook.value = null
        selectedBookForReview.value = null
        newReview.value = { rating: '', comment: '' }
        actionMode.value = null
        editingReview.value = null
        step.value = null

        alert('✅ 您的書評已成功提交')
    } catch (error) {
        alert('❌ 書評送出失敗，請稍後再試')
        console.error(error)
    }
}

const editReview = review => {
    editingReview.value = review
    reviewText.value = review.comment
    reviewRating.value = review.rating
    step.value = 'editReview'
}

const updateReview = async () => {
    if (!reviewRating.value || !reviewText.value.trim()) {
        alert('請完整填寫評分與評論內容')
        return
    }

    const userId = Number(user.value)
    if (!userId) {
        alert('登入資訊失效或尚未登入，請重新登入後再提交書評')
        return
    }

    const payload = {
        bookId: editingReview.value.bookId,
        userId: Number(user.value),
        rating: reviewRating.value,
        comment: reviewText.value
    }

    try {
        const res = await fetch(`http://localhost:8080/api/book-comments/${editingReview.value.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
            credentials: 'include'
        })

        if (!res.ok) throw new Error('更新失敗')

        const data = await res.json()

        const index = allReviews.value.findIndex(r => r.id === editingReview.value.id)
        if (index !== -1) {
            allReviews.value[index].rating = data.rating
            allReviews.value[index].comment = data.comment
            allReviews.value[index].date = data.updatedAt || allReviews.value[index].date
        }

        alert('✅ 書評更新成功')

        step.value = 'write'
        actionMode.value = 'edit'
        editingReview.value = null
        reviewText.value = ''
        reviewRating.value = 5
    } catch (error) {
        alert('❌ 書評更新失敗，請稍後再試')
        console.error(error)
    }
}

const deleteReview = async review => {
    if (!confirm('確定要刪除這則書評嗎？')) return

    try {
        const res = await fetch(`http://localhost:8080/api/book-comments/${review.id}`, {
            method: 'DELETE',
            credentials: 'include'
        })

        if (!res.ok) throw new Error('刪除失敗')

        allReviews.value = allReviews.value.filter(r => r.id !== review.id)

        const existing = borrowedBooks.value.find(b => b.id === review.bookId)
        if (!existing) {
            borrowedBooks.value.push({
                id: review.bookId,
                title: review.bookTitle,
                author: review.bookAuthor
            })
        }

        alert('✅ 書評已刪除')
        step.value = 'write'
    } catch (error) {
        alert('❌ 書評刪除失敗，請稍後再試')
        console.error(error)
    }
}

const startWritingReview = book => {
    selectedBookForReview.value = book
    newReview.value = { rating: '', comment: '' }
}

watch(step, val => {
    if (val === 'read') fetchRandomBooks()
})
</script>
