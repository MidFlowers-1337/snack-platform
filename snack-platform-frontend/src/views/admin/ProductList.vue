<template>
  <div class="product-list">
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
      <el-table :data="products" v-loading="loading" style="width: 100%">
        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                :src="row.image || 'https://via.placeholder.com/60?text=Product'"
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
                <div class="product-name">{{ row.name }}</div>
                <div class="product-desc">{{ row.description }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="category.name" label="分类" width="120" align="center" />
        <el-table-column prop="price" label="建议售价" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price?.toFixed(2) }}</span>
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
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editProduct(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteProduct(row)">
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
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="建议售价" prop="price">
          <el-input-number
            v-model="productForm.price"
            :min="0.01"
            :precision="2"
            :step="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <el-input v-model="productForm.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="productForm.description"
            type="textarea"
            placeholder="请输入商品描述"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="productForm.status"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Picture } from '@element-plus/icons-vue'
import {
  getProducts,
  getCategories,
  createProduct,
  updateProduct,
  deleteProduct as deleteProductApi
} from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const products = ref([])
const categories = ref([])
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
const currentProduct = ref(null)
const formRef = ref()
const submitting = ref(false)

const productForm = reactive({
  name: '',
  categoryId: '',
  price: 0,
  image: '',
  description: '',
  status: 1
})

const productRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入建议售价', trigger: 'blur' }
  ]
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchProducts()
}

const handlePageChange = () => {
  fetchProducts()
}

const handleStatusChange = async (row) => {
  try {
    await updateProduct(row.id, { status: row.status })
    ElMessage.success(row.status === 1 ? '已上架' : '已下架')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const openAddDialog = () => {
  isEdit.value = false
  currentProduct.value = null
  Object.assign(productForm, {
    name: '',
    categoryId: '',
    price: 0,
    image: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const editProduct = (row) => {
  isEdit.value = true
  currentProduct.value = row
  Object.assign(productForm, {
    name: row.name,
    categoryId: row.categoryId,
    price: row.price,
    image: row.image,
    description: row.description,
    status: row.status
  })
  dialogVisible.value = true
}

const deleteProduct = (row) => {
  ElMessageBox.confirm(
    `确定要删除商品 "${row.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteProductApi(row.id)
      ElMessage.success('删除成功')
      fetchProducts()
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
      await updateProduct(currentProduct.value.id, productForm)
      ElMessage.success('更新成功')
    } else {
      await createProduct(productForm)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await getProducts(params)
    products.value = res.data.records || res.data || []
    total.value = res.data.total || products.value.length
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

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<style scoped>
.product-list h2 {
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

.product-info .product-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>