<template>
  <div class="bg-white p-8 w-full text-center">
    <!-- 第一層入口（僅在未選擇 step 時顯示） -->
    <div v-if="!step">
      <!-- 頁面標題區 -->
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

      <!-- 功能選單卡片 -->
      <div class="flex justify-center gap-12 mb-12">
        <div class="bg-blue-100 hover:bg-blue-200 transition-all p-6 rounded-2xl shadow w-72 cursor-pointer text-center"
          @click="goToWrite">
          <div class="text-4xl mb-2">📝</div>
          <div class="text-xl font-bold mb-1">撰寫心得</div>
          <div class="text-sm text-gray-700">針對您借閱的書籍，留下寶貴評論與評分</div>
        </div>

        <div
          class="bg-green-100 hover:bg-green-200 transition-all p-6 rounded-2xl shadow w-72 cursor-pointer text-center"
          @click="step = 'read'">
          <div class="text-4xl mb-2">📖</div>
          <div class="text-xl font-bold mb-1">閱讀心得</div>
          <div class="text-sm text-gray-700">查看其他讀者對書籍的評價與心得內容</div>
        </div>
      </div>
    </div>

    <!-- 第二層內容：返回按鈕 -->
    <div v-if="step" class="text-left max-w-5xl mx-auto mb-6">
      <button class="text-blue-600 underline mb-4" @click="step = null">← 返回功能總覽</button>
    </div>

    <!-- 撰寫心得 -->
    <div v-if="step === 'write'" class="max-w-5xl mx-auto text-left">
      <!-- 空白佔位，待日後設計介面 -->
    </div>

    <!-- 閱讀心得 -->
    <div v-if="step === 'read'" class="max-w-4xl mx-auto">
      <h2 class="text-xl font-bold mb-4">讀者書評列表</h2>
      <div class="flex justify-end mb-4">
        <label class="mr-2">排序：</label>
        <select v-model="sortOption" class="border rounded px-2 py-1">
          <option value="latest">最新</option>
          <option value="rating">評分最高</option>
        </select>
      </div>
      <div v-for="review in sortedReviews" :key="review.id" class="border rounded p-4 mb-4">
        <div class="flex justify-between items-center mb-2">
          <h3 class="font-semibold">{{ review.reviewer }}</h3>
          <span>⭐ {{ review.rating }} 分</span>
        </div>
        <p class="text-gray-700">{{ review.comment }}</p>
      </div>
    </div>
  </div>
</template>




<script setup>
const step = ref(null)
const router = useRouter()
const user = useCookie('user')
const sortOption = ref('latest')

const goToWrite = () => {
  if (!user.value) {
    alert('請先登入會員後才能撰寫書評')
    router.push('/login')
  } else {
    step.value = 'write'
  }
}

const books = ref([
  {
    id: 1,
    title: '解憂雜貨店',
    reviewed: false,
    newRating: '',
    newComment: '',
  },
  {
    id: 2,
    title: '我們與惡的距離',
    reviewed: true,
    rating: 4,
    comment: '劇情深刻，發人省思。',
    editing: false,
    newRating: 4,
    newComment: '劇情深刻，發人省思。',
  },
])

const submitReview = (book) => {
  if (!book.newRating || !book.newComment.trim()) {
    alert('請填寫完整內容')
    return
  }
  book.reviewed = true
  book.rating = book.newRating
  book.comment = book.newComment
  alert('已送出書評')
}

const startEdit = (book) => { book.editing = true }
const saveEdit = (book) => {
  book.rating = book.newRating
  book.comment = book.newComment
  book.editing = false
  alert('書評已更新')
}
const cancelEdit = (book) => {
  book.editing = false
  book.newRating = book.rating
  book.newComment = book.comment
}
const deleteReview = (book) => {
  if (confirm('確定要刪除這則評論嗎？')) {
    book.reviewed = false
    book.rating = ''
    book.comment = ''
    book.newRating = ''
    book.newComment = ''
    book.editing = false
    alert('已刪除書評')
  }
}

const reviews = ref([
  { id: 1, reviewer: '小明', rating: 5, comment: '超好看！' },
  { id: 2, reviewer: '阿綠', rating: 3, comment: '節奏稍慢但有深度。' },
  { id: 3, reviewer: '小華', rating: 4, comment: '值得一讀。' },
])

const sortedReviews = computed(() => {
  if (sortOption.value === 'rating') {
    return [...reviews.value].sort((a, b) => b.rating - a.rating)
  }
  return [...reviews.value].reverse()
})
</script>
