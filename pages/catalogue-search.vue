<template>
  <div class="container">
    <h1>館藏查詢</h1>
    <!-- Simple Search -->
    <div v-if="!isAdvancedSearch" class="simple-search">
      <div class="search-bar">
        <input
          v-model="simpleSearchQuery"
          @keyup.enter="performSimpleSearch"
          type="text"
          placeholder="輸入關鍵字..."
        />
        <button class="btn btn-primary" @click="performSimpleSearch">搜尋</button>
        <button class="btn btn-secondary" @click="toggleAdvancedSearch">進階搜尋</button>
      </div>
    </div>

    <!-- Advanced Search -->
    <div v-else class="advanced-search">
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
          <option value="year">出版年</option>
        </select>
        <input v-model="condition.value" type="text" placeholder="輸入搜尋內容" />
        <button
          v-if="index > 0"
          class="btn btn-danger"
          @click="removeCondition(index)"
        >
          移除
        </button>
      </div>
      <div class="search-bar">
        <button
          class="btn btn-primary"
          :class="{ 'btn-disabled': advancedSearchConditions.length >= 6 }"
          @click="addCondition"
        >
          新增條件
        </button>
        <button class="btn btn-primary" @click="performAdvancedSearch">搜尋</button>
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
              <option value="year_desc">出版年↓</option>
              <option value="year_asc">出版年↑</option>
            </select>
          </div>
        </div>
      </div>
      <div v-for="book in currentPageResults" :key="book.isbn" class="result-item">
        <div class="result-info">
          <p><strong>書名:</strong> {{ book.title }}</p>
          <p><strong>作者:</strong> {{ book.author }}</p>
          <p><strong>出版設:</strong> {{ book.publisher }}</p>
          <p><strong>出版年:</strong> {{ book.year }}</p>
          <p><strong>ISBN:</strong> {{ book.isbn }}</p>
          <p>
            <strong>在架狀態:</strong>
            <span :class="book.available ? 'availability' : 'unavailable'">
              {{ book.available ? '是' : '否' }}
            </span>
          </p>
          <p v-if="book.reserved">
            <strong>預約狀態:</strong>
            <span class="reserved">已預約</span>
          </p>
        </div>
        <div class="result-actions">
          <button
            class="btn"
            :class="book.favorite ? 'favorite' : 'not-favorite'"
            @click="toggleFavorite(book)"
          >
            最愛
          </button>
          <button
            class="btn bookinfo-btn"
            
            @click=""
            
          >
            更多資訊
          </button>
        </div>
      </div>
      <!-- Pagination -->
      <div class="result-pagination">
        <div class="result-pagination-controls">
          <button 
            class="pagination-btn"
            :disabled="currentPage === 1"
            @click="currentPage--"
          >
            <span class="sr-only">上一頁</span>
          </button>
          <span>共{{ totalPages }}頁</span>
          <input
            type="number"
            :value="currentPage"
            class="pagination-input"
            min="1"
            :max="totalPages"
            @change="e => goToPage(parseInt(e.target.value))"
          />
          <span>/{{ totalPages }}頁</span>
          <button 
            class="pagination-btn"
            :disabled="currentPage >= totalPages"
            @click="currentPage++"
          >
            <span class="sr-only">下一頁</span>
          </button>
        </div>
        <div class="pagination-info">
          顯示第 {{ (currentPage - 1) * itemsPerPage + 1 }} 到 {{ Math.min(currentPage * itemsPerPage, searchResults.length) }} 筆，共 {{ searchResults.length }} 筆
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// State
const simpleSearchQuery = ref('');
const isAdvancedSearch = ref(false);
const advancedSearchConditions = ref([
  { field: 'title', operator: 'AND', value: '' },
]);
const searchResults = ref([]);
const searched = ref(false);
const currentPage = ref(1);
const pageSizes = ref([10, 25, 50]);
const itemsPerPage = ref(10);
const sortConfig = ref({
  field: 'title_asc'
});

