<template>
  <div class="scroll-wrapper">
    <div class="intro">
  <div class="p-6 max-w-4xl mx-auto">
    <div class="mb-6">
      <Button variant="outline" class="mb-4" @click="router.back()">
        <ChevronLeftIcon class="h-4 w-4 mr-2" />
        返回
      </Button>
      <h1 class="text-3xl font-bold mb-2">{{ bookTitle }}</h1>
      <p class="text-lg text-gray-600">作者：{{ bookAuthor }}</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- 左側：書本封面 -->
      <div class="aspect-[3/4] relative bg-gray-100 rounded-lg overflow-hidden">
        <img :src="coverUrl" :alt="bookTitle" class="object-cover w-full h-full" />
      </div>

      <!-- 右側：書本詳細信息 -->
      <div class="md:col-span-2 space-y-6">
        <!-- 基本信息 -->
        <div class="bg-white rounded-lg border p-6 space-y-4">
          <h2 class="text-xl font-semibold mb-4">基本信息</h2>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-500">ISBN</p>
              <p>978-3-16-148410-0</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">出版社</p>
              <p>某某出版社</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">出版日期</p>
              <p>2024-01-01</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">頁數</p>
              <p>300</p>
            </div>
          </div>
        </div>

        <!-- 簡介 -->
        <div class="bg-white rounded-lg border p-6">
          <h2 class="text-xl font-semibold mb-4">內容簡介</h2>
          <p class="text-gray-600 leading-relaxed">
            這是一個模擬的書本簡介。在這裡，我們可以看到這本書的詳細介紹和內容概要。
            實際應用中，這裡應該顯示真實的書本簡介內容。書本簡介可以包含多個段落，
            描述書本的主要內容、特色、作者背景等信息。
          </p>
        </div>

        <!-- 借閱狀態 -->
        <div class="bg-white rounded-lg border p-6">
          <h2 class="text-xl font-semibold mb-4">借閱狀態</h2>
          <div class="space-y-2">
            <p><span class="text-gray-500">當前狀態：</span><Badge>可借閱</Badge></p>
            <p><span class="text-gray-500">館藏數量：</span>5本</p>
            <p><span class="text-gray-500">可借閱數量：</span>3本</p>
          </div>
          <div class="mt-6">
            <Button class="w-full" @click="handleReserve">預約借閱</Button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Button from '@/components/button/Button.vue'
import Badge from '@/components/badge/Badge.vue'
import { ChevronLeftIcon } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

// 從路由參數獲取書本信息
const bookTitle = computed(() => route.query.title || '未知書名')
const bookAuthor = computed(() => route.query.author || '未知作者')
const bookId = computed(() => route.params.id)

// 模擬封面圖片
const coverUrl = computed(() => 
  route.query.coverUrl || `https://via.placeholder.com/400x600/4ECDC4/FFFFFF?text=${encodeURIComponent(bookTitle.value)}`
)

// 處理預約按鈕點擊
function handleReserve() {
  router.push({
    path: '/book-reservation',
    query: {
      bookId: bookId.value,
      title: bookTitle.value,
      author: bookAuthor.value,
      coverUrl: coverUrl.value
    }
  })
}
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