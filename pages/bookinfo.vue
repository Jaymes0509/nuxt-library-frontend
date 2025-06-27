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
        <button class="reserve-btn" @click="addToReservationList" :disabled="!book.is_available"
          :class="{ 'disabled': !book.is_available }">
          {{ book.is_available === 1 ? '加入預約清單' : '無法預約' }}
        </button>
        <button class="back-btn" @click="goBack">
          ← 返回搜尋結果
        </button>
      </div>
    </div>
  </div>

  <!-- 自訂提示視窗 -->
  <CustomAlert :show="customAlert.show" :title="customAlert.title" :message="customAlert.message"
    :items="customAlert.items" @close="closeAlert" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useHead } from '#imports'
import { reservationAPI, bookAPI } from '~/utils/api'
import CustomAlert from '~/components/CustomAlert.vue'

const route = useRoute()
const router = useRouter()

const book = ref({})

// 自訂提示視窗
const customAlert = ref({
  show: false,
  title: '',
  message: '',
  items: [],
})

const showAlert = (title, message, items = []) => {
  customAlert.value.title = title
  customAlert.value.message = message
  customAlert.value.items = items
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
  } catch (error) {
    console.error('無法取得書籍資料', error)
    if (error.code === 'ECONNABORTED') {
      alert('連線超時，請稍後再試')
    } else if (error.response) {
      // 伺服器回應錯誤
      alert(`取得書籍資料失敗：${error.response.data?.message || '未知錯誤'}`)
    } else if (error.request) {
      // 請求發送失敗
      alert('無法連接到伺服器，請檢查網路連線')
    } else {
      // 其他錯誤
      alert('發生未知錯誤，請稍後再試')
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

const goBack = () => {
  const query = {
    returnPage: route.query.returnPage,
    returnType: route.query.returnType,
  };

  if (route.query.returnType === 'advanced') {
    query.returnAdvanced = route.query.returnAdvanced;
  } else {
    query.q = route.query.returnQuery;
  }

  router.push({
    path: '/catalogue-search',
    query,
  });
};

// 加入預約清單功能
const addToReservationList = async () => {
  // 檢查登入狀態
  const storedUser = localStorage.getItem('user')
  const jwtToken = localStorage.getItem('jwt_token')
  const authToken = localStorage.getItem('authToken')

  if (!storedUser && !jwtToken && !authToken) {
    showAlert('登入提醒', '請先登入會員才能使用預約功能')
    router.push('/login')
    return
  }

  try {
    console.log('開始加入預約清單，書籍：', book.value)

    const bookId = book.value.bookId || parseInt(book.value.isbn) || 1
    console.log('使用的 bookId：', bookId)

    // 檢查書籍是否可預約
    if (!book.value.is_available) {
      console.error('書籍不可預約：', book.value)
      showAlert('預約失敗', '此書籍目前不可預約')
      return
    }

    // 準備加入預約清單的資料
    const logData = {
      book_id: bookId,
      user_id: 1,
      action: 'ADD_TO_LIST',
      status: 'PENDING',
      message: `加入預約清單：${book.value.title}`
    }

    console.log('發送預約日誌資料：', logData)

    // 使用 addReservationLog API 記錄操作
    const response = await reservationAPI.addReservationLog(logData)
    console.log('API 完整回應：', response)
    console.log('API 回應狀態：', response.status)
    console.log('API 回應資料：', response.data)

    if (response.data && response.data.success) {
      console.log('成功加入預約清單！')
      showAlert('預約成功', '已成功加入預約清單！')

      // 移除跳轉邏輯，只顯示成功訊息
      // 用戶可以繼續瀏覽書籍詳情或手動返回
    } else {
      // 處理加入失敗
      const errorMessage = response.data?.message ||
        response.data?.error ||
        '加入預約清單失敗'

      // 移除「系統錯誤：」前綴
      const cleanErrorMessage = errorMessage.replace(/^系統錯誤：/, '')

      console.error('加入預約清單失敗詳情：', {
        message: cleanErrorMessage,
        response: response.data
      })
      showAlert('加入失敗', cleanErrorMessage)
    }
  } catch (error) {
    console.error('加入預約清單失敗：', error)
    console.error('錯誤詳情：', {
      message: error.message,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data
    })

    if (error.response?.status === 409) {
      showAlert('重複預約', '此書籍已在預約清單中')
    } else if (error.response?.data?.message) {
      showAlert('加入失敗', error.response.data.message)
    } else {
      showAlert('系統錯誤', '加入預約清單失敗，請稍後再試')
    }
  }
}
</script>

<style scoped>
/* 容器最外層：flex 佈局，手機先上下堆疊 */
.book-detail-wrapper {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin: 1rem;
}

/* 左側封面圖片區 */
.cover-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.cover-image {
  max-width: 300px;
  max-height: 400px;
  width: 100%;
  height: auto;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 右側文字資訊區 */
.info-area {
  flex: 2;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 書名樣式 */
.book-title {
  margin: 0;
  font-size: 1.75rem;
  font-weight: bold;
  color: #333;
}

/* 每個區段標題與內容 */
.section-block h2 {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: #555;
}

.section-block p {
  margin: 0;
  line-height: 1.6;
  color: #444;
}

/* 詳細資料列表 */
.details-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.details-list li {
  margin-bottom: 0.5rem;
  line-height: 1.5;
  color: #444;
}

/* 可用性狀態樣式 */
.available {
  color: #28a745;
  font-weight: bold;
}

.unavailable {
  color: #dc3545;
  font-weight: bold;
}

/* 動作區域：放置按鈕 */
.action-area {
  margin-top: 1rem;
  display: flex;
  gap: 1rem;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.reserve-btn {
  background-color: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0.6rem 1.2rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.reserve-btn:hover:not(.disabled) {
  background-color: #155a9c;
}

.reserve-btn.disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.back-btn {
  background-color: #6c757d;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 0.6rem 1.2rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.back-btn:hover {
  background-color: #5a6268;
}

/* RWD：寬度 ≥ 768px 時左右並列 */
@media screen and (min-width: 768px) {
  .book-detail-wrapper {
    flex-direction: row;
  }

  .cover-area,
  .info-area {
    align-items: flex-start;
  }

  .cover-area {
    max-width: 40%;
  }

  .info-area {
    max-width: 60%;
  }
}

/* 手機版響應式調整 */
@media screen and (max-width: 767px) {
  .action-area {
    flex-direction: column;
  }

  .reserve-btn,
  .back-btn {
    width: 100%;
  }
}
</style>