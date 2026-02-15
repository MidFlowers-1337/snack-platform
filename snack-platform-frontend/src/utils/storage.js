/**
 * localStorage 安全封装
 * 防止 JSON.parse 报错导致页面白屏
 */

/**
 * 安全地从 localStorage 读取并解析 JSON
 * @param {string} key - localStorage 的 key
 * @param {*} defaultValue - 解析失败时的默认值
 * @returns {*} 解析后的值或默认值
 */
export function getStorageItem(key, defaultValue = null) {
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : defaultValue
  } catch (e) {
    console.warn(`Failed to parse localStorage key "${key}":`, e)
    localStorage.removeItem(key)
    return defaultValue
  }
}

/**
 * 安全地将值序列化为 JSON 并存入 localStorage
 * @param {string} key - localStorage 的 key
 * @param {*} value - 要存储的值
 */
export function setStorageItem(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (e) {
    console.warn(`Failed to save to localStorage key "${key}":`, e)
  }
}

/**
 * 从 localStorage 移除指定 key
 * @param {string} key - localStorage 的 key
 */
export function removeStorageItem(key) {
  localStorage.removeItem(key)
}
