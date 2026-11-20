<template>
  <el-card>
    <template #header>
      <span>我的薪资</span>
    </template>

    <el-skeleton v-if="loading" :rows="4" animated />

    <template v-else-if="list.length > 0">
      <el-table :data="list" border>
        <el-table-column prop="salaryMonth" label="薪资月份" width="120" />
        <el-table-column prop="employeeName" label="员工" width="120" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="positionName" label="岗位" width="120" />
        <el-table-column label="基本工资" width="110">
          <template #default="{ row }">{{ formatMoney(row.baseSalary) }}</template>
        </el-table-column>
        <el-table-column label="实发工资" width="110">
          <template #default="{ row }">{{ formatMoney(row.actualSalary) }}</template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="发放日期" width="120" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="onView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <el-empty v-else description="暂无薪资信息" />

    <!-- 详情对话框（只读） -->
    <el-dialog v-model="dlgVisible" :title="`薪资详情 - ${detail?.salaryMonth || ''}`" width="640">
      <el-descriptions :column="2" border v-if="detail">
        <el-descriptions-item label="薪资月份">{{ detail.salaryMonth }}</el-descriptions-item>
        <el-descriptions-item label="发放日期">{{ detail.paymentDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="员工">{{ detail.employeeName }}（{{ detail.empNo }}）</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ detail.positionName }}</el-descriptions-item>
        <el-descriptions-item label="">{{ }}</el-descriptions-item>
        <el-descriptions-item label="基本工资">¥ {{ formatMoney(detail.baseSalary) }}</el-descriptions-item>
        <el-descriptions-item label="岗位工资">¥ {{ formatMoney(detail.positionSalary) }}</el-descriptions-item>
        <el-descriptions-item label="绩效工资">¥ {{ formatMoney(detail.performanceSalary) }}</el-descriptions-item>
        <el-descriptions-item label="补贴">¥ {{ formatMoney(detail.allowance) }}</el-descriptions-item>
        <el-descriptions-item label="扣款">¥ {{ formatMoney(detail.deduction) }}</el-descriptions-item>
        <el-descriptions-item label="实发工资">¥ {{ formatMoney(detail.actualSalary) }}</el-descriptions-item>
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
import { getMySalary } from '@/api/salary'

const list = ref([])
const loading = ref(false)
const dlgVisible = ref(false)
const detail = ref(null)

const formatMoney = (v) => (v == null ? '-' : Number(v).toFixed(2))

const onView = (row) => {
  detail.value = row
  dlgVisible.value = true
}

onMounted(async () => {
  loading.value = true
  try {
    list.value = await getMySalary() || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
    list.value = []
  } finally {
    loading.value = false
  }
})
</script>