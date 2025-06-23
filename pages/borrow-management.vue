<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 頁面標題 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">借閱管理</h1>
        <p class="mt-2 text-gray-600">管理您的借閱記錄、續借和還書</p>
      </div>

      <!-- 統計卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-blue-100 rounded-lg">
              <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">當前借閱</p>
              <p class="text-2xl font-semibold text-gray-900">{{ statistics.currentBorrows || 0 }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-red-100 rounded-lg">
              <svg class="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">逾期借閱</p>
              <p class="text-2xl font-semibold text-red-600">{{ statistics.overdueBorrows || 0 }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-green-100 rounded-lg">
              <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">已歸還</p>
              <p class="text-2xl font-semibold text-gray-900">{{ statistics.returnedBorrows || 0 }}</p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-lg shadow p-6">
          <div class="flex items-center">
            <div class="p-2 bg-yellow-100 rounded-lg">
              <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
              </svg>
            </div>
            <div class="ml-4">
              <p class="text-sm font-medium text-gray-500">總續借次數</p>
              <p class="text-2xl font-semibold text-gray-900">{{ statistics.totalRenewals || 0 }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 標籤頁 -->
      <div class="bg-white rounded-lg shadow">
        <div class="border-b border-gray-200">
          <nav class="-mb-px flex space-x-8 px-6">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              @click="activeTab = tab.id"
              :class="[
                activeTab === tab.id
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
                'whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm'
              ]"
            >
              {{ tab.name }}
            </button>
          </nav>
        </div>

        <div class="p-6">
          <!-- 當前借閱 -->
          <div v-if="activeTab === 'current'" class="space-y-4">
            <div v-if="loading" class="text-center py-8">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p class="mt-2 text-gray-500">載入中...</p>
            </div>
            
            <div v-else-if="currentBorrows.length === 0" class="text-center py-8">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">目前沒有借閱中的書籍</h3>
              <p class="mt-1 text-sm text-gray-500">開始借閱您喜歡的書籍吧！</p>
            </div>
            
            <div v-else class="space-y-4">
              <div
                v-for="book in currentBorrows"
                :key="book.borrowId"
                class="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <h3 class="text-lg font-medium text-gray-900">{{ book.bookTitle }}</h3>
                    <p class="text-sm text-gray-500">作者：{{ book.bookAuthor }}</p>
                    <p class="text-sm text-gray-500">借閱日期：{{ formatDate(book.borrowDate) }}</p>
                    <p class="text-sm text-gray-500">到期日期：{{ formatDate(book.dueDate) }}</p>
                    <p class="text-sm text-gray-500">續借次數：{{ book.renewCount }}/2</p>
                  </div>
                  <div class="flex space-x-2">
                    <button
                      v-if="canRenewBook(book)"
                      @click="renewBook(book.borrowId)"
                      :disabled="renewing === book.borrowId"
                      class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                    >
                      {{ renewing === book.borrowId ? '續借中...' : '續借' }}
                    </button>
                    <button
                      @click="returnBook(book.borrowId)"
                      :disabled="returning === book.borrowId"
                      class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                    >
                      {{ returning === book.borrowId ? '還書中...' : '還書' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 借閱歷史 -->
          <div v-if="activeTab === 'history'" class="space-y-4">
            <div v-if="loading" class="text-center py-8">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p class="mt-2 text-gray-500">載入中...</p>
            </div>
            
            <div v-else-if="borrowHistory.length === 0" class="text-center py-8">
              <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
              </svg>
              <h3 class="mt-2 text-sm font-medium text-gray-900">沒有借閱歷史</h3>
              <p class="mt-1 text-sm text-gray-500">開始您的第一次借閱吧！</p>
            </div>
            
            <div v-else class="space-y-4">
              <div
                v-for="book in borrowHistory"
                :key="book.borrowId"
                class="border border-gray-200 rounded-lg p-4"
              >
                <div class="flex justify-between items-start">
                  <div class="flex-1">
                    <h3 class="text-lg font-medium text-gray-900">{{ book.bookTitle }}</h3>
                    <p class="text-sm text-gray-500">作者：{{ book.bookAuthor }}</p>
                    <p class="text-sm text-gray-500">借閱日期：{{ formatDate(book.borrowDate) }}</p>
                    <p class="text-sm text-gray-500">到期日期：{{ formatDate(book.dueDate) }}</p>
                    <p v-if="book.returnDate" class="text-sm text-gray-500">歸還日期：{{ formatDate(book.returnDate) }}</p>
                    <p class="text-sm text-gray-500">續借次數：{{ book.renewCount }}</p>
                    <span
                      :class="[
                        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                        getStatusClass(book.status)
                      ]"
                    >
                      {{ getStatusText(book.status) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 借閱限制 -->
          <div v-if="activeTab === 'limits'" class="space-y-4">
            <div v-if="loading" class="text-center py-8">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p class="mt-2 text-gray-500">載入中...</p>
            </div>
            
            <div v-else class="space-y-6">
              <div class="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <h3 class="text-lg font-medium text-blue-900 mb-2">借閱限制資訊</h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p class="text-sm text-blue-700">當前借閱：{{ limits.currentBorrows }}/{{ limits.maxBorrows }}</p>
                    <p class="text-sm text-blue-700">逾期書籍：{{ limits.overdueBorrows }}/{{ limits.maxOverdueBooks }}</p>
                  </div>
                  <div>
                    <p class="text-sm text-blue-700">可借閱：{{ limits.canBorrow ? '是' : '否' }}</p>
                    <p class="text-sm text-blue-700">有逾期限制：{{ limits.hasOverdueLimit ? '是' : '否' }}</p>
                  </div>
                </div>
              </div>

              <div v-if="limits.overdueBorrows > 0" class="bg-red-50 border border-red-200 rounded-lg p-4">
                <h3 class="text-lg font-medium text-red-900 mb-2">逾期提醒</h3>
                <p class="text-sm text-red-700">您有 {{ limits.overdueBorrows }} 本書籍已逾期，請盡快歸還以避免影響借閱權益。</p>
              </div>

              <div v-if="!limits.canBorrow" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <h3 class="text-lg font-medium text-yellow-900 mb-2">借閱限制</h3>
                <p class="text-sm text-yellow-700">您目前無法借閱新書，請檢查借閱限制或歸還逾期書籍。</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { borrowApi, borrowUtils } from '~/utils/borrowApi'

// 響應式資料
const activeTab = ref('current')
const loading = ref(false)
const renewing = ref(null)
const returning = ref(null)

const currentBorrows = ref([])
const borrowHistory = ref([])
const statistics = ref({})
const limits = ref({})

// 標籤頁配置
const tabs = [
  { id: 'current', name: '當前借閱' },
  { id: 'history', name: '借閱歷史' },
  { id: 'limits', name: '借閱限制' }
]

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('zh-TW')
}

// 檢查是否可以續借
const canRenewBook = (book) => {
  if (book.status !== 'BORROWED') return false
  if (book.renewCount >= 2) return false
  return borrowUtils.canRenew(book.dueDate)
}

// 取得狀態樣式
const getStatusClass = (status) => {
  switch (status) {
    case 'BORROWED':
      return 'bg-blue-100 text-blue-800'
    case 'RENEWED':
      return 'bg-yellow-100 text-yellow-800'
    case 'RETURNED':
      return 'bg-green-100 text-green-800'
    case 'OVERDUE':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

// 取得狀態文字
const getStatusText = (status) => {
  switch (status) {
    case 'BORROWED':
      return '借閱中'
    case 'RENEWED':
      return '已續借'
    case 'RETURNED':
      return '已歸還'
    case 'OVERDUE':
      return '已逾期'
    default:
      return '未知狀態'
  }
}

// 載入當前借閱
const loadCurrentBorrows = async () => {
  try {
    loading.value = true
    const response = await borrowApi.getCurrentBorrows()
    if (response.success) {
      currentBorrows.value = response.data
    }
  } catch (error) {
    console.error('載入當前借閱失敗:', error)
  } finally {
    loading.value = false
  }
}

// 載入借閱歷史
const loadBorrowHistory = async () => {
  try {
    loading.value = true
    const response = await borrowApi.getBorrowHistory()
    if (response.success) {
      borrowHistory.value = response.data
    }
  } catch (error) {
    console.error('載入借閱歷史失敗:', error)
  } finally {
    loading.value = false
  }
}

// 載入統計資料
const loadStatistics = async () => {
  try {
    const response = await borrowApi.getBorrowStatistics()
    if (response.success) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('載入統計資料失敗:', error)
  }
}

// 載入借閱限制
const loadLimits = async () => {
  try {
    const response = await borrowApi.checkBorrowLimits()
    if (response.success) {
      limits.value = response.data
    }
  } catch (error) {
    console.error('載入借閱限制失敗:', error)
  }
}

// 續借書籍
const renewBook = async (borrowId) => {
  try {
    renewing.value = borrowId
    
    // 先檢查是否可以續借
    const checkResponse = await borrowApi.checkRenewable(borrowId)
    if (!checkResponse.success || !checkResponse.data.canRenew) {
      alert('此書目前無法續借')
      return
    }
    
    // 執行續借
    const response = await borrowApi.renewBook(borrowId)
    if (response.success) {
      alert('續借成功！')
      await loadCurrentBorrows()
      await loadStatistics()
    } else {
      alert('續借失敗: ' + response.message)
    }
  } catch (error) {
    alert('續借失敗: ' + error.message)
  } finally {
    renewing.value = null
  }
}

// 還書
const returnBook = async (borrowId) => {
  if (!confirm('確定要歸還這本書嗎？')) return
  
  try {
    returning.value = borrowId
    const response = await borrowApi.returnBook(borrowId)
    if (response.success) {
      alert('還書成功！')
      await loadCurrentBorrows()
      await loadStatistics()
    } else {
      alert('還書失敗: ' + response.message)
    }
  } catch (error) {
    alert('還書失敗: ' + error.message)
  } finally {
    returning.value = null
  }
}

// 監聽標籤頁切換
const watchTabChange = () => {
  if (activeTab.value === 'current' && currentBorrows.value.length === 0) {
    loadCurrentBorrows()
  } else if (activeTab.value === 'history' && borrowHistory.value.length === 0) {
    loadBorrowHistory()
  } else if (activeTab.value === 'limits') {
    loadLimits()
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadCurrentBorrows(),
    loadStatistics(),
    loadLimits()
  ])
})

// 監聽標籤頁變化
watch(activeTab, watchTabChange)
</script> 