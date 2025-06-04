<template>
   <div class="layout">
      <HeaderBar />

      <div class="content-body">
         <Sidebar v-model:isOpen="isSidebarOpen" />

         <button class="toggle-btn" @click="isSidebarOpen = !isSidebarOpen" :class="{ collapsed: !isSidebarOpen }">
            {{ isSidebarOpen ? '« 收合' : '» 展開' }}
         </button>

         <div :class="['main-container', { 'main--collapsed': !isSidebarOpen }]">
            <NuxtPage />
         </div>
      </div>

      <!-- ✅ Footer 移出 main-container -->
      <div :class="['footer-container', { 'footer--collapsed': !isSidebarOpen }]">
         <FooterBar />
      </div>
   </div>

</template>

<script setup>
import { ref } from 'vue'
import HeaderBar from '@/components/HeaderBar/index.vue'
import FooterBar from '@/components/FooterBar.vue'
import Sidebar from '@/components/Sidebar.vue'

const isSidebarOpen = ref(true)
</script>

<style>
.layout {
   display: flex;
   flex-direction: column;
   height: 100vh;
}

.content-body {
   display: flex;
   flex: 1;
   min-height: 0;
}


/* 內容區：Sidebar 對應的 margin */
.main-container {
   flex: 1;
   display: flex;
   flex-direction: column;
   margin-left: 50px;
   transition: margin-left 0.3s ease;
       /* 加入背景圖 */
   background-image: url('/public/images/bg-library.png'); /* 路徑可根據實際調整 */
   background-size: cover;        /* 背景圖填滿整個區域 */
   background-repeat: no-repeat;  /* 不重複 */
   background-position: center;   /* 圖片置中 */
}

.main--collapsed {
   margin-left: 0;
}

.main-container> :first-child {
   flex: 1;
   padding: 20px;
   overflow-y: auto;
}

.toggle-btn {
   position: fixed;
   top: 106px;
   left: 190px;
   /* 當 sidebar 展開時的位置 */
   z-index: 10000;
   background: linear-gradient(135deg, #ff9900, #ff3300);
   color: black;
   font-weight: bold;
   padding: 6px 12px;
   border: none;
   border-radius: 4px;
   cursor: pointer;
   transition: left 0.3s ease;
}

/* ✅ sidebar 收合時，button 貼齊畫面左邊 */
.toggle-btn.collapsed {
   left: 0;
}

/* ✅ Footer 樣式：跟 Sidebar 對齊 */
.footer-container {
   width: 100%;
   height: 40px;
   /* ✅ 可自行調整 footer 高度 */
   background-color: #f1f1f1;
   padding: 0;
   box-shadow: 0 -1px 5px rgba(0, 0, 0, 0.1);
   transition: margin-left 0.3s ease;
   margin-left: 190px;
   /* 配合 Sidebar 展開狀態 */
}

.footer--collapsed {
   margin-left: 0;
}
</style>