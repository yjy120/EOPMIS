<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>薪资管理</span>
        <el-button v-if="hasPerm('hr:salary:add')" type="primary" :icon="Plus" @click="onCreate">新增薪资</el-button>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width: 220px">
          <el-option v-for="e in allEmployees" :key="e.id" :label="`${e.name}（${e.empNo}）`" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="薪资月份">
        <el-date-picker
          v-model="query.salaryMonth"
          type="month"
          value-format="YYYY-MM"
          placeholder="全部"
          clearable
          style="width: 160px"
        />
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
      <el-table-column prop="positionName" label="岗位" width="120" />
      <el-table-column prop="salaryMonth" label="月份" width="100" />
      <el-table-column prop="baseSalary" label="基本工资" width="110">
        <template #default="{ row }">{{ formatMoney(row.baseSalary) }}</template>
      </el-table-column>
      <el-table-column prop="actualSalary" label="实发工资" width="110">
        <template #default="{ row }">{{ formatMoney(row.actualSalary) }}</template>
      </el-table-column>
      <el-table-column prop="paymentDate" label="发放日期" width="120" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPerm('hr:salary:edit')" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('hr:salary:delete')" size="small" type="danger" @click="onDelete(row)">删除</el-button>
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
    <el-dialog v-model="dlgVisible" :title="dialogTitle" width="760" @closed="onClosed">
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
            <el-form-item label="薪资月份" prop="salaryMonth">
              <el-date-picker v-model="form.salaryMonth" type="month" value-format="YYYY-MM" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="基本工资" prop="baseSalary">
              <el-input-number v-model="form.baseSalary" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="岗位工资" prop="positionSalary">
              <el-input-number v-model="form.positionSalary" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="绩效工资" prop="performanceSalary">
              <el-input-number v-model="form.performanceSalary" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="补贴" prop="allowance">
              <el-input-number v-model="form.allowance" :min="0" :precision="2" :step="50" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扣款" prop="deduction">
              <el-input-number v-model="form.deduction" :min="0" :precision="2" :step="50" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实发工资" prop="actualSalary">
              <el-input-number v-model="form.actualSalary" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发放日期">
              <el-date-picker v-model="form.paymentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { pageSalary, getSalary, createSalary, updateSalary, deleteSalary } from '@/api/salary'
import { pageEmployees } from '@/api/employee'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 20, employeeId: null, salaryMonth: null })

const allEmployees = ref([])
const loadAllEmployees = async () => {
  const data = await pageEmployees({ pageNo: 1, pageSize: 1000 })
  allEmployees.value = data.records || []
}

const formatMoney = (v) => (v == null ? '-' : Number(v).toFixed(2))

const dlgVisible = ref(false)
const dialogTitle = computed(() => form.id ? '编辑薪资' : '新增薪资')
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  employeeId: null,
  salaryMonth: null,
  baseSalary: 0,
  positionSalary: 0,
  performanceSalary: 0,
  allowance: 0,
  deduction: 0,
  actualSalary: 0,
  paymentDate: null,
  remark: ''
})
const formRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  salaryMonth: [{ required: true, message: '请选择薪资月份', trigger: 'change' }],
  baseSalary: [{ required: true, message: '请填写基本工资', trigger: 'blur' }],
  positionSalary: [{ required: true, message: '请填写岗位工资', trigger: 'blur' }],
  performanceSalary: [{ required: true, message: '请填写绩效工资', trigger: 'blur' }],
  allowance: [{ required: true, message: '请填写补贴', trigger: 'blur' }],
  deduction: [{ required: true, message: '请填写扣款', trigger: 'blur' }],
  actualSalary: [{ required: true, message: '请填写实发工资', trigger: 'blur' }]
}

const onSearch = async () => {
  loading.value = true
  try {
    const data = await pageSalary(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null, employeeId: null, salaryMonth: null,
    baseSalary: 0, positionSalary: 0, performanceSalary: 0,
    allowance: 0, deduction: 0, actualSalary: 0,
    paymentDate: null, remark: ''
  })
}

const onCreate = () => {
  resetForm()
  dlgVisible.value = true
}

const fillForm = (row) => {
  Object.assign(form, {
    id: row.id, employeeId: row.employeeId, salaryMonth: row.salaryMonth,
    baseSalary: row.baseSalary, positionSalary: row.positionSalary,
    performanceSalary: row.performanceSalary, allowance: row.allowance,
    deduction: row.deduction, actualSalary: row.actualSalary,
    paymentDate: row.paymentDate, remark: row.remark
  })
}

const onEdit = async (row) => {
  const full = await getSalary(row.id)
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
        await updateSalary(form)
      } else {
        await createSalary(form)
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
  await ElMessageBox.confirm(`确认删除该薪资记录（月份：${row.salaryMonth}）？`, '提示', { type: 'warning' })
  await deleteSalary(row.id)
  ElMessage.success('已删除')
  onSearch()
}

onMounted(async () => {
  await loadAllEmployees()
  onSearch()
})
</script>