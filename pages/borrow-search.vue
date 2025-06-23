<template>
  <div class="container">
    <h1>借書查詢</h1>

    <!-- 登入檢查 -->
    <div v-if="!isLoggedIn" class="login-required">
      <div class="login-required-icon">🔒</div>
      <h2>需要登入會員</h2>
      <p>您需要登入會員才能使用借書查詢功能</p>
      <button @click="goToLogin" class="login-required-btn">
        前往登入
      </button>
    </div>

    <!-- 借書查詢內容（只有登入後才顯示） -->
    <div v-else>
      <!-- Simple Search -->
      <div v-if="!isAdvancedSearch" class="simple-search">
        <div class="search-bar">
          <input v-model="simpleSearchQuery" @keyup.enter="performSimpleSearch" type="text" placeholder="輸入關鍵字..." />
          <button class="btn btn-primary" @click="performSimpleSearch">搜尋</button>
          <button class="btn btn-secondary" @click="toggleAdvancedSearch">進階搜尋</button>
        </div>
      </div>

      <!-- Advanced Search -->
      <div v-else class="advanced-search">
        <div class="search-layout">
          <!-- 左側：進階搜尋條件 -->
          <div class="search-conditions">
            <div class="search-bar">
              <h2>進階搜尋</h2>
              <button class="btn btn-secondary" @click="toggleAdvancedSearch">返回單一搜尋</button>
            </div>
            <div v-for="(condition, index) in advancedSearchConditions" :key="index" class="condition">
              <select v-if="index > 0" v-model="condition.operator">
                <option value="AND">AND</option>
                <option value="OR">OR</option>
                <option value="NOT">NOT</option>
              </select>
              <select v-model="condition.field">
                <option value="title">書名</option>
                <option value="author">作者</option>
                <option value="isbn">ISBN</option>
                <option value="publisher">出版社</option>
                <option value="classification">分類號</option>
                <option value="version">版本項</option>
              </select>
              <input v-model="condition.value" type="text" placeholder="輸入搜尋內容" />
              <button v-if="index > 0" class="btn btn-danger" @click="removeCondition(index)">
                移除
              </button>
            </div>
            <div class="search-bar">
              <button class="btn btn-primary" :class="{ 'btn-disabled': advancedSearchConditions.length >= 6 }"
                @click="addCondition">
                新增條件
              </button>
              <button class="btn btn-primary" @click="performAdvancedSearch">搜尋</button>
            </div>
          </div>

          <!-- 右側：過濾條件 -->
          <div class="advanced-filters">
            <div class="filter-section">
              <!-- 出版年 -->
              <div class="condition">
                <label>出版年</label>
                <input v-model="yearFrom" type="number" placeholder="西元年" style="width:100px;" />
                <span>至</span>
                <input v-model="yearTo" type="number" placeholder="西元年" style="width:100px;" />
              </div>
              <hr>
              <!-- 分類法 -->
              <div class="condition">
                <label>分類法</label>
                <select v-model="selectedCategorySystem">
                  <option value="">全部</option>
                  <option v-for="opt in classificationSystemOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <hr>
              <!-- 語言 -->
              <div class="condition">
                <label>語言</label>
                <select v-model="selectedLanguages">
                  <option value="">全部</option>
                  <option v-for="lang in languageOptions" :key="lang.value" :value="lang.value">
                    {{ lang.label }}
                  </option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Search Results -->
      <div v-if="currentPageResults.length" class="results">
        <h2>搜尋結果</h2>
        <div class="result-control-panel">
          <div class="result-control-panel-left">
            <div class="result-row">
              <span class="result-label">每頁顯示：</span>
              <select v-model="itemsPerPage" class="result-select">
                <option v-for="size in pageSizes" :key="size" :value="size">{{ size }} 筆</option>
              </select>
            </div>
            <div class="result-row">
              <span class="result-label">排序：</span>
              <select v-model="sortConfig.field" class="result-select">
                <option value="title_desc">書名↓</option>
                <option value="title_asc">書名↑</option>
                <option value="author_desc">作者↓</option>
                <option value="author_asc">作者↑</option>
                <option value="publisher_desc">出版社↓</option>
                <option value="publisher_asc">出版社↑</option>
                <option value="publishdate_desc">出版年↓</option>
                <option value="publishdate_asc">出版年↑</option>
              </select>
            </div>
          </div>
          <div class="result-control-panel-right">
            <button class="btn btn-secondary" @click="goToBorrowList">
              查看借書清單
            </button>
          </div>
        </div>
        <div v-for="book in currentPageResults" :key="book.isbn" class="result-item">
          <div class="result-image">
            <img :src="book.imgUrl || `https://covers.openlibrary.org/b/isbn/${book.isbn}-M.jpg`" :alt="book.title"
              @error="handleImageError" class="book-cover"
              onerror="this.onerror=null;this.src='https://cdn-icons-png.flaticon.com/512/2232/2232688.png';" />
          </div>
          <div class="result-info">
            <p><strong>書名:</strong> {{ book.title }}</p>
            <p><strong>作者:</strong> {{ book.author }}</p>
            <p><strong>出版社:</strong> {{ book.publisher }}</p>
            <p><strong>出版年:</strong> {{ book.publishdate }}</p>
            <p><strong>ISBN:</strong> {{ book.isbn }}</p>
          </div>
          <div class="result-actions">
            <button class="btn bookinfo-btn" @click="navigateToBookDetail(book)">
              更多資訊
            </button>
          </div>
        </div>
        <!-- Pagination -->
        <div class="result-pagination">
          <div class="result-pagination-controls">
            <button class="pagination-btn" :disabled="currentPage === 1" @click="currentPage--">
              <span class="sr-only">上一頁</span>
            </button>
            <span>共{{ totalPages }}頁</span>
            <input type="number" :value="currentPage" class="pagination-input" min="1" :max="totalPages"
              @change="e => goToPage(parseInt(e.target.value))" />
            <span>/{{ totalPages }}頁</span>
            <button class="pagination-btn" :disabled="currentPage >= totalPages" @click="currentPage++">
              <span class="sr-only">下一頁</span>
            </button>
          </div>
          <div class="pagination-info">
            顯示第 {{ Math.min((currentPage - 1) * itemsPerPage + 1, searchResults.totalElements) }} 到
            {{ Math.min(currentPage * itemsPerPage, searchResults.totalElements) }} 筆，
            共 {{ searchResults.totalElements }} 筆
          </div>
        </div>
      </div>
      <div v-else-if="searched" class="no-results">
        無搜尋結果
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'

