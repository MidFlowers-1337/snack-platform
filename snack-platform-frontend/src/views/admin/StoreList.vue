<template>
  <div class="store-list">
    <div class="page-header">
      <h2>门店管理</h2>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        添加门店
      </el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="门店名称">
          <el-input v-model="searchForm.keyword" placeholder="搜索门店" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="营业中" :value="1" />
            <el-option label="休息中" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 门店列表 -->
    <el-card>
      <el-table :data="stores" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="门店名称" min-width="150" />
        <el-table-column prop="address" label="地址" min-width="200" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="businessHours" label="营业时间" width="130" />
        <el-table-column prop="adminUser.username" label="管理员" width="120" />
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
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="editStore(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteStore(row)">
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

    <!-- 添加/编辑门店对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑门店' : '添加门店'"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="storeForm"
        :rules="storeRules"
        label-width="100px"
      >
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="storeForm.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="storeForm.address" placeholder="请输入门店地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="storeForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="storeForm.businessHours" placeholder="如：09:00-22:00" />
        </el-form-item>
        <el-form-item label="门店图片" prop="image">
          <el-input v-model="storeForm.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="管理员" prop="adminUserId" v-if="!isEdit">
          <el-select
            v-model="storeForm.adminUserId"
            placeholder="请选择管理员"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in storeAdmins"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="storeForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="营业中"
            inactive-text="休息中"
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
import { Plus } from '@element-plus/icons-vue'
import {
  getStores,
  createStore,
  updateStore,
  deleteStore as deleteStoreApi
} from '@/api/store'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const stores = ref([])
const storeAdmins = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentStore = ref(null)
const formRef = ref()
const submitting = ref(false)

const storeForm = reactive({
  name: '',
  address: '',
  phone: '',
  businessHours: '',
  image: '',
  adminUserId: '',
  status: 1
})

const storeRules = {
  name: [
    { required: true, message: '请输入门店名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入门店地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleSearch = () => {
  currentPage.value = 1
  fetchStores()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchStores()
}

const handlePageChange = () => {
  fetchStores()
}

const handleStatusChange = async (row) => {
  try {
    await updateStore(row.id, { status: row.status })
    ElMessage.success(row.status === 1 ? '已开启营业' : '已暂停营业')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const openAddDialog = () => {
  isEdit.value = false
  currentStore.value = null
  Object.assign(storeForm, {
    name: '',
    address: '',
    phone: '',
    businessHours: '09:00-22:00',
    image: '',
    adminUserId: '',
    status: 1
  })
  dialogVisible.value = true
}

const editStore = (row) => {
  isEdit.value = true
  currentStore.value = row
  Object.assign(storeForm, {
    name: row.name,
    address: row.address,
    phone: row.phone,
    businessHours: row.businessHours,
    image: row.image,
    adminUserId: row.adminUserId,
    status: row.status
  })
  dialogVisible.value = true
}

const deleteStore = (row) => {
  ElMessageBox.confirm(
    `确定要删除门店 "${row.name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteStoreApi(row.id)
      ElMessage.success('删除成功')
      fetchStores()
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
      await updateStore(currentStore.value.id, storeForm)
      ElMessage.success('更新成功')
    } else {
      await createStore(storeForm)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchStores()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const fetchStores = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await getStores(params)
    stores.value = res.data.records || res.data || []
    total.value = res.data.total || stores.value.length
  } catch (error) {
    console.error('获取门店列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 模拟获取门店管理员列表
const fetchStoreAdmins = async () => {
  // 实际项目中应该调用API获取
  storeAdmins.value = [
    { id: 2, username: 'store1' },
    { id: 3, username: 'store2' }
  ]
}

onMounted(() => {
  fetchStores()
  fetchStoreAdmins()
})
</script>

<style scoped>
.store-list h2 {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>