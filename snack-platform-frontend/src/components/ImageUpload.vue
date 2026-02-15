<template>
  <div class="image-upload">
    <el-upload
      class="uploader"
      :action="uploadUrl"
      :headers="headers"
      :show-file-list="false"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      accept="image/jpeg,image/png,image/gif,image/webp"
    >
      <el-image
        v-if="modelValue"
        :src="modelValue"
        fit="cover"
        class="preview-image"
      />
      <div v-else class="upload-placeholder">
        <el-icon><Plus /></el-icon>
        <span>上传图片</span>
      </div>
    </el-upload>
    <div class="upload-tip" v-if="tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: String, default: '' },
  tip: { type: String, default: '支持 JPG/PNG/GIF/WebP，最大 5MB' }
})

const emit = defineEmits(['update:modelValue'])

const uploadUrl = (import.meta.env.VITE_API_BASE_URL || '/api') + '/file/upload'

const headers = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleSuccess = (response) => {
  if (response.code === 200) {
    emit('update:modelValue', response.data.url)
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleError = () => {
  ElMessage.error('上传失败，请重试')
}
</script>

<style scoped>
.uploader {
  width: 120px;
  height: 120px;
}

.uploader :deep(.el-upload) {
  width: 120px;
  height: 120px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}

.uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.preview-image {
  width: 120px;
  height: 120px;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #8c939d;
}

.upload-placeholder .el-icon {
  font-size: 28px;
}

.upload-placeholder span {
  font-size: 12px;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
