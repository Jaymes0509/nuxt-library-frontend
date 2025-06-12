<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const search = ref('')
const selectedBookId = ref<number | null>(null)
const sortOption = ref<'likes' | 'date'>('date')
const userHasBorrowed = ref(true)

onMounted(() => {
  const keyword = route.query.search
  if (typeof keyword === 'string') {
    search.value = keyword
    selectedBookId.value = null
  }
  window.addEventListener('scroll', handleScroll)
})

watch(() => route.query.search, (newVal) => {
  if (typeof newVal === 'string') {
    search.value = newVal
    selectedBookId.value = null
  }
})

const books = ref([
  {
    id: 1,
    title: '解憂雜貨店',
    author: '東野圭吾',
    description: '神秘的雜貨店連接過去與未來。',
    cover: 'https://upload.wikimedia.org/wikipedia/zh/thumb/0/0b/Namiya.jpg/220px-Namiya.jpg'
  },
  {
    id: 2,
    title: '追風箏的人',
    author: '卡勒德·胡賽尼',
    description: '一個阿富汗男孩與摯友的生命交錯故事。',
    cover: 'https://example.com/kite.jpg'
  },
  {
    id: 3,
    title: '白夜行',
    author: '東野圭吾',
    description: '一段跨越二十年的罪與愛。',
    cover: 'https://example.com/white-night.jpg'
  }
])

const allReviews = ref<Record<number, Review[]>>({
  1: [
    {
      id: 1,
      user: '小明',
      content: '這本書讓我重新思考人生，非常感人。',
      rating: 5,
      createdAt: '2025-05-28',
      likes: 2,
      likedByCurrentUser: false
    },
    {
      id: 2,
      user: '阿豪',
      content: '這本書很神奇，真的會回信給你嗎？',
      rating: 4,
      createdAt: '2025-05-26',
      likes: 2,
      likedByCurrentUser: false
    }
  ],
  2: [],
  3: []
})

const showScrollTop = ref(false)
function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
function handleScroll() {
  showScrollTop.value = window.scrollY > 200
}
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

const filteredBooks = computed(() => {
  if (!search.value) return books.value
  return books.value.filter(b =>
    b.title.toLowerCase().includes(search.value.toLowerCase()) ||
    b.author.toLowerCase().includes(search.value.toLowerCase())
  )
})

const selectedBook = computed(() =>
  books.value.find(b => b.id === selectedBookId.value) || null
)

const reviews = computed(() => {
  const list = allReviews.value[selectedBookId.value || -1] || []
  return [...list].sort((a, b) => {
    if (sortOption.value === 'likes') {
      if (b.likes !== a.likes) return b.likes - a.likes
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    }
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
})

const averageRating = computed(() => {
  const list = allReviews.value[selectedBookId.value || -1] || []
  if (list.length === 0) return '尚無評分'
  const total = list.reduce((sum, r) => sum + r.rating, 0)
  return (total / list.length).toFixed(1)
})

function toggleLike(reviewId: number) {
  const review = allReviews.value[selectedBookId.value!]?.find(r => r.id === reviewId)
  if (!review || !userHasBorrowed.value) return
  if (review.likedByCurrentUser) {
    review.likes--
    review.likedByCurrentUser = false
  } else {
    review.likes++
    review.likedByCurrentUser = true
  }
}

function backToSearch() {
  selectedBookId.value = null
  search.value = ''
}

interface Review {
  id: number
  user: string
  content: string
  rating: number
  createdAt: string
  likes: number
  likedByCurrentUser: boolean
}
</script>

<template>
  <div class="p-4 space-y-6">
    <h1 class="text-2xl font-bold">📖 看心得</h1>

    <!-- 搜尋 -->
    <div class="flex gap-2 items-center">
      <input
        v-model="search"
        placeholder="輸入書名或作者搜尋..."
        class="border px-3 py-2 rounded w-full"
      />
      <span v-if="search" class="text-sm text-gray-500">共 {{ filteredBooks.length }} 本結果</span>
    </div>

    <!-- 書籍清單 -->
    <div v-if="!selectedBook">
      <ul class="space-y-2 mt-4">
        <li
          v-for="book in filteredBooks"
          :key="book.id"
          @click="selectedBookId = book.id"
          class="cursor-pointer p-3 border rounded hover:bg-gray-50"
        >
          {{ book.title }} - {{ book.author }}
        </li>
      </ul>
    </div>

    <!-- 書籍詳情 -->
    <div v-else>
      <button @click="backToSearch" class="text-sm text-blue-600 underline mb-2">← 返回搜尋</button>

      <div class="flex gap-4 items-start">
        <img :src="selectedBook.cover" alt="封面" class="w-36 h-auto rounded shadow" />
        <div class="space-y-2">
          <div class="flex items-center gap-4 flex-wrap">
            <h2 class="text-xl font-semibold">{{ selectedBook.title }}</h2>
            <span class="text-yellow-500 text-sm">⭐ 平均評分：{{ averageRating }}<span v-if="averageRating !== '尚無評分'"> / 5</span></span>
          </div>
          <p class="text-gray-700">
            作者：
            <span
              class="text-blue-600 underline cursor-pointer"
              @click="() => { router.push({ path: '/comment/experience', query: { search: selectedBook?.author || '' } }) }"
            >
              {{ selectedBook.author }}
            </span>
          </p>
          <p class="text-gray-600 text-sm">{{ selectedBook.description }}</p>
        </div>
      </div>

      <!-- 排序與評論列表 -->
      <div class="flex items-center justify-end gap-2 mt-4">
        <label class="text-sm">排序依據：</label>
        <select v-model="sortOption" class="border rounded px-2 py-1 text-sm">
          <option value="date">最新</option>
          <option value="likes">最多讚</option>
        </select>
      </div>

      <div v-if="reviews.length" class="space-y-4 mt-2">
        <div v-for="r in reviews" :key="r.id" class="p-4 border rounded-xl bg-white shadow">
          <div class="flex justify-between items-center mb-1">
            <div class="font-semibold">{{ selectedBook.title }} - {{ r.user }}</div>
            <div class="text-sm text-gray-500">{{ r.createdAt }}</div>
          </div>
          <div class="text-yellow-500 mb-2">⭐️ x {{ r.rating }}</div>
          <p class="text-gray-800">{{ r.content }}</p>
          <div class="mt-2 flex justify-end">
            <button
              @click="toggleLike(r.id)"
              class="text-sm flex items-center gap-1 px-2 py-1 rounded"
              :class="r.likedByCurrentUser ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-600'"
              :disabled="!userHasBorrowed"
            >
              👍 {{ r.likes }}
            </button>
          </div>
        </div>
      </div>

      <div v-else class="text-gray-500 mt-2 text-sm">目前尚無心得。</div>
    </div>
  </div>

  <button
    v-if="showScrollTop"
    @click="scrollToTop"
    class="fixed bottom-6 right-6 bg-blue-600 text-white px-4 py-2 rounded-full shadow-lg hover:bg-blue-700"
  >
    ⬆ 回頂部
  </button>
</template>