// State
const route = useRoute()
const router = useRouter()
const book = ref(null)
const simpleSearchQuery = ref('');
const isAdvancedSearch = ref(false);
const advancedSearchConditions = ref([
  { field: 'title', operator: 'AND', value: '' },
]);
const searchResults = ref({
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 10,
  number: 0
});
const searched = ref(false);
const currentPage = ref(1);
const pageSizes = ref([10, 25, 50]);
const itemsPerPage = ref(10);
const sortConfig = ref({
  field: 'title_asc'
});

// 登入狀態檢查
const isLoggedIn = ref(false)

// 添加缺少的 ref 變數
const yearFrom = ref('');
const yearTo = ref('');
const selectedCategorySystem = ref('');
const selectedLanguages = ref('');

// 分類選項
const classificationSystemOptions = ref([
  { value: 'CLC', label: '中國圖書分類法' },
  { value: 'LCC', label: '美國國會圖書館分類法' }
]);

// 語言選項
const languageOptions = ref([
  { value: '中文', label: '中文' },
  { value: '英文', label: '英文' },
  { value: '日文', label: '日文' },
  { value: '法文', label: '法文' },
  { value: '德文', label: '德文' }
]);

// 檢查登入狀態
const checkLoginStatus = () => {
  // 檢查 localStorage 中的用戶資訊
  const storedUser = localStorage.getItem('user')
  const jwtToken = localStorage.getItem('jwt_token')
  const authToken = localStorage.getItem('authToken')

  console.log('=== 登入狀態檢查 ===')
  console.log('storedUser:', storedUser)
  console.log('jwtToken:', jwtToken)
  console.log('authToken:', authToken)

  if (storedUser) {
    try {
      const user = JSON.parse(storedUser)
      isLoggedIn.value = true
      console.log('✅ 用戶已登入：', user)
    } catch (e) {
      console.error('❌ 解析用戶資訊失敗：', e)
      isLoggedIn.value = false
    }
  } else if (jwtToken || authToken) {
    // 如果有 token 但沒有用戶資訊，也視為已登入
    isLoggedIn.value = true
    console.log('✅ 檢測到登入 token')
  } else {
    isLoggedIn.value = false
    console.log('❌ 用戶未登入')
  }

  console.log('最終登入狀態：', isLoggedIn.value)
  console.log('==================')
}

