<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-content">
        <h1 class="hero-title">发现你的<br>下一款心动零食</h1>
        <p class="hero-sub">精选品质零食，门店新鲜自提，下单即享</p>
        <button class="hero-cta" @click="router.push('/stores')">
          开始选购
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M3 8h10M9 4l4 4-4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </button>
      </div>
      <div class="hero-visual">
        <div class="hero-circle"></div>
        <div class="hero-dots"></div>
      </div>
    </section>

    <!-- Quick Entry -->
    <section class="quick-grid">
      <div class="quick-card" @click="router.push('/stores')">
        <div class="quick-icon">
          <el-icon :size="24"><Shop /></el-icon>
        </div>
        <div class="quick-info">
          <span class="quick-label">附近门店</span>
          <span class="quick-desc">查找你身边的门店</span>
        </div>
      </div>
      <div class="quick-card" @click="goToCurrentStore">
        <div class="quick-icon quick-icon--green">
          <el-icon :size="24"><Goods /></el-icon>
        </div>
        <div class="quick-info">
          <span class="quick-label">商品浏览</span>
          <span class="quick-desc">探索全品类零食</span>
        </div>
      </div>
      <div class="quick-card" @click="router.push('/cart')">
        <div class="quick-icon quick-icon--amber">
          <el-icon :size="24"><ShoppingCart /></el-icon>
        </div>
        <div class="quick-info">
          <span class="quick-label">购物车</span>
          <span class="quick-desc">查看已选商品</span>
        </div>
      </div>
      <div class="quick-card" @click="router.push('/orders')">
        <div class="quick-icon quick-icon--purple">
          <el-icon :size="24"><List /></el-icon>
        </div>
        <div class="quick-info">
          <span class="quick-label">我的订单</span>
          <span class="quick-desc">跟踪订单状态</span>
        </div>
      </div>
    </section>

    <!-- Current Store -->
    <section class="section" v-if="currentStore">
      <div class="section-head">
        <h2 class="section-title">当前门店</h2>
        <a class="section-action" @click.prevent="router.push('/stores')">切换门店</a>
      </div>
      <div class="current-store" @click="goToCurrentStore">
        <div class="store-mark">
          <el-icon :size="28"><Shop /></el-icon>
        </div>
        <div class="store-detail">
          <h3 class="store-name">{{ currentStore.name }}</h3>
          <p class="store-meta">
            <span>{{ currentStore.address }}</span>
            <span class="meta-sep">·</span>
            <span>{{ currentStore.businessHours || '09:00-22:00' }}</span>
          </p>
        </div>
        <button class="store-go">进入购物</button>
      </div>
    </section>

    <!-- Flash Sale -->
    <section class="section" v-if="activePromotions.length > 0">
      <div class="section-head">
        <div class="section-head-left">
          <h2 class="section-title">限时特惠</h2>
          <div class="countdown-inline" v-if="countdown">
            <span class="cd-block">{{ countdown.hours }}</span>
            <span class="cd-sep">:</span>
            <span class="cd-block">{{ countdown.minutes }}</span>
            <span class="cd-sep">:</span>
            <span class="cd-block">{{ countdown.seconds }}</span>
          </div>
        </div>
      </div>
      <div class="promo-grid">
        <div class="promo-card" v-for="promo in activePromotions.slice(0, 4)" :key="promo.id">
          <div class="promo-badge">限时</div>
          <h4 class="promo-title">{{ promo.title }}</h4>
          <div class="promo-pricing">
            <span class="promo-now">¥{{ promo.promotionPrice }}</span>
            <span class="promo-was">¥{{ promo.originalPrice }}</span>
          </div>
          <div class="promo-progress-wrap">
            <div class="promo-bar">
              <div class="promo-bar-fill" :style="{ width: getProgress(promo) + '%' }"></div>
            </div>
            <span class="promo-sold">已抢 {{ promo.soldCount || 0 }} 件</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Hot Products -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">热销排行</h2>
      </div>
      <div class="hot-grid" v-loading="hotLoading">
        <div class="hot-card" v-for="(item, idx) in hotProducts" :key="item.productId">
          <div class="hot-img">
            <img v-if="item.image" :src="item.image" :alt="item.productName" loading="lazy" />
            <div v-else class="hot-placeholder">
              <el-icon :size="32"><Goods /></el-icon>
            </div>
            <span class="hot-rank" v-if="idx < 3">{{ idx + 1 }}</span>
          </div>
          <div class="hot-info">
            <h4 class="hot-name">{{ item.productName }}</h4>
            <div class="hot-footer">
              <span class="hot-price">¥{{ item.price }}</span>
              <span class="hot-sold">{{ item.totalSold }}件已售</span>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!hotLoading && hotProducts.length === 0" description="暂无数据" />
    </section>

    <!-- Stores -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">推荐门店</h2>
        <a class="section-action" @click.prevent="router.push('/stores')">查看全部</a>
      </div>
      <div class="store-grid" v-loading="loading">
        <div class="store-card" v-for="store in stores" :key="store.id" @click="selectStore(store)">
          <div class="store-card-top">
            <el-icon :size="28"><Shop /></el-icon>
          </div>
          <div class="store-card-body">
            <h3 class="store-card-name">{{ store.name }}</h3>
            <p class="store-card-addr">{{ store.address }}</p>
            <span class="store-card-status" :class="{ open: store.status === 1 }">
              {{ store.status === 1 ? '营业中' : '休息中' }}
            </span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, Goods, ShoppingCart, List } from '@element-plus/icons-vue'
