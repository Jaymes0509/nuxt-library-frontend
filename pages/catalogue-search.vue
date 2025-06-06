<template>
  <div class="search-bg">
    <h1 class="search-title">館藏查詢</h1>
    <div class="search-main">
      <!-- 控制面板 -->
      <div class="search-control-panel">
        <div class="search-control-panel-left">
          <!-- 簡單搜尋 -->
          <div v-if="!isAdvancedSearch" class="search-row">
            <input
              v-model="simpleSearchQuery"
              @keyup.enter="performSimpleSearch"
              type="text"
              placeholder="輸入關鍵字..."
              class="search-input"
            />
            <button class="search-btn search-btn-primary" @click="performSimpleSearch">搜尋</button>
            <button class="search-btn search-btn-secondary" @click="toggleAdvancedSearch">進階搜尋</button>
          </div>

          <!-- 進階搜尋 -->
          <div v-else class="advanced-search">
            <div class="search-row">
              <h2>進階搜尋</h2>
              <button class="search-btn search-btn-secondary" @click="toggleAdvancedSearch">返回單一搜尋</button>
            </div>
            <div v-for="(condition, index) in advancedSearchConditions" :key="index" class="search-condition">
              <select v-model="condition.field" class="search-select">
                <option value="title">書名</option>
                <option value="author">作者</option>
                <option value="isbn">ISBN</option>
                <option value="publisher">出版社</option>
                <option value="year">出版年</option>
              </select>
              <select v-model="condition.operator" class="search-select">
                <option value="AND">AND</option>
                <option value="OR">OR</option>
                <option value="NOT">NOT</option>
              </select>
              <input v-model="condition.value" type="text" placeholder="輸入搜尋內容" class="search-input" />
              <button
                v-if="index > 0"
                class="search-btn search-btn-danger"
                @click="removeCondition(index)"
              >
                移除
              </button>
            </div>
            <div class="search-row">
              <button
                class="search-btn search-btn-primary"
                :class="{ 'search-btn-disabled': advancedSearchConditions.length >= 6 }"
                @click="addCondition"
              >
                新增條件
              </button>
              <button class="search-btn search-btn-primary" @click="performAdvancedSearch">搜尋</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜尋結果 -->
      <div v-if="currentPageResults.length" class="search-results">
        <div v-for="book in currentPageResults" :key="book.isbn" class="search-result-item">
          <div class="search-result-info">
            <h3 class="search-result-title">{{ book.title }}</h3>
            <p class="search-result-author">{{ book.author }}</p>
            <div class="search-result-details">
              <p>出版年：{{ book.year }}</p>
              <p>ISBN：{{ book.isbn }}</p>
              <p>
                在架狀態：
                <span :class="book.available ? 'availability' : 'unavailable'">
                  {{ book.available ? '是' : '否' }}
                </span>
              </p>
            </div>
            <div class="search-result-actions">
              <button
                class="search-btn"
                :class="book.favorite ? 'favorite' : 'not-favorite'"
                @click="toggleFavorite(book)"
              >
                最愛
              </button>
              <button
                class="search-btn"
                :class="book.reserved ? 'reserved-btn' : 'not-reserved'"
                @click="reserveBook(book)"
                :disabled="false"
              >
                詳情
              </button>
            </div>
          </div>
        </div>

        <!-- 分頁控制 -->
        <div class="search-pagination">
          <div class="search-pagination-controls">
            <button 
              class="search-pagination-btn"
              :disabled="currentPage === 1"
              @click="currentPage--"
            >
              上一頁
            </button>
            <span>共{{ totalPages }}頁</span>
            <input
              type="number"
              :value="currentPage"
              class="search-pagination-input"
              min="1"
              :max="totalPages"
              @change="e => currentPage = parseInt(e.target.value) || 1"
            />
            <span>/{{ totalPages }}頁</span>
            <button 
              class="search-pagination-btn"
              :disabled="currentPage >= totalPages"
              @click="currentPage++"
            >
              下一頁
            </button>
          </div>
          <div class="search-pagination-info">
            顯示第 {{ (currentPage - 1) * itemsPerPage + 1 }} 到 {{ Math.min(currentPage * itemsPerPage, searchResults.length) }} 筆，共 {{ searchResults.length }} 筆
          </div>
        </div>
      </div>
      <div v-else-if="searched" class="search-no-results">
        無搜尋結果
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// State
const simpleSearchQuery = ref('');
const isAdvancedSearch = ref(false);
const advancedSearchConditions = ref([
  { field: 'title', operator: 'AND', value: '' },
]);
const searchResults = ref([]);
const searched = ref(false);
const currentPage = ref(1);
const itemsPerPage = 10;

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

