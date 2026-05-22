import request from './request'

const ORDER_STATUS_TO_CODE = Object.freeze({
  PENDING: 0,
  PENDING_PAYMENT: 0,
  PAID: 1,
  PENDING_ACCEPT: 1,
  CONFIRMED: 2,
  ACCEPTED: 2,
  READY: 3,
  READY_FOR_PICKUP: 3,
  COMPLETED: 4,
  CANCELLED: 5,
  REJECTED: 5
})

const ORDER_CODE_TO_STATUS = Object.freeze({
  0: 'PENDING',
  1: 'PAID',
  2: 'CONFIRMED',
  3: 'READY',
  4: 'COMPLETED',
  5: 'CANCELLED'
})

function toStatusCode(status) {
  if (status === null || status === undefined || status === '') {
    return status
  }

  if (typeof status === 'number') {
    return status
  }

  const normalized = String(status).trim()
  if (normalized === '') {
    return ''
  }

  if (/^\d+$/.test(normalized)) {
    return Number(normalized)
  }

  return ORDER_STATUS_TO_CODE[normalized] ?? status
}

function toStatusText(status) {
  if (status === null || status === undefined) {
    return status
  }

  if (typeof status === 'number') {
    return ORDER_CODE_TO_STATUS[status] ?? String(status)
  }

  const normalized = String(status).trim()
  if (/^\d+$/.test(normalized)) {
    const numeric = Number(normalized)
    return ORDER_CODE_TO_STATUS[numeric] ?? normalized
  }

  return normalized
}

function normalizeOrder(order) {
  if (!order || typeof order !== 'object') {
    return order
  }

  return {
    ...order,
    status: toStatusText(order.status)
  }
}

function normalizeOrderResponse(res) {
  if (!res || typeof res !== 'object') {
    return res
  }

  const { data } = res
  if (Array.isArray(data)) {
    return {
      ...res,
      data: data.map(normalizeOrder)
    }
  }

  if (data && Array.isArray(data.records)) {
    return {
      ...res,
      data: {
        ...data,
        records: data.records.map(normalizeOrder)
      }
    }
  }

  if (data && typeof data === 'object') {
    return {
      ...res,
      data: normalizeOrder(data)
    }
  }

  return res
}

function normalizePageParams(params) {
  const requestParams = params ? { ...params } : {}
  if (Object.prototype.hasOwnProperty.call(requestParams, 'page') &&
    !Object.prototype.hasOwnProperty.call(requestParams, 'pageNum')) {
    requestParams.pageNum = requestParams.page
  }
  if (Object.prototype.hasOwnProperty.call(requestParams, 'size') &&
    !Object.prototype.hasOwnProperty.call(requestParams, 'pageSize')) {
    requestParams.pageSize = requestParams.size
  }
  delete requestParams.page
  delete requestParams.size
  return requestParams
}

// ==================== 濞戝牐鍨傞懓鍛伂 API ====================

// 閸掓稑缂撶拋銏犲礋
export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  }).then(normalizeOrderResponse)
}

// 閼惧嘲褰囬幋鎴犳畱鐠併垹宕熼崚妤勩€?
export function getMyOrders(params) {
  const requestParams = normalizePageParams(params)
  if (Object.prototype.hasOwnProperty.call(requestParams, 'status')) {
    requestParams.status = toStatusCode(requestParams.status)
  }

  return request({
    url: '/orders',
    method: 'get',
    params: requestParams
  }).then(normalizeOrderResponse)
}

// 閼惧嘲褰囩拋銏犲礋鐠囷附鍎?
export function getOrderDetail(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  }).then(normalizeOrderResponse)
}

// 閼惧嘲褰囩拋銏犲礋鐠囷附鍎忛敍鍫濆焼閸氬稄绱?
export function getOrderById(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  }).then(normalizeOrderResponse)
}

// 閺€顖欑帛鐠併垹宕熼敍鍫熌侀幏鐕傜礆
export function payOrder(id) {
  return request({
    url: `/orders/${id}/pay`,
    method: 'post'
  })
}

// 閸欐牗绉风拋銏犲礋
export function cancelOrder(id) {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'post'
  })
}

// 閼惧嘲褰囩拋銏犲礋閻樿埖鈧胶绮虹拋鈽呯礄閺囧じ鍞崗銊╁櫤閹峰褰囩拋鈩冩殶閿?
export function getOrderStats() {
  return request({
    url: '/orders/stats',
    method: 'get'
  })
}

// 閺嶆悂鏀㈢拋銏犲礋閿涘牊绉风拹纭呪偓鍛扮翻閸忋儴鍤滈幓鎰垳閿?
// ==================== 闂傘劌绨电粻锛勬倞閸?API ====================

// 閼惧嘲褰囬梻銊ョ暗鐠併垹宕熼崚妤勩€?
export function getStoreOrders(params) {
  const requestParams = normalizePageParams(params)
  if (Object.prototype.hasOwnProperty.call(requestParams, 'status')) {
    requestParams.status = toStatusCode(requestParams.status)
  }

  return request({
    url: '/store/orders',
    method: 'get',
    params: requestParams
  }).then(normalizeOrderResponse)
}

// 閹恒儱宕?
export function acceptOrder(id) {
  return request({
    url: `/store/orders/${id}/accept`,
    method: 'post'
  })
}

// 閹锋帒宕?
export function rejectOrder(id, data) {
  return request({
    url: `/store/orders/${id}/reject`,
    method: 'post',
    data
  })
}

// 閺嶅洩顔囨径鍥彛鐎瑰本鍨?
export function readyOrder(id) {
  return request({
    url: `/store/orders/${id}/ready`,
    method: 'post'
  })
}

// 绾喛顓荤拋銏犲礋
// 閺嶆悂鏀㈤崣鏍彛閿涘牓妫惔妤冾伂閿?
export function verifyPickup(pickupCode) {
  return request({
    url: '/store/orders/verify',
    method: 'post',
    params: { pickupCode: String(pickupCode || '').trim() }
  })
}
