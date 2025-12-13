<template>
  <div class="user-list">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="搜索用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable>
            <el-option label="消费者" value="CONSUMER" />
            <el-option label="门店管理员" value="STORE_ADMIN" />
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="140" align="center">
          <template #default="{ row }">
            <el-select
              v-model="row.role"
              :disabled="row.role === 'SYSTEM_ADMIN'"
              size="small"
              @change="handleRoleChange(row)"
            >
              <el-option label="消费者" value="CONSUMER" />
              <el-option label="门店管理员" value="STORE_ADMIN" />
              <el-option label="系统管理员" value="SYSTEM_ADMIN" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.role === 'SYSTEM_ADMIN'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, updateUserStatus, updateUserRole } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

// 加载状态
const loading = ref(false)
// 用户列表
const users = ref([])
// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  username: '',
  role: ''
})

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  searchForm.username = ''
  searchForm.role = ''
  handleSearch()
}

/**
 * 分页大小改变
 */
const handleSizeChange = () => {
  currentPage.value = 1
  fetchUsers()
}

/**
 * 页码改变
 */
const handlePageChange = () => {
  fetchUsers()
}

/**
 * 处理状态改变
 */
const handleStatusChange = async (row) => {
  const enabled = row.status === 1
  const action = enabled ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${row.username}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateUserStatus(row.id, enabled)
    ElMessage.success(`已${action}用户`)
  } catch (error) {
    // 用户取消或请求失败，恢复原状态
    row.status = enabled ? 0 : 1
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

/**
 * 处理角色改变
 */
const handleRoleChange = async (row) => {
  const roleMap = {
    'CONSUMER': '消费者',
    'STORE_ADMIN': '门店管理员',
    'SYSTEM_ADMIN': '系统管理员'
  }
  
  // 保存原角色用于恢复
  const originalRole = row._originalRole || row.role
  
  try {
    await ElMessageBox.confirm(
      `确定要将用户 "${row.username}" 的角色修改为 "${roleMap[row.role]}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateUserRole(row.id, row.role)
    row._originalRole = row.role
    ElMessage.success('角色修改成功')
  } catch (error) {
    // 用户取消或请求失败，恢复原角色
    row.role = originalRole
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  }
}

/**
 * 获取用户列表
 */
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const res = await getUserList(params)
    users.value = (res.data.records || res.data || []).map(user => ({
      ...user,
      _originalRole: user.role // 保存原始角色用于取消时恢复
    }))
    total.value = res.data.total || users.value.length
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-list h2 {
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