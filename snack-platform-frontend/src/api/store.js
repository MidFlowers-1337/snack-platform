import request from './request'

// ==================== 消费者端 API ====================

// 获取门店列表（消费者）
export function getStores() {
  return request({
    url: '/stores',
    method: 'get'
  })
}

// 获取门店详情
export function getStoreDetail(id) {
  return request({
    url: `/stores/${id}`,
    method: 'get'
  })
}

// 兼容旧命名：部分页面使用 getStoreById
export function getStoreById(id) {
  return getStoreDetail(id)
}

// 获取门店商品列表（消费者）
export function getStoreProducts(storeId, params) {
  return request({
    url: `/stores/${storeId}/products`,
    method: 'get',
    params
  })
}

// ==================== 系统管理员 API ====================

// 获取门店列表（管理员）
export function getAdminStores(params) {
  return request({
    url: '/admin/stores',
    method: 'get',
    params
  })
}

// 创建门店
export function createStore(data) {
  return request({
    url: '/admin/stores',
    method: 'post',
    data
  })
}

// 更新门店
export function updateStore(id, data) {
  return request({
    url: `/admin/stores/${id}`,
    method: 'put',
    data
  })
}

// 删除门店
export function deleteStore(id) {
  return request({
    url: `/admin/stores/${id}`,
    method: 'delete'
  })
}