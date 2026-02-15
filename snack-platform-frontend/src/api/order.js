import request from './request'

// ==================== 消费者端 API ====================

// 创建订单
export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

// 获取我的订单列表
export function getMyOrders(params) {
  return request({
    url: '/orders',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}

// 获取订单详情（别名）
export function getOrderById(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}

// 支付订单（模拟）
export function payOrder(id) {
  return request({
    url: `/orders/${id}/pay`,
    method: 'post'
  })
}

// 取消订单
export function cancelOrder(id) {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'post'
  })
}

// 获取订单状态统计（替代全量拉取计数）
export function getOrderStats() {
  return request({
    url: '/orders/stats',
    method: 'get'
  })
}

// 核销订单（消费者输入自提码）
export function verifyOrder(id, data) {
  return request({
    url: `/orders/${id}/verify`,
    method: 'post',
    data
  })
}

// ==================== 门店管理员 API ====================

// 获取门店订单列表
export function getStoreOrders(params) {
  return request({
    url: '/store/orders',
    method: 'get',
    params
  })
}

// 接单
export function acceptOrder(id) {
  return request({
    url: `/store/orders/${id}/accept`,
    method: 'post'
  })
}

// 拒单
export function rejectOrder(id, data) {
  return request({
    url: `/store/orders/${id}/reject`,
    method: 'post',
    data
  })
}

// 标记备货完成
export function readyOrder(id) {
  return request({
    url: `/store/orders/${id}/ready`,
    method: 'post'
  })
}

// 确认订单
export function confirmOrder(id) {
  return request({
    url: `/store/orders/${id}/confirm`,
    method: 'post'
  })
}

// 核销取货（门店端）
export function verifyPickup(pickupCode) {
  return request({
    url: '/store/orders/verify',
    method: 'post',
    data: { pickupCode }
  })
}