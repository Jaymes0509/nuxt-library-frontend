<template>
  <div class="book-detail-wrapper">
    <!-- 左半邊：封面圖片 -->
    <div class="cover-area">
      <img class="cover-image" :src="bookCoverUrl" :alt="`書籍《${book.title}》封面`" />
    </div>

    <!-- 右半邊：文字內容區 -->
    <div class="info-area">
      <!-- 書名 -->
      <h1 class="book-title">{{ book.title }}</h1>

      <!-- 內容簡介 -->
      <section class="section-block" v-if="book.summary">
        <h2>內容簡介</h2>
        <p>{{ book.summary }}</p>
      </section>

      <!-- 作者介紹 -->
      <section class="section-block">
        <h2>作者</h2>
        <p>{{ book.author }}</p>
      </section>

      <!-- 詳細資料 -->
      <section class="section-block">
        <h2>詳細資料</h2>
        <ul class="details-list">
          <li><strong>ISBN：</strong>{{ book.isbn }}</li>
          <li><strong>出版社：</strong>{{ book.publisher }}</li>
          <li><strong>出版年：</strong>{{ book.publishdate }}</li>
          <li><strong>版本項：</strong>{{ book.version }}</li>
          <li><strong>分類系統：</strong>{{ book.categorysystem }}</li>
          <li><strong>分類號：</strong>{{ book.classification }}</li>
          <li><strong>語言：</strong>{{ book.language }}</li>
          <li>
            <strong>在架狀態：</strong>
            <span :class="book.is_available === 1 ? 'available' : 'unavailable'">
              {{ book.is_available === 1 ? '是' : '否' }}
            </span>
          </li>
        </ul>
      </section>

      <!-- 動作區域 -->
      <div class="action-area">
        <button class="reserve-btn" @click="addToBorrowList"
          :disabled="!book.is_available || isInBorrowList(book.bookId || book.id)"
          :class="{ 'disabled': !book.is_available || isInBorrowList(book.bookId || book.id) }">
          {{ getBorrowButtonText() }}
        </button>
        <button class="back-btn" @click="goBack">
          ← 返回搜尋結果
        </button>
      </div>
    </div>
  </div>
  <CustomAlert :show="customAlert.show" :title="customAlert.title" :message="customAlert.message" @close="closeAlert" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useHead } from '#imports'
import { bookAPI } from '~/utils/api'
import CustomAlert from '~/components/CustomAlert.vue'

const route = useRoute()
const router = useRouter()

const book = ref({})
const borrowList = ref([])

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

onMounted(async () => {
  const isbn = route.query.isbn
  if (!isbn) return

  try {
    // 使用新的 API 配置
    const res = await bookAPI.getBookByIsbn(isbn)
    const data = res.data

    // 統一處理 is_available（支援後端回傳為 boolean 或 int）
    data.is_available = (data.is_available === 1 || data.is_available === '1' || data.is_available === true) ? 1 : 0

    // 添加日誌來追蹤 imgUrl
    console.log('API 回應的完整資料：', data)
    console.log('API 回應中的 imgUrl：', data.imgUrl)
    console.log('URL 參數中的 imgUrl：', route.query.imgUrl)

    book.value = data

    // 載入借書清單
    loadBorrowList()
  } catch (error) {
    console.error('無法取得書籍資料', error)
    if (error.code === 'ECONNABORTED') {
      showAlert('連線錯誤', '連線超時，請稍後再試')
    } else if (error.response) {
      // 伺服器回應錯誤
      showAlert('查詢失敗', `取得書籍資料失敗：${error.response.data?.message || '未知錯誤'}`)
    } else if (error.request) {
      // 請求發送失敗
      showAlert('連線錯誤', '無法連接到伺服器，請檢查網路連線')
    } else {
      // 其他錯誤
      showAlert('發生錯誤', '發生未知錯誤，請稍後再試')
    }
  }
  console.log('book 資料：', book.value)
})

const bookCoverUrl = computed(() => {
  if (book.value.imgUrl) {
    return book.value.imgUrl
  }
  if (route.query.imgUrl) {
    return route.query.imgUrl
  }
  return 'https://cdn-icons-png.flaticon.com/512/2232/2232688.png'
})

// 載入借書清單
const loadBorrowList = () => {
  console.log('開始載入借書清單')
  const savedList = localStorage.getItem('borrowList')
  console.log('從 localStorage 讀取的原始資料:', savedList)

  if (savedList) {
    try {
      borrowList.value = JSON.parse(savedList)
      console.log('成功解析借書清單:', borrowList.value)
    } catch (e) {
      console.error('載入借書清單失敗', e)
      borrowList.value = []
    }
  } else {
    console.log('localStorage 中沒有借書清單資料')
    borrowList.value = []
  }
}

// 儲存借書清單
const saveBorrowList = () => {
  localStorage.setItem('borrowList', JSON.stringify(borrowList.value))
}

// 檢查書籍是否在借書清單中
const isInBorrowList = (bookId) => {
  // 如果沒有書籍ID，直接返回false
  if (!bookId) {
    console.log('書籍ID為空，不在借書清單中')
    return false
  }

  // 檢查借書清單是否為空
  if (!borrowList.value || borrowList.value.length === 0) {
    console.log('借書清單為空')
    return false
  }

  // 轉換ID為字串進行比較，避免類型不匹配
  const targetId = String(bookId)

  // 檢查是否有匹配的書籍
  const found = borrowList.value.some(book => {
    const bookIdStr = String(book.id)
    const isMatch = bookIdStr === targetId
    console.log(`比較: ${bookIdStr} === ${targetId} = ${isMatch}`)
    return isMatch
  })

  console.log(`書籍ID ${targetId} 在借書清單中: ${found}`)
  return found
}

