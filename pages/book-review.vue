<template>
  <div class="page-container">
    <!-- <div v-if="step" class="text-left">
      <button class="back-button" @click="step = null">
        ← 返回功能總覽
      </button>
    </div> -->

    <!-- 第一層 -->
    <div v-if="!step">
      <div style="margin-bottom: 2.5rem; border-bottom: 1px solid #ccc; padding-bottom: 1rem;">
        <div class="feature-header">
          <span style="font-size: 2rem; margin-right: 0.5rem;">📚</span>
          讀者書評
        </div>
        <div class="feature-subtitle">功能總覽</div>
      </div>

      <div style="display: flex; justify-content: center; gap: 3rem; margin-bottom: 3rem;">
        <div class="feature-card blue" @click="goToWrite">
          <div style="font-size: 2rem; margin-bottom: 0.5rem;">📝</div>
          <div class="feature-card-title">撰寫心得</div>
          <div class="feature-card-text">針對您借閱的書籍，留下寶貴評論與評分</div>
        </div>
        <div class="feature-card green" @click="step = 'read'">
          <div style="font-size: 2rem; margin-bottom: 0.5rem;">📖</div>
          <div class="feature-card-title">閱讀書評</div>
          <div class="feature-card-text">查看其他讀者對書籍的評價與感想</div>
        </div>
      </div>

      <div class="login-buttons">
        <button class="login" @click="simulateLogin">模擬登入會員</button>
        <button class="logout" @click="simulateLogout">模擬登出會員</button>
      </div>
    </div>

    <!-- 第二層 撰寫新書評或修改&刪除舊書評 -->

    <!-- 只在第二層才顯示返回按鈕 -->
    <button v-if="step === 'write' && actionMode === null" @click="step = null" class="back-button">
      ← 返回
    </button>

    <div v-if="step === 'write' && actionMode === null">
      <h2 class="feature-card-title" style="margin-bottom: 1rem;">請選擇操作類型</h2>
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; max-width: 960px; margin: 0 auto;">
        <div class="book-card" @click="actionMode = 'new'">
          <h3 class="book-title">✍ 撰寫新的書評</h3>
          <p class="book-author">針對您尚未評論過的書籍新增書評</p>
        </div>
        <div class="book-card" @click="goToEditReviews">
          <h3 class="book-title">🛠 修改或檢視我的書評</h3>
          <p class="book-author">檢視您已發表的書評，進行修改或刪除</p>
        </div>
      </div>
    </div>

    <!-- 第二層 撰寫新書評 -->
    <div v-if="step === 'write' && actionMode === 'new' && !selectedBookForReview"
      style="max-width: 768px; margin: 0 auto; text-align: left;">
      <button v-if="step === 'write' && actionMode === 'new' && !selectedBookForReview" @click="actionMode = null"
        class="back-button">
        ← 返回操作選單
      </button>
      <h2 class="feature-card-title" style="margin-bottom: 1.5rem;">請選擇您想撰寫書評的書籍</h2>
      <div v-if="borrowedBooks.length === 0" class="book-author">您目前沒有可以撰寫書評的書籍。</div>
      <div v-for="book in borrowedBooks" :key="book.bookId" class="book-card" @click="startWritingReview(book)">
        <h3 class="book-title">{{ book.title }}</h3>
        <p class="book-author">作者：{{ book.author }}</p>
        <p class="book-link">點擊撰寫書評</p>
      </div>
    </div>

    <!-- 第二層 修改書評 -->
    <div v-if="step === 'write' && actionMode === 'edit'" style="max-width: 960px; margin: 0 auto; text-align: left;">
      <button class="back-button" style="position: fixed; right: 1rem; bottom: 10rem; z-index: 20;"
        @click="actionMode = null">
        ← 返回操作選單
      </button>
      <h2 class="feature-card-title" style="margin: 1rem 0 1.5rem;">我的書評列表</h2>
      <div v-if="myReviews.length === 0" class="book-author">您尚未撰寫任何書評。</div>
      <div v-for="review in myReviews" :key="review.commentId" class="book-card">
        <h3 class="book-title">{{ review.bookTitle }}</h3>
        <p class="book-author">作者：{{ review.bookAuthor }}</p>
        <p style="font-size: 0.875rem; margin: 0.5rem 0;">⭐ {{ review.rating }} 分</p>
        <p>{{ review.comment }}</p>
        <div class="review-actions">
          <button @click="editReview(review)">修改</button>
          <button class="delete" @click="deleteReview(review)">刪除</button>
        </div>
      </div>
    </div>

    <!-- 第三層 撰寫書評表單 -->
    <div v-if="step === 'write' && selectedBookForReview" style="max-width: 768px; margin: 0 auto; text-align: left;">
      <button class="back-button" @click="selectedBookForReview = null">← 返回書籍列表</button>

      <h2 class="feature-card-title" style="margin: 1rem 0;">撰寫《{{ selectedBookForReview.title }}》的書評</h2>
      <div class="form-group">
        <label>評分（1~5 分）：</label>
        <select v-model="newReview.rating">
          <option disabled value="">請選擇評分</option>
          <option v-for="n in 5" :key="n" :value="n">{{ n }} 分</option>
        </select>
      </div>
      <div class="form-group">
        <label>書評內容：</label>
        <textarea v-model="newReview.comment" rows="6"></textarea>
      </div>
      <div class="review-actions">
        <button @click="submitReview"
          style="background-color: #2563eb; color: white; padding: 0.5rem 1rem; border-radius: 0.375rem;">提交</button>
        <button class="book-link" @click="selectedBookForReview = null">取消</button>
      </div>
    </div>

    <!-- 第二層 閱讀書評 -->
    <div v-if="step === 'read'" style="max-width: 960px; margin: 0 auto; text-align: left;">
      <button v-if="step === 'read'" @click="step = null" class="back-button">
        ← 返回
      </button>

      <div class="form-group"
        style="display: flex; flex-wrap: wrap; gap: 1rem; align-items: center; margin-bottom: 1.5rem;">
        <label>分類：</label>
        <select v-model="categoryFilter">
          <option value="">全部</option>
          <option value="總類">總類</option>
          <option value="哲學類">哲學類</option>
          <option value="宗教類">宗教類</option>
          <option value="科學類">科學類</option>
          <option value="應用科學類">應用科學類</option>
          <option value="社會科學類">社會科學類</option>
          <option value="史地類：中國史地">史地類：中國史地</option>
          <option value="史地類：世界史地">史地類：世界史地</option>
          <option value="語言文學類">語言文學類</option>
          <option value="藝術類">藝術類</option>
        </select>

        <label>搜尋書名：</label>
        <input v-model="searchKeyword" type="text" placeholder="輸入書名關鍵字" />
      </div>

      <h2 class="feature-card-title">搜尋結果</h2>
      <div v-if="searchedBooks.length === 0" class="book-author">找不到符合條件的書籍</div>
      <div v-for="book in searchedBooks" :key="book.id" class="book-card">
        <h3 class="book-title">{{ book.title }}</h3>
        <p class="book-author">作者：{{ book.author }}</p>
        <button class="book-link" @click="viewBookReviews(book)">查看書評</button>
      </div>

      <div class="pagination">
        <button :disabled="currentPage === 1" @click="() => currentPage--">上一頁</button>
        <span>第 {{ currentPage }} 頁，共 {{ searchPageInfo.totalPages }} 頁</span>
        <button :disabled="currentPage === searchPageInfo.totalPages" @click="() => currentPage++">下一頁</button>
      </div>

      <h2 class="feature-card-title" style="margin-top: 2rem;">隨機推薦書籍</h2>
      <div v-if="randomBooks.length === 0" class="book-author">目前沒有隨機書籍</div>
      <div v-for="book in randomBooks" :key="book.id" class="book-card">
        <h3 class="book-title">{{ book.title }}</h3>
        <p class="book-author">作者：{{ book.author }}</p>
        <button class="book-link" @click="viewBookReviews(book)">查看書評</button>
      </div>
    </div>

    <!-- 第四層 單本書的所有書評 -->
    <div v-if="step === 'bookReviews'" style="max-width: 768px; margin: 0 auto; text-align: left;">
      <button class="back-button" @click="returnToPreviousStepAndReset">← 返回上一層</button>
      <h2 class="feature-card-title" style="margin-top: 1rem;">{{ selectedBook.title }} 的書評</h2>

      <div class="form-group"
        style="display: flex; justify-content: flex-end; align-items: center; gap: 0.5rem; margin-bottom: 1rem;">
        <label>排序：</label>
        <select v-model="sortReviewOption">
          <option value="latest">最新時間</option>
          <option value="likes">點讚數</option>
        </select>
      </div>

      <div v-if="sortedBookReviews.length === 0" class="book-author">
        目前尚無任何書評，歡迎成為第一位分享者！
      </div>

      <div v-for="review in sortedBookReviews" :key="review.comment_id" class="book-card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
          <h3 class="book-title">{{ review.reviewer }}</h3>
          <span class="book-author">{{ formatDate(review.updatedAt || review.createdAt) }}</span>
        </div>
        <p style="margin-bottom: 0.5rem;">⭐ {{ review.rating }} 分</p>
        <p>{{ review.comment }}</p>
        <p style="font-size: 0.875rem; color: #6b7280; margin-top: 0.5rem;">
          👍 點讚數：{{ review.likes }}
          <button class="book-link" @click="toggleLike(review)" :disabled="review.cooldown">
            {{ review.liked ? '取消讚' : '點讚' }}
          </button>
          <span v-if="review.cooldown" style="color: red; margin-left: 0.5rem; font-size: 0.75rem;">冷卻中...</span>
        </p>
      </div>
    </div>

    <!-- 第四層 修改書評表單 -->
    <div v-if="step === 'editReview'" style="max-width: 768px; margin: 0 auto; text-align: left;">
      <button class="back-button" @click="() => { step = 'write'; actionMode = 'edit'; editingReview = null }">
        ← 返回書評列表
      </button>

      <h2 class="feature-card-title" style="margin: 1rem 0;">修改《{{ editingReview.bookTitle }}》的書評</h2>
      <div class="form-group">
        <label>評分（1~5 分）：</label>
        <select v-model="reviewRating">
          <option disabled value="">請選擇評分</option>
          <option v-for="n in 5" :key="n" :value="n">{{ n }} 分</option>
        </select>
      </div>
      <div class="form-group">
        <label>書評內容：</label>
        <textarea v-model="reviewText" rows="6"></textarea>
      </div>
      <div class="review-actions">
        <button @click="updateReview"
          style="background-color: #2563eb; color: white; padding: 0.5rem 1rem; border-radius: 0.375rem;">更新</button>
        <button class="book-link"
          @click="() => { step = 'write'; actionMode = 'edit'; editingReview = null }">取消</button>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* 頁面主容器樣式 */
