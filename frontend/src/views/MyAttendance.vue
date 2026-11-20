<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>我的考勤</span>
        <el-button-group>
          <el-button size="small" @click="setRange(0)">最近 1 个月</el-button>
          <el-button size="small" @click="setRange(1)">最近 3 个月</el-button>
        </el-button-group>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px"
          @change="onDateRangeChange"
        />
      </el-form-item>
      <el-form-item label="出勤状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option v-for="opt in statusOptions" :key="opt.value" :value="opt.value" :label="opt.label" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-skeleton v-if="loading" :rows="4" animated />

    <template v-else-if="list.length > 0">
      <el-table :data="list" border>
        <el-table-column prop="attendanceDate" label="考勤日期" width="120" />
        <el-table-column prop="employeeName" label="员工" width="110" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column label="出勤状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leaveType" label="请假类型" width="100" />
        <el-table-column label="加班时长" width="100">
          <template #default="{ row }">{{ row.overtimeHours ? `${row.overtimeHours} 小时` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </template>

    <el-empty v-else description="暂无考勤信息" />
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyAttendance } from '@/api/attendance'

const list = ref([])
const loading = ref(false)
const query = reactive({ startDate: null, endDate: null, status: null })
const dateRange = ref([])

const statusOptions = [
  { value: 1, label: '出勤' },
  { value: 2, label: '迟到' },
  { value: 3, label: '早退' },
  { value: 4, label: '请假' },
  { value: 5, label: '加班' },
  { value: 6, label: '缺勤' }
]
const statusTagType = (s) => ({ 1: 'success', 2: 'warning', 3: 'warning', 4: 'info', 5: 'primary', 6: 'danger' })[s] || ''

const onDateRangeChange = (val) => {
  if (val && val.length === 2) {
    query.startDate = val[0]
    query.endDate = val[1]
  } else {
    query.startDate = null
    query.endDate = null
  }
}

const setRange = (months) => {
  const end = new Date()
  const start = new Date()
  start.setMonth(start.getMonth() - months)
  query.endDate = end.toISOString().slice(0, 10)
  query.startDate = start.toISOString().slice(0, 10)
  dateRange.value = [query.startDate, query.endDate]
  onSearch()
}

const onSearch = async () => {
  loading.value = true
  try {
    list.value = await getMyAttendance(query) || []
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 2a 默认列出最近 1 个月
  setRange(0)
})
</script>