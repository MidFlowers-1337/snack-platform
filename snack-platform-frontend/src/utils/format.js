/**
 * 时间格式化工具函数
 */

/**
 * 格式化时间为本地化字符串
 * @param {string|Date} time - 时间字符串或 Date 对象
 * @returns {string} 格式化后的时间字符串
 */
export function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

/**
 * 格式化日期时间（同 formatTime，语义别名）
 * @param {string|Date} dateTime - 时间字符串或 Date 对象
 * @returns {string} 格式化后的时间字符串
 */
export function formatDateTime(dateTime) {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

/**
 * 格式化金额（保留两位小数）
 * @param {number} amount - 金额
 * @returns {string} 格式化后的金额字符串
 */
export function formatMoney(amount) {
  if (amount == null) return '0.00'
  return Number(amount).toFixed(2)
}

/**
 * 格式化日期为 YYYY-MM-DD
 * @param {string|Date} date - 日期
 * @returns {string}
 */
export function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
