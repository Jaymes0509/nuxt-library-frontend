// 使用 mitt 全域事件總線:在互不相屬的元件（例如不同頁面、不同功能模組）之間，建立輕量、非侵入式的溝通橋樑。
// 學生取消預約在一個頁面: 沒辦法直接叫 admin 那邊的方法
// 管理者管理座位在另一頁面: 無法透過 props/ref 通訊
// 但兩者需要同步更新: 就需要事件通知
import mitt from 'mitt'
export const eventBus = mitt()