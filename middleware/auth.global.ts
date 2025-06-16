export default defineNuxtRouteMiddleware((to) => {
  const isLoggedIn = useCookie('authToken').value
  const protectedPaths = ['/seat-reservation2']

  if (!isLoggedIn && protectedPaths.includes(to.path)) {
    return navigateTo(`/login?redirect=${to.fullPath}`)
  }
})
