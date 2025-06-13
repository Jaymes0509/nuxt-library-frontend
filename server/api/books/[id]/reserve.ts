import { defineEventHandler, readBody, createError } from 'h3'

export default defineEventHandler(async (event) => {
  try {
    const body = await readBody(event)
    const bookId = event.context.params.id

    // 驗證必要欄位
    if (!body.time || !body.location || !body.method) {
      throw createError({
        statusCode: 400,
        statusMessage: '缺少必要欄位'
      })
    }

    // 這裡應該添加資料庫操作，目前先模擬成功
    const reservation = {
      id: Math.random().toString(36).substring(7),
      bookId,
      time: body.time,
      location: body.location,
      method: body.method,
      status: 'pending',
      createdAt: new Date().toISOString()
    }

    // 將預約信息存儲到全局變量中（實際應該使用資料庫）
    if (!global.reservations) {
      global.reservations = []
    }
    global.reservations.push(reservation)

    return {
      status: 'success',
      data: reservation
    }
  } catch (error) {
    console.error('預約失敗：', error)
    throw createError({
      statusCode: error.statusCode || 500,
      statusMessage: error.statusMessage || '預約失敗'
    })
  }
}) 