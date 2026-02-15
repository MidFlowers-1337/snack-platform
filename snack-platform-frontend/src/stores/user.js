import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getProfile } from '@/api/auth'
import { getStorageItem } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(getStorageItem('userInfo'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')
  const isConsumer = computed(() => role.value === 'CONSUMER')
  const isStoreAdmin = computed(() => role.value === 'STORE_ADMIN')
  const isSystemAdmin = computed(() => role.value === 'SYSTEM_ADMIN')
  const storeId = computed(() => userInfo.value?.storeId || null)

  // 登录
  async function login(credentials) {
    const res = await loginApi(credentials)
    const { token: tokenValue, ...user } = res.data
    token.value = tokenValue
    userInfo.value = user
    localStorage.setItem('token', tokenValue)
    localStorage.setItem('userInfo', JSON.stringify(user))
    return { token: tokenValue, user }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return
    try {
      const res = await getProfile()
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    } catch (error) {
      logout()
    }
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    role,
    isConsumer,
    isStoreAdmin,
    isSystemAdmin,
    storeId,
    login,
    fetchUserInfo,
    logout
  }
})