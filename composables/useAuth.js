// frontend/nuxt-library-frontend/composables/useAuth.js
import { ref } from 'vue'

// 從 localStorage 讀取用戶資訊
const getStoredUser = () => {
  try {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  } catch (e) {
    console.error('解析用戶資訊失敗：', e)
    return null
  }
}

const user = ref(getStoredUser())

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
        setUser(data) // 使用 setUser 方法保存用戶資訊
    }

    // 登出方法
    function logout() {
        user.value = null
        localStorage.removeItem('user')
        localStorage.removeItem('jwt_token')
        localStorage.removeItem('authToken')
    }

    function setUser(u) {
        user.value = u
        localStorage.setItem('user', JSON.stringify(u))
        // 觸發 storage 事件，讓其他組件知道用戶狀態改變
        window.dispatchEvent(new StorageEvent('storage', {
            key: 'user',
            newValue: JSON.stringify(u)
        }))
    }

    // 檢查登入狀態
    function checkLoginStatus() {
        const storedUser = getStoredUser()
        if (storedUser) {
            user.value = storedUser
            return true
        }
        return false
    }

    return { user, login, logout, setUser, checkLoginStatus }
}