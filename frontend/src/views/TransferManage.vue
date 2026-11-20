<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>人事调动</span>
        <el-button v-if="hasPerm('hr:transfer:add')" type="primary" :icon="Plus" @click="onCreate">发起调动</el-button>
      </div>
    </template>

    <el-form inline :model="query" style="margin-bottom: 12px">
      <el-form-item label="员工">
        <el-select v-model="query.employeeId" placeholder="全部" clearable filterable style="width: 220px">
          <el-option v-for="e in allEmployees" :key="e.id" :label="`${e.name}（${e.empNo}）`" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
          <el-option :value="1" label="已生效" />
          <el-option :value="2" label="已撤销" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="employeeName" label="员工" width="120" />
      <el-table-column prop="empNo" label="工号" width="110" />
      <el-table-column prop="fromDeptName" label="调出部门" width="120" />
      <el-table-column prop="fromPositionName" label="原岗位" width="120" />
      <el-table-column prop="toDeptName" label="调入部门" width="120" />
      <el-table-column prop="toPositionName" label="新岗位" width="120" />
      <el-table-column prop="transferDate" label="调动日期" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPerm('hr:transfer:edit') && row.status === 1" size="small" @click="onEdit(row)">编辑</el-button>
          <el-button v-if="hasPerm('hr:transfer:revoke') && row.status === 1" size="small" type="warning" @click="onRevoke(row)">撤销</el-button>
          <el-button v-if="hasPerm('hr:transfer:edit')" size="small" type="danger" @click="onDelete(row)">删除</el-button>
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
        <el-form-item label="员工" prop="employeeId">
          <el-select v-model="form.employeeId" filterable style="width: 100%" @change="onEmpChange">
            <el-option v-for="e in allEmployees" :key="e.id" :label="`${e.name}（${e.empNo}）`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调出部门" prop="fromDeptId">
              <el-tree-select
                v-model="form.fromDeptId"
                :data="deptTreeData"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                check-strictly
                default-expand-all
                placeholder="请选择"
                style="width: 100%"
                @change="onFromDeptChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="原岗位" prop="fromPositionId">
              <el-select v-model="form.fromPositionId" filterable placeholder="请选择" style="width: 100%">
                <el-option v-for="p in fromPositions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调入部门" prop="toDeptId">
              <el-tree-select
                v-model="form.toDeptId"
                :data="deptTreeData"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                check-strictly
                default-expand-all
                placeholder="请选择"
                style="width: 100%"
                @change="onToDeptChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="新岗位" prop="toPositionId">
              <el-select v-model="form.toPositionId" filterable placeholder="请选择" style="width: 100%">
                <el-option v-for="p in toPositions" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="调动日期" prop="transferDate">
              <el-date-picker v-model="form.transferDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="调动原因">
              <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="调动原因（可空）" />
            </el-form-item>
          </el-col>
        </el-row>
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
import {
  pageTransfers, getTransfer, createTransfer, updateTransfer, deleteTransfer, revokeTransfer
} from '@/api/transfer'
import { pageEmployees, getEmployee } from '@/api/employee'
import { listAllDepts } from '@/api/dept'
import { listAllPositions } from '@/api/position'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const hasPerm = (p) => userStore.permissions.includes(p)

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 20, employeeId: null, status: null })

const allEmployees = ref([])
const allDepts = ref([])
const allPositions = ref([])
const deptTreeData = computed(() => {
  const map = new Map()
  const roots = []
  for (const d of allDepts.value) map.set(d.id, { ...d, children: [] })
  for (const d of allDepts.value) {
    const node = map.get(d.id)
    if (!d.parentId || d.parentId === 0) roots.push(node)
    else if (map.has(d.parentId)) map.get(d.parentId).children.push(node)
    else roots.push(node)
  }
  return roots
})
const fromPositions = ref([])
const toPositions = ref([])

const loadAllEmployees = async () => {
  const data = await pageEmployees({ pageNo: 1, pageSize: 1000 })
  allEmployees.value = data.records || []
}
const loadAllDepts = async () => { allDepts.value = await listAllDepts() }
const loadAllPositions = async () => { allPositions.value = await listAllPositions() }

const dlgVisible = ref(false)
const dialogTitle = computed(() => form.id ? '编辑调动' : '发起调动')
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  employeeId: null,
  fromDeptId: null,
  fromPositionId: null,
  toDeptId: null,
  toPositionId: null,
  transferDate: null,
  reason: ''
})
const formRules = {
  employeeId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  fromDeptId: [{ required: true, message: '请选择调出部门', trigger: 'change' }],
  fromPositionId: [{ required: true, message: '请选择原岗位', trigger: 'change' }],
  toDeptId: [{ required: true, message: '请选择调入部门', trigger: 'change' }],
  toPositionId: [{ required: true, message: '请选择新岗位', trigger: 'change' }],
  transferDate: [{ required: true, message: '请选择调动日期', trigger: 'change' }]
}

const onSearch = async () => {
  loading.value = true
  try {
    const data = await pageTransfers(query)
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

const onFromDeptChange = () => {
  form.fromPositionId = null
  fromPositions.value = form.fromDeptId ? allPositions.value.filter(p => p.deptId === form.fromDeptId) : []
}
const onToDeptChange = () => {
  form.toPositionId = null
  toPositions.value = form.toDeptId ? allPositions.value.filter(p => p.deptId === form.toDeptId) : []
}
const onEmpChange = async () => {
  // 选员工后自动回填调出部门/岗位（当前部门）
  if (!form.employeeId || form.id) return
  try {
    const emp = await getEmployee(form.employeeId)
    form.fromDeptId = emp.deptId
    form.fromPositionId = emp.positionId
    onFromDeptChange()
  } catch (e) {
    // ignore
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null, employeeId: null,
    fromDeptId: null, fromPositionId: null,
    toDeptId: null, toPositionId: null,
    transferDate: null, reason: ''
  })
  fromPositions.value = []
  toPositions.value = []
}

const onCreate = () => {
  resetForm()
  dlgVisible.value = true
}

const fillForm = (row) => {
  Object.assign(form, {
    id: row.id, employeeId: row.employeeId,
    fromDeptId: row.fromDeptId, fromPositionId: row.fromPositionId,
    toDeptId: row.toDeptId, toPositionId: row.toPositionId,
    transferDate: row.transferDate, reason: row.reason
  })
  onFromDeptChange()
  onToDeptChange()
  if (form.fromDeptId) fromPositions.value = allPositions.value.filter(p => p.deptId === form.fromDeptId)
  if (form.toDeptId) toPositions.value = allPositions.value.filter(p => p.deptId === form.toDeptId)
  // 重新覆盖（因为 onFromDeptChange 会清空 fromPositionId）
  form.fromPositionId = row.fromPositionId
  form.toPositionId = row.toPositionId
}

const onEdit = async (row) => {
  const full = await getTransfer(row.id)
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
        await updateTransfer(form)
      } else {
        await createTransfer(form)
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
  await ElMessageBox.confirm(`确认删除该调动记录？`, '提示', { type: 'warning' })
  await deleteTransfer(row.id)
  ElMessage.success('已删除')
  onSearch()
}

const onRevoke = async (row) => {
  await ElMessageBox.confirm(`确认撤销调动？撤销后历史保留，但状态置为"已撤销"。`, '提示', { type: 'warning' })
  await revokeTransfer(row.id)
  ElMessage.success('已撤销')
  onSearch()
}

onMounted(async () => {
  await Promise.all([loadAllEmployees(), loadAllDepts(), loadAllPositions()])
  onSearch()
})
</script>