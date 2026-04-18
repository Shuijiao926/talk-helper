/**
 * 时间工具函数
 */

/**
 * 格式化时间为 MM:SS 或 HH:MM:SS 格式
 * @param time 时间（秒）
 * @returns 格式化后的时间字符串
 */
export function formatTime(time: number): string {
  if (!time || isNaN(time)) return '0:00';
  
  const hours = Math.floor(time / 3600);
  const minutes = Math.floor((time % 3600) / 60);
  const seconds = Math.floor(time % 60);
  
  if (hours > 0) {
    // 如果超过1小时，显示 HH:MM:SS 格式
    return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  } else {
    // 否则显示 MM:SS 格式
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  }
}

/**
 * 格式化时间为简洁格式（用于显示总时长）
 * @param time 时间（秒）
 * @returns 格式化后的时间字符串
 */
export function formatDuration(time: number): string {
  if (!time || isNaN(time)) return 'N/A';
  
  const hours = Math.floor(time / 3600);
  const minutes = Math.floor((time % 3600) / 60);
  
  if (hours > 0) {
    return `${hours}h${minutes}m`;
  } else {
    return `${minutes}m`;
  }
} 