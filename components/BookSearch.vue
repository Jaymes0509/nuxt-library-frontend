<template>
  <div class="w-full p-4 mx-auto">
    <!-- 搜尋卡片 -->
    <div ref="searchCard" class="mb-8 transition-all duration-300 max-w-4xl mx-auto">
      <Card>
        <CardHeader>
          <CardTitle>圖書館藏書搜尋</CardTitle>
        </CardHeader>
        <CardContent>
          <!-- 搜尋條件列表 -->
          <div
            v-for="(condition, index) in searchConditions"
            :key="index"
            class="mb-4 flex items-center space-x-2"
          >
            <!-- 邏輯運算符（從第二個條件開始顯示） -->
            <Select v-if="index > 0" v-model="condition.operator" class="w-24">
              <SelectTrigger>
                <SelectValue placeholder="邏輯" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="AND">AND</SelectItem>
                <SelectItem value="OR">OR</SelectItem>
                <SelectItem value="NOT">NOT</SelectItem>
              </SelectContent>
            </Select>

            <!-- 欄位下拉 -->
            <Select v-model="condition.field">
              <SelectTrigger>
                <SelectValue placeholder="選擇欄位" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">所有欄位</SelectItem>
                <SelectItem value="title">書名</SelectItem>
                <SelectItem value="author">作者</SelectItem>
                <SelectItem value="isbn">ISBN</SelectItem>
                <SelectItem value="category">分類</SelectItem>
                <SelectItem value="publisher">出版社</SelectItem>
                <SelectItem value="year">出版年</SelectItem>
              </SelectContent>
            </Select>

            <!-- 關鍵字輸入 -->
            <Input v-model="condition.value" placeholder="輸入關鍵字" />

            <!-- 刪除條件按鈕 -->
            <Button
              variant="destructive"
              @click="removeCondition(index)"
              :disabled="searchConditions.length === 1"
            >
              <Trash2Icon class="h-5 w-5" />
            </Button>
          </div>

          <!-- 新增條件、搜尋按鈕 -->
          <div class="flex items-center space-x-2">
            <Button @click="addCondition">新增搜尋條件</Button>
            <Button @click="search">
              <SearchIcon class="h-5 w-5" />
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>

    <!-- 搜尋結果表格 + 分頁選項 -->
    <div v-if="results.length" class="mt-4">
      <!-- 1. 分頁設定：每頁顯示筆數下拉 -->
      <div class="flex items-center justify-between mb-2">
        <!-- 每頁顯示幾筆：可自行調整 options 內容 -->
        <div class="flex items-center space-x-2">
          <span>每頁顯示：</span>
          <select
            v-model.number="itemsPerPage"
            class="border rounded px-2 py-1"
            @change="currentPage = 1"
          >
            <option :value="5">5 筆</option>
            <option :value="10">10 筆</option>
            <option :value="20">20 筆</option>
            <option :value="50">50 筆</option>
          </select>
        </div>

        <!-- 總共幾頁 & 總筆數提示 -->
        <div class="text-sm text-gray-500">
          共 {{ results.length }} 筆 &nbsp;｜&nbsp; 共 {{ totalPages }} 頁 &nbsp;｜
          目前第 {{ currentPage }} 頁
        </div>
      </div>

      <!-- 2. 實際表格：改為用 paginatedResults -->
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead @click="sort('title')" class="cursor-pointer">
              書名
              <span v-if="sortKey === 'title'">
                {{ sortOrder === 'asc' ? '↑' : '↓' }}
              </span>
            </TableHead>
            <TableHead @click="sort('author')" class="cursor-pointer">
              作者
              <span v-if="sortKey === 'author'">
                {{ sortOrder === 'asc' ? '↑' : '↓' }}
              </span>
            </TableHead>
            <TableHead @click="sort('publisher')" class="cursor-pointer">
              出版社
              <span v-if="sortKey === 'publisher'">
                {{ sortOrder === 'asc' ? '↑' : '↓' }}
              </span>
            </TableHead>
            <TableHead>出版年</TableHead>
            <TableHead>ISBN</TableHead>
            <TableHead>在架</TableHead>
            <TableHead>預約人數</TableHead>
            <TableHead>操作</TableHead>
            <TableHead>最愛</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <!-- 這裡改成 paginatedResults -->
          <TableRow v-for="result in paginatedResults" :key="result.id">
            <TableCell>{{ result.title }}</TableCell>
            <TableCell>{{ result.author }}</TableCell>
            <TableCell>{{ result.publisher }}</TableCell>
            <TableCell>{{ result.year }}</TableCell>
            <TableCell>{{ result.isbn }}</TableCell>
            <TableCell>
              <!-- 只有當 reservations 為 0 且 available 為 true 才顯示「是」，否則顯示「否」 -->
              {{ (result.reservations === 0 && result.available) ? '是' : '否' }}
            </TableCell>
            <TableCell>{{ result.reservations }}</TableCell>
            <TableCell>
              <Button :disabled="!(result.reservations === 0 && result.available)" @click="reserveBook(result.id)">
                預約
              </Button>
            </TableCell>
            <TableCell>
              <Button variant="ghost" @click="toggleFavorite(result.id)">
                <HeartIcon
                  :class="{ 'fill-red-500': result.isFavorite }"
                  class="h-5 w-5"
                />
              </Button>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>

      <!-- 3. 分頁按鈕列：上一頁 / 頁碼 / 下一頁 -->
      <div class="mt-4 flex items-center justify-center space-x-2">
        <!-- 上一頁 -->
        <Button
          size="sm"
          :disabled="currentPage === 1"
          @click="currentPage--"
        >
          上一頁
        </Button>

        <!-- 動態生成頁碼按鈕 -->
        <template v-for="page in totalPages" :key="page">
          <Button
            size="sm"
            variant="outline"
            :class="{'bg-blue-500 text-white': currentPage === page}"
            @click="currentPage = page"
          >
            {{ page }}
          </Button>
        </template>

        <!-- 下一頁 -->
        <Button
          size="sm"
          :disabled="currentPage === totalPages"
          @click="currentPage++"
        >
          下一頁
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
} from '@/components/card';
import { Button } from '@/components/button';
import { Input } from '@/components/input';
import {
  Select,
  SelectTrigger,
  SelectValue,
  SelectContent,
  SelectItem,
} from '@/components/select';
import {
  Table,
  TableHeader,
  TableRow,
  TableHead,
  TableBody,
  TableCell,
} from '@/components/table';
import {
  SearchIcon,
  HeartIcon,
  Trash2Icon,
} from 'lucide-vue-next';

