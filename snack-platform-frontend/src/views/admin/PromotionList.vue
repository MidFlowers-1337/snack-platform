<template>
  <div class="promotion-list">
    <div class="page-header">
      <h2>促销管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        创建促销
      </el-button>
    </div>

    <!-- 促销列表 -->
    <el-card>
      <el-table :data="promotions" v-loading="loading" style="width: 100%">
        <el-table-column label="活动名称" prop="title" min-width="160" />
        <el-table-column label="关联商品" min-width="120">
          <template #default="{ row }">
            <span>{{ row.productName || `商品#${row.productId}` }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原价" width="100" align="center">
          <template #default="{ row }">
            <span class="price-original">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="促销价" width="100" align="center">
          <template #default="{ row }">
            <span class="price-promo">¥{{ row.promotionPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存 / 已售" width="120" align="center">
          <template #default="{ row }">
            <span>{{ row.stock }} / {{ row.soldCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" min-width="200">
          <template #default="{ row }">
            <div class="time-range">
              <div>{{ formatTime(row.startTime) }}</div>
              <div style="color: #999;">至</div>
              <div>{{ formatTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)" size="small">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">
              编辑
            </el-button>
            <el-popconfirm title="确定删除该活动？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchPromotions"
          @current-change="fetchPromotions"
        />
      </div>
    </el-card>

    <!-- 创建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑促销活动' : '创建促销活动'"
      width="560px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="formData.title" placeholder="例如：周末限时特惠" />
        </el-form-item>
        <el-form-item label="商品ID" prop="productId">
          <el-input-number v-model="formData.productId" :min="1" placeholder="关联商品" style="width: 100%" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="formData.originalPrice" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="促销价" prop="promotionPrice">
          <el-input-number v-model="formData.promotionPrice" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="formData.stock" :min="1" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="formData.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getPromotions, createPromotion, updatePromotion, deletePromotion } from '@/api/promotion'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const promotions = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formRef = ref()
const formData = reactive({
  title: '',
  productId: null,
  originalPrice: null,
  promotionPrice: null,
  stock: 100,
  timeRange: null
})

const formRules = {
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  productId: [{ required: true, message: '请输入商品ID', trigger: 'blur' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }],
  promotionPrice: [{ required: true, message: '请输入促销价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

const formatTime = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const getStatusText = (row) => {
  const now = new Date()
  const start = new Date(row.startTime)
  const end = new Date(row.endTime)
  if (now < start) return '未开始'
  if (now > end) return '已结束'
  if (row.stock <= 0) return '已售罄'
  return '进行中'
}

const getStatusType = (row) => {
  const text = getStatusText(row)
  const map = { '未开始': 'info', '已结束': 'info', '已售罄': 'warning', '进行中': 'success' }
  return map[text] || 'info'
}

const fetchPromotions = async () => {
  loading.value = true
  try {
    const res = await getPromotions({ pageNum: pageNum.value, pageSize: pageSize.value })
    const page = res.data
    promotions.value = page.records || []
    total.value = page.total || 0
  } catch (error) {
    ElMessage.error('获取促销列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  Object.assign(formData, { title: '', productId: null, originalPrice: null, promotionPrice: null, stock: 100, timeRange: null })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    title: row.title,
    productId: row.productId,
    originalPrice: row.originalPrice,
    promotionPrice: row.promotionPrice,
    stock: row.stock,
    timeRange: [row.startTime, row.endTime]
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    title: formData.title,
    productId: formData.productId,
    originalPrice: formData.originalPrice,
    promotionPrice: formData.promotionPrice,
    stock: formData.stock,
    startTime: formData.timeRange[0],
    endTime: formData.timeRange[1]
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePromotion(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createPromotion(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchPromotions()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await deletePromotion(id)
    ElMessage.success('删除成功')
    fetchPromotions()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchPromotions()
})
</script>

<style scoped>
.promotion-list {
  max-width: 1400px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.price-original {
  color: #999;
  text-decoration: line-through;
  font-size: 13px;
}

.price-promo {
  color: #f56c6c;
  font-weight: 700;
  font-size: 15px;
}

.time-range {
  font-size: 13px;
  line-height: 1.6;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
