<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>劳动合同</span>
        <el-button v-if="hasPerm('hr:contract:add')" type="primary" :icon="Plus" @click="onCreate">新增合同</el-button>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="合同编号">
        <el-input v-model="query.contractNo" placeholder="编号" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width: 220px">
          <el-option v-for="e in allEmployees" :key="e.id" :label="`${e.name}（${e.empNo}）`" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option :value="1" label="有效" />
          <el-option :value="2" label="即将到期" />
          <el-option :value="3" label="已到期" />
          <el-option :value="4" label="已终止" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="contractNo" label="合同编号" width="160" />
      <el-table-column prop="employeeName" label="员工" width="120" />
      <el-table-column prop="empNo" label="工号" width="110" />
      <el-table-column prop="contractType" label="合同类型" width="110" />
      <el-table-column prop="signDate" label="签订日期" width="120" />
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPerm('hr:contract:edit') && row.status !== 4" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('hr:contract:renew') && row.status === 1" size="small" type="success" @click="onRenew(row)">续签</el-button>
          <el-button v-if="hasPerm('hr:contract:terminate') && row.status === 1" size="small" type="warning" @click="onTerminate(row)">终止</el-button>
          <el-button v-if="hasPerm('hr:contract:delete')" size="small" type="danger" @click="onDelete(row)">删除</el-button>
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
    <el-dialog v-model="dlgVisible" :title="dialogTitle" width="720" @closed="onClosed">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同编号" prop="contractNo">
              <el-input v-model="form.contractNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工" prop="employeeId">
              <el-select v-model="form.employeeId" filterable style="width: 100%">
                <el-option v-for="e in allEmployees" :key="e.id" :label="`${e.name}（${e.empNo}）`" :value="e.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同类型" prop="contractType">
              <el-select v-model="form.contractType" style="width: 100%">
                <el-option value="固定期限" label="固定期限" />
                <el-option value="无固定期限" label="无固定期限" />
                <el-option value="实习协议" label="实习协议" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签订日期" prop="signDate">
              <el-date-picker v-model="form.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="试用期(月)">
              <el-input-number v-model="form.probationMonths" :min="0" :max="12" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同期限(月)">
              <el-input-number v-model="form.termMonths" :min="1" style="width: 100%" />
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

    <!-- 续签对话框 -->
    <el-dialog v-model="renewDlgVisible" :title="`续签合同（原 ${renewFrom?.contractNo || ''}）`" width="720">
      <el-form :model="renewForm" :rules="formRules" ref="renewFormRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="新合同编号" prop="contractNo">
              <el-input v-model="renewForm.contractNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="员工">
              <el-input :model-value="renewFrom?.employeeName + '（' + renewFrom?.empNo + '）'" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="新合同类型" prop="contractType">
              <el-select v-model="renewForm.contractType" style="width: 100%">
                <el-option value="固定期限" label="固定期限" />
                <el-option value="无固定期限" label="无固定期限" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签订日期" prop="signDate">
              <el-date-picker v-model="renewForm.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="新开始日期" prop="startDate">
              <el-date-picker v-model="renewForm.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新结束日期" prop="endDate">
              <el-date-picker v-model="renewForm.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="试用期(月)">
              <el-input-number v-model="renewForm.probationMonths" :min="0" :max="12" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同期限(月)">
              <el-input-number v-model="renewForm.termMonths" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="renewForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewDlgVisible = false">取消</el-button>
        <el-button type="primary" :loading="renewSaving" @click="onRenewSubmit">续签</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  pageContracts, getContract, createContract, updateContract, deleteContract,
  renewContract, terminateContract
} from '@/api/contract'
import { pageEmployees } from '@/api/employee'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 20, contractNo: '', employeeId: null, status: null })

const allEmployees = ref([])
const loadAllEmployees = async () => {
  const data = await pageEmployees({ pageNo: 1, pageSize: 1000 })
  allEmployees.value = data.records || []
}

const dlgVisible = ref(false)
const dialogTitle = computed(() => form.id ? '编辑合同' : '新增合同')
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  contractNo: '',
  employeeId: null,
  contractType: '固定期限',
  signDate: null,
  startDate: null,
  endDate: null,
  probationMonths: 0,
  termMonths: 36,
  remark: ''
})
const formRules = {
  contractNo: [{ required: true, message: '请填写合同编号', trigger: 'blur' }],
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  contractType: [{ required: true, message: '请选择合同类型', trigger: 'change' }],
  signDate: [{ required: true, message: '请选择签订日期', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

// 续签
const renewDlgVisible = ref(false)
const renewSaving = ref(false)
const renewFormRef = ref()
const renewFrom = ref(null)
const renewForm = reactive({
  contractNo: '',
  contractType: '固定期限',
  signDate: null,
  startDate: null,
  endDate: null,
  probationMonths: 0,
  termMonths: 36,
  remark: ''
})

const onSearch = async () => {
  loading.value = true
  try {
    const data = await pageContracts(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const statusTagType = (s) => ({
  1: 'success',
  2: 'warning',
  3: 'info',
  4: 'danger'
})[s] || ''

const resetForm = () => {
  Object.assign(form, {
    id: null, contractNo: '', employeeId: null, contractType: '固定期限',
    signDate: null, startDate: null, endDate: null,
    probationMonths: 0, termMonths: 36, remark: ''
  })
}

const onCreate = () => {
  resetForm()
  dlgVisible.value = true
}

const fillForm = (row) => {
  Object.assign(form, {
    id: row.id, contractNo: row.contractNo, employeeId: row.employeeId,
    contractType: row.contractType, signDate: row.signDate, startDate: row.startDate,
    endDate: row.endDate, probationMonths: row.probationMonths,
    termMonths: row.termMonths, remark: row.remark
  })
}

const onEdit = async (row) => {
  const full = await getContract(row.id)
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
        await updateContract(form)
      } else {
        await createContract(form)
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
  await ElMessageBox.confirm(`确认删除合同【${row.contractNo}】？`, '提示', { type: 'warning' })
  await deleteContract(row.id)
  ElMessage.success('已删除')
  onSearch()
}

const onRenew = (row) => {
  renewFrom.value = row
  Object.assign(renewForm, {
    contractNo: '',
    contractType: row.contractType || '固定期限',
    signDate: new Date().toISOString().slice(0, 10),
    startDate: null,
    endDate: null,
    probationMonths: 0,
    termMonths: row.termMonths || 36,
    remark: ''
  })
  renewDlgVisible.value = true
}

const onRenewSubmit = async () => {
  await renewFormRef.value.validate(async valid => {
    if (!valid) return
    renewSaving.value = true
    try {
      const payload = { ...renewForm, employeeId: renewFrom.value.employeeId }
      await renewContract(renewFrom.value.id, payload)
      ElMessage.success('续签成功（原合同已置为已到期）')
      renewDlgVisible.value = false
      onSearch()
    } finally {
      renewSaving.value = false
    }
  })
}

const onTerminate = async (row) => {
  try {
    const { value: date } = await ElMessageBox.prompt('请输入终止日期（YYYY-MM-DD）', `终止合同【${row.contractNo}】`, {
      inputPattern: /^\d{4}-\d{2}-\d{2}$/,
      inputErrorMessage: '日期格式不正确',
      inputValue: new Date().toISOString().slice(0, 10)
    })
    const { value: reason } = await ElMessageBox.prompt('请输入终止原因（可空）', '终止原因', { inputValue: '' })
    await terminateContract(row.id, { terminateDate: date, terminateReason: reason || null })
    ElMessage.success('已终止')
    onSearch()
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(async () => {
  await loadAllEmployees()
  onSearch()
})
</script>