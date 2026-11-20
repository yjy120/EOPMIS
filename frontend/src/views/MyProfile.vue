<template>
  <el-card>
    <template #header>
      <span>我的档案</span>
    </template>

    <el-skeleton v-if="loading" :rows="6" animated />

    <template v-else-if="profile">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="职工编号">{{ profile.empNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ profile.genderLabel }}</el-descriptions-item>
        <el-descriptions-item label="出生日期">{{ profile.birthDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ profile.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系方式">{{ profile.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ profile.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ profile.positionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职级">{{ profile.level || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ profile.entryDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="profile.status === 1 ? 'success' : 'info'">{{ profile.statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ profile.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </template>

    <el-empty v-else description="未查询到档案" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyProfile } from '@/api/me'

const profile = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    profile.value = await getMyProfile()
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
    profile.value = null
  } finally {
    loading.value = false
  }
})
</script>