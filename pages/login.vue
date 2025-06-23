<script setup lang="ts">
definePageMeta({
  layout: 'blank',
})

import { useRouter } from 'vue-router'
import { useAuth } from '~/composables/useAuth'

const router = useRouter()
const { user, setUser } = useAuth()

const login = () => {
  // 設定為管理者
  const adminUser = {
    id: 1,
    name: '開發管理員',
    role: 'admin'
  }

  // 使用 setUser 方法保存用戶資訊
  setUser(adminUser)

  // 跳轉到管理者介面
  router.push('/manager')
}
</script>

<template>
  <LayoutAuth reverse>
    <div class="grid mx-auto max-w-sm gap-6">
      <div class="grid gap-2 text-center">
        <h1 class="text-2xl font-semibold tracking-tight">
          會員登入
        </h1>
      </div>
      <AuthSignIn />
    </div>
    <button @click="login" class="mt-4 px-4 py-2 bg-blue-700 text-white rounded hover:bg-blue-900 transition">
      管理者登入（開發用）
    </button>
  </LayoutAuth>
</template>