import { getStores, getStoreDetail } from '@/api/store'
import { getHotProducts } from '@/api/product'
import { getActivePromotions } from '@/api/promotion'
import { useCartStore } from '@/stores'

const router = useRouter()
const cartStore = useCartStore()

const stores = ref([])
const currentStore = ref(null)
const loading = ref(false)
const hotProducts = ref([])
const hotLoading = ref(false)
const activePromotions = ref([])
const countdown = ref(null)
let countdownTimer = null

const goToCurrentStore = () => {
  if (cartStore.currentStoreId) {
    router.push(`/stores/${cartStore.currentStoreId}/products`)
  } else {
    router.push('/stores')
  }
}

const selectStore = (store) => {
  cartStore.setCurrentStore(store.id)
  router.push(`/stores/${store.id}/products`)
}

const fetchStores = async () => {
  loading.value = true
  try {
    const res = await getStores()
    stores.value = (res.data || []).slice(0, 3)
  } catch (e) {
    console.error('获取门店失败:', e)
  } finally {
    loading.value = false
  }
}

const fetchCurrentStore = async () => {
  if (cartStore.currentStoreId) {
    try {
      const res = await getStoreDetail(cartStore.currentStoreId)
      currentStore.value = res.data
    } catch (e) {
      console.error('获取当前门店失败:', e)
    }
  }
}

const fetchHotProducts = async () => {
  hotLoading.value = true
  try {
    const res = await getHotProducts(8)
    hotProducts.value = res.data || []
  } catch (e) {
    console.error('获取热销商品失败:', e)
  } finally {
    hotLoading.value = false
  }
}

const fetchActivePromotions = async () => {
  try {
    const res = await getActivePromotions()
    activePromotions.value = res.data || []
    startCountdown()
  } catch (e) {
    console.error('获取促销活动失败:', e)
  }
}

const getProgress = (promo) => {
  const total = (promo.stock || 0) + (promo.soldCount || 0)
  if (total <= 0) return 100
  return Math.round(((promo.soldCount || 0) / total) * 100)
}

const startCountdown = () => {
  if (countdownTimer) clearInterval(countdownTimer)
  const tick = () => {
    const now = new Date()
    const ends = activePromotions.value
      .map(p => new Date(p.endTime))
      .filter(d => d > now)
      .sort((a, b) => a - b)
    if (ends.length === 0) { countdown.value = null; return }
    const diff = Math.max(0, Math.floor((ends[0] - now) / 1000))
    countdown.value = {
      hours: String(Math.floor(diff / 3600)).padStart(2, '0'),
      minutes: String(Math.floor((diff % 3600) / 60)).padStart(2, '0'),
      seconds: String(diff % 60).padStart(2, '0')
    }
  }
  tick()
  countdownTimer = setInterval(tick, 1000)
}

onMounted(() => {
  fetchStores()
  fetchCurrentStore()
  fetchHotProducts()
  fetchActivePromotions()
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped>
.home {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-md);
}

/* ===== Hero ===== */
.hero {
  position: relative;
  background: var(--color-text);
  border-radius: var(--radius-lg);
  padding: var(--space-3xl) var(--space-2xl);
  margin-bottom: var(--space-2xl);
  overflow: hidden;
  min-height: 280px;
  display: flex;
  align-items: center;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.25;
  color: #fff;
  margin-bottom: var(--space-md);
  letter-spacing: -0.02em;
}

.hero-sub {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: var(--space-xl);
  line-height: 1.6;
}

.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--transition-fast), transform var(--transition-fast);
}

.hero-cta:hover {
  background: var(--color-primary-hover);
}

.hero-cta:active {
  transform: scale(0.97);
}

/* Hero decorations — abstract, not emoji */
.hero-visual {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 45%;
}

.hero-circle {
  position: absolute;
  top: 50%;
  right: 10%;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0.12;
  transform: translateY(-50%);
}

