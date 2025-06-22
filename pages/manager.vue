<script setup>
import { useAuth } from '~/composables/useAuth'
const { user } = useAuth()
const router = useRouter()

</script>

<template>
    <div v-if="user && user.role === 'admin'" class="w-full flex flex-col items-center gap-8">
        <div class="w-full flex flex-col items-center gap-8">
            <h1 class="text-3xl font-bold">管理介面</h1>
            <div class="flex gap-6">
                <NuxtLink to="/manager/books">
                    <button class="px-6 py-3 bg-primary text-white rounded-lg shadow hover:bg-primary/80 transition">
                        書籍資訊
                    </button>
                </NuxtLink>
                <NuxtLink to="/manager/accounts">
                    <button class="px-6 py-3 bg-primary text-white rounded-lg shadow hover:bg-primary/80 transition">
                        帳號資訊
                    </button>
                </NuxtLink>
                <NuxtPage />
            </div>
        </div>
    </div>
    <div v-else>
        <p>您沒有權限瀏覽此頁面。</p>
    </div>

</template>

<style scoped>
/* 你可以根據需要調整按鈕樣式 */
</style>