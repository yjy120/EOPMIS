<template>
  <el-card>
    <template #header>
      <span>我的合同</span>
    </template>

    <el-skeleton v-if="loading" :rows="4" animated />

    <template v-else-if="list.length > 0">
      <el-table :data="list" border>
        <el-table-column prop="contractNo" label="合同编号" width="180" />
        <el-table-column prop="contractType" label="合同类型" width="120" />
        <el-table-column prop="signDate" label="签订日期" width="120" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="onView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <el-empty v-else description="暂无合同信息" />

    <!-- 详情对话框（只读） -->
    <el-dialog v-model="dlgVisible" :title="`合同详情 - ${detail?.contractNo || ''}`" width="640">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="合同编号">{{ detail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ detail.contractType }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ detail.employeeName }}（{{ detail.empNo }}）</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detail.positionName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="tagType(detail.status)">{{ detail.statusLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签订日期">{{ detail.signDate }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detail.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detail.endDate }}</el-descriptions-item>
        <el-descriptions-item label="合同期限">{{ detail.termMonths || '-' }} 月</el-descriptions-item>
        <el-descriptions-item label="试用期">{{ detail.probationMonths || 0 }} 月</el-descriptions-item>
        <el-descriptions-item label="终止日期" v-if="detail.status === 4">{{ detail.terminateDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="终止原因" :span="2" v-if="detail.status === 4">{{ detail.terminateReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="dlgVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyContracts } from '@/api/contract'

const list = ref([])
const loading = ref(false)
const dlgVisible = ref(false)
const detail = ref(null)

const tagType = (s) => ({ 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' })[s] || ''

const onView = (row) => {
  detail.value = row
  dlgVisible.value = true
}

onMounted(async () => {
  loading.value = true
  try {
    list.value = await getMyContracts() || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
    list.value = []
  } finally {
    loading.value = false
  }
})
</script>