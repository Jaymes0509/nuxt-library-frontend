export default defineNuxtRouteMiddleware((to, from) => {
  const isLoggedIn = useCookie('authToken').value  // 假設你用 cookie 儲存登入狀態

  if (!isLoggedIn && to.path === '/seat-reservation2') {
    return navigateTo('/login')
  }
})
