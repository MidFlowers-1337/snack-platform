import request from './request'

/**
 * 获取操作日志列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} [params.module] - 操作模块
 * @param {string} [params.username] - 用户名
 * @param {string} [params.startTime] - 开始时间
 * @param {string} [params.endTime] - 结束时间
 * @returns {Promise} 操作日志分页列表
 */
export function getLogList(params) {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}