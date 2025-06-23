// 借閱API服務
// 統一管理所有借閱相關的API呼叫

import axios from 'axios'

const BASE_URL = 'http://localhost:8080'

// 建立axios實例
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 請求攔截器：自動添加token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 回應攔截器：統一處理錯誤
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('API 錯誤：', error)
    
    // 處理常見錯誤
    if (error.response?.status === 401) {
      // 未授權，清除 token 並跳轉到登入頁
      localStorage.removeItem('jwt_token')
      window.location.href = '/login'
    }
    
    return Promise.reject(error)
  }
)

// 借閱相關API
export const borrowApi = {
  /**
   * 借書
   * @param {number} bookId - 書籍ID
   * @returns {Promise} 借書結果
   */
  borrowBook: async (bookId) => {
    try {
      const response = await api.post('/api/borrows/borrow', { bookId })
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '借書失敗')
    }
  },

  /**
   * 還書
   * @param {number} borrowId - 借閱ID
   * @returns {Promise} 還書結果
   */
  returnBook: async (borrowId) => {
    try {
      const response = await api.post(`/api/borrows/return/${borrowId}`)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '還書失敗')
    }
  },

  /**
   * 續借
   * @param {number} borrowId - 借閱ID
   * @returns {Promise} 續借結果
   */
  renewBook: async (borrowId) => {
    try {
      const response = await api.post(`/api/borrows/renew/${borrowId}`)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '續借失敗')
    }
  },

  /**
   * 檢查是否可續借
   * @param {number} borrowId - 借閱ID
   * @returns {Promise} 檢查結果
   */
  checkRenewable: async (borrowId) => {
    try {
      const response = await api.get(`/api/borrows/renew/${borrowId}/check`)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '檢查續借資格失敗')
    }
  },

  /**
   * 獲取借閱歷史
   * @param {Object} options - 查詢選項
   * @param {boolean} options.withPagination - 是否使用分頁
   * @param {number} options.page - 頁碼
   * @param {number} options.size - 每頁大小
   * @returns {Promise} 借閱歷史
   */
  getBorrowHistory: async (options = {}) => {
    try {
      const params = new URLSearchParams()
      if (options.withPagination !== undefined) {
        params.append('withPagination', options.withPagination)
      }
      if (options.page) {
        params.append('page', options.page)
      }
      if (options.size) {
        params.append('size', options.size)
      }

      const response = await api.get(`/api/borrows/history?${params.toString()}`)
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '獲取借閱歷史失敗')
    }
  },

  /**
   * 獲取當前借閱
   * @returns {Promise} 當前借閱列表
   */
  getCurrentBorrows: async () => {
    try {
      const response = await api.get('/api/borrows/current')
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '獲取當前借閱失敗')
    }
  },

  /**
   * 獲取逾期借閱
   * @returns {Promise} 逾期借閱列表
   */
  getOverdueBorrows: async () => {
    try {
      const response = await api.get('/api/borrows/overdue')
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '獲取逾期借閱失敗')
    }
  },

  /**
   * 獲取借閱統計
   * @returns {Promise} 借閱統計資料
   */
  getBorrowStatistics: async () => {
    try {
      const response = await api.get('/api/borrows/statistics')
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '獲取借閱統計失敗')
    }
  },

  /**
   * 檢查借閱限制
   * @returns {Promise} 借閱限制資訊
   */
  checkBorrowLimits: async () => {
    try {
      const response = await api.get('/api/borrows/check-limits')
      return response.data
    } catch (error) {
      throw new Error(error.response?.data?.message || '檢查借閱限制失敗')
    }
  },

  /**
   * 批量借書
   * @param {Object} borrowData - 批量借書資料
   * @param {Array<Object>} borrowData.books - 書籍列表，每個物件包含 bookId, duration, location, method
   * @returns {Promise<any>}
   */
  borrowBooks: async (borrowData) => {
    try {
      const response = await api.post('/api/borrows/batch-borrow', borrowData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || '批量借書失敗');
    }
  }
}

// 工具函數
export const borrowUtils = {
  /**
   * 檢查是否可以續借
   * @param {string} dueDate - 到期日期
   * @returns {boolean} 是否可以續借
   */
  canRenew: (dueDate) => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const dueDateObj = new Date(dueDate)
    const diffTime = dueDateObj.getTime() - today.getTime()
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays <= 3 && diffDays > 0
  },

  /**
   * 檢查是否逾期
   * @param {string} dueDate - 到期日期
   * @returns {boolean} 是否逾期
   */
  isOverdue: (dueDate) => {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const dueDateObj = new Date(dueDate)
    return today > dueDateObj
  },

  /**
   * 格式化到期日顯示
   * @param {string} dueDate - 到期日期
   * @param {boolean} isReturned - 是否已歸還
   * @returns {string} 格式化後的日期
   */
  formatDueDate: (dueDate, isReturned) => {
    if (isReturned) {
      return '已歸還'
    }
    if (borrowUtils.isOverdue(dueDate)) {
      return `${dueDate} (逾期)`
    }
    return dueDate
  },

  /**
   * 取得按鈕文字
   * @param {Object} book - 書籍資訊
   * @returns {string} 按鈕文字
   */
  getButtonText: (book) => {
    if (book.isReturned) {
      return '已歸還'
    }
    if (borrowUtils.isOverdue(book.dueDate)) {
      return '已逾期'
    }
    if (book.renewCount >= 2) {
      return '已達上限'
    }
    if (!borrowUtils.canRenew(book.dueDate)) {
      return '尚未到續借時間'
    }
    return '續借'
  }
}

export default borrowApi 