const searchCard = ref(null);

// 原本的搜尋條件維護
const searchConditions = ref([
  { field: 'title', value: '', operator: null },
]);

// 所有資料、搜尋結果、排序設定
const libraryData = ref(
  Array.from({ length: 100 }, function(_, i) {
    return {
      id: i + 1,
      title: '書名 ' + (i + 1),
      author: '作者 ' + (i % 5),
      isbn: '9780000' + i,
      category: ['文學', '程式設計', '歷史'][i % 3],
      publisher: '出版社 ' + (i % 4),
      year: 2000 + (i % 24),
      available: (i % 2 === 0),
      reservations: (i % 3),
      isFavorite: (i % 7 === 0),
    };
  })
);
const results = ref([]); // 存放篩選後的搜尋結果

// 排序的關鍵欄位和方向
const sortKey = ref('title');
const sortOrder = ref('asc');

// －－ 新增分頁相關的 －－
// 每頁顯示筆數（可由下拉綁定）
const itemsPerPage = ref(10);

// 目前所在頁碼（從 1 開始）
const currentPage = ref(1);

// 計算「排序後但尚未分頁」的陣列
const sortedResults = computed(function() {
  if (!sortKey.value) {
    return results.value;
  }
  return results.value.slice().sort(function(a, b) {
    const va = (a[sortKey.value] || '').toString().toLowerCase();
    const vb = (b[sortKey.value] || '').toString().toLowerCase();
    if (sortOrder.value === 'asc') {
      return va.localeCompare(vb);
    } else {
      return vb.localeCompare(va);
    }
  });
});

// 計算總頁數
const totalPages = computed(function() {
  return Math.ceil(results.value.length / itemsPerPage.value) || 1;
});

// 計算「分頁後要顯示」的資料陣列
const paginatedResults = computed(function() {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  return sortedResults.value.slice(start, start + itemsPerPage.value);
});

// －－ 原本的函式（微調：search 時把 currentPage 重設） －－
function addCondition() {
  if (searchConditions.value.length < 6) {
    searchConditions.value.push({ field: 'title', value: '', operator: 'AND' });
  }
}

function removeCondition(index) {
  if (searchConditions.value.length > 1) {
    searchConditions.value.splice(index, 1);
  }
}

function sort(key) {
  if (sortKey.value === key) {
    sortOrder.value = (sortOrder.value === 'asc') ? 'desc' : 'asc';
  } else {
    sortKey.value = key;
    sortOrder.value = 'asc';
  }
}

function reserveBook(id) {
  // 找到對應的書籍，並且只有在 reservations === 0 且 available === true 時才允許預約
  const book = libraryData.value.find(function(b) { return b.id === id; });
  if (book && book.reservations === 0 && book.available) {
    book.available = false;
    book.reservations++;
    search(); // 執行完「預約」後也要重新篩選一次以更新結果
  }
}

function toggleFavorite(id) {
  const book = libraryData.value.find(function(b) { return b.id === id; });
  if (book) {
    book.isFavorite = !book.isFavorite;
  }
}

function search() {
  // 每次搜尋都把 currentPage 重設為 1
  currentPage.value = 1;

  const conditions = searchConditions.value;
  results.value = libraryData.value.filter(function(book) {
    var result = true;
    for (var i = 0; i < conditions.length; i++) {
      var field = conditions[i].field;
      var value = conditions[i].value;
      var operator = conditions[i].operator;

      if (!value) {
        continue;
      }

      var match;
      if (field === 'all') {
        // 全欄位比對
        match = Object.values(book).some(function(v) {
          return v.toString().toLowerCase().includes(value.toLowerCase());
        });
      } else {
        match = book[field]
          .toString()
          .toLowerCase()
          .includes(value.toLowerCase());
      }

      if (i === 0) {
        result = match;
      } else if (operator === 'AND') {
        result = result && match;
      } else if (operator === 'OR') {
        result = result || match;
      } else if (operator === 'NOT') {
        result = result && !match;
      }
    }
    return result;
  });

  // 滾到搜尋卡片頂端
  searchCard.value.scrollIntoView({ behavior: 'smooth' });
}

// 如果你想在 itemsPerPage 改變時，也把 currentPage 重設，可以用 watch：
watch(itemsPerPage, function() {
  currentPage.value = 1;
});
</script>

<style scoped>
/* 這裡可以添加自訂樣式 */
</style>
