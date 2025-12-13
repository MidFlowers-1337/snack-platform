<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores'

const userStore = useUserStore()

onMounted(() => {
  // 如果有token，尝试获取用户信息
  if (userStore.token) {
    userStore.fetchUserInfo()
  }
})
</script>

<style>
/* 全局样式已在 style.css 中定义 */
</style>
