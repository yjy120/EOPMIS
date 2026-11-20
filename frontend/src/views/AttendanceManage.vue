<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>考勤管理</span>
        <el-button v-if="hasPerm('hr:attendance:add')" type="primary" :icon="Plus" @click="onCreate">新增考勤</el-button>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width: 220px">
          <el-option v-for="e in allEmployees" :key="e.id" :label="`${e.name}（${e.empNo}）`" :value="e.id" />
        </el-select>
      </el-form-item>
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

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="employeeName" label="员工" width="110" />
      <el-table-column prop="empNo" label="工号" width="100" />
      <el-table-column prop="deptName" label="部门" width="120" />
      <el-table-column prop="attendanceDate" label="考勤日期" width="120" />
      <el-table-column label="出勤状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="leaveType" label="请假类型" width="100" />
      <el-table-column prop="overtimeHours" label="加班时长" width="100">
        <template #default="{ row }">{{ row.overtimeHours ? `${row.overtimeHours} 小时` : '-' }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPerm('hr:attendance:edit')" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('hr:attendance:delete')" size="small" type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 12px; text-align: right"
      v-model:current-page="query.pageNo"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="onSearch"
      @current-change="onSearch"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dlgVisible" :title="dialogTitle" width="640" @closed="onClosed">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="员工" prop="employeeId">
              <el-select v-model="form.employeeId" filterable style="width: 100%">
                <el-option v-for="e in allEmployees" :key="e.id" :label="`${e.name}（${e.empNo}）`" :value="e.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考勤日期" prop="attendanceDate">
              <el-date-picker v-model="form.attendanceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出勤状态" prop="status">
              <el-select v-model="form.status" style="width: 100%" @change="onStatusChange">
                <el-option v-for="opt in statusOptions" :key="opt.value" :value="opt.value" :label="opt.label" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.status === 4" label="请假类型" prop="leaveType">
              <el-select v-model="form.leaveType" style="width: 100%">
                <el-option v-for="t in leaveTypes" :key="t" :value="t" :label="t" />
              </el-select>
            </el-form-item>
            <el-form-item v-else-if="form.status === 5" label="加班时长(小时)" prop="overtimeHours">
              <el-input-number v-model="form.overtimeHours" :min="0" :max="24" :precision="2" :step="0.5" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlgVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { pageAttendance, getAttendance, createAttendance, updateAttendance, deleteAttendance } from '@/api/attendance'
import { pageEmployees } from '@/api/employee'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 20, employeeId: null, startDate: null, endDate: null, status: null })
const dateRange = ref([])
const onDateRangeChange = (val) => {
  if (val && val.length === 2) {
    query.startDate = val[0]
    query.endDate = val[1]
  } else {
    query.startDate = null
    query.endDate = null
  }
}

const allEmployees = ref([])
const loadAllEmployees = async () => {
  const data = await pageEmployees({ pageNo: 1, pageSize: 1000 })
  allEmployees.value = data.records || []
}

const statusOptions = [
  { value: 1, label: '出勤' },
  { value: 2, label: '迟到' },
  { value: 3, label: '早退' },
  { value: 4, label: '请假' },
  { value: 5, label: '加班' },
  { value: 6, label: '缺勤' }
]
const leaveTypes = ['事假', '病假', '年假', '调休']
const statusTagType = (s) => ({ 1: 'success', 2: 'warning', 3: 'warning', 4: 'info', 5: 'primary', 6: 'danger' })[s] || ''

const dlgVisible = ref(false)
const dialogTitle = computed(() => form.id ? '编辑考勤' : '新增考勤')
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  employeeId: null,
  attendanceDate: null,
  status: 1,
  leaveType: null,
  overtimeHours: 0,
  remark: ''
})
const formRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }],
  status: [{ required: true, message: '请选择出勤状态', trigger: 'change' }]
}

const onStatusChange = (val) => {
  if (val !== 4) form.leaveType = null
  if (val !== 5) form.overtimeHours = 0
}

const onSearch = async () => {
  loading.value = true
  try {
    const data = await pageAttendance(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null, employeeId: null, attendanceDate: null,
    status: 1, leaveType: null, overtimeHours: 0, remark: ''
  })
}

const onCreate = () => {
  resetForm()
  dlgVisible.value = true
}

const fillForm = (row) => {
  Object.assign(form, {
    id: row.id, employeeId: row.employeeId, attendanceDate: row.attendanceDate,
    status: row.status, leaveType: row.leaveType, overtimeHours: row.overtimeHours,
    remark: row.remark
  })
}

const onEdit = async (row) => {
  const full = await getAttendance(row.id)
  fillForm(full)
  dlgVisible.value = true
}

const onClosed = () => { /* placeholder */ }

const onSubmit = async () => {
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      if (form.id) {
        await updateAttendance(form)
      } else {
        await createAttendance(form)
      }
      ElMessage.success('保存成功')
      dlgVisible.value = false
      onSearch()
    } finally {
      saving.value = false
    }
  })
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除该考勤记录（日期：${row.attendanceDate}）？`, '提示', { type: 'warning' })
  await deleteAttendance(row.id)
  ElMessage.success('已删除')
  onSearch()
}

onMounted(async () => {
  await loadAllEmployees()
  onSearch()
})
</script>