.hero-dots {
  position: absolute;
  top: 20%;
  right: 35%;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

/* ===== Quick Entry ===== */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-md);
  margin-bottom: var(--space-2xl);
}

.quick-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg) var(--space-md);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.quick-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  background: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-icon--green {
  background: var(--color-accent-light);
  color: var(--color-accent);
}

.quick-icon--amber {
  background: #FEF3C7;
  color: #D97706;
}

.quick-icon--purple {
  background: #F3E8FF;
  color: #7C3AED;
}

.quick-label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 2px;
}

.quick-desc {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ===== Section Pattern ===== */
.section {
  margin-bottom: var(--space-2xl);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
}

.section-head-left {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.section-action {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-primary);
  cursor: pointer;
  text-decoration: none;
  transition: opacity var(--transition-fast);
}

.section-action:hover {
  opacity: 0.75;
}

/* ===== Current Store ===== */
.current-store {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  padding: var(--space-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.current-store:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.store-mark {
  width: 56px;
  height: 56px;
  background: var(--color-accent-light);
  color: var(--color-accent);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.store-detail {
  flex: 1;
  min-width: 0;
}

.store-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.store-meta {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.meta-sep {
  margin: 0 6px;
  color: var(--color-text-muted);
}

.store-go {
  padding: 8px 20px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background var(--transition-fast);
  white-space: nowrap;
}

.store-go:hover {
  background: var(--color-primary-hover);
}

/* ===== Flash Sale ===== */
.countdown-inline {
  display: flex;
  align-items: center;
  gap: 3px;
}

.cd-block {
  display: inline-block;
  background: var(--color-text);
  color: #fff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  min-width: 26px;
  text-align: center;
}

.cd-sep {
  font-weight: 700;
  color: var(--color-text);
  font-size: 13px;
}

.promo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-md);
}

.promo-card {
  padding: var(--space-lg);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  position: relative;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.promo-card:hover {
  border-color: var(--color-danger);
  box-shadow: var(--shadow-md);
}

.promo-badge {
  position: absolute;
  top: var(--space-md);
  right: var(--space-md);
  background: var(--color-danger);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

.promo-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-sm);
  padding-right: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.promo-pricing {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: var(--space-md);
}

.promo-now {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-danger);
}

.promo-was {
  font-size: 13px;
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.promo-progress-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.promo-bar {
  flex: 1;
  height: 6px;
  background: #FEE2E2;
  border-radius: var(--radius-full);
  overflow: hidden;
}

.promo-bar-fill {
  height: 100%;
  background: var(--color-danger);
  border-radius: var(--radius-full);
  transition: width 300ms ease;
}

.promo-sold {
  font-size: 12px;
  color: var(--color-text-muted);
  white-space: nowrap;
}

/* ===== Hot Products ===== */
.hot-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-md);
}

.hot-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.hot-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-md);
}

.hot-img {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  background: var(--color-bg);
  overflow: hidden;
}

.hot-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 300ms ease;
}

.hot-card:hover .hot-img img {
  transform: scale(1.03);
}

.hot-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-placeholder);
}

.hot-rank {
  position: absolute;
  top: var(--space-sm);
  left: var(--space-sm);
  width: 24px;
  height: 24px;
  background: var(--color-text);
  color: #fff;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.hot-info {
  padding: var(--space-md);
}

.hot-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  margin-bottom: var(--space-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hot-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-primary);
}

.hot-sold {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* ===== Store Grid ===== */
.store-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-md);
}

.store-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
}

.store-card:hover {
  border-color: var(--color-accent);
  box-shadow: var(--shadow-md);
}

.store-card-top {
  height: 80px;
  background: var(--color-accent-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-accent);
}

.store-card-body {
  padding: var(--space-md);
}

.store-card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 6px;
}

.store-card-addr {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: var(--space-sm);
  line-height: 1.5;
}

.store-card-status {
  display: inline-block;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-muted);
  padding: 2px 8px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.store-card-status.open {
  color: var(--color-accent);
  border-color: var(--color-accent);
  background: var(--color-accent-light);
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .hero {
    padding: var(--space-xl) var(--space-lg);
    min-height: auto;
    border-radius: var(--radius-md);
  }

  .hero-title {
    font-size: 26px;
  }

  .hero-sub {
    font-size: 14px;
  }

  .hero-visual {
    display: none;
  }

  .quick-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .promo-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .hot-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .store-grid {
    grid-template-columns: 1fr;
  }

  .current-store {
    flex-direction: column;
    text-align: center;
    gap: var(--space-md);
  }

  .store-meta {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .meta-sep {
    display: none;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .hot-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .promo-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
