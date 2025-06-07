
<template>
    <div class="book-detail-wrapper">
      <!-- 直接顯示範例資料，左半邊：封面圖片 -->
      <div class="cover-area">
        <img
          class="cover-image"
          :src="book.coverImage"
          :alt="`書籍《${book.title}》封面`"
        />
      </div>
  
      <!-- 右半邊：文字內容區 -->
      <div class="info-area">
        <!-- 書名 -->
        <h1 class="book-title">{{ book.title }}</h1>
  
        <!-- 內容簡介 -->
        <section class="section-block">
          <h2>內容簡介</h2>
          <p>{{ book.summary }}</p>
        </section>
  
        <!-- 作者介紹 -->
        <section class="section-block">
          <h2>作者介紹</h2>
          <p>{{ book.authorBio }}</p>
        </section>
  
        <!-- 詳細資料 -->
        <section class="section-block">
          <h2>詳細資料</h2>
          <ul class="details-list">
            <li><strong>ISBN：</strong>{{ book.details.ISBN }}</li>
            <li><strong>出版社：</strong>{{ book.details.publisher }}</li>
            <li><strong>出版日期：</strong>{{ book.details.publishDate }}</li>
            <li><strong>頁數：</strong>{{ book.details.pageCount }} 頁</li>
          </ul>
        </section>
  
        <!-- 預約按鈕 -->
        <div class="action-area">
          <button class="reserve-btn" @click="reserveBook">
            📖 預約此書
          </button>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: 'BookDetail',
    data() {
      return {
        // 把封面換成指定的圖片
        book: {
          title: 'Vue 全端開發實戰',
          coverImage: 'https://cdnec.sanmin.com.tw/product_images/626/626324909.jpg',
          summary:
            '本書帶你從零開始學習 Vue.js，逐步建構一個完整的全端專案。內容包含元件化思維、Vue Router 路由設計、Vuex 狀態管理，以及如何用 Node.js 搭建後端 API 等，最後整合成可上線的範例應用。',
          authorBio:
            '作者：張三，資深前端工程師，擁有 8 年 Vue.js 開發經驗，曾在多家知名公司擔任技術顧問與講師，並發表多篇 Vue.js 系列教學文章。',
          details: {
            ISBN: '978-986-1234-56-7',
            publisher: '前端文化出版社',
            publishDate: '2023-09-15',
            pageCount: 432
          }
        }
      }
    },
    methods: {
      reserveBook() {
        // 先示範 alert 提示
        alert(`已成功預約：《${this.book.title}》`)
      }
    }
  }
  </script>
  
  <style scoped>
  /* 容器最外層：flex 佈局，手機先上下堆疊 */
  .book-detail-wrapper {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    margin: 1rem;
  }
  
  /* 左側封面圖片區 */
  .cover-area {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: flex-start;
  }
  
  .cover-image {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  }
  
  /* 右側文字資訊區 */
  .info-area {
    flex: 2;
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  /* 書名樣式 */
  .book-title {
    margin: 0;
    font-size: 1.75rem;
    font-weight: bold;
    color: #333;
  }
  
  /* 每個區段標題與內容 */
  .section-block h2 {
    margin: 0 0 0.5rem;
    font-size: 1.25rem;
    color: #555;
  }
  
  .section-block p {
    margin: 0;
    line-height: 1.6;
    color: #444;
  }
  
  /* 詳細資料列表 */
  .details-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  .details-list li {
    margin-bottom: 0.5rem;
    line-height: 1.5;
    color: #444;
  }
  
  /* 動作區域：放置預約按鈕 */
  .action-area {
    margin-top: 1rem;
    display: flex;
    justify-content: flex-start;
  }
  
  .reserve-btn {
    background-color: #1976d2;
    color: #fff;
    border: none;
    border-radius: 4px;
    padding: 0.6rem 1.2rem;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.2s ease;
  }
  
  .reserve-btn:hover {
    background-color: #155a9c;
  }
  
  /* RWD：寬度 ≥ 768px 時左右並列 */
  @media screen and (min-width: 768px) {
    .book-detail-wrapper {
      flex-direction: row;
    }
    .cover-area,
    .info-area {
      align-items: flex-start;
    }
    .cover-area {
      max-width: 40%;
    }
    .info-area {
      max-width: 60%;
    }
  }
  </style>
  