// 獲取借書按鈕文字
const getBorrowButtonText = () => {
  console.log('getBorrowButtonText 被調用')
  console.log('書籍資訊:', book.value)
  console.log('書籍ID:', book.value.id)
  console.log('書籍可用性:', book.value.is_available)

  if (!book.value.is_available) {
    console.log('書籍不可借閱，返回「無法借閱」')
    return '無法借閱'
  }

  const inList = isInBorrowList(book.value.bookId || book.value.id)
  console.log('是否在借書清單中:', inList)

  if (inList) {
    console.log('返回「已加入借書清單」')
    return '已加入借書清單'
  }

  console.log('返回「加入借書清單」')
  return '加入借書清單'
}

const goBack = () => {
  // 如果來自借書清單，返回借書清單頁面
  if (route.query.from === 'borrow-list') {
    router.push('/borrow/borrow-record')
    return
  }

  // 否則返回借書搜尋頁面
  const query = {
    q: route.query.returnQuery,
    page: route.query.returnPage,
    from: '/borrow/borrow-bookinfo',
    returnType: route.query.returnType
  }

  if (route.query.categorysystem) query.categorysystem = route.query.categorysystem
  if (route.query.language) query.language = route.query.language
  if (route.query.yearFrom) query.yearFrom = route.query.yearFrom
  if (route.query.yearTo) query.yearTo = route.query.yearTo

  router.push({
    path: '/borrow/borrow-search',
    query
  })
}

// 加入借書清單功能
const addToBorrowList = async () => {
  // 檢查登入狀態
  const storedUser = localStorage.getItem('user')
  const jwtToken = localStorage.getItem('jwt_token')
  const authToken = localStorage.getItem('authToken')

  if (!storedUser && !jwtToken && !authToken) {
    showAlert('提醒', '請先登入會員才能使用借書功能')
    router.push('/login')
    return
  }

  try {
    console.log('開始加入借書清單，書籍：', book.value)

    // 檢查書籍是否可借閱
    if (!book.value.is_available) {
      console.error('書籍不可借閱：', book.value)
      showAlert('提醒', '此書籍目前不可借閱')
      return
    }

    // 檢查是否已在借書清單中
    if (isInBorrowList(book.value.bookId || book.value.id)) {
      showAlert('加入失敗', '此書籍已在預約清單中')
      return
    }

    // 構造要儲存的書籍物件
    const bookToAdd = {
      ...book.value,
      id: book.value.bookId || book.value.id, // 確保有 id
      addedDate: new Date().toISOString()
    }

    // 更新借書清單
    borrowList.value.push(bookToAdd)
    saveBorrowList()

    console.log('成功加入借書清單，目前清單：', borrowList.value)
    showAlert('成功', '成功加入借書清單')

    // 更新按鈕狀態
    // Vue 的響應式系統會自動處理
  } catch (error) {
    console.error('加入借書清單時發生錯誤', error)
    showAlert('錯誤', '操作失敗，請稍後再試')
  }
}

// 設置頁面標題
useHead({
  title: computed(() => book.value.title ? `借書 - ${book.value.title}` : '借書 - 書籍詳情')
})
</script>

<style scoped>
/* 佈局 */
.book-detail-wrapper {
  display: flex;
  gap: 3rem;
  padding: 2.5rem;
  background-color: transparent;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .book-detail-wrapper {
    flex-direction: column;
    padding: 1.5rem;
  }
}

/* 左半邊：封面 */
.cover-area {
  flex: 0 0 300px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.cover-image {
  width: 100%;
  max-width: 300px;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.cover-image:hover {
  transform: scale(1.03);
}

/* 右半邊：資訊 */
.info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.book-title {
  font-size: 2.25rem;
  font-weight: bold;
  color: #1a202c;
  line-height: 1.3;
  margin: 0;
}

.section-block {
  border-top: 1px solid #e2e8f0;
  padding-top: 1.5rem;
}

.section-block h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 0.75rem;
}

.section-block p {
  font-size: 1rem;
  line-height: 1.6;
  color: #4a5568;
}

.details-list {
  list-style-type: none;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 0.75rem;
}

.details-list li {
  font-size: 1rem;
  color: #4a5568;
}

.details-list strong {
  color: #2d3748;
  margin-right: 0.5rem;
}

/* 狀態標籤 */
.available {
  color: #38a169;
  font-weight: 600;
}

.unavailable {
  color: #e53e3e;
  font-weight: 600;
}

/* 動作按鈕區 */
.action-area {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
  flex-wrap: wrap;
}

.reserve-btn,
.back-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
}

.reserve-btn {
  background-color: #3182ce;
  color: white;
}

.reserve-btn:hover:not(:disabled) {
  background-color: #2b6cb0;
}

.reserve-btn.disabled {
  background-color: #a0aec0;
  cursor: not-allowed;
  opacity: 0.7;
}

.back-btn {
  background-color: #e2e8f0;
  color: #2d3748;
}

.back-btn:hover {
  background-color: #cbd5e0;
}
</style>