const reserveBook = (book) => {
  router.push({
    path: `/${book.isbn}`,
    query: {
      title: book.title,
      author: book.author,
      coverUrl: book.coverUrl
    }
  });
};
</script>

<style scoped>
.search-bg {
  padding: 24px 24px 100px 24px;
  background: #fff;
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
}

.search-control-panel {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.search-control-panel-left {
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

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 8px 16px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  background: #fff;
  color: #18181b;
}

.search-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.search-select {
  min-width: 120px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 1rem;
  background: #fff;
  color: #18181b;
  cursor: pointer;
}

.search-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.search-btn-primary {
  background: #2563eb;
  color: #fff;
}

.search-btn-primary:hover {
  background: #1d4ed8;
}

.search-btn-secondary {
  background: transparent;
  color: #2563eb;
  text-decoration: underline;
}

.search-btn-secondary:hover {
  color: #1d4ed8;
}

.search-btn-danger {
  background: #dc2626;
  color: #fff;
}

.search-btn-danger:hover {
  background: #b91c1c;
}

.search-btn-disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.search-condition {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-result-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  transition: all 0.2s;
}

.search-result-item:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.search-result-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-result-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #18181b;
  margin: 0;
}

.search-result-author {
  color: #6b7280;
  margin: 0;
}

.search-result-details {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin: 8px 0;
}

.search-result-details p {
  margin: 0;
  color: #4b5563;
}

.search-result-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.availability {
  color: #16a34a;
  font-weight: 500;
}

.unavailable {
  color: #dc2626;
  font-weight: 500;
}

.favorite {
  background: #16a34a;
  color: #fff;
}

.not-favorite {
  background: #6b7280;
  color: #fff;
}

.reserved-btn {
  background: #dc2626;
  color: #fff;
}

.not-reserved {
  background: #2563eb;
  color: #fff;
}

.search-pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.search-pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-pagination-btn {
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

.search-pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.search-pagination-btn:hover {
  background: #f3f4f6;
}

.search-pagination-input {
  height: 32px;
  width: 48px;
  text-align: center;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  color: #18181b;
  background: #fff;
}

/* 隱藏 Chrome, Safari, Edge, Opera 的箭頭 */
.search-pagination-input::-webkit-outer-spin-button,
.search-pagination-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 隱藏 Firefox 的箭頭 */
.search-pagination-input[type=number] {
  -moz-appearance: textfield;
}

.search-pagination-info {
  font-size: 0.95rem;
  color: #4b5563;
  text-align: center;
}

.search-no-results {
  text-align: center;
  padding: 48px;
  color: #6b7280;
  font-size: 1.1rem;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .search-control-panel {
    flex-direction: column;
  }

  .search-result-details {
    flex-direction: column;
    gap: 8px;
  }
}

@media (max-width: 640px) {
  .search-bg {
    padding: 16px;
  }

  .search-row {
    flex-direction: column;
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .search-btn {
    width: 100%;
  }

  .search-condition {
    flex-direction: column;
  }

  .search-select {
    width: 100%;
  }

  .search-pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .search-title {
    font-size: 1.5rem;
  }

  .search-result-actions {
    flex-direction: column;
  }
}
</style>