import request from './request'

// ==================== 公共 API ====================

// 获取分类列表
export function getCategories() {
  return request({
    url: '/categories',
    method: 'get'
  })
}

// 兼容旧命名：部分页面使用 getProducts
export function getProducts(params) {
  return getAdminProducts(params)
}

// ==================== 系统管理员 API ====================

// 获取分类列表（管理员）
export function getAdminCategories(params) {
  return request({
    url: '/admin/categories',
    method: 'get',
    params
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: '/admin/categories',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/admin/categories/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/admin/categories/${id}`,
    method: 'delete'
  })
}

// 获取商品列表（管理员）
export function getAdminProducts(params) {
  return request({
    url: '/admin/products',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'get'
  })
}

// 创建商品
export function createProduct(data) {
  return request({
    url: '/admin/products',
    method: 'post',
    data
  })
}

// 更新商品
export function updateProduct(id, data) {
  return request({
    url: `/admin/products/${id}`,
    method: 'put',
    data
  })
}

// 删除商品
export function deleteProduct(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete'
  })
}

// ==================== 消费者端 API ====================

// 获取热销商品推荐
export function getHotProducts(limit = 10) {
  return request({
    url: '/products/recommendations/hot',
    method: 'get',
    params: { limit }
  })
}