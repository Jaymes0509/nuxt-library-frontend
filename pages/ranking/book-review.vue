<template>
  <LoginRequiredPrompt v-if="showLoginPrompt" />
  <div v-else class="page-container">
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
        <div class="feature-card blue" @click="handleGoToWrite">
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
.page-container {
  background-color: white;
  padding: 2rem;
  width: 100%;
  text-align: center;
}

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

.sort-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 1rem;
  gap: 0.5rem;
}
</style>


<script setup>
import { ref, computed, onMounted, toRaw, nextTick } from 'vue'
import { jwtDecode } from 'jwt-decode'
const showLoginPrompt = ref(false)

/* 🔐 使用者身份 */
const email = ref('')
const userId = ref(null)
const isLoggedIn = ref(false)
const isReady = ref(false)

/* 📄 畫面狀態控制 */
const step = ref(null)
const previousStep = ref('read')
const actionMode = ref(null)
const selectedBook = ref(null)
const selectedBookForReview = ref(null)
const editingReview = ref(null)

/* ✍️ 書評輸入 */
const newReview = ref({ rating: '', comment: '' })
const reviewText = ref('')
const reviewRating = ref(5)

/* 📚 書籍與書評資料 */
const borrowedBooks = ref([])
const allBorrowedBooks = ref([])
const allReviews = ref([])
const bookReviews = ref([])
const myReviews = ref([])

/* 🔎 搜尋與篩選 */
const categoryFilter = ref('')
const searchKeyword = ref('')
const sortReviewOption = ref('latest')

/* 📄 分頁資訊 */
const searchPageInfo = ref({ totalPages: 1, number: 0 })
const currentPage = ref(1)

/* 🕒 常數設定 */
const cooldownSeconds = 24 * 60 * 60
const randomBooks = ref([])
const searchedBooks = ref([])

/* 📚 書籍相關功能 */
const fetchReviewableBooks = async () => {
  if (!userId.value) return
  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/reviewable-books/${userId.value}`, { credentials: 'include' })
    if (!res.ok) throw new Error('取得可撰寫書籍失敗')
    const data = await res.json()
    borrowedBooks.value = data
    allBorrowedBooks.value = data.slice()
  } catch (err) {
    console.error(err)
    borrowedBooks.value = []
    allBorrowedBooks.value = []
  }
}

const fetchAllBorrowedBooks = async () => {
  if (!userId.value) return
  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/borrowed-books/${userId.value}`, { credentials: 'include' })
    if (!res.ok) throw new Error('取得所有借閱書籍失敗')
    const data = await res.json()
    allBorrowedBooks.value = data
    borrowedBooks.value = data.slice()
  } catch (error) {
    console.error(error)
    allBorrowedBooks.value = []
    borrowedBooks.value = []
  }
}

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

const fetchSearchedBooks = async () => {
  try {
    const params = new URLSearchParams()
    if (categoryFilter.value.trim()) params.append('category', categoryFilter.value.trim())
    if (searchKeyword.value.trim()) params.append('keyword', searchKeyword.value.trim())
    params.append('page', currentPage.value)
    params.append('pageSize', 10)

    const res = await fetch(`http://localhost:8080/api/search-books?${params.toString()}`, { credentials: 'include' })
    if (!res.ok) throw new Error('取得書籍失敗')
    const data = await res.json()
    searchedBooks.value = data.content
    searchPageInfo.value = data
  } catch (error) {
    console.error(error)
    searchedBooks.value = []
    searchPageInfo.value = { totalPages: 1, number: 0 }
  }
}

