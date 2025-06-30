<template>
    <div class="search-result-page">
        <h2>🔍 搜尋結果："{{ keyword }}"</h2>
        <div v-if="pending">載入中...</div>
        <div v-else-if="data?.length">
            <ul>
                <li v-for="item in data" :key="item.url">
                    <NuxtLink v-if="item.type === '書籍' && item.isbn" :to="`/books?isbn=${item.isbn}`">
                        <strong>[{{ item.type }}]</strong> {{ item.title }}
                    </NuxtLink>


                    <NuxtLink v-else :to="item.url">
                        <strong>[{{ item.type }}]</strong> {{ item.title }}
                    </NuxtLink>
                </li>

            </ul>
        </div>
        <div v-else>❌ 沒有找到相關資料</div>
    </div>
</template>

<script setup>
const route = useRoute()
const keyword = ref(route.query.keyword || '')
const data = ref([])
const pending = ref(true)


const fetchSearchResults = async () => {
    pending.value = true
    try {
        const res = await $fetch('http://localhost:8080/api/search', {
            query: { keyword: keyword.value }
        })
        data.value = res
    } catch (err) {
        console.error('❌ 搜尋失敗：', err)
        data.value = []
    } finally {
        pending.value = false
    }
}

// 初始載入
await fetchSearchResults()

// 監聽 keyword 變動（URL 變更時）
watch(() => route.query.keyword, async (newKeyword) => {
    keyword.value = newKeyword || ''
    await fetchSearchResults()
})
</script>

<style>
.search-result-page {
    text-align: center;
}

.search {
    display: flex;
    align-items: center;
    gap: 8px;
}

.search-input {
    padding: 6px 10px;
    font-size: 16px;
    flex-grow: 1;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.search-icon {
    background-color: transparent;
    border: none;
    font-size: 20px;
    cursor: pointer;
}

li {
    font-size: large;
    line-height: 3;
    list-style: none;
}
</style>
