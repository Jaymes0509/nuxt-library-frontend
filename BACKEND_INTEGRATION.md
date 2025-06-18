# 後端整合說明

## 您的後端配置

根據您提供的配置，您的 Spring Boot 後端已經設定完成：

### 資料庫配置
- **資料庫類型**: MySQL
- **主機**: localhost:3308
- **資料庫名稱**: library
- **用戶名**: root
- **密碼**: uTXa3ZqJ1DMHR9WypFboLgYcKsvfNe58

### 服務器配置
- **端口**: 8080
- **URL**: http://localhost:8080

## 前端配置

### 1. 環境變數設定

在專案根目錄建立 `.env` 檔案：

```env
# API 配置
API_BASE_URL=http://localhost:8080
API_SECRET=your-api-secret-here

# 應用程式配置
NODE_ENV=development
```

### 2. 已移除的檔案

我已經移除了以下不需要的檔案，因為您有完整的後端：

- `server/utils/database.ts` - 前端模擬資料庫
- `server/utils/mysql.js` - 前端 MySQL 連接
- `server/api/reservation-list/` - 前端模擬 API
- `server/api/books/[id]/reserve.ts` - 前端模擬預約 API

### 3. API 端點對應

前端現在會直接呼叫您的 Spring Boot 後端：

| 前端 API 呼叫 | 後端端點 |
|--------------|----------|
| `GET /api/bookreservations` | `GET /api/bookreservations?userId={userId}` |
| `POST /api/bookreservations` | `POST /api/bookreservations` |
| `POST /api/bookreservations/batch` | `POST /api/bookreservations/batch` |
| `DELETE /api/bookreservations/{id}` | `DELETE /api/bookreservations/{id}` |
| `DELETE /api/bookreservations/batch` | `DELETE /api/bookreservations/batch` |
| `PUT /api/bookreservations/{id}/status` | `PUT /api/bookreservations/{id}/status` |

## 啟動步驟

### 1. 啟動後端
```bash
# 在您的 Spring Boot 專案目錄
./mvnw spring-boot:run
```

### 2. 啟動前端
```bash
# 在 Nuxt 專案目錄
npm run dev
```

### 3. 訪問應用程式
- 前端: http://localhost:3000
- 後端 API: http://localhost:8080

## 資料庫表結構

根據您的配置，您應該有以下資料表：

### 預約相關表
```sql
-- 預約清單表
CREATE TABLE reservation_list (
  id INT AUTO_INCREMENT PRIMARY KEY,
  book_isbn VARCHAR(20) NOT NULL,
  book_title VARCHAR(255) NOT NULL,
  book_author VARCHAR(255) NOT NULL,
  book_publisher VARCHAR(255),
  user_id VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(20) DEFAULT 'pending'
);

-- 預約記錄表
CREATE TABLE reservations (
  id INT AUTO_INCREMENT PRIMARY KEY,
  reservation_id VARCHAR(50) NOT NULL,
  book_isbn VARCHAR(20) NOT NULL,
  user_id VARCHAR(50) NOT NULL,
  pickup_time DATETIME NOT NULL,
  pickup_location VARCHAR(100) NOT NULL,
  pickup_method VARCHAR(50) NOT NULL,
  status VARCHAR(20) DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 測試 API

您可以使用以下命令測試後端 API：

```bash
# 獲取預約清單
curl -X GET "http://localhost:8080/api/bookreservations?userId=test"

# 新增預約
curl -X POST "http://localhost:8080/api/bookreservations" \
  -H "Content-Type: application/json" \
  -d '{
    "bookIsbn": "9787115545381",
    "bookTitle": "JavaScript 高級程式設計",
    "bookAuthor": "Nicholas C. Zakas",
    "userId": "test"
  }'
```

## 注意事項

1. **CORS 設定**: 確保您的後端已設定 CORS 允許前端域名
2. **認證**: 目前使用固定的 `userId: 'current'`，實際部署時需要整合用戶認證
3. **錯誤處理**: 前端已包含完整的錯誤處理機制
4. **資料格式**: 前端會自動轉換資料格式以配合後端需求

## 下一步

1. 確保您的 Spring Boot 後端正在運行
2. 建立 `.env` 檔案並設定正確的 API 網址
3. 啟動前端開發伺服器
4. 測試完整的預約流程 