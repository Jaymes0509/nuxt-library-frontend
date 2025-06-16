<template>
   <div class="layout">
      <HeaderBar />

      <div class="content-body">
         <Sidebar v-model:isOpen="isSidebarOpen" />

         <div :class="['main-container', { 'main--collapsed': !isSidebarOpen }]">
            <NuxtPage />
         </div>
      </div>

      <!-- ✅ Footer 移出 main-container -->
      <div :class="['footer-container', { 'footer--collapsed': !isSidebarOpen }]">
         <FooterBar />
      </div>

        <!-- 讀者登入按鈕 -->
      <MemberLoginButton />
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
   background-image: url('/public/images/bg-library.png');
   background-size: cover;
   background-repeat: no-repeat;
   background-position: center;
}

/* 內容區：Sidebar 對應的 margin */
.main-container {
   flex: 1;
   display: flex;
   flex-direction: column;
   margin-left: 50px;
   transition: margin-left 0.3s ease;
}

.main--collapsed {
   margin-left: 0;
}

.main-container> :first-child {
   flex: 1;
   padding: 20px;
   overflow-y: auto;
}

/* ✅ Footer 樣式：跟 Sidebar 對齊 */
.footer-container {
   width: 100%;
   height: 40px;
   background-color: #f1f1f1;
   padding: 0;
   box-shadow: 0 -1px 5px rgba(0, 0, 0, 0.1);
   transition: margin-left 0.3s ease;
   margin-left: 190px;
}

.footer--collapsed {
   margin-left: 0;
}
</style>