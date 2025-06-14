<template>
  <div ref="topContainer" class="p-8 w-full">
    <h1 class="text-3xl font-bold mb-6 text-center">圖書排行榜</h1>

    <!-- 排行分類選單 -->
    <div v-if="step === 'summary'"
      class="grid gap-6 justify-items-center mb-12"
      style="grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));">
      <button v-for="option in sortOptions" :key="option.value" @click="step = option.value"
        class="text-lg font-medium text-gray-700 hover:text-blue-600 hover:underline">
        {{ option.label }}
      </button>
    </div>

    <!-- 總覽排行榜（三卡片） -->
    <div v-if="step === 'summary'"
      class="grid gap-6 justify-items-center mb-12"
      style="grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));">
      <div v-for="type in ['reserve', 'borrow', 'rating']" :key="type"
        class="bg-white rounded-xl shadow p-6 w-full max-w-[320px]">
        <h2 class="text-xl font-bold border-b pb-2 mb-4">
          {{
            type === 'reserve' ? '預約次數排行榜(總和)' :
              type === 'borrow' ? '借閱次數排行榜(總和)' :
                '評分高低排行榜(總和)'
          }}
        </h2>
        <ol class="space-y-4">
          <li v-for="(book, index) in topBooks(type, true)" :key="book.book_id + '-' + type"
            class="flex gap-4 items-center border-b pb-2">
            <div class="text-center w-12">
              <div class="text-lg font-bold text-red-600">NO.{{ index + 1 }}</div>
            </div>
            <img :src="book.cover || defaultCover" alt="封面" class="w-14 h-20 object-cover rounded border shadow" />
            <div class="flex-1">
              <div class="font-semibold">{{ book.title }}</div>
              <div class="text-sm text-gray-600">作者：{{ book.author }}</div>
              <div class="text-sm mt-1">
                {{
                  type === 'reserve' ? '預約次數：' :
                    type === 'borrow' ? '借閱次數：' :
                      '平均評分：'
                }}
                <span class="font-bold">
                  {{ type === 'rating' ? book.stat_count.toFixed(1) : book.stat_count }}
                </span>
              </div>
            </div>
          </li>
        </ol>
      </div>
    </div>

    <!-- 詳細排行榜畫面（第二層） -->
    <div v-else class="bg-white p-8 rounded-xl shadow w-full">
      <h2 class="text-3xl font-bold mb-6 text-center">
        {{
          step === 'borrow' ? '📘 借閱排行榜詳細' :
            step === 'reserve' ? '📗 預約排行榜詳細' :
              '📙 評分排行榜詳細'
        }}
      </h2>

      <!-- 查詢篩選區塊 -->
      <div class="grid md:grid-cols-2 gap-4 mb-6">
        <div>
          <!-- 書籍分類在上 -->
          <label class="block font-medium mb-1">書籍分類：</label>
          <select v-model="selectedCategory" class="border rounded px-2 py-1 w-full mb-4">
            <option value="">全部分類</option>
            <option v-for="cat in bookCategories" :key="cat" :value="cat">{{ cat }}</option>
          </select>

          <!-- 時間篩選在下 -->
          <label class="block font-medium mb-1">時間篩選：</label>
          <div class="flex gap-2">
            <select v-model="selectedPeriod" class="border rounded px-2 py-1 w-full">
              <option value="all">總和</option>
              <option value="year">年份</option>
              <option value="month">月份</option>
            </select>
            <select v-if="selectedPeriod === 'year'" v-model="selectedYear" class="border rounded px-2 py-1 w-32">
              <option v-for="year in years" :key="year" :value="year">{{ year }}</option>
            </select>
            <select v-if="selectedPeriod === 'month'" v-model="selectedMonth" class="border rounded px-2 py-1 w-32">
              <option v-for="month in months" :key="month" :value="month">{{ month }} 月</option>
            </select>
          </div>
        </div>

        <div class="flex items-end justify-end">
          <button @click="step = 'summary'" class="text-sm text-blue-500 hover:underline">
            ← 返回排行榜總覽
          </button>
        </div>
      </div>


      <!-- 書籍清單 -->
      <ol class="space-y-6">
        <li v-for="(book, index) in topBooks(step)" :key="book.book_id"
          class="flex items-center gap-6 border-b pb-6 bg-white rounded shadow px-4 py-6">
          <div class="text-center min-w-[50px]">
            <div class="text-3xl font-bold text-red-600">{{ index + 1 }}</div>
          </div>
          <img :src="book.cover || defaultCover" class="w-32 h-44 object-cover rounded border shadow" />
          <div class="flex-1">
            <div class="text-xl font-bold text-black mb-2">{{ book.title }}</div>
            <div class="text-sm text-gray-700 mb-2">作者：{{ book.author }}</div>
            <div class="text-sm text-gray-600" style="line-height: 1.6">
              {{
                step === 'borrow' ? '借閱次數：' :
                  step === 'reserve' ? '預約次數：' :
                    '平均評分：'
              }}
              <span class="font-bold">
                {{ step === 'rating' ? book.stat_count.toFixed(1) : book.stat_count }}
              </span>
            </div>
          </div>
        </li>
      </ol>

      <!-- 分頁與每頁顯示設定 -->
      <div class="flex flex-wrap justify-between items-center mt-8">
        <div>
          每頁顯示：
          <select v-model="pageSize" class="border px-2 py-1 rounded">
            <option v-for="size in [10, 20, 50, 100]" :key="size" :value="size">{{ size }}</option>
          </select> 本
        </div>
        <div class="flex flex-wrap gap-1">
          <button v-for="page in totalPages" :key="page" @click="currentPage = page"
            :class="['px-3 py-1 border rounded', currentPage === page ? 'bg-blue-500 text-white' : 'bg-white']">
            {{ page }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// 當前顯示的階段（summary 或各排行榜類型）
const step = ref('summary')

// 預設封面圖片連結
const defaultCover = 'https://via.placeholder.com/80x120.png?text=No+Cover'

// 排行分類選項，用於第一層選擇排行榜類型
const sortOptions = [
  { label: '預約次數', value: 'reserve' },
  { label: '借閱次數', value: 'borrow' },
  { label: '評分高低', value: 'rating' }
]

// 篩選條件（時間與分類）
const selectedPeriod = ref('all')
const selectedCategory = ref('')
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)

