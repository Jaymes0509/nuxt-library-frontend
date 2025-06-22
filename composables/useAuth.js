// frontend/nuxt-library-frontend/composables/useAuth.js
import { ref } from 'vue'

const user = ref(JSON.parse(localStorage.getItem('user')) || null)

export function useAuth() {
    // 登入方法
    async function login(account, password) {
        // 這裡用 fetch 或 axios 呼叫後端登入 API
        const res = await fetch('/api/login', {
            method: 'POST',
            body: JSON.stringify({ account, password }),
            headers: { 'Content-Type': 'application/json' }
        })
        const data = await res.json()
        user.value = data // 假設 data 裡有 role
    }

    // 登出方法
    function logout() {
        user.value = null
    }

    function setUser(u) {
        user.value = u
        localStorage.setItem('user', JSON.stringify(u))
    }

    return { user, login, logout, setUser }
}