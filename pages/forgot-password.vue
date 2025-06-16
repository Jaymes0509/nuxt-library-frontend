<script setup lang="ts">
definePageMeta({
  layout: 'blank',
})
</script>

<template>



  <div class="flex flex-col items-center justify-center gap-6 bg-muted p-6 min-h-svh md:p-10">
    <div class="max-w-sm w-full flex flex-col gap-6" style="background-color: rgba(255, 255, 255, 0.2);">
      <Card>
        <CardHeader class="text-center">
          <CardTitle class="text-xl">
            <h1>忘記密碼</h1>
          </CardTitle>
          <CardDescription>
            請輸入您的電子郵件地址，我們將發送重置密碼的連結至您的信箱。
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="grid mx-auto max-w-sm gap-6">
            <AuthForgotPassword />
            <p class="text-center text-sm text-muted-foreground">
              已經有帳號了嗎？
              <NuxtLink to="/login" class="underline underline-offset-4 hover:text-primary">
                登入
              </NuxtLink>
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<style scoped></style>
