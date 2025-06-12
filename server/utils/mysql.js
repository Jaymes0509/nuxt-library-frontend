import mysql from 'mysql2/promise';
import { useRuntimeConfig } from '#imports'

const config = useRuntimeConfig()

// 創建連接池
const pool = mysql.createPool({
  host: config.mysqlHost,
  port: config.mysqlPort,
  user: config.mysqlUser,
  password: config.mysqlPassword,
  database: config.mysqlDatabase,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  timezone: '+08:00'
});

export default pool; 