// 分頁設定
const currentPage = ref(1)
const pageSize = ref(10)

// 書籍分類列表
const bookCategories = [
  '哲學類', '宗教類', '科學類', '應用科學類', '社會科學類',
  '語文類', '藝術類', '歷史類', '地理類', '文學類', '總類', '其他'
]

// 年與月選項
// 可供選擇的年份列表，供年份下拉選單使用
// 自動產生從 2020 年到當前年份的選項，供年份下拉選單使用
const years = Array.from({ length: new Date().getFullYear() - 2020 + 1 }, (_, i) => 2020 + i)
const months = Array.from({ length: 12 }, (_, i) => i + 1)

// 排行榜假資料（僅供展示用途）
// 假資料格式說明：
// book_id：書籍 ID（唯一值）
// title：書名
// author：作者名稱
// stat_count：統計數值（依照排行榜類型為預約次數／借閱次數／平均評分）
// cover：封面圖片網址（目前為空字串，未使用）
// period：時間範圍（all / year / month）
// category：書籍分類（對應下方 bookCategories）
// type：排行榜類型（reserve / borrow / rating）
const rankedBooks = ref([
  // 預約排行榜 (10 本)
  { book_id: 1, title: '解憂雜貨店', author: '東野圭吾', stat_count: 25, cover: '', period: 'all', category: '文學類', type: 'reserve' },
  { book_id: 2, title: '追風箏的人', author: '卡勒德·胡賽尼', stat_count: 22, cover: '', period: 'year', category: '文學類', type: 'reserve' },
  { book_id: 3, title: '白夜行', author: '東野圭吾', stat_count: 19, cover: '', period: 'month', category: '文學類', type: 'reserve' },
  { book_id: 4, title: '活著', author: '余華', stat_count: 18, cover: '', period: 'year', category: '文學類', type: 'reserve' },
  { book_id: 5, title: '小王子', author: '安東尼·聖修伯里', stat_count: 17, cover: '', period: 'all', category: '文學類', type: 'reserve' },
  { book_id: 6, title: '百年孤寂', author: '馬奎斯', stat_count: 16, cover: '', period: 'month', category: '文學類', type: 'reserve' },
  { book_id: 7, title: '擺渡人', author: '克萊兒·麥克福爾', stat_count: 15, cover: '', period: 'all', category: '文學類', type: 'reserve' },
  { book_id: 8, title: '你當像鳥飛往你的山', author: '塔拉・魏斯特弗', stat_count: 14, cover: '', period: 'year', category: '歷史類', type: 'reserve' },
  { book_id: 9, title: '三體', author: '劉慈欣', stat_count: 13, cover: '', period: 'month', category: '科學類', type: 'reserve' },
  { book_id: 10, title: '房思琪的初戀樂園', author: '林奕含', stat_count: 12, cover: '', period: 'all', category: '文學類', type: 'reserve' },

  // 借閱排行榜 (10 本)
  { book_id: 101, title: '人類大歷史', author: '尤瓦爾·赫拉利', stat_count: 59, cover: '', period: 'year', category: '歷史類', type: 'borrow' },
  { book_id: 102, title: '三體', author: '劉慈欣', stat_count: 51, cover: '', period: 'month', category: '科學類', type: 'borrow' },
  { book_id: 103, title: '活著', author: '余華', stat_count: 48, cover: '', period: 'all', category: '文學類', type: 'borrow' },
  { book_id: 104, title: '小王子', author: '安東尼·聖修伯里', stat_count: 45, cover: '', period: 'all', category: '文學類', type: 'borrow' },
  { book_id: 105, title: '解憂雜貨店', author: '東野圭吾', stat_count: 44, cover: '', period: 'year', category: '文學類', type: 'borrow' },
  { book_id: 106, title: '追風箏的人', author: '卡勒德·胡賽尼', stat_count: 43, cover: '', period: 'year', category: '文學類', type: 'borrow' },
  { book_id: 107, title: '房思琪的初戀樂園', author: '林奕含', stat_count: 41, cover: '', period: 'month', category: '文學類', type: 'borrow' },
  { book_id: 108, title: '擺渡人', author: '克萊兒·麥克福爾', stat_count: 39, cover: '', period: 'all', category: '文學類', type: 'borrow' },
  { book_id: 109, title: '你當像鳥飛往你的山', author: '塔拉・魏斯特弗', stat_count: 38, cover: '', period: 'year', category: '歷史類', type: 'borrow' },
  { book_id: 110, title: '白夜行', author: '東野圭吾', stat_count: 37, cover: '', period: 'month', category: '文學類', type: 'borrow' },

  // 評分排行榜 (10 本)
  { book_id: 201, title: '嫌疑犯X的獻身', author: '東野圭吾', stat_count: 4.9, cover: '', period: 'all', category: '文學類', type: 'rating' },
  { book_id: 202, title: '看不見的城市', author: '卡爾維諾', stat_count: 4.7, cover: '', period: 'year', category: '文學類', type: 'rating' },
  { book_id: 203, title: '挪威的森林', author: '村上春樹', stat_count: 4.6, cover: '', period: 'month', category: '文學類', type: 'rating' },
  { book_id: 204, title: '小王子', author: '安東尼·聖修伯里', stat_count: 4.5, cover: '', period: 'all', category: '文學類', type: 'rating' },
  { book_id: 205, title: '解憂雜貨店', author: '東野圭吾', stat_count: 4.4, cover: '', period: 'year', category: '文學類', type: 'rating' },
  { book_id: 206, title: '人類大歷史', author: '尤瓦爾·赫拉利', stat_count: 4.3, cover: '', period: 'all', category: '歷史類', type: 'rating' },
  { book_id: 207, title: '活著', author: '余華', stat_count: 4.2, cover: '', period: 'year', category: '文學類', type: 'rating' },
  { book_id: 208, title: '白夜行', author: '東野圭吾', stat_count: 4.2, cover: '', period: 'month', category: '文學類', type: 'rating' },
  { book_id: 209, title: '追風箏的人', author: '卡勒德·胡賽尼', stat_count: 4.1, cover: '', period: 'all', category: '文學類', type: 'rating' },
  { book_id: 210, title: '擺渡人', author: '克萊兒·麥克福爾', stat_count: 4.0, cover: '', period: 'month', category: '文學類', type: 'rating' }
])

// 根據當前條件過濾與排序書籍列表
function topBooks(type, isSummary = false) {
  const filtered = rankedBooks.value.filter(book => {
    const periodMatch = selectedPeriod.value === 'all' || book.period === selectedPeriod.value
    const categoryMatch = !selectedCategory.value || book.category === selectedCategory.value
    const typeMatch = isSummary || step.value === type
    return periodMatch && categoryMatch && typeMatch
  }).sort((a, b) => b.stat_count - a.stat_count)

  const start = (currentPage.value - 1) * pageSize.value
  return filtered.slice(start, start + pageSize.value)
}

// 計算總頁數供分頁元件使用
const totalPages = computed(() => {
  const filtered = rankedBooks.value.filter(book => {
    const periodMatch = selectedPeriod.value === 'all' || book.period === selectedPeriod.value
    const categoryMatch = !selectedCategory.value || book.category === selectedCategory.value
    return periodMatch && categoryMatch
  })
  return Math.ceil(filtered.length / pageSize.value) || 1
})

// 當篩選條件改變時，自動重置分頁為第一頁
watch([selectedPeriod, selectedCategory], () => {
  currentPage.value = 1
})
</script>