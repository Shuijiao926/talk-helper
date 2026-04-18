/**
 * 脚本专用日志工具
 * 提供彩色输出和结构化日志
 */

export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3,
}

interface LoggerOptions {
  level: LogLevel
  prefix?: string
}

class ScriptLogger {
  private level: LogLevel
  private prefix: string

  constructor(options: LoggerOptions = { level: LogLevel.INFO }) {
    this.level = options.level
    this.prefix = options.prefix || 'SCRIPT'
  }

  private getTimestamp(): string {
    return new Date().toISOString()
  }

  private formatMessage(level: string, message: string, meta?: any): string {
    const timestamp = this.getTimestamp()
    const metaStr = meta ? ` ${JSON.stringify(meta)}` : ''
    return `[${timestamp}] [${this.prefix}] [${level}] ${message}${metaStr}`
  }

  debug(message: string, meta?: any): void {
    if (this.level <= LogLevel.DEBUG) {
      console.log(`🔍 ${this.formatMessage('DEBUG', message, meta)}`)
    }
  }

  info(message: string, meta?: any): void {
    if (this.level <= LogLevel.INFO) {
      console.log(`ℹ️  ${this.formatMessage('INFO', message, meta)}`)
    }
  }

  warn(message: string, meta?: any): void {
    if (this.level <= LogLevel.WARN) {
      console.warn(`⚠️  ${this.formatMessage('WARN', message, meta)}`)
    }
  }

  error(message: string, meta?: any): void {
    if (this.level <= LogLevel.ERROR) {
      console.error(`❌ ${this.formatMessage('ERROR', message, meta)}`)
    }
  }

  success(message: string, meta?: any): void {
    console.log(`✅ ${this.formatMessage('SUCCESS', message, meta)}`)
  }

  progress(message: string, current?: number, total?: number): void {
    const progressStr = current !== undefined && total !== undefined 
      ? ` (${current}/${total})` 
      : ''
    console.log(`🔄 ${this.formatMessage('PROGRESS', message + progressStr)}`)
  }
}

// 创建默认日志实例
export const logger = new ScriptLogger({
  level: process.env.NODE_ENV === 'development' ? LogLevel.DEBUG : LogLevel.INFO,
  prefix: 'SCRIPT'
})

// 导出创建自定义日志器的函数
export const createLogger = (options: LoggerOptions): ScriptLogger => {
  return new ScriptLogger(options)
} 