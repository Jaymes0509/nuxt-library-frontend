<template>
  <div class="scroll-wrapper">
    <div class="intro">
      <div class="p-6 max-w-4xl mx-auto">
        <div class="mb-6">
          <Button variant="outline" class="mb-4" @click="router.back()">
            <ChevronLeftIcon class="h-4 w-4 mr-2" />
            返回
          </Button>
          <h1 class="text-3xl font-bold mb-2">{{ book?.title || '載入中...' }}</h1>
          <p class="text-lg text-gray-600">作者：{{ book?.author || '載入中...' }}</p>
        </div>

        <div v-if="book" class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- 左側：書本封面 -->
          <div class="aspect-[3/4] relative bg-gray-100 rounded-lg overflow-hidden">
            <img :src="coverUrl" :alt="book.title" class="object-cover w-full h-full" />
          </div>

          <!-- 右側：書本詳細信息 -->
          <div class="md:col-span-2 space-y-6">
            <!-- 基本信息 -->
            <div class="bg-white rounded-lg border p-6 space-y-4">
              <h2 class="text-xl font-semibold mb-4">基本信息</h2>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <p class="text-sm text-gray-500">ISBN</p>
                  <p>{{ book.isbn }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">出版社</p>
                  <p>{{ book.publisher }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">出版日期</p>
                  <p>{{ book.publishdate }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">語言</p>
                  <p>{{ book.language }}</p>
                </div>
                <div>
                  <p class="text-sm text-gray-500">分類</p>
                  <p>{{ book.classification }}</p>
                </div>
              </div>
            </div>

            <!-- 簡介 -->
            <div class="bg-white rounded-lg border p-6">
              <h2 class="text-xl font-semibold mb-4">內容簡介</h2>
              <p class="text-gray-600 leading-relaxed">
                {{ book.description || '暫無簡介' }}
              </p>
            </div>

            <!-- 借閱狀態 -->
            <div class="bg-white rounded-lg border p-6">
              <h2 class="text-xl font-semibold mb-4">借閱狀態</h2>
              <div class="space-y-2">
                <p>
                  <span class="text-gray-500">當前狀態：</span>
                  <Badge>{{ book.is_available === 1 ? '可借閱' : '已借出' }}</Badge>
                </p>
                <p><span class="text-gray-500">館藏數量：</span>{{ book.total_copies }}本</p>
                <p><span class="text-gray-500">可借閱數量：</span>{{ book.available_copies }}本</p>
              </div>
              <div class="mt-6">
                <Button 
                  class="w-full" 
                  :disabled="book.is_available !== 1"
                  @click="handleReserve"
                >
                  {{ book.is_available === 1 ? '預約借閱' : '目前無法借閱' }}
                </Button>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 載入中或錯誤狀態 -->
        <div v-else-if="error" class="text-center py-8">
          <p class="text-red-500">{{ error }}</p>
        </div>
        <div v-else class="text-center py-8">
          <p class="text-gray-500">載入中...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Button from '@/components/button/Button.vue'
import Badge from '@/components/badge/Badge.vue'
import { ChevronLeftIcon } from 'lucide-vue-next'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const book = ref({
  id: route.params.id,
  title: route.query.title || '載入中...',
  author: route.query.author || '載入中...',
  isbn: route.query.isbn,
  publisher: route.query.publisher,
  publishdate: route.query.publishdate,
  classification: route.query.classification,
  language: route.query.language,
  description: route.query.description,
  is_available: Number(route.query.is_available),
  total_copies: parseInt(route.query.total_copies || '1'),
  available_copies: parseInt(route.query.available_copies || '0')
})
const error = ref(null)

// 獲取書本封面
const coverUrl = computed(() => {
  if (!book.value?.isbn) return 'https://via.placeholder.com/400x600/4ECDC4/FFFFFF?text=No+Cover'
  return `https://covers.openlibrary.org/b/isbn/${book.value.isbn}-L.jpg`
})

// 獲取書本詳細信息
async function fetchBookDetails() {
  try {
    error.value = null
    const response = await axios.get(`http://localhost:8080/api/books/${route.params.id}`)
    // 合併查詢參數和 API 響應數據，優先使用 API 數據
    book.value = {
      ...book.value,
      ...response.data
    }
  } catch (e) {
    console.error('獲取書籍詳情失敗：', e)
    error.value = '無法載入完整書籍信息'
  }
}

// 處理預約按鈕點擊
function handleReserve() {
  if (book.value?.is_available !== 1) return
  
  router.push({
    path: '/book-reservation',
    query: {
      bookId: route.params.id,
      title: book.value.title,
      author: book.value.author,
      isbn: book.value.isbn
    }
  })
}

onMounted(() => {
  fetchBookDetails()
})
</script>

<style scoped>
.scroll-wrapper {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.intro {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
  background: transparent;
}

/* 滾動條預設為透明 */
.intro::-webkit-scrollbar {
  width: 8px;
}

.intro::-webkit-scrollbar-thumb {
  background-color: transparent;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

/* 滑鼠靠近 wrapper 時顯示滾動條 */
.scroll-wrapper:hover .intro::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.4);
}

/* 滑鼠靠近時滾動條背景也顯示 */
.scroll-wrapper:hover .intro {
  scrollbar-color: rgba(0, 0, 0, 0.4) transparent;
}

.grid {
  display: grid;
}

.bg-white.rounded-lg.border.p-6.space-y-4 {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(229, 231, 235, 0.4);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.bg-white.rounded-lg.border.p-6 {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(229, 231, 235, 0.4);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}
</style> 