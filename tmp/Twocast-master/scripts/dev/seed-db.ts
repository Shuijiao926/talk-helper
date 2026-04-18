#!/usr/bin/env npx tsx

/**
 * 数据库种子脚本
 * 用于在开发环境中初始化测试数据
 * 
 * 使用方法：
 * npx tsx scripts/dev/seed-db.ts
 * 或者：
 * yarn script:seed-db
 */

import { logger } from '../utils/logger'
import { ensureInitialized, handleError, exit } from '../utils'

// 可以导入项目中的模型和服务
// import { db } from '@/db'
// import { users, posts } from '@/db/schema'

async function seedDatabase() {
  try {
    logger.info('开始数据库种子脚本...')
    
    // 确保环境正确初始化
    await ensureInitialized()
    
    logger.progress('正在清理现有数据...')
    // 这里添加清理逻辑
    // await db.delete(posts)
    // await db.delete(users)
    
    logger.progress('正在创建用户数据...')
    // 这里添加创建用户逻辑
    const users = [
      { name: 'John Doe', email: 'john@example.com' },
      { name: 'Jane Smith', email: 'jane@example.com' },
    ]
    
    for (let i = 0; i < users.length; i++) {
      const user = users[i]
      logger.progress(`创建用户: ${user.name}`, i + 1, users.length)
      // await db.insert(users).values(user)
      
      // 模拟异步操作
      await new Promise(resolve => setTimeout(resolve, 100))
    }
    
    logger.progress('正在创建文章数据...')
    // 这里添加创建文章逻辑
    
    logger.success('数据库种子脚本执行成功！')
    exit(0, '所有测试数据已创建完成')
    
  } catch (error) {
    handleError(error, 'seed-db script')
  }
}

// 如果脚本被直接执行
if (require.main === module) {
  seedDatabase()
} 