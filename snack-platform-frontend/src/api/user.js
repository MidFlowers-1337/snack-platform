import request from './request'

// ==================== 系统管理员 API ====================

/**
 * 获取用户列表（管理员）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.username - 用户名（可选，模糊搜索）
 * @param {string} params.role - 角色（可选，精确匹配）
 * @returns {Promise} 用户分页列表
 */
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

/**
 * 更新用户状态（启用/禁用）
 * @param {number} id - 用户ID
 * @param {boolean} enabled - 是否启用
 * @returns {Promise} 操作结果
 */
export function updateUserStatus(id, enabled) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data: { enabled }
  })
}

/**
 * 更新用户角色
 * @param {number} id - 用户ID
 * @param {string} role - 新角色（CONSUMER/STORE_ADMIN/SYSTEM_ADMIN）
 * @returns {Promise} 操作结果
 */
export function updateUserRole(id, role) {
  return request({
    url: `/admin/users/${id}/role`,
    method: 'put',
    data: { role }
  })
}