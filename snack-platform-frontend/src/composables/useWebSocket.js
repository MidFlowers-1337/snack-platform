/**
 * WebSocket 连接管理（STOMP over SockJS）
 *
 * What: 封装 WebSocket 连接、订阅、断线重连逻辑
 * Why: 实时接收订单状态变更通知
 * Why good: composable 模式复用，自动重连，组件卸载时自动清理
 */
import { ref, onUnmounted } from 'vue'

// 轻量 STOMP 客户端（不引入额外依赖，直接使用原生 WebSocket）
const WS_URL = (import.meta.env.VITE_WS_URL || (window.location.protocol === 'https:' ? 'wss:' : 'ws:') + '//' + window.location.host) + '/api/ws/websocket'

export function useWebSocket(userId, storeId = null) {
  const connected = ref(false)
  const notifications = ref([])
  const lastMessage = ref(null)

  let ws = null
  let reconnectTimer = null
  let reconnectAttempts = 0
  const MAX_RECONNECT_ATTEMPTS = 10
  const RECONNECT_DELAY = 3000

  const connect = () => {
    if (ws && ws.readyState === WebSocket.OPEN) return

    const token = localStorage.getItem('token')
    if (!token) return

    try {
      ws = new WebSocket(WS_URL + '?token=' + token)

      ws.onopen = () => {
        connected.value = true
        reconnectAttempts = 0
        console.log('[WS] 连接成功')
      }

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          lastMessage.value = data
          notifications.value.unshift(data)
          // 保留最近 50 条
          if (notifications.value.length > 50) {
            notifications.value = notifications.value.slice(0, 50)
          }
          // 播放提示音
          playNotificationSound()
        } catch (e) {
          console.warn('[WS] 消息解析失败:', e)
        }
      }

      ws.onclose = () => {
        connected.value = false
        console.log('[WS] 连接关闭')
        tryReconnect()
      }

      ws.onerror = (error) => {
        console.error('[WS] 连接错误:', error)
      }
    } catch (e) {
      console.error('[WS] 创建连接失败:', e)
      tryReconnect()
    }
  }

  const tryReconnect = () => {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      console.warn('[WS] 超过最大重连次数，停止重连')
      return
    }
    reconnectTimer = setTimeout(() => {
      reconnectAttempts++
      console.log(`[WS] 尝试重连... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`)
      connect()
    }, RECONNECT_DELAY)
  }

  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
    }
    if (ws) {
      ws.close()
      ws = null
    }
    connected.value = false
  }

  const clearNotifications = () => {
    notifications.value = []
  }

  // 简单的提示音
  const playNotificationSound = () => {
    try {
      const audioContext = new (window.AudioContext || window.webkitAudioContext)()
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()
      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)
      oscillator.frequency.value = 800
      oscillator.type = 'sine'
      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5)
      oscillator.start(audioContext.currentTime)
      oscillator.stop(audioContext.currentTime + 0.5)
    } catch (e) {
      // 静默失败（某些浏览器需要用户交互后才能播放音频）
    }
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    connected,
    notifications,
    lastMessage,
    connect,
    disconnect,
    clearNotifications
  }
}
