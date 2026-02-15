import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartList, addToCart, updateCartItem, removeCartItem, clearStoreCart } from '@/api/cart'
import { useUserStore } from './user'

export const useCartStore = defineStore('cart', () => {
  // 购物车项 { id, skuId, productName, productImage, price, quantity, stock, storeId, storeName }
  const items = ref([])
  // 当前选择的门店ID
  const currentStoreId = ref(localStorage.getItem('currentStoreId') || null)
  // 加载状态
  const loading = ref(false)

  // 计算属性
  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalAmount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  // 兼容旧属性名
  const totalPrice = computed(() => totalAmount.value)

  const isEmpty = computed(() => items.value.length === 0)

  /**
   * 判断是否已登录
   */
  function isLoggedIn() {
    const userStore = useUserStore()
    return userStore.isLoggedIn
  }

  /**
   * 从 localStorage 加载购物车（未登录降级模式）
   */
  function loadFromStorage() {
    try {
      const stored = localStorage.getItem('cart')
      items.value = stored ? JSON.parse(stored) : []
    } catch (e) {
      console.warn('Failed to parse cart from localStorage:', e)
      items.value = []
      localStorage.removeItem('cart')
    }
  }

  /**
   * 保存到 localStorage（未登录降级模式）
   */
  function saveToStorage() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  /**
   * 设置当前门店
   */
  function setCurrentStore(storeId) {
    if (currentStoreId.value !== storeId) {
      // 切换门店时清空本地购物车
      items.value = []
      saveToStorage()
    }
    currentStoreId.value = storeId
    localStorage.setItem('currentStoreId', storeId)
  }

  /**
   * 从后端加载购物车（登录后调用）
   */
  async function fetchCart() {
    if (!isLoggedIn()) {
      loadFromStorage()
      return
    }
    loading.value = true
    try {
      const res = await getCartList()
      items.value = res.data || []
    } catch (error) {
      console.error('获取购物车失败:', error)
      // 后端失败时降级到本地缓存
      loadFromStorage()
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加商品到购物车
   */
  async function addItem(item) {
    if (isLoggedIn()) {
      try {
        await addToCart({
          storeId: currentStoreId.value || item.storeId,
          skuId: item.skuId,
          quantity: 1
        })
        // 同步刷新
        await fetchCart()
      } catch (error) {
        console.error('添加购物车失败:', error)
        throw error
      }
    } else {
      // 未登录：本地 localStorage 模式
      const existingItem = items.value.find(i => i.skuId === item.skuId)
      if (existingItem) {
        if (existingItem.quantity < item.stock) {
          existingItem.quantity++
        }
      } else {
        items.value.push({
          skuId: item.skuId,
          productName: item.productName,
          productImage: item.productImage,
          price: item.price,
          quantity: 1,
          stock: item.stock,
          storeId: currentStoreId.value || item.storeId
        })
      }
      saveToStorage()
    }
  }

  /**
   * 更新商品数量
   */
  async function updateQuantity(skuId, quantity) {
    if (isLoggedIn()) {
      const item = items.value.find(i => i.skuId === skuId)
      if (!item) return
      try {
        await updateCartItem(item.id, { quantity })
        // 本地同步更新
        item.quantity = quantity
      } catch (error) {
        console.error('更新购物车失败:', error)
        throw error
      }
    } else {
      const item = items.value.find(i => i.skuId === skuId)
      if (item) {
        if (quantity <= 0) {
          removeItemLocal(skuId)
        } else if (quantity <= item.stock) {
          item.quantity = quantity
          saveToStorage()
        }
      }
    }
  }

  /**
   * 移除商品（本地）
   */
  function removeItemLocal(skuId) {
    const index = items.value.findIndex(i => i.skuId === skuId)
    if (index > -1) {
      items.value.splice(index, 1)
      saveToStorage()
    }
  }

  /**
   * 移除商品
   */
  async function removeItem(skuId) {
    if (isLoggedIn()) {
      const item = items.value.find(i => i.skuId === skuId)
      if (!item || !item.id) {
        removeItemLocal(skuId)
        return
      }
      try {
        await removeCartItem(item.id)
        // 本地同步移除
        const index = items.value.findIndex(i => i.skuId === skuId)
        if (index > -1) {
          items.value.splice(index, 1)
        }
      } catch (error) {
        console.error('删除购物车项失败:', error)
        throw error
      }
    } else {
      removeItemLocal(skuId)
    }
  }

  /**
   * 清空购物车
   */
  async function clearCart() {
    if (isLoggedIn() && currentStoreId.value) {
      try {
        await clearStoreCart(currentStoreId.value)
      } catch (error) {
        console.error('清空购物车失败:', error)
      }
    }
    items.value = []
    saveToStorage()
  }

  // 初始化：尝试从 localStorage 加载
  loadFromStorage()

  return {
    items,
    currentStoreId,
    loading,
    totalCount,
    totalAmount,
    totalPrice,
    isEmpty,
    setCurrentStore,
    fetchCart,
    addItem,
    updateQuantity,
    removeItem,
    clearCart
  }
})
