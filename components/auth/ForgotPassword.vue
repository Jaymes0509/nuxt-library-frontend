<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { Loader2 } from 'lucide-vue-next'

const isLoading = ref(false)
const email = ref('')

async function onSubmit(event: Event) {
  event.preventDefault()
  if (!email.value) {
    alert('請輸入 Email')
    return
  }
  isLoading.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/member/request-password-reset', {
      email: email.value
    })
    alert(res.data || '密碼重置信已寄出，請至信箱查收')
  } catch (e: any) {
    alert(e?.response?.data || '寄信失敗，請確認 Email 是否正確')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <form @submit="onSubmit">
    <div class="grid gap-4">
      <div class="grid gap-2">
        <Label for="email">
          Email :
        </Label>
        <Input id="email" v-model="email" placeholder="name@example.com" type="email" auto-capitalize="none"
          auto-complete="email" auto-correct="off" :disabled="isLoading" />
        <Button :disabled="isLoading" style="margin-top: 10px;">
          <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
          發送
        </Button>
      </div>
    </div>
  </form>
</template>

<style scoped></style>