// 跳轉到登入頁面
const goToLogin = () => {
  router.push('/login')
}

onMounted(async () => {
  // 檢查登入狀態
  checkLoginStatus()

  const route = useRoute();

  //  進階搜尋返回
  if (route.query.returnType === 'advanced' && route.query.returnAdvanced) {
    try {
      const restored = JSON.parse(route.query.returnAdvanced);
      isAdvancedSearch.value = true;
      advancedSearchConditions.value = restored.conditions || [];
      yearFrom.value = restored.yearFrom || '';
      yearTo.value = restored.yearTo || '';
      selectedCategorySystem.value = restored.selectedCategorySystem || '';
      selectedLanguages.value = restored.selectedLanguages || '';
      currentPage.value = parseInt(route.query.returnPage) || 1;
      await performAdvancedSearch();
    } catch (e) {
      console.error("還原進階搜尋失敗", e);
    }
    return; //  early return 避免簡易搜尋干擾
  }

  //  簡易搜尋返回
  if (route.query.q && route.query.returnType === 'simple') {
    simpleSearchQuery.value = route.query.q;
    currentPage.value = parseInt(route.query.returnPage) || 1;
    await performSimpleSearch();
  }

  //  若網址有 bookId，嘗試載入書籍資料
  const bookId = route.query.bookId;
  if (bookId) {
    try {
      const response = await axios.get(`http://localhost:8080/api/books/${bookId}`);
      book.value = response.data;
    } catch (e) {
      console.error('找不到書籍', e);
    }
  }
});

// 修改後的導航到書籍詳情頁方法
const navigateToBookDetail = async (book) => {
  router.push({
    path: '/borrow-bookinfo',
    query: {
      id: book.id || book.bookId,
      title: book.title,
      author: book.author,
      isbn: book.isbn,
      publisher: book.publisher,
      publishdate: book.publishdate,
      classification: book.classification,
      language: book.language,
      summary: book.summary || '',
      imgUrl: book.imgUrl || 'https://cdn-icons-png.flaticon.com/512/2232/2232688.png',
      is_available: book.is_available.toString(),
      categorysystem: book.categorysystem || '',
      total_copies: String(book.total_copies || 1),
      available_copies: String(book.available_copies || (book.is_available === 1 ? 1 : 0)),

      // 搜尋狀態
      returnQuery: isAdvancedSearch.value ? 'advanced' : simpleSearchQuery.value,
      returnPage: currentPage.value.toString(),
      returnType: isAdvancedSearch.value ? 'advanced' : 'simple',
      returnAdvanced: isAdvancedSearch.value
        ? JSON.stringify({
          conditions: advancedSearchConditions.value,
          yearFrom: yearFrom.value,
          yearTo: yearTo.value,
          selectedCategorySystem: selectedCategorySystem.value,
          selectedLanguages: selectedLanguages.value
        })
        : undefined
    }
  });
};

const handleImageError = (event) => {
  event.target.src = 'https://cdn-icons-png.flaticon.com/512/2232/2232688.png';
}

function scrollToContainerTop() {
  const container = document.querySelector('.container');
  if (container) {
    container.scrollTop = 0;
  }
}

