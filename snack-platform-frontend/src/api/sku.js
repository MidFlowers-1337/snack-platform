import request from './request'

// ==================== 消费者端 API ====================

// 获取指定门店的SKU列表
export function getStoreSkus(storeId, params) {
  return request({
    url: `/stores/${storeId}/skus`,
    method: 'get',
    params
  })
}

// 获取指定门店的单个SKU详情
export function getStoreSku(storeId, skuId) {
  return request({
    url: `/stores/${storeId}/skus/${skuId}`,
    method: 'get'
  })
}

// ==================== 门店管理员 API ====================

// 获取本店SKU列表
export function getMyStoreSkus(params) {
  return request({
    url: '/store/skus',
    method: 'get',
    params
  })
}

// 添加商品到门店
export function createStoreSku(data) {
  return request({
    url: '/store/skus',
    method: 'post',
    data
  })
}

// 更新门店SKU（价格、库存、状态）
export function updateStoreSku(id, data) {
  return request({
    url: `/store/skus/${id}`,
    method: 'put',
    data
  })
}

// 删除门店SKU
export function deleteStoreSku(id) {
  return request({
    url: `/store/skus/${id}`,
    method: 'delete'
  })
}