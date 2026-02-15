/**
 * 常量定义和状态映射工具
 */

/**
 * 订单状态文本映射
 */
export const ORDER_STATUS_TEXT = {
  'PENDING': '待支付',
  'PAID': '已支付',
  'CONFIRMED': '已确认',
  'READY': '待取货',
  'COMPLETED': '已完成',
  'CANCELLED': '已取消',
  'REJECTED': '已拒绝'
}

/**
 * 订单状态 Tag 类型映射（Element Plus Tag type）
 */
export const ORDER_STATUS_TYPE = {
  'PENDING': 'warning',
  'PAID': 'primary',
  'CONFIRMED': 'primary',
  'READY': 'success',
  'COMPLETED': 'success',
  'CANCELLED': 'info',
  'REJECTED': 'danger'
}

/**
 * 获取订单状态文本
 * @param {string} status - 订单状态码
 * @returns {string} 状态文本
 */
export function getStatusText(status) {
  return ORDER_STATUS_TEXT[status] || status
}

/**
 * 获取订单状态 Tag 类型
 * @param {string} status - 订单状态码
 * @returns {string} Element Plus Tag type
 */
export function getStatusType(status) {
  return ORDER_STATUS_TYPE[status] || 'info'
}

/**
 * 角色文本映射
 */
export const ROLE_TEXT = {
  'CONSUMER': '普通用户',
  'STORE_ADMIN': '门店管理员',
  'SYSTEM_ADMIN': '系统管理员'
}

/**
 * 获取角色文本
 * @param {string} role - 角色代码
 * @returns {string} 角色文本
 */
export function getRoleText(role) {
  return ROLE_TEXT[role] || role
}
