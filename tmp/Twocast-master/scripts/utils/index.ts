/**
 * 脚本公共工具函数
 * 这个文件包含所有脚本共用的工具函数
 */

import { config } from 'dotenv'
import path from 'path'

// 加载环境变量
config({ path: path.resolve(process.cwd(), '.env.local') })
config({ path: path.resolve(process.cwd(), '.env') })

/**
 * 异步延迟函数
 * @param ms 延迟毫秒数
 */
export const delay = (ms: number): Promise<void> => {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 退出进程的工具函数
 * @param code 退出码，默认为 0
 * @param message 退出消息
 */
export const exit = (code: number = 0, message?: string): never => {
  if (message) {
    if (code === 0) {
      console.log(`✅ ${message}`)
    } else {
      console.error(`❌ ${message}`)
    }
  }
  process.exit(code)
}

/**
 * 确保数据库连接等资源正确初始化
 */
export const ensureInitialized = async (): Promise<void> => {
  // 这里可以添加数据库连接检查等逻辑
  // 例如检查环境变量是否正确配置
  const requiredEnvs = ['DATABASE_URL']
  
  for (const env of requiredEnvs) {
    if (!process.env[env]) {
      exit(1, `Missing required environment variable: ${env}`)
    }
  }
}

/**
 * 统一的错误处理函数
 */
export const handleError = (error: unknown, context?: string): never => {
  const message = error instanceof Error ? error.message : String(error)
  const contextStr = context ? ` in ${context}` : ''
  console.error(`❌ Error${contextStr}:`, message)
  if (error instanceof Error && error.stack) {
    console.error(error.stack)
  }
  process.exit(1)
} 