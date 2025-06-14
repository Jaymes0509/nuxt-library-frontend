
<template>
  <div class="bg-white p-8 w-full text-center">

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
    <div v-if="step === 'write' && actionMode === null" class="max-w-4xl mx-auto">
      <button class="text-blue-600 underline mb-4" @click="step = null">← 返回功能總覽</button>
      <h2 class="text-xl font-bold mb-4">請選擇操作類型</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div class="border rounded p-6 shadow hover:shadow-md transition cursor-pointer" @click="actionMode = 'new'">
          <h3 class="text-lg font-semibold mb-2">✍ 撰寫新的書評</h3>
          <p class="text-sm text-gray-600">針對您尚未評論過的借閱書籍新增書評</p>
        </div>
        <div class="border rounded p-6 shadow hover:shadow-md transition cursor-pointer" @click="actionMode = 'edit'">
          <h3 class="text-lg font-semibold mb-2">🛠 修改或查看我的書評</h3>
          <p class="text-sm text-gray-600">檢視您已發表過的書評內容，進行修改或刪除</p>
        </div>
      </div>
    </div>

    <!-- 第二層：撰寫新書評 → 選書 -->
    <div v-if="step === 'write' && actionMode === 'new' && !selectedBookForReview" class="max-w-3xl mx-auto text-left">
      <button class="text-blue-600 underline mb-4" @click="actionMode = null">← 返回操作選單</button>
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
        <button @click="submitReview" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">提交</button>
        <button @click="selectedBookForReview = null" class="text-gray-600 hover:underline">取消</button>
      </div>
    </div>

    <!-- 第三層：修改書評表單 -->
    <div v-if="step === 'editReview'" class="max-w-3xl mx-auto text-left">
      <button class="text-blue-600 underline mb-4" @click="step = 'write'; actionMode = 'edit'">← 返回書評列表</button>
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
        <button @click="updateReview" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">儲存修改</button>
        <button @click="step = 'write'; actionMode = 'edit'" class="text-gray-600 hover:underline">取消</button>
      </div>
    </div>


    <!-- 閱讀心得：篩選與搜尋 -->
    <div v-if="step === 'read'" class="max-w-5xl mx-auto mb-6 text-left">
      <button class="text-blue-600 underline mb-4" @click="step = null">← 返回功能總覽</button>
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

    <!-- 閱讀心得：隨機書籍推薦 -->
    <div v-if="step === 'read'" class="max-w-4xl mx-auto">
      <h2 class="text-xl font-bold mb-4">隨機書籍推薦</h2>
      <p class="text-sm text-gray-600 mb-4">🔍 測試中：請點選 <strong>「活著」</strong> 查看範例書評，其餘書籍會顯示「尚無書評」。</p>
      <div class="grid grid-cols-1 gap-6 mb-6">
        <div v-for="book in filteredBooks" :key="book.id" class="border rounded p-4 shadow hover:shadow-md transition">
          <h3 class="text-lg font-semibold mb-1">{{ book.title }}</h3>
          <p class="text-sm text-gray-600">作者：{{ book.author }}</p>
          <button @click="viewBookReviews(book)" class="text-blue-600 hover:underline">查看書評</button>
        </div>
      </div>
    </div>

    <!-- 第三層：單一本書的所有書評 -->
    <div v-if="step === 'bookReviews'" class="max-w-3xl mx-auto text-left">
      <button class="text-blue-600 underline mb-4" @click="step = 'read'">← 返回隨機書籍</button>

      <div class="bg-yellow-100 border-l-4 border-yellow-500 text-yellow-800 p-4 rounded mb-6">
        ⚠ 尚未連接資料庫：目前為假資料展示，未來將讀取實際書評
      </div>

      <h2 class="text-2xl font-bold mb-4">{{ selectedBook.title }} 的書評</h2>

      <div class="flex justify-end mb-4">
        <label class="mr-2">排序：</label>
        <select v-model="sortReviewOption" class="border rounded px-2 py-1">
          <option value="latest">最新時間</option>
          <option value="likes">點讚數</option>
        </select>
      </div>

      <div v-for="review in sortedBookReviews" :key="review.id" class="border rounded p-4 mb-4">
        <div class="flex justify-between items-center mb-2">
          <h3 class="font-semibold">{{ review.reviewer }}</h3>
          <span class="text-sm text-gray-500">{{ review.date }}</span>
        </div>
        <p class="mb-2">⭐ {{ review.rating }} 分</p>
        <p class="text-gray-700">{{ review.comment }}</p>
        <p class="text-sm text-gray-500 mt-2">
  👍 點讚數：{{ review.likes }}
  <button @click="likeReview(review)" class="text-blue-600 hover:underline ml-2">點讚</button>
</p>

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

const user = useCookie('user')
const categoryFilter = ref('')
const searchKeyword = ref('')
const sortReviewOption = ref('latest')
const bookReviews = ref([])
const allReviews = ref([])
const reviewText = ref('')
const reviewRating = ref(5)

