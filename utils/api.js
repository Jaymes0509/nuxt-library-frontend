import axios from 'axios'

// 獲取 Nuxt 配置
const config = useRuntimeConfig()

// 建立 axios 實例
const api = axios.create({
  baseURL: config.public.apiBase || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 請求攔截器
api.interceptors.request.use(
  (config) => {
    // 可以在這裡添加認證 token
    const token = localStorage.getItem('authToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 回應攔截器
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('API 錯誤：', error)
    
    // 處理常見錯誤
    if (error.response?.status === 401) {
      // 未授權，清除 token 並跳轉到登入頁
      localStorage.removeItem('authToken')
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

// 預約相關 API
export const reservationAPI = {
  // 獲取預約清單 - 修改為可選參數
  getReservationList: (userId) => {
    if (userId) {
      return api.get('/api/bookreservations', { params: { userId } })
    } else {
      return api.get('/api/bookreservations')
    }
  },
  
  // 獲取當前用戶的預約清單
  getCurrentUserReservations: () => api.get('/api/bookreservations/current'),
  
  // 獲取預約歷史記錄
  getReservations: (userId) => {
    if (userId) {
      return api.get('/api/bookreservations/history', { params: { userId } })
    } else {
      return api.get('/api/bookreservations/history')
    }
  },
  
  // 新增單本預約
  addReservation: (data) => api.post('/api/bookreservations', data),
  
  // 批量預約
  batchReservation: (data) => {
    console.log(JSON.stringify(data, null, 2))
    return api.post('/api/bookreservations/batch', data)
  },
  
  // 刪除預約
  deleteReservation: (id) => api.delete(`/api/bookreservations/${id}`),
  
  // 批量刪除預約
  batchDeleteReservation: (ids) => api.delete('/api/bookreservations/batch', { data: { reservationIds: ids } }),
  
  // 更新預約狀態
  updateReservationStatus: (id, status) => api.put(`/api/bookreservations/${id}/status`, { status }),

  // 新增預約日誌
  addReservationLog: (data) => {
    return api.post('/api/reservation-logs', data)
  },

  // 確認預約
  confirmReservation: (data) => {
    return api.post('/api/reservations/confirm', data)
  },

  // 取得使用者的預約日誌
  getReservationLogs: (userId) => {
    return api.get('/api/reservation-logs', { params: { userId } })
  },

  // 刪除預約日誌
  deleteReservationLog: (logId) => {
    return api.delete(`/api/reservation-logs/${logId}`)
  },

  // 批量刪除預約日誌
  batchDeleteReservationLogs: (logIds) => {
    return api.delete('/api/reservation-logs/batch', { data: { logIds } })
  }
}

// 書籍相關 API
export const bookAPI = {
  // 獲取書籍列表
  getBooks: (params) => api.get('/api/books', { params }),
  
  // 獲取書籍詳情（根據 ID）
  getBookDetail: (id) => api.get(`/api/books/${id}`),
  
  // 根據 ISBN 獲取書籍詳情
  getBookByIsbn: (isbn) => api.get(`/api/books/isbn/${isbn}`),
  
  // 搜尋書籍
  searchBooks: (query) => api.get('/api/books/search', { params: { q: query } })
}

// 用戶相關 API
export const userAPI = {
  // 獲取當前用戶資訊
  getCurrentUser: () => api.get('/api/users/current'),
  
  // 登入
  login: (credentials) => api.post('/api/auth/login', credentials),
  
  // 登出
  logout: () => api.post('/api/auth/logout')
}

export default api 