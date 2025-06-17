<template>
  <div class="reader-login-btn" @click="goToLogin" aria-label="讀者登入" role="button">
    <img src="/public/images/user.png" alt="User Icon" class="user-icon" />
    <div class="label">讀者登入</div>
  </div>
</template>

<script setup>
const goToLogin = () => {
  navigateTo('/login') //  將這裡換成你的登入頁路由
}
</script>

<style scoped>
.reader-login-btn {
  position: fixed;
  bottom: 5rem;
  right: 3rem;
  border-radius: 50%;
  width: 60px;
  height: 60px;
  text-align: center;
  font-size: 24px;
  line-height: 60px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  z-index: 1000;
  transition: transform 0.2s;
}

.reader-login-btn:hover {
  transform: scale(1.05);
}

.reader-login-btn .label {
  position: absolute;
  font-weight: bold;
  font-size: 16px;
  /*  字體加大一點 */
  bottom: -18px;
  left: 50%;
  transform: translate(-50%, 30%);
  /* 同時 X, Y 位移 */
  color: black;
  white-space: nowrap;
}

.user-icon {
  width: 60px;
  height: 60px;
}

/* 手機尺寸（寬度小於 600px） */
@media (max-width: 600px) {
  .reader-login-btn .label {
    font-size: 12px;
    bottom: -20px;
  }

  .reader-login-btn {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
}

@media (min-width: 1200px) {
  .reader-login-btn .label {
    font-size: 18px;
    bottom: -26px;
  }
}
</style>
