import request from './request'

/**
 * 获取当前用户购物车列表
 */
export function getCartList() {
  return request({
    url: '/cart',
    method: 'get'
  })
}

/**
 * 添加商品到购物车
 * @param {Object} data - { storeId, skuId, quantity }
 */
export function addToCart(data) {
  return request({
    url: '/cart',
    method: 'post',
    data
  })
}

/**
 * 更新购物车项数量
 * @param {number} id - 购物车项ID
 * @param {Object} data - { quantity }
 */
export function updateCartItem(id, data) {
  return request({
    url: `/cart/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除购物车项
 * @param {number} id - 购物车项ID
 */
export function removeCartItem(id) {
  return request({
    url: `/cart/${id}`,
    method: 'delete'
  })
}

/**
 * 清空指定门店的购物车
 * @param {number} storeId - 门店ID
 */
export function clearStoreCart(storeId) {
  return request({
    url: `/cart/clear/${storeId}`,
    method: 'delete'
  })
}
