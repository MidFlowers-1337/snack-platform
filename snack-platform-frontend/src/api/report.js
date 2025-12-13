import request from './request'

// ==================== 门店管理员 API ====================

// 获取门店仪表盘数据
export function getStoreDashboard() {
  return request({
    url: '/store/report/dashboard',
    method: 'get'
  })
}

// 获取门店销售报表
export function getStoreSalesReport(params) {
  return request({
    url: '/store/report/sales',
    method: 'get',
    params
  })
}

// ==================== 系统管理员 API ====================

// 获取平台仪表盘数据
export function getAdminDashboard() {
  return request({
    url: '/admin/report/dashboard',
    method: 'get'
  })
}

// 获取平台销售报表
export function getAdminSalesReport(params) {
  return request({
    url: '/admin/report/sales',
    method: 'get',
    params
  })
}

// 兼容旧命名：部分页面使用 getSalesReport
export function getSalesReport(params) {
  return getAdminSalesReport(params)
}