// 修改 fetchBooks 函數
const fetchBooks = async (params) => {
  try {
    const response = await axios.get('http://localhost:8080/api/books/simple-search', {
      params: {
        ...params,
        page: currentPage.value - 1,
        size: itemsPerPage.value
        // sortField 和 sortDir 已經在 params 裡
      }
    });

    if (response.data) {
      console.log('API 回應資料：', response.data);
      console.log('API 回應資料類型：', typeof response.data);

      const content = Array.isArray(response.data.content) ? response.data.content : [];

      // 為每本書獲取封面並處理可用性
      for (const book of content) {
        if (book.imgUrl) {
          book.imgUrl = book.imgUrl.trim();
        } else {
          book.imgUrl = 'https://cdn-icons-png.flaticon.com/512/2232/2232688.png';
        }

        // 正確處理可用性欄位
        if (book.is_available !== undefined) {
          // 如果是布林值，轉換為數字
          if (typeof book.is_available === 'boolean') {
            book.is_available = book.is_available ? 1 : 0;
          } else if (typeof book.is_available === 'number') {
            book.is_available = book.is_available > 0 ? 1 : 0;
          } else {
            book.is_available = 0;
          }
        } else {
          book.is_available = 0;
        }

        // 確保有可借閱數量欄位
        if (book.available_copies === undefined) {
          book.available_copies = book.is_available;
        }

        if (book.total_copies === undefined) {
          book.total_copies = 1;
        }
      }

      searchResults.value = {
        ...response.data,
        content
      };
    } else {
      searchResults.value = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        size: itemsPerPage.value,
        number: 0
      };
    }
    searched.value = true;
  } catch (error) {
    console.error('搜尋錯誤：', error);
    alert('搜尋失敗，請稍後再試');
    searchResults.value = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: itemsPerPage.value,
      number: 0
    };
    searched.value = true;
  }
};

// 修改 computed properties
const totalPages = computed(() => searchResults.value.totalPages || 0);

// 直接用後端回傳順序
const currentPageResults = computed(() => searchResults.value.content || []);

// Methods
const toggleAdvancedSearch = () => {
  isAdvancedSearch.value = !isAdvancedSearch.value;
  if (!isAdvancedSearch.value) {
    advancedSearchConditions.value = [{ field: 'title', operator: 'AND', value: '' }];
    searchResults.value = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: itemsPerPage.value,
      number: 0
    };
    searched.value = false;
    currentPage.value = 1;
  }
};

const addCondition = () => {
  if (advancedSearchConditions.value.length < 6) {
    advancedSearchConditions.value.push({ field: 'title', operator: 'AND', value: '' });
  }
};

const removeCondition = (index) => {
  advancedSearchConditions.value.splice(index, 1);
};

// 修改簡單搜尋函數
const performSimpleSearch = async () => {
  const query = simpleSearchQuery.value.trim();
  if (!query) {
    searchResults.value = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: itemsPerPage.value,
      number: 0
    };
    searched.value = true;
    currentPage.value = 1;
    return;
  }

  // 取得排序欄位與方向
  const [field, direction] = sortConfig.value.field.split('_');

  await fetchBooks({
    field: 'fulltext',
    keyword: query,
    sortField: field,
    sortDir: direction
  });
  scrollToContainerTop();
};

