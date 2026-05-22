<template>
  <div class="sku-list">
    <div class="page-header">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加商品
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.keyword" placeholder="搜索商品" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable>
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商品列表 -->
    <el-card>
      <el-table :data="skuList" v-loading="loading" style="width: 100%">
        <el-table-column label="商品信息" min-width="250">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                :src="row.product?.image || 'https://via.placeholder.com/60?text=Product'"
                fit="cover"
                class="product-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-info">
                <div class="product-name">{{ row.product?.name }}</div>
                <div class="product-category">{{ row.product?.category?.name }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="售价" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" align="center">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock <= 10 }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editSku(row)">
              编辑
            </el-button>
            <el-button type="warning" size="small" @click="addStock(row)">
              补货
            </el-button>
            <el-button type="danger" size="small" @click="deleteSku(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '添加商品'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="skuForm"
        :rules="skuRules"
        label-width="100px"
      >
        <el-form-item label="选择商品" prop="productId" v-if="!isEdit">
          <el-select
            v-model="skuForm.productId"
            placeholder="请选择商品"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="product in availableProducts"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称" v-else>
          <span>{{ currentSku?.product?.name }}</span>
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number
            v-model="skuForm.price"
            :min="0.01"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number
            v-model="skuForm.stock"
            :min="0"
            :max="99999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="skuForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 补货对话框 -->
    <el-dialog v-model="stockDialogVisible" title="补货" width="400px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="商品">
          <span>{{ currentSku?.product?.name }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ currentSku?.stock }}</span>
        </el-form-item>
        <el-form-item label="补货数量">
          <el-input-number v-model="stockForm.addStock" :min="1" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock" :loading="submittingStock">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Picture } from '@element-plus/icons-vue'
import { getMyStoreSkus, createStoreSku, updateStoreSku, deleteStoreSku } from '@/api/sku'
import { getProducts, getCategories } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const skuList = ref([])
const categories = ref([])
const availableProducts = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  categoryId: '',
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentSku = ref(null)
const formRef = ref()
const submitting = ref(false)

const skuForm = reactive({
  productId: '',
  price: 0,
  stock: 0,
  status: 1
})

const skuRules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入售价', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' }
  ]
}

const stockDialogVisible = ref(false)
const submittingStock = ref(false)
const stockForm = reactive({
  addStock: 10
})

const handleSearch = () => {
  currentPage.value = 1
  fetchSkus()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchSkus()
}

const handlePageChange = () => {
  fetchSkus()
}

const handleStatusChange = async (row) => {
  try {
    await updateStoreSku(row.id, { status: row.status })
    ElMessage.success(row.status === 1 ? '已上架' : '已下架')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const openAddDialog = () => {
  isEdit.value = false
  currentSku.value = null
  Object.assign(skuForm, {
    productId: '',
    price: 0,
    stock: 0,
    status: 1
  })
  dialogVisible.value = true
}

const editSku = (row) => {
  isEdit.value = true
  currentSku.value = row
  Object.assign(skuForm, {
    productId: row.productId,
    price: row.price,
    stock: row.stock,
    status: row.status
  })
  dialogVisible.value = true
}

const addStock = (row) => {
  currentSku.value = row
  stockForm.addStock = 10
  stockDialogVisible.value = true
}

const deleteSku = (row) => {
  ElMessageBox.confirm(
    `确定要删除商品 ${row.product?.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteStoreSku(row.id)
      ElMessage.success('删除成功')
      fetchSkus()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateStoreSku(currentSku.value.id, skuForm)
      ElMessage.success('更新成功')
    } else {
      await createStoreSku(skuForm)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchSkus()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const submitStock = async () => {
  submittingStock.value = true
  try {
    await updateStoreSku(currentSku.value.id, {
      stock: currentSku.value.stock + stockForm.addStock
    })
    ElMessage.success('补货成功')
    stockDialogVisible.value = false
    fetchSkus()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submittingStock.value = false
  }
}

const fetchSkus = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    const res = await getMyStoreSkus(params)
    skuList.value = res.data.records || res.data || []
    total.value = res.data.total || skuList.value.length
  } catch (error) {
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

const fetchProducts = async () => {
  try {
    const res = await getProducts({ status: 1 })
    availableProducts.value = res.data.records || res.data || []
  } catch (error) {
    console.error('获取商品失败:', error)
  }
}

onMounted(() => {
  fetchSkus()
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.sku-list h2 {
  margin: 0;
  color: #333;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  overflow: hidden;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.image-placeholder .el-icon {
  font-size: 20px;
  color: #c0c4cc;
}

.product-info .product-name {
  font-size: 14px;
  color: #333;
}

.product-info .product-category {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