.page-container {
  background-color: white;
  padding: 2rem;
  width: 100%;
  text-align: center;
}

/* 返回按鈕固定於右下 */
.back-button {
  position: fixed;
  right: 1rem;
  bottom: 10rem;
  background-color: white;
  color: #2563eb;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 20;
  transition: background-color 0.2s;
}

.back-button:hover {
  background-color: #ebf8ff;
}

/* 功能標題與圖示 */
.feature-header {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 2rem;
  font-weight: bold;
}

.feature-subtitle {
  display: flex;
  justify-content: center;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e40af;
  border-left: 4px solid #2563eb;
  padding-left: 0.75rem;
}

/* 功能選單卡片 */
.feature-card {
  width: 18rem;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  text-align: center;
  transition: background-color 0.3s;
}

.feature-card.blue {
  background-color: #dbeafe;
}

.feature-card.blue:hover {
  background-color: #bfdbfe;
}

.feature-card.green {
  background-color: #d1fae5;
}

.feature-card.green:hover {
  background-color: #a7f3d0;
}

.feature-card-title {
  font-size: 1.25rem;
  font-weight: bold;
  margin-bottom: 0.25rem;
}

.feature-card-text {
  font-size: 0.875rem;
  color: #374151;
}

/* 模擬登入登出按鈕 */
.login-buttons button {
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  color: white;
  margin-top: 0.5rem;
  width: 200px;
}

