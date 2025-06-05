
<template>
  <div class="container">
    <h1>館藏查詢</h1>
    <!-- Simple Search -->
    <div v-if="!isAdvancedSearch" class="search-bar simple-search">
      <input
        v-model="simpleSearchQuery"
        @keyup.enter="performSimpleSearch"
        type="text"
        placeholder="輸入關鍵字..."
      />
      <button class="btn btn-primary" @click="performSimpleSearch">搜尋</button>
      <button class="btn btn-secondary" @click="toggleAdvancedSearch">進階搜尋</button>
    </div>

    <!-- Advanced Search -->
    <div v-else class="advanced-search">
      <div class="search-bar">
        <h2>進階搜尋</h2>
        <button class="btn btn-secondary" @click="toggleAdvancedSearch">返回單一搜尋</button>
      </div>
      <div v-for="(condition, index) in advancedSearchConditions" :key="index" class="condition">
        <select v-model="condition.field">
          <option value="title">書名</option>
          <option value="author">作者</option>
          <option value="isbn">ISBN</option>
          <option value="publisher">出版社</option>
          <option value="year">出版年</option>
        </select>
        <select v-model="condition.operator">
          <option value="AND">AND</option>
          <option value="OR">OR</option>
          <option value="NOT">NOT</option>
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
      <div v-for="book in currentPageResults" :key="book.isbn" class="result-item">
        <div class="result-info">
          <p><strong>書名:</strong> {{ book.title }}</p>
          <p><strong>作者:</strong> {{ book.author }}</p>
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
            class="btn"
            :class="book.reserved ? 'reserved-btn' : 'not-reserved'"
            @click="reserveBook(book)"
            :disabled="false"
          >
            預約
          </button>
        </div>
      </div>
      <!-- Pagination -->
      <div class="pagination">
        <span>查詢結果: {{ searchResults.length }}筆</span>
        <button @click="currentPage--" :disabled="currentPage === 1">上一頁</button>
        <span>第 {{ currentPage }} 頁 / 共 {{ totalPages }} 頁</span>
        <button @click="currentPage++" :disabled="currentPage === totalPages">下一頁</button>
      </div>
    </div>
    <div v-else-if="searched" class="no-results">
      無搜尋結果
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

// State
const simpleSearchQuery = ref('');
const isAdvancedSearch = ref(false);
const advancedSearchConditions = ref([
  { field: 'title', operator: 'AND', value: '' },
]);
const searchResults = ref([]);
const searched = ref(false);
const currentPage = ref(1);
const itemsPerPage = 5;

// Mock book data
const books = ref([
  { title: 'JavaScript 入門', author: '張三', isbn: '1234567890', publisher: '技術出版社', year: '2020', available: true, reserved: false, favorite: false },
  { title: 'Vue.js 實戰', author: '李四', isbn: '0987654321', publisher: '前端出版社', year: '2021', available: false, reserved: false, favorite: false },
  { title: 'Python 程式設計', author: '王五', isbn: '1122334455', publisher: '技術出版社', year: '2019', available: true, reserved: false, favorite: false },
  { title: 'HTML 基礎', author: '陳六', isbn: '9876543210', publisher: '基礎出版社', year: '2018', available: false, reserved: false, favorite: false },
  { title: 'CSS 進階', author: '趙七', isbn: '4567891230', publisher: '進階出版社', year: '2022', available: true, reserved: false, favorite: false },
  { title: 'Node.js 實戰', author: '孫八', isbn: '3216549870', publisher: '實戰出版社', year: '2023', available: false, reserved: false, favorite: false },
  { title: 'React 入門', author: '吳九', isbn: '6543219870', publisher: '技術出版社', year: '2020', available: true, reserved: false, favorite: false },
]);

// Computed properties
const totalPages = computed(() => Math.ceil(searchResults.value.length / itemsPerPage));
const currentPageResults = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return searchResults.value.slice(start, end);
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
  searchResults.value = books.value.filter(book =>
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
  let results = [...books.value];
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

const reserveBook = (book) => {
  book.reserved = !book.reserved;
  if (book.reserved) {
    alert(`已成功預約《${book.title}》！`);
  } else {
    alert(`已取消預約《${book.title}》！`);
  }
};
</script>

<style scoped>
body {
  font-family: Arial, sans-serif;
  background-color: #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  margin: 0;
  padding: 20px;
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
.reserved-btn {
  background-color: #ccc; 
  color: #333;
}
.reserved-btn:hover {
  background-color: #b3b3b3; 
}
.not-reserved {
  background-color: #007bff; 
  color: white;
}
.not-reserved:hover {
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
  align-items: center;
  padding: 10px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #ddd;
  margin: 0 50px;
}

.result-item:hover {
  background-color: #96c0fdbe; 
  transition: background-color 0.2s ease;
}

.result-item:last-child {
  border-bottom: none;
}
.result-info {
  flex: 1;
}
.result-info p {
  margin: 0;
  font-size: 14px;
}
.result-info p strong {
  margin-right: 5px;
}
.availability {
  color: green;
}
.unavailable {
  color: red;
}
.reserved {
  color: red;
}
.result-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}
.pagination span {
  font-size: 14px;
}
.pagination button {
  padding: 5px 10px;
  border: 1px solid #ccc;
  background-color: #fff;
  cursor: pointer;
  border-radius: 4px;
}
.pagination button:disabled {
  background-color: #f0f0f0;
  cursor: not-allowed;
}
.no-results {
  color: #6c757d;
  margin-top: 20px;
}
h2 {
  font-size: 20px;
  margin-bottom: 10px;
}
</style>
```