/* 📖 書評相關 */
const fetchMyReviews = async () => {
  if (!userId.value) return
  try {
    if (allBorrowedBooks.value.length === 0) {
      await fetchAllBorrowedBooks()
    }

    const res = await fetch(`http://localhost:8080/api/book-comments/user/${userId.value}`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error('取得我的書評失敗')

    const reviews = await res.json()
    const rawBooks = toRaw(allBorrowedBooks.value)

    const enriched = reviews.map(r => {
      const book = rawBooks.find(b => String(b.bookId) === String(r.bookId)) || {}
      return {
        ...r,
        bookTitle: book.title || '未知書名',
        bookAuthor: book.author || '未知作者',
        reviewer: '您'
      }
    })

    allReviews.value = enriched
    myReviews.value = enriched // 🔧 同步到 myReviews，用於我的專屬畫面
  } catch (error) {
    console.error(error)
    allReviews.value = []
    myReviews.value = []
  }
}


const submitReview = async () => {
  if (!newReview.value.rating || !newReview.value.comment.trim()) {
    alert('請完整填寫評分與評論內容')
    return
  }

  if (!userId.value) {
    alert('登入資訊失效，請重新登入後再提交書評')
    return
  }

  const payload = {
    bookId: selectedBookForReview.value.bookId,
    userId: userId.value,
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

const updateReview = async () => {
  if (!reviewRating.value || !reviewText.value.trim()) {
    alert('請完整填寫評分與評論內容')
    return
  }

  if (!userId.value) {
    alert('登入資訊失效，請重新登入後再提交書評')
    return
  }

  const payload = {
    bookId: editingReview.value.bookId,
    userId: userId.value,
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

    const index = allReviews.value.findIndex(r => r.commentId === editingReview.value.commentId)
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
    const res = await fetch(`http://localhost:8080/api/book-comments/${review.commentId}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    if (!res.ok) throw new Error('刪除失敗')

    allReviews.value = allReviews.value.filter(r => r.commentId !== review.commentId)
    await fetchAllBorrowedBooks()

    const rawBooks = toRaw(allBorrowedBooks.value)
    const book = rawBooks.find(b => String(b.bookId) === String(review.bookId))
    if (book && !borrowedBooks.value.some(b => b.bookId === book.bookId)) {
      borrowedBooks.value.push({ bookId: book.bookId, title: book.title, author: book.author })
    }

    alert('✅ 書評已刪除')
    step.value = 'write'
    actionMode.value = 'edit'
  } catch (error) {
    alert('❌ 書評刪除失敗，請稍後再試')
    console.error(error)
  }
}

const enrichReviewsWithLikes = async () => {
  for (const review of allReviews.value) {
    const commentId = review.commentId || review.id

    try {
      const res1 = await fetch(`http://localhost:8080/api/comment/${commentId}/like-count`)
      review.likes = await res1.json()
    } catch {
      review.likes = 0
    }

    if (userId.value) {
      try {
        const res2 = await fetch(`http://localhost:8080/api/comment/${commentId}/liked?userId=${userId.value}`)
        review.liked = await res2.json()
      } catch {
        review.liked = false
      }
    } else {
      review.liked = false
    }
  }
}

/* 🎯 書評相關 UI 操作 */
const startWritingReview = book => {
  selectedBookForReview.value = book
  newReview.value = { rating: '', comment: '' }
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

const toggleLike = async review => {
  const commentId = review.commentId || review.id
  if (!userId.value) return alert('請先登入才能點讚書評')

  if (review.liked) {
    if (review.cooldown) return alert(`取消點讚請等待 ${cooldownSeconds} 秒冷卻時間`)
    try {
      const res = await fetch(`http://localhost:8080/api/comment/${commentId}/like?userId=${userId.value}`, {
        method: 'DELETE',
        credentials: 'include'
      })
      if (!res.ok) throw new Error()
      review.liked = false
      review.likes--
      review.cooldown = true
      setTimeout(() => (review.cooldown = false), cooldownSeconds * 1000)
    } catch {
      alert('取消點讚失敗')
    }
  } else {
    try {
      const res = await fetch(`http://localhost:8080/api/comment/${commentId}/like?userId=${userId.value}`, {
        method: 'POST',
        credentials: 'include'
      })
      if (!res.ok) throw new Error()
      review.liked = true
      review.likes++
    } catch {
      alert('點讚失敗')
    }
  }
}

const viewBookReviews = async book => {
  selectedBook.value = book
  previousStep.value = step.value
  step.value = 'bookReviews'

  try {
    const res = await fetch(`http://localhost:8080/api/book-comments/book/${book.bookId}`, {
      credentials: 'include'
    })
    if (!res.ok) throw new Error()
    const reviews = await res.json()
    initReviews(reviews)

    for (const review of bookReviews.value) {
      const commentId = review.commentId || review.id
      try {
        const res1 = await fetch(`http://localhost:8080/api/comment/${commentId}/like-count`)
        review.likes = await res1.json()
      } catch {
        review.likes = 0
      }

      if (userId.value) {
        try {
          const res2 = await fetch(`http://localhost:8080/api/comment/${commentId}/liked?userId=${userId.value}`)
          review.liked = await res2.json()
        } catch {
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

/* 🔁 流程控制與畫面回復 */
const goToEditReviews = async () => {
  actionMode.value = 'edit'
  await fetchAllBorrowedBooks()
  await fetchMyReviews()
}

const goToWrite = async () => {
  // 等待初始化完成
  if (!isReady.value) {
    alert('資料尚未初始化完成，請稍候再試')
    return
  }

  if (!isLoggedIn.value) {
    alert('請先登入會員')
    return
  }
  selectedBook.value = null
  selectedBookForReview.value = null
  editingReview.value = null
  actionMode.value = null
  newReview.value = { rating: '', comment: '' }
  await fetchReviewableBooks()
  step.value = 'write'
}

function handleGoToWrite() {
  if (!isReady.value) {
    alert('請稍候，資料尚在初始化中')
    return
  }

  if (!isLoggedIn.value) {
    showLoginPrompt.value = true
    return
  }
  goToWrite()
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

/* 🛠 工具 */
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

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date)) return ''
  return date.toLocaleString()
}

/* 🔄 搜尋觸發 */
watch([categoryFilter, searchKeyword, currentPage], () => {
  if (categoryFilter.value.trim() || searchKeyword.value.trim()) {
    fetchSearchedBooks()
  } else {
    searchedBooks.value = []
    searchPageInfo.value = { totalPages: 1, number: 0 }
  }
})

/* 📊 書評排序邏輯 */
const sortedBookReviews = computed(() => {
  if (sortReviewOption.value === 'likes') {
    return [...bookReviews.value].sort((a, b) => b.likes - a.likes)
  } else {
    return [...bookReviews.value].sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt))
  }
})

watch(isLoggedIn, (val) => {
})

onMounted(async () => {
  const token = localStorage.getItem('jwt_token')
  await fetchRandomBooks()

  if (!token) {
    console.warn('❌ 沒有 token，略過會員初始化')
    isReady.value = true
    return
  }

  try {
    const decoded = jwtDecode(token)
    email.value = decoded?.sub || ''
    if (email.value) {
      const encodedEmail = encodeURIComponent(email.value)
      const res = await $fetch(`http://localhost:8080/api/book-comments/user-id-by-email/${encodedEmail}`)
      userId.value = res
      isLoggedIn.value = true
    } else {
      console.warn('⚠️ token 中沒有 email')
    }

    await fetchReviewableBooks()
    await fetchMyReviews()
    await enrichReviewsWithLikes()
  } catch (err) {
    console.error('❌ token 解析或 $fetch 失敗:', err)
  }

  isReady.value = true
})
</script>
