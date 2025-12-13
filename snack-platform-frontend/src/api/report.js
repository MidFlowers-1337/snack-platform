import request from './request'

// ==================== 门店管理员 API ====================

// 获取门店仪表盘数据
export function getStoreDashboard() {
  return request({
    url: '/store/dashboard',
    method: 'get'
  })
}

// 获取门店销售报表
export function getStoreSalesReport(params) {
  return request({
    url: '/store/reports/sales',
    method: 'get',
    params
  })
}

// ==================== 系统管理员 API ====================

// 获取平台仪表盘数据
export function getAdminDashboard() {
  return request({
    url: '/admin/dashboard',
    method: 'get'
  })
}

// 获取平台销售报表
export function getAdminSalesReport(params) {
  return request({
    url: '/admin/reports/sales',
    method: 'get',
    params
  })
}