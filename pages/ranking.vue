<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

type PeriodStats = {
  borrow: number
  reserve: number
  rating: number
}

type Book = {
  id: number
  title: string
  author: string
  cover: string
  description: string
  stats: {
    month: Record<string, PeriodStats>
    year: Record<string, PeriodStats>
    total: PeriodStats
  }
}

const now = new Date()
const currentYear = now.getFullYear()

const months = ref(
  Array.from({ length: 12 }, (_, i) => {
    const m = (i + 1).toString().padStart(2, '0')
    return `${currentYear}-${m}`
  })
)
const years = ref(
  Array.from({ length: 5 }, (_, i) => (currentYear - 1 - i).toString())
)
const selectedMonth = ref(`${currentYear}-${(now.getMonth() + 1).toString().padStart(2, '0')}`)
const selectedYear = ref((currentYear - 1).toString())

const currentTab = ref<'reserve' | 'borrow' | 'rating'>('reserve')
const currentPeriod = ref<'month' | 'year' | 'total'>('month')

const books = ref<Book[]>([
  {
    id: 1,
    title: '解憂雜貨店',
    author: '東野圭吾',
    cover: 'https://upload.wikimedia.org/wikipedia/zh/thumb/0/0b/Namiya.jpg/220px-Namiya.jpg',
    description: '一間雜貨店，解答人生難題的奇幻故事。',
    stats: {
      month: {
        '2025-01': { borrow: 12, reserve: 6, rating: 4.7 },
        '2025-02': { borrow: 10, reserve: 4, rating: 4.8 }
      },
      year: {
        '2023': { borrow: 60, reserve: 15, rating: 4.7 },
        '2024': { borrow: 80, reserve: 20, rating: 4.8 }
      },
      total: { borrow: 152, reserve: 41, rating: 4.8 }
    }
  },
  {
    id: 2,
    title: '追風箏的人',
    author: '卡勒德·胡賽尼',
    cover: 'https://i.imgur.com/3ZQ3Z2L.jpeg',
    description: '一段關於贖罪與友情的感人故事。',
    stats: {
      month: {
        '2025-01': { borrow: 8, reserve: 7, rating: 4.5 },
        '2025-02': { borrow: 9, reserve: 5, rating: 4.6 }
      },
      year: {
        '2023': { borrow: 55, reserve: 22, rating: 4.6 },
        '2024': { borrow: 70, reserve: 30, rating: 4.6 }
      },
      total: { borrow: 142, reserve: 64, rating: 4.6 }
    }
  }
])

const sortedBooks = computed(() => {
  const tab = currentTab.value
  const period = currentPeriod.value

  if (period === 'month') {
    const m = selectedMonth.value
    return [...books.value]
      .filter(book => book.stats.month[m])
      .sort((a, b) => b.stats.month[m][tab] - a.stats.month[m][tab])
  }

  if (period === 'year') {
    const y = selectedYear.value
    return [...books.value]
      .filter(book => book.stats.year[y])
      .sort((a, b) => b.stats.year[y][tab] - a.stats.year[y][tab])
  }

  return [...books.value].sort(
    (a, b) => b.stats.total[tab] - a.stats.total[tab]
  )
})

function goToExperience(title: string, id: number) {
  router.push({
    path: '/comment/experience',
    query: { search: title },
    hash: `#book-${id}`
  })
}