// 修改進階搜尋函數
const performAdvancedSearch = async () => {
  // 檢查是否有任何搜尋條件
  const hasSearchConditions = advancedSearchConditions.value.some(cond => cond.value.trim());
  const hasFilterConditions = yearFrom.value || yearTo.value || selectedCategorySystem.value || selectedLanguages.value;

  if (!hasSearchConditions && !hasFilterConditions) {
    alert('請至少輸入一個搜尋條件');
    return;
  }

  // 組成條件陣列
  const conditions = advancedSearchConditions.value
    .filter(cond => cond.value.trim())
    .map(cond => ({
      field: cond.field,
      operator: cond.operator,
      value: cond.value.trim()
    }));

  // 添加其他篩選條件
  if (yearFrom.value || yearTo.value) {
    conditions.push({
      field: 'publishdate',
      operator: 'AND',
      value: {
        from: yearFrom.value || null,
        to: yearFrom.value || null
      }
    });
  }
  if (selectedCategorySystem.value) {
    conditions.push({
      field: 'categorysystem',
      operator: 'AND',
      value: {
        cs_id: selectedCategorySystem.value,
        include_children: true
      }
    });
  }
  if (selectedLanguages.value) {
    conditions.push({
      field: 'language',
      operator: 'AND',
      value: selectedLanguages.value
    });
  }

  // 分頁與排序參數
  const [sortField, sortDir] = sortConfig.value.field.split('_');

  try {
    console.log('→ Advanced Search Payload:', conditions);
    const response = await axios.post('http://localhost:8080/api/books/advanced-search', conditions, {
      params: {
        page: currentPage.value - 1,
        size: itemsPerPage.value,
        sortField,
        sortDir
      }
    });

    // 為每本書獲取封面
    const content = Array.isArray(response.data.content) ? response.data.content : [];
    for (const book of content) {
      if (book.imgUrl) {
        book.imgUrl = book.imgUrl.trim();
      } else {
        book.imgUrl = 'https://cdn-icons-png.flaticon.com/512/2232/2232688.png';
      }

      // 正確處理可用性欄位
      if (book.is_available !== undefined) {
        // 如果是布林值，轉換為數字
        if (typeof book.is_available === 'boolean') {
          book.is_available = book.is_available ? 1 : 0;
        } else if (typeof book.is_available === 'number') {
          book.is_available = book.is_available > 0 ? 1 : 0;
        } else {
          book.is_available = 0;
        }
      } else {
        book.is_available = 0;
      }

      // 確保有可借閱數量欄位
      if (book.available_copies === undefined) {
        book.available_copies = book.is_available;
      }

      if (book.total_copies === undefined) {
        book.total_copies = 1;
      }
    }

    searchResults.value = {
      ...response.data,
      content
    };
    searched.value = true;
    scrollToContainerTop();
  } catch (error) {
    console.error('進階查詢錯誤：', error);
    alert('搜尋失敗，請稍後再試');
    searchResults.value = {
      content: [],
      totalElements: 0,
      totalPages: 0,
      size: itemsPerPage.value,
      number: 0
    };
    searched.value = true;
    scrollToContainerTop();
  }
};

const goToPage = (page) => {
  const pageNum = parseInt(page);
  if (pageNum && !isNaN(pageNum)) {
    if (pageNum < 1) {
      currentPage.value = 1;
    } else if (pageNum > totalPages.value) {
      currentPage.value = totalPages.value;
    } else {
      currentPage.value = pageNum;
    }
    scrollToContainerTop();
  }
};

// 導航到借書清單
const goToBorrowList = () => {
  router.push('/borrow-record');
};

watch(itemsPerPage, () => {
  currentPage.value = 1;
  if (searched.value) {
    if (isAdvancedSearch.value) {
      performAdvancedSearch();
    } else {
      performSimpleSearch();
    }
  }
}, { deep: true });

watch(sortConfig, () => {
  if (searched.value) {
    if (isAdvancedSearch.value) {
      performAdvancedSearch();
    } else {
      performSimpleSearch();
    }
  }
}, { deep: true });
</script>

<style scoped>
body {
  font-family: Arial, sans-serif;
  background-color: #f0f0f0;
  margin: 0;
  padding: 20px;
  align-items: center;
}

.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background-color: transparent;
  padding: 20px;
  height: 100vh;
  overflow-y: auto;
  /* 隱藏滾動條（跨瀏覽器） */
  scrollbar-width: none;
  /* Firefox */
  -ms-overflow-style: none;
  /* IE 10+ */
}

.container::-webkit-scrollbar {
  display: none;
  /* Chrome/Safari/Edge */
}

.simple-search,
.advanced-search {
  max-width: 100%;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-bar input {
  flex: 1;
  min-width: 200px;
  max-width: 600px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
}

.search-bar input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 5px rgba(0, 123, 255, 0.3);
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: transparent;
  color: #007bff;
  text-decoration: underline;
}

