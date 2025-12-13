import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  // 购物车项 { skuId, productName, productImage, price, quantity, stock }
  const items = ref(JSON.parse(localStorage.getItem('cart') || '[]'))
  // 当前选择的门店ID
  const currentStoreId = ref(localStorage.getItem('currentStoreId') || null)

  // 计算属性
  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalAmount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  const isEmpty = computed(() => items.value.length === 0)

  // 设置当前门店
  function setCurrentStore(storeId) {
    if (currentStoreId.value !== storeId) {
      // 切换门店时清空购物车
      items.value = []
      saveToStorage()
    }
    currentStoreId.value = storeId
    localStorage.setItem('currentStoreId', storeId)
  }

  // 添加商品到购物车
  function addItem(item) {
    const existingItem = items.value.find(i => i.skuId === item.skuId)
    if (existingItem) {
      // 检查库存
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
        stock: item.stock
      })
    }
    saveToStorage()
  }

  // 更新商品数量
  function updateQuantity(skuId, quantity) {
    const item = items.value.find(i => i.skuId === skuId)
    if (item) {
      if (quantity <= 0) {
        removeItem(skuId)
      } else if (quantity <= item.stock) {
        item.quantity = quantity
        saveToStorage()
      }
    }
  }

  // 移除商品
  function removeItem(skuId) {
    const index = items.value.findIndex(i => i.skuId === skuId)
    if (index > -1) {
      items.value.splice(index, 1)
      saveToStorage()
    }
  }

  // 清空购物车
  function clearCart() {
    items.value = []
    saveToStorage()
  }

  // 保存到本地存储
  function saveToStorage() {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  return {
    items,
    currentStoreId,
    totalCount,
    totalAmount,
    isEmpty,
    setCurrentStore,
    addItem,
    updateQuantity,
    removeItem,
    clearCart
  }
})