// 回頂部功能
const showScrollTop = ref(false)
function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
function handleScroll() {
  showScrollTop.value = window.scrollY > 200
}
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">🏆 圖書排行榜</h1>

    <!-- 指標 -->
    <div class="flex flex-wrap gap-2 mb-4">
      <button
        class="px-4 py-2 rounded border"
        :class="currentTab === 'reserve' ? 'bg-gray-200 font-bold' : ''"
        @click="currentTab = 'reserve'"
      >
        📅 預約次數
      </button>
      <button
        class="px-4 py-2 rounded border"
        :class="currentTab === 'borrow' ? 'bg-gray-200 font-bold' : ''"
        @click="currentTab = 'borrow'"
      >
        📚 借閱次數
      </button>
      <button
        class="px-4 py-2 rounded border"
        :class="currentTab === 'rating' ? 'bg-gray-200 font-bold' : ''"
        @click="currentTab = 'rating'"
      >
        ⭐ 評分高低
      </button>
    </div>

    <!-- 時間篩選 -->
    <div class="flex flex-wrap items-center gap-4 mb-6">
      <div>
        <label class="font-medium mr-2">📆 統計範圍：</label>
        <select v-model="currentPeriod" class="border rounded px-2 py-1">
          <option value="month">月份</option>
          <option value="year">年份</option>
          <option value="total">總和</option>
        </select>
      </div>

      <div v-if="currentPeriod === 'month'">
        <label class="mr-2">月份：</label>
        <select v-model="selectedMonth" class="border rounded px-2 py-1">
          <option v-for="m in months" :key="m" :value="m">{{ m }}</option>
        </select>
      </div>

      <div v-else-if="currentPeriod === 'year'">
        <label class="mr-2">年份：</label>
        <select v-model="selectedYear" class="border rounded px-2 py-1">
          <option v-for="y in years" :key="y" :value="y">{{ y }}</option>
        </select>
      </div>
    </div>

    <!-- 排行榜 -->
    <div class="space-y-4">
      <div
        v-for="(book, index) in sortedBooks"
        :key="book.id"
        class="p-4 border rounded-xl shadow-sm hover:shadow-md transition flex gap-4"
      >
        <img
          :src="book.cover"
          alt="封面"
          class="w-20 h-28 object-cover rounded-md border"
        />
        <div class="flex-1">
          <div class="flex justify-between items-center mb-2">
            <div class="text-xl font-semibold text-blue-600">
              {{ index + 1 }}. {{ book.title }}
            </div>
            <div
              :class="{
                'text-blue-600 font-bold': currentTab === 'borrow',
                'text-green-600 font-bold': currentTab === 'reserve',
                'text-yellow-500 font-bold': currentTab === 'rating'
              }"
            >
              {{
                currentTab === 'rating'
                  ? '⭐ ' +
                      (
                        currentPeriod === 'month'
                          ? book.stats.month[selectedMonth]?.rating
                          : currentPeriod === 'year'
                            ? book.stats.year[selectedYear]?.rating
                            : book.stats.total.rating
                      ).toFixed(1)
                  : currentTab === 'borrow'
                    ? '借閱：' +
                        (
                          currentPeriod === 'month'
                            ? book.stats.month[selectedMonth]?.borrow
                            : currentPeriod === 'year'
                              ? book.stats.year[selectedYear]?.borrow
                              : book.stats.total.borrow
                        ) + ' 次'
                    : '預約：' +
                        (
                          currentPeriod === 'month'
                            ? book.stats.month[selectedMonth]?.reserve
                            : currentPeriod === 'year'
                              ? book.stats.year[selectedYear]?.reserve
                              : book.stats.total.reserve
                        ) + ' 次'
              }}
            </div>
          </div>
          <div class="text-sm text-gray-500 mb-1">作者：{{ book.author }}</div>
          <div class="flex justify-between items-start">
            <p class="text-sm text-gray-700 w-full">
              {{ book.description }}
            </p>
            <button
              class="ml-4 px-3 py-1 text-sm border rounded hover:bg-gray-100 whitespace-nowrap"
              @click="goToExperience(book.title, book.id)"
            >
              心得
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 回頂部按鈕 -->
  <button
    v-if="showScrollTop"
    @click="scrollToTop"
    class="fixed bottom-6 right-6 bg-blue-600 text-white px-4 py-2 rounded-full shadow-lg hover:bg-blue-700"
  >
    ⬆ 回頂部
  </button>
</template>
