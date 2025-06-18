<template>
    <div>
        <h2>搜尋結果：{{ route.query.q }}</h2>
        <!-- 實際結果可接後端 API 顯示結果列表 -->
        <p>（此處顯示與 "{{ route.query.q }}" 相關的書籍/資訊）</p>
    </div>
</template>

<script setup>
const route = useRoute()
// 可在這裡發送 API 取得搜尋結果
</script>
