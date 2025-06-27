<script setup lang="ts">
import { Loader2 } from 'lucide-vue-next'
import PasswordInput from '@/components/PasswordInput.vue'
import axios from 'axios'

const email = ref('')
const password = ref('')
const isLoading = ref(false)

async function onSubmit(event: Event) {
  event.preventDefault()
  if (!email.value || !password.value)
    return

  isLoading.value = true

  // setTimeout(() => {
  //   if (email.value === 'demo@gmail.com' && password.value === 'password')
  //     navigateTo('/')

  //   isLoading.value = false
  // }, 3000)


  try {
    // 呼叫後端登入 API
    const res = await axios.post('http://localhost:8080/api/auth/login', {
      email: email.value,
      password: password.value
    })
    // 登入成功，儲存 token
    const token = res.data.token
    const user = res.data.user
    localStorage.setItem('jwt_token', token)
    localStorage.setItem('user', JSON.stringify(user))

    // 跳轉到首頁或會員頁
    window.location.href = '/' // 或用 navigateTo('/')
  } catch (err: any) {
    alert('登入失敗：' + (err.response?.data?.message || err.message))
  } finally {
    isLoading.value = false
  }

}
</script>

<template>
  <form class="grid gap-6" @submit="onSubmit">
    <Separator label="Or continue with" />
    <div class="grid gap-2">
      <Label for="email">
        Email
      </Label>
      <Input id="email" v-model="email" type="email" placeholder="name@example.com" :disabled="isLoading"
        auto-capitalize="none" auto-complete="email" auto-correct="off" />
    </div>
    <br>
    <div class="grid gap-2">
      <div class="flex items-center">
        <Label for="password">
          Password :
        </Label>
      </div>
      <PasswordInput id="password" v-model="password" />
    </div>


    <br>
    <Button type="submit" class="w-full" :disabled="isLoading">
      <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
      登入
    </Button>
    <br><br><br>

    <div class="mt-4 text-center text-sm text-muted-foreground">
      還沒有帳號嗎?
      <NuxtLink to="/card-application" class="underline">
        申請帳號
      </NuxtLink>
    </div>
    <div class="flex justify-end">
      <NuxtLink to="/forgot-password" class="ml-auto inline-block text-sm underline">
        忘記密碼
      </NuxtLink>
    </div>
  </form>

</template>

<style scoped></style>