// Mock book data
const books = [
  '哈利波特：神秘的魔法石', '哈利波特：消失的密室', '哈利波特：阿茲卡班的逃犯',
  '魔戒首部曲：魔戒現身', '魔戒二部曲：雙城奇謀', '魔戒三部曲：王者再臨',
  '三體', '三體Ⅱ黑暗森林', '三體Ⅲ死神永生', '挪威的森林',
  '1984', '美麗新世界', '動物農莊', '華氏451度',
  '追風箏的孩子', '群山回唱', '燦爛千陽', '小王子',
  '百年孤寂', '霍爾的移動城堡', '神隱少女', '天空之城'
].map((title, index) => {
  return {
    title,
    author: [
      'J.K. 羅琳', '托爾金', '劉慈欣', '村上春樹',
      '喬治·歐威爾', '赫胥黎', '卡勒德·胡賽尼', '安東尼·聖修伯里',
      '加西亞·馬奎斯', '宮崎駿'
    ][index % 10],
    isbn: `978${Math.random().toString().slice(2, 12)}`,
    publisher: ['技術出版社', '前端出版社', '基礎出版社', '進階出版社', '實戰出版社'][index % 5],
    year: `${2018 + (index % 6)}`,
    available: index % 3 === 0,
    reserved: false,
    favorite: false,
    coverUrl: null
  };
});

// Computed properties
const totalPages = computed(() => Math.ceil(searchResults.value.length / itemsPerPage.value));
const currentPageResults = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return sortedResults.value.slice(start, end);
});

const sortedResults = computed(() => {
  const [field, order] = sortConfig.value.field.split('_');
  return [...searchResults.value].sort((a, b) => {
    const modifier = order === 'asc' ? 1 : -1;
    if (a[field] < b[field]) return -1 * modifier;
    if (a[field] > b[field]) return 1 * modifier;
    return 0;
  });
});

// Methods
const toggleAdvancedSearch = () => {
  isAdvancedSearch.value = !isAdvancedSearch.value;
  if (!isAdvancedSearch.value) {
    advancedSearchConditions.value = [{ field: 'title', operator: 'AND', value: '' }];
    searchResults.value = [];
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

const performSimpleSearch = () => {
  const query = simpleSearchQuery.value.toLowerCase().trim();
  if (!query) {
    searchResults.value = [];
    searched.value = true;
    currentPage.value = 1;
    return;
  }
  searchResults.value = books.filter(book =>
    book.title.toLowerCase().includes(query) ||
    book.author.toLowerCase().includes(query) ||
    book.isbn.includes(query) ||
    book.publisher.toLowerCase().includes(query) ||
    book.year.includes(query)
  );
  searched.value = true;
  currentPage.value = 1;
};

const performAdvancedSearch = () => {
  let results = [...books];
  advancedSearchConditions.value.forEach((condition, index) => {
    const value = condition.value.toLowerCase().trim();
    if (!value) return;

    const filtered = results.filter(book => {
      const fieldValue = book[condition.field].toLowerCase();
      return fieldValue.includes(value);
    });

    if (condition.operator === 'AND' || index === 0) {
      results = filtered;
    } else if (condition.operator === 'OR') {
      results = [...new Set([...results, ...filtered])];
    } else if (condition.operator === 'NOT') {
      results = results.filter(book => !filtered.includes(book));
    }
  });
  searchResults.value = results;
  searched.value = true;
  currentPage.value = 1;
};

const toggleFavorite = (book) => {
  book.favorite = !book.favorite;
  if (book.favorite) {
    alert(`《${book.title}》已加入最愛！`);
  } else {
    alert(`《${book.title}》已從最愛移除！`);
  }
};

const goToPage = (page) => {
  const pageNum = parseInt(page);
  if (pageNum && !isNaN(pageNum) && pageNum >= 1 && pageNum <= totalPages.value) {
    currentPage.value = pageNum;
  }
};

watch(itemsPerPage, () => {
  currentPage.value = 1;
});
</script>

<style scoped>
.search-bg {
  padding: 24px 24px 100px 24px;
  background: transparent;
}

.search-title {
  font-size: 2rem;
  font-weight: bold;
  margin-bottom: 16px;
  color: #18181b;
}

.search-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
  background: transparent;
}
.container {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: auto;
}
.simple-search,
.advanced-search {
  max-width: 600px;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.search-bar input {
  flex: 1;
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
F
.favorite {
  background-color: #ccc; 
  color: #333;
}
.favorite:hover {
  background-color: #b3b3b3; 
}
.not-favorite {
  background-color: #dc3545; 
  color: white;
}
.not-favorite:hover {
  background-color: #b02a37;
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
}
.results {
  margin-top: 20px;
}
.result-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
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
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.result-control-panel {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
}
.result-control-panel-left {
  display: flex;
  align-items: center;
  gap: 32px;
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

.search-results {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>