const randomBooks = ref([])
const borrowedBooks = ref([
  { id: 1, title: '活著', author: '余華' },
  { id: 3, title: '追風箏的人', author: '卡勒德胡賽尼' },
  { id: 6, title: '房思琪的初戀樂園', author: '林奕含' }
])

const goToWrite = () => {
  if (!user.value || typeof user.value !== 'string' || user.value.trim() === '') {
    window.alert('請先登入會員後才能撰寫書評')
    return
  }

  // ✅ 進入撰寫流程前先清空所有狀態
  selectedBook.value = null
  selectedBookForReview.value = null
  editingReview.value = null
  actionMode.value = null
  newReview.value = { rating: '', comment: '' }

  step.value = 'write'
}


const simulateLogin = () => {
  document.cookie = "user=mock-member; path=/"
  location.reload()
}

const simulateLogout = () => {
  document.cookie = "user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"
  location.reload()
}

const fetchRandomBooks = async () => {
  randomBooks.value = [
    { id: 1, title: '活著', author: '余華', category: '文學' },
    { id: 2, title: '百年孤寂', author: '馬奎斯', category: '文學' },
    { id: 3, title: '追風箏的人', author: '卡勒德胡賽尼', category: '心理' },
    { id: 4, title: '小王子', author: '聖修伯里', category: '文學' },
    { id: 5, title: '1984', author: '喬治歐威爾', category: '科幻' },
    { id: 6, title: '房思琪的初戀樂園', author: '林奕含', category: '心理' },
    { id: 7, title: '解憂雜貨店', author: '東野圭吾', category: '文學' },
    { id: 8, title: '我們與惡的距離', author: '公共電視', category: '心理' },
    { id: 9, title: '天橋上的魔術師', author: '吳明益', category: '歷史' },
    { id: 10, title: '時間的秩序', author: '羅威利', category: '科幻' }
  ]
}

const filteredBooks = computed(() => {
  return randomBooks.value.filter(book => {
    const matchCategory = categoryFilter.value === '' || book.category === categoryFilter.value
    const matchKeyword = searchKeyword.value === '' || book.title.includes(searchKeyword.value)
    return matchCategory && matchKeyword
  })
})

const myReviews = computed(() => {
  return allReviews.value.filter(r => r.reviewer === '您')
})

const viewBookReviews = (book) => {
  selectedBook.value = book
  step.value = 'bookReviews'
  bookReviews.value = allReviews.value.filter(r => r.bookId === book.id)
}

const sortedBookReviews = computed(() => {
  if (sortReviewOption.value === 'likes') {
    return [...bookReviews.value].sort((a, b) => b.likes - a.likes)
  } else {
    return [...bookReviews.value].sort((a, b) => new Date(b.date) - new Date(a.date))
  }
})

const submitReview = () => {
  if (!newReview.value.rating || !newReview.value.comment.trim()) {
    alert('請完整填寫評分與評論內容')
    return
  }

  const now = new Date().toISOString().split('T')[0]
  allReviews.value.push({
    id: Date.now(),
    reviewer: '您',
    rating: newReview.value.rating,
    comment: newReview.value.comment,
    date: now,
    likes: 0,
    likedBy: [],
    bookId: selectedBookForReview.value.id,
    bookTitle: selectedBookForReview.value.title
  })

  


  // 從可撰寫書評的清單中移除該書
  borrowedBooks.value = borrowedBooks.value.filter(book => book.id !== selectedBookForReview.value.id)

  // ✅ 清空所有狀態並返回首頁
  selectedBook.value = null
  selectedBookForReview.value = null
  newReview.value = { rating: '', comment: '' }
  actionMode.value = null
  editingReview.value = null
  step.value = null

  alert('您的書評已成功提交')
}

const likeReview = (review) => {
  if (!user.value || typeof user.value !== 'string' || user.value.trim() === '') {
    alert('請先登入才能點讚書評')
    return
  }

  if (review.reviewer === '您') {
    alert('不能對自己的書評按讚喔！')
    return
  }

  if (!review.likedBy) review.likedBy = []

  if (review.likedBy.includes(user.value)) {
    alert('您已點過讚囉！')
    return
  }

  review.likes++
  review.likedBy.push(user.value)
}


const editReview = (review) => {
  editingReview.value = review
  reviewText.value = review.comment
  reviewRating.value = review.rating
  step.value = 'editReview'
}

const deleteReview = (review) => {
  if (confirm('確定要刪除這則書評嗎？')) {
    allReviews.value = allReviews.value.filter(r => r.id !== review.id)
  }
}

const startWritingReview = (book) => {
  selectedBookForReview.value = book
  newReview.value = { rating: '', comment: '' }
}

watch(step, (val) => {
  if (val === 'read') fetchRandomBooks()
})
</script>