.login-buttons .login {
  background-color: #16a34a;
}

.login-buttons .logout {
  background-color: #4b5563;
}

/* 書籍選擇與評論卡片 */
.book-card {
  border: 1px solid #ccc;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  text-align: left;
  cursor: pointer;
  transition: box-shadow 0.3s;
}

.book-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.book-title {
  font-size: 1.125rem;
  font-weight: 600;
}

.book-author {
  font-size: 0.875rem;
  color: #6b7280;
}

.book-link {
  font-size: 0.875rem;
  color: #2563eb;
  margin-top: 0.5rem;
  display: inline-block;
}

.book-link:hover {
  text-decoration: underline;
}

/* 表單欄位與標籤 */
.form-group {
  margin-bottom: 1rem;
  text-align: left;
}

.form-group label {
  font-weight: 500;
  display: block;
  margin-bottom: 0.5rem;
}

.form-group select,
.form-group textarea,
input[type="text"] {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ccc;
  border-radius: 0.375rem;
}

/* 書評排序與按鈕 */
.sort-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 1rem;
  gap: 0.5rem;
}

.review-actions {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.review-actions button {
  font-size: 0.875rem;
  color: #2563eb;
  background: none;
  border: none;
  cursor: pointer;
}

.review-actions .delete {
  color: #dc2626;
}

.review-actions button:hover {
  text-decoration: underline;
}

/* 分頁控制 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.pagination button {
  padding: 0.25rem 0.75rem;
  border-radius: 0.375rem;
  border: 1px solid #ccc;
  background-color: white;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>

<script setup>
import { ref, computed, onMounted, toRaw, nextTick } from 'vue'
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
const previousStep = ref('read')

// 分頁用變數
const searchPageInfo = ref({ totalPages: 1, number: 0 }) // 分頁資訊
const currentPage = ref(1) // 當前頁碼，1 起算

// 取消點讚冷卻秒數
const cooldownSeconds = 24 * 60 * 60

const randomBooks = ref([])

const borrowedBooks = ref([])

const allBorrowedBooks = ref([]) // 新增完整書籍清單

const fetchReviewableBooks = async () => {
  if (!isLoggedIn.value) return
  const userId = Number(user.value)

  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/reviewable-books/${userId}`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error('取得可撰寫書評的書籍失敗')
    const data = await res.json()
    borrowedBooks.value = data
    allBorrowedBooks.value = data.slice() // 複製一份完整清單，保持不變
    // console.log('fetchReviewableBooks 讀到的書籍資料:', allBorrowedBooks.value)

    // console.log('fetchReviewableBooks 詳細資料:', JSON.stringify(allBorrowedBooks.value, null, 2))
    console.log('fetchReviewableBooks 抓到的書籍資料:', data)
  } catch (err) {
    console.error(err)
    borrowedBooks.value = []
    allBorrowedBooks.value = []
  }
}

const deleteReview = async review => {
  if (!confirm('確定要刪除這則書評嗎？')) return

  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/${review.commentId}`, {
      method: 'DELETE',
      credentials: 'include'
    })

    if (!res.ok) throw new Error('刪除失敗')

    // 從書評列表移除
    allReviews.value = allReviews.value.filter(r => r.commentId !== review.commentId)

    // 重新取得完整借閱書籍清單（包含有無評論的所有書）
    await fetchAllBorrowedBooks()

    // 找尋對應書籍
    const rawBooks = toRaw(allBorrowedBooks.value)
    const book = rawBooks.find(b => String(b.bookId) === String(review.bookId))

    if (book) {
      // 這裡判斷撰寫書評用的 borrowedBooks 是否已包含，沒包含再放入
      const bookExists = borrowedBooks.value.some(b => b.bookId === book.bookId)
      if (!bookExists) {
        borrowedBooks.value.push({
          bookId: book.bookId,
          title: book.title,
          author: book.author
        })
      }
    } else {
      console.warn(`找不到書籍資料，bookId: ${review.bookId}`)
    }

    alert('✅ 書評已刪除')
    step.value = 'write'
    actionMode.value = 'edit'
  } catch (error) {
    alert('❌ 書評刪除失敗，請稍後再試')
    console.error(error)
  }
}

const goToWrite = async () => {
  if (!isLoggedIn.value) {
    alert('請先登入會員後才能撰寫書評')
    return
  }

  // 清空狀態
  selectedBook.value = null
  selectedBookForReview.value = null
  editingReview.value = null
  actionMode.value = null
  newReview.value = { rating: '', comment: '' }

  // 從後端載入可撰寫書評的書籍
  await fetchReviewableBooks()

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

const fetchMyReviews = async () => {
  if (!isLoggedIn.value) return
  const userId = Number(user.value)

  try {
    if (allBorrowedBooks.value.length === 0) {
      await fetchAllBorrowedBooks()  // 確保抓取所有借閱書籍
    }

    const res = await fetch(`http://localhost:8080/api/book-comments/user/${userId}`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error('取得我的書評失敗')
    const reviews = await res.json()

    const rawBooks = toRaw(allBorrowedBooks.value)
    allReviews.value = reviews.map(r => {
      const book = rawBooks.find(b => String(b.bookId) === String(r.bookId)) || {}
      return {
        ...r,
        bookTitle: book.title || '未知書名',
        bookAuthor: book.author || '未知作者',
        reviewer: '您'
      }
    })
    console.log('fetchMyReviews 執行完畢, allReviews:', allReviews.value)
  } catch (error) {
    console.error(error)
    allReviews.value = []
  }
}

const fetchAllBorrowedBooks = async () => {
  if (!isLoggedIn.value) return
  const userId = Number(user.value)
  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/borrowed-books/${userId}`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error('取得所有借閱書籍失敗')
    const data = await res.json()
    allBorrowedBooks.value = data
    borrowedBooks.value = data.slice() // 複製一份給撰寫用
    console.log('fetchAllBorrowedBooks 取得完整借閱書籍:', data)
  } catch (error) {
    console.error(error)
    allBorrowedBooks.value = []
    borrowedBooks.value = []
  }
}

onMounted(async () => {
  if (isLoggedIn.value) {
    await fetchReviewableBooks()
    await fetchMyReviews()
    await enrichReviewsWithLikes()
  }
})

// 替每則書評補上「點讚數」與「是否已點讚」
const enrichReviewsWithLikes = async () => {
  for (const review of allReviews.value) {
    const commentId = review.commentId || review.id

    // 查詢點讚數量
    try {
      const res1 = await fetch(`http://localhost:8080/api/comment/${commentId}/like-count`)
      const likeCount = await res1.json()
      review.likes = likeCount
    } catch (err) {
      console.warn('載入點讚數失敗', commentId, err)
      review.likes = 0
    }

    // 查詢使用者是否已點讚
    if (user.value) {
      try {
        const res2 = await fetch(`http://localhost:8080/api/comment/${commentId}/liked?userId=${user.value}`)
        const liked = await res2.json()
        review.liked = liked
      } catch (err) {
        console.warn('載入點讚狀態失敗', commentId, err)
        review.liked = false
      }
    } else {
      review.liked = false
    }
  }
}

const goToEditReviews = async () => {
  actionMode.value = 'edit'
  await fetchAllBorrowedBooks()  // 先確保 allBorrowedBooks 有所有借閱書籍資料
  await fetchMyReviews()        // 再抓書評並配對書籍資料
}

const myReviews = computed(() => {
  return allReviews.value.filter(r => r.reviewer === '您')
})

const viewBookReviews = async book => {
  selectedBook.value = book
  previousStep.value = step.value
  step.value = 'bookReviews'

  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/book/${book.bookId}`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error('取得書評失敗')
    const reviews = await res.json()
    initReviews(reviews)

    // 加入點讚數與是否已點讚
    for (const review of bookReviews.value) {
      const commentId = review.commentId || review.id

      // 點讚數量
      try {
        const res1 = await fetch(`http://localhost:8080/api/comment/${commentId}/like-count`)
        const likeCount = await res1.json()
        review.likes = likeCount
      } catch (err) {
        console.warn('載入點讚數失敗', commentId, err)
        review.likes = 0
      }

      // 是否已點讚
      if (user.value) {
        try {
          const res2 = await fetch(`http://localhost:8080/api/comment/${commentId}/liked?userId=${user.value}`)
          const liked = await res2.json()
          review.liked = liked
        } catch (err) {
          console.warn('載入點讚狀態失敗', commentId, err)
          review.liked = false
        }
      } else {
        review.liked = false
      }
    }
  } catch (error) {
    console.error(error)
    bookReviews.value = []
  }
}


const returnToPrevious = () => {
  step.value = previousStep.value || null
}

const returnToPreviousStepAndReset = async () => {
  const targetStep = previousStep.value || null
  step.value = targetStep

  if (targetStep === 'write' || targetStep === 'read') {
    categoryFilter.value = ''
    searchKeyword.value = ''
    await nextTick()
  }
}



function initReviews(reviews) {
  bookReviews.value = reviews.map(r => ({
    ...r,
    likes: Number(r.likes) || 0,
    liked: false,
    cooldown: false,
    createdAt: r.createdAt,
    updatedAt: r.updatedAt
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

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date)) return ''
  return date.toLocaleString() // 你也可以改成自己想要的格式
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
    // 使用 updatedAt（如果有）或 createdAt 排序
    return [...bookReviews.value].sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt))
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
      const res = await fetch(`http://localhost:8080/api/comment/${commentId}/like?userId=${user.value}`, {
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
      const res = await fetch(`http://localhost:8080/api/comment/${commentId}/like?userId=${user.value}`, {
        method: 'POST', // or 'DELETE'
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
  // console.log('selectedBookForReview.value', selectedBookForReview.value)

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
    bookId: selectedBookForReview.value.bookId,
    userId,
    rating: Number(newReview.value.rating),
    comment: newReview.value.comment
  }

  console.log('送出書評的 payload:', payload)

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
      commentId: data.commentId,
      reviewer: '您',
      rating: data.rating,
      comment: data.comment,
      date: data.createdAt,
      likes: 0,
      likedBy: [],
      bookId: data.bookId,
      bookTitle: selectedBookForReview.value.title
    })

    borrowedBooks.value = borrowedBooks.value.filter(book => book.bookId !== selectedBookForReview.value.bookId)

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
  editingReview.value = {
    ...review,
    bookTitle: review.bookTitle || '未知書名',
    bookAuthor: review.bookAuthor || '未知作者',
    updatedAt: review.updatedAt
  }
  reviewText.value = review.comment
  reviewRating.value = review.rating
  step.value = 'editReview'
}

const updateReview = async () => {
  // console.log('updateReview 被呼叫，editingReview:', editingReview.value)

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
    userId,
    rating: reviewRating.value,
    comment: reviewText.value
  }

  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/${editingReview.value.commentId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      credentials: 'include'
    })

    if (!res.ok) throw new Error('更新失敗')

    const data = await res.json()
    // console.log('更新後 API 回傳的書評資料', data)

    const index = allReviews.value.findIndex(r => r.commentId === editingReview.value.commentId)
    // console.log('找到的 index:', index)

    if (index !== -1) {
      allReviews.value[index].rating = data.rating
      allReviews.value[index].comment = data.comment
      allReviews.value[index].date = data.updatedAt || allReviews.value[index].date
    }

    // console.log('更新後的 allReviews', allReviews.value)
    // console.log('myReviews 重新計算結果', myReviews.value)

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

const startWritingReview = book => {
  selectedBookForReview.value = book
  newReview.value = { rating: '', comment: '' }
}

watch(step, val => {
  if (val === 'read') fetchRandomBooks()
})


</script>