.btn-secondary:hover {
  color: #0056b3;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-danger:hover {
  background-color: #b02a37;
}

.btn-disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.bookinfo-btn {
  background-color: #007bff;
  color: white;
}

.bookinfo-btn:hover {
  background-color: #0056b3;
}

.condition {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.condition select,
.condition input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
}

.condition input {
  flex: 1;
  min-width: 150px;
}

.results {
  margin-top: 20px;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ddd;
  margin: 0 50px;
  gap: 10px;
}

.result-item:hover {
  background-color: #96c0fdbe;
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.result-item:last-child {
  border-bottom: none;
}

.result-info {
  flex: 1;
}

.result-info p {
  margin: 8px;
  font-size: 18px;
}

.result-info p strong {
  margin-right: 5px;
}

.result-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-control-panel {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.result-control-panel-left {
  display: flex;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
}

.result-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-label {
  font-size: 1rem;
  color: #222;
}

.result-select {
  width: 120px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 4px 8px;
  font-size: 1rem;
  background: #fff;
  color: #18181b;
}

.result-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.result-pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  height: 32px;
  min-width: 32px;
  padding: 0 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #18181b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 1rem;
  line-height: 1;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-btn:hover {
  background: #f3f4f6;
}

.pagination-input {
  height: 32px;
  width: 48px;
  text-align: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  color: #18181b;
  background: #fff;
}

.pagination-info {
  font-size: 0.95rem;
  color: #4b5563;
  text-align: center;
}

.no-results {
  color: #6c757d;
  margin-top: 20px;
}

h2 {
  font-size: 20px;
  margin-bottom: 10px;
}

/* 佈局樣式 */
.search-layout {
  display: flex;
  gap: 20px;
}

.search-conditions {
  flex: 1;
  max-width: 600px;
}

.advanced-filters {
  min-width: 200px;
  padding: 15px;
  border-radius: 4px;
}

.filter-section h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.condition label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

/* RWD 樣式 */
@media (max-width: 768px) {
  .search-layout {
    flex-direction: column;
  }

  .search-conditions,
  .advanced-filters {
    max-width: 100%;
    min-width: auto;
  }

  .advanced-filters {
    order: 2;
    /* 確保 advanced-filters 移到下方 */
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-bar input,
  .search-bar button {
    width: 100%;
    margin-bottom: 10px;
  }

  .condition {
    flex-direction: column;
    align-items: stretch;
  }

  .condition select,
  .condition input {
    width: 100%;
    margin-bottom: 10px;
  }

  .result-item {
    flex-direction: row;
    align-items: flex-start;
    margin: 0 10px;
  }

  .result-actions {
    margin-top: 10px;
  }

  .result-control-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .result-control-panel-left {
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .container {
    padding: 10px;
  }

  .search-bar input {
    min-width: 150px;
  }

  .result-row {
    flex-direction: column;
    gap: 8px;
  }

  .result-select {
    width: 100%;
  }

  .pagination-controls {
    flex-direction: column;
    gap: 8px;
  }

  .pagination-input {
    width: 60px;
  }
}

.result-image {
  width: 120px;
  height: 180px;
  margin-right: 20px;
  flex-shrink: 0;
}

.book-cover {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.book-cover:hover {
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .result-image {
    width: 100px;
    height: 150px;
    margin-right: 15px;
  }
}

@media (max-width: 480px) {
  .result-image {
    width: 80px;
    height: 120px;
    margin-right: 10px;
  }
}

/* 登入提示樣式 */
.login-required {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  color: #6b7280;
  background: transparent;
  border-radius: 12px;
  margin: 20px;
}

.login-required-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.login-required h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.login-required p {
  font-size: 1rem;
  color: #6b7280;
  margin-bottom: 24px;
  max-width: 400px;
}

.login-required-btn {
  padding: 12px 32px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.login-required-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
}

.login-required-btn:active {
  transform: translateY(0);
}
</style>