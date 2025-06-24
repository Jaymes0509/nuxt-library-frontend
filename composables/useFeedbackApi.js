  // composables/useFeedbackApi.js

  // 查詢所有留言
  export async function getAllFeedback() {
    try {
      const { data, error } = await useFetch('http://localhost:8080/api/feedback/all', {
        method: 'GET'
      })

      if (error.value) {
        console.error('載入留言失敗', error.value)
        return []
      }

      return data.value
    } catch (err) {
      console.error('載入留言時發生錯誤', err)
      return []
    }
  }

  // 回覆留言
  export async function replyFeedback({ id, reply, status }) {
    try {
      const { data, error } = await useFetch('http://localhost:8080/api/feedback/reply', {
        method: 'POST',
        body: { id, reply, status }
      })

      if (error.value) {
        return { success: false, message: error.value.data || '未知錯誤' }
      }

      return { success: true }
    } catch (err) {
      return { success: false, message: err.message }
    }
  }
