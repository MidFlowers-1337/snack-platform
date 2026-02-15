import request from './request'

// ==================== 管理端 API ====================

// 分页查询促销活动
export function getPromotions(params) {
  return request({
    url: '/admin/promotions',
    method: 'get',
    params
  })
}

// 创建促销活动
export function createPromotion(data) {
  return request({
    url: '/admin/promotions',
    method: 'post',
    data
  })
}

// 更新促销活动
export function updatePromotion(id, data) {
  return request({
    url: `/admin/promotions/${id}`,
    method: 'put',
    data
  })
}

// 删除促销活动
export function deletePromotion(id) {
  return request({
    url: `/admin/promotions/${id}`,
    method: 'delete'
  })
}

// ==================== 消费者端 API ====================

// 获取进行中的促销活动
export function getActivePromotions() {
  return request({
    url: '/promotions/active',
    method: 